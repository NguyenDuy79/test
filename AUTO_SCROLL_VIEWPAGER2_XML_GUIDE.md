# 📱 Hướng Dẫn Tự Động Cuộn Auto-Scroll ViewPager2 Bằng XML Layout (Android Views)

Tài liệu này hướng dẫn chi tiết cách xây dựng một **Auto-Scrolling ViewPager2** (Banner Slider tự động chạy) trên giao diện **Android XML Layout (View System)** với các tính năng:
1. **Auto-Scroll**: Tự động chuyển trang sau khoảng thời gian đếm ngược.
2. **Tạm dừng & Reset khi vuốt tay**: Khi người dùng chạm ngón tay vào `ViewPager2`, timer tự động dừng lại. Sau khi nhả tay ra, timer đếm lại từ đầu.
3. **Page Transformer**: Hiệu ứng thu phóng (Scale) và mờ dần (Fade) khi cuộn giữa các trang.

---

## 🛠️ 1. File Giao Diện XML Layout (`item_banner.xml` & `activity_banner.xml`)

### Layout Trang Banner (`res/layout/item_banner_slider.xml`):
```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res/app"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="8dp">

    <com.google.android.material.imageview.ShapeableImageView
        android:id="@+id/imgBanner"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scaleType="centerCrop"
        app:shapeAppearanceOverlay="@style/ShapeAppearance.Material3.Corner.Medium" />

</FrameLayout>
```

---

## 💻 2. Mã Nguồn Kotlin Xử Lý Auto-Scroll (`AutoScrollViewPagerHelper.kt`)

Tạo 1 Helper class để quản lý tự động cuộn và sự kiện chạm tay cho `ViewPager2`:

```kotlin
package com.example.fitness.myapplication.core.utils

import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

class AutoScrollViewPagerHelper(
    private val viewPager: ViewPager2,
    private val autoScrollIntervalMs: Long = 3000L,
) {
    private val handler = Handler(Looper.getMainLooper())

    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            val itemCount = viewPager.adapter?.itemCount ?: 0
            if (itemCount > 0) {
                val nextPage = (viewPager.currentItem + 1) % itemCount
                viewPager.setCurrentItem(nextPage, true)
                // Đặt lịch chuyển trang tiếp theo
                handler.postDelayed(this, autoScrollIntervalMs)
            }
        }
    }

    init {
        setupTouchListener()
        setupPageTransformer()
    }

    /**
     * Bắt đầu tự động cuộn trang.
     */
    fun startAutoScroll() {
        stopAutoScroll()
        handler.postDelayed(autoScrollRunnable, autoScrollIntervalMs)
    }

    /**
     * Dừng tự động cuộn trang.
     */
    fun stopAutoScroll() {
        handler.removeCallbacks(autoScrollRunnable)
    }

    /**
     * Tạm dừng đếm ngược khi chạm ngón tay vào ViewPager2 và đếm lại từ đầu khi buông tay.
     */
    private fun setupTouchListener() {
        val recyclerView = viewPager.getChildAt(0) as? RecyclerView ?: return

        recyclerView.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                        // Người dùng đang chạm ngón tay -> Dừng đếm ngược
                        stopAutoScroll()
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        // Nhả tay ra -> Khởi tạo lại đếm ngược từ đầu
                        startAutoScroll()
                    }
                }
                return false
            }
        })
    }

    /**
     * Cấu hình hiệu ứng thu phóng (Scale) và lề 2 bên (Margin Page Transformer).
     */
    private fun setupPageTransformer() {
        val compositePageTransformer = CompositePageTransformer().apply {
            // Khoảng cách giữa các trang (Margin)
            addTransformer(MarginPageTransformer(16))

            // Hiệu ứng Zoom / Fade (Page Transformer)
            addTransformer { page, position ->
                val r = 1 - abs(position)
                page.scaleY = 0.85f + r * 0.15f
                page.alpha = 0.5f + r * 0.5f
            }
        }
        viewPager.setPageTransformer(compositePageTransformer)
    }
}
```

---

## 🚀 3. Cách Sử Dụng Trong Activity / Fragment XML

```kotlin
class BannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBannerBinding
    private lateinit var autoScrollHelper: AutoScrollViewPagerHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bannerImages = listOf(
            R.drawable.banner1,
            R.drawable.banner2,
            R.drawable.image_login
        )

        val adapter = BannerAdapter(bannerImages)
        binding.viewPager.adapter = adapter

        // Khởi tạo AutoScrollHelper
        autoScrollHelper = AutoScrollViewPagerHelper(
            viewPager = binding.viewPager,
            autoScrollIntervalMs = 3000L // 3 giây chuyển trang 1 lần
        )
    }

    override fun onResume() {
        super.onResume()
        autoScrollHelper.startAutoScroll()
    }

    override fun onPause() {
        super.onPause()
        autoScrollHelper.stopAutoScroll()
    }
}
```

---

## 📌 Tóm Tắt Ưu Điểm Khi Sử Dụng Với XML ViewPager2:
- Tự động dừng đếm ngược ngay khi người dùng giữ ngón tay chạm vào `ViewPager2`.
- Tự động reset timer đúng 3 giây sau khi người dùng vừa vuốt xong.
- Tự động tạm dừng cuộn khi Activity đi vào `onPause()` để tiết kiệm pin và tài nguyên bộ nhớ.
