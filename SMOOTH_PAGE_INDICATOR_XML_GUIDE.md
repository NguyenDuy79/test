# 🎨 Hướng Dẫn Làm Smooth Page Indicator Cho ViewPager2 Bằng XML Layout (Custom View)

Tài liệu này hướng dẫn chi tiết cách tạo một **Smooth Page Indicator View** (Chỉ báo trang với animation mượt mà tương tự `smooth_page_indicator` Flutter) trên giao diện **Android XML Layout (View System)**.

---

## 🛠️ 1. Mã Nguồn Custom View `SmoothPageIndicatorView.kt`

Tạo một Custom View kế thừa từ `View` để vẽ các chấm indicator và tính toán vị trí cuộn theo `ViewPager2`:

```kotlin
package com.example.fitness.myapplication.core.designsystem.component

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.fitness.myapplication.R

enum class XmlIndicatorEffect {
    EXPANDING, // Mở rộng chấm active thành dạng viên thuốc (Capsule)
    SCALE,     // Phóng to kích thước chấm active
    SIMPLE     // Đổi màu đơn giản
}

class SmoothPageIndicatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var count: Int = 0
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    var effect: XmlIndicatorEffect = XmlIndicatorEffect.EXPANDING
        set(value) {
            field = value
            invalidate()
        }

    private var activePosition: Int = 0
    private var positionOffset: Float = 0f

    private val dotRadius = dpToPx(4f)
    private val expandedDotWidth = dpToPx(24f)
    private val spacing = dpToPx(8f)

    private val activePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.orange_primary_500)
    }

    private val inactivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.neutral_300)
    }

    private val rectF = RectF()

    /**
     * Gắn ViewPager2 để tự động lắng nghe sự kiện cuộn trang.
     */
    fun attachToViewPager2(viewPager: ViewPager2) {
        this.count = viewPager.adapter?.itemCount ?: 0

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                this@SmoothPageIndicatorView.activePosition = position
                this@SmoothPageIndicatorView.positionOffset = positionOffset
                invalidate() // Vẽ lại Canvas liên tục khi ngón tay vuốt
            }

            override fun onPageSelected(position: Int) {
                this@SmoothPageIndicatorView.activePosition = position
                invalidate()
            }
        })
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (count <= 0) {
            setMeasuredDimension(0, 0)
            return
        }

        val dotHeight = (dotRadius * 2).toInt()
        val totalWidth = when (effect) {
            XmlIndicatorEffect.EXPANDING -> {
                (count - 1) * (dotRadius * 2 + spacing) + expandedDotWidth
            }
            else -> {
                count * (dotRadius * 2 + spacing) - spacing
            }
        }.toInt()

        setMeasuredDimension(
            resolveSize(totalWidth + paddingLeft + paddingRight, widthMeasureSpec),
            resolveSize(dotHeight + paddingTop + paddingBottom, heightMeasureSpec)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (count <= 0) return

        var startX = paddingLeft.toFloat()
        val centerY = height / 2f

        for (i in 0 until count) {
            val isSelected = (i == activePosition)

            val currentWidth = when (effect) {
                XmlIndicatorEffect.EXPANDING -> {
                    if (isSelected) {
                        expandedDotWidth - (expandedDotWidth - dotRadius * 2) * positionOffset
                    } else if (i == activePosition + 1) {
                        dotRadius * 2 + (expandedDotWidth - dotRadius * 2) * positionOffset
                    } else {
                        dotRadius * 2
                    }
                }
                else -> dotRadius * 2
            }

            val paint = if (isSelected) activePaint else inactivePaint

            rectF.set(
                startX,
                centerY - dotRadius,
                startX + currentWidth,
                centerY + dotRadius
            )

            canvas.drawRoundRect(rectF, dotRadius, dotRadius, paint)
            startX += currentWidth + spacing
        }
    }

    private fun dpToPx(dp: Float): Float = dp * context.resources.displayMetrics.density
}
```

---

## 📝 2. Khai Báo Trong File XML Layout (`activity_main.xml`)

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:gravity="center_horizontal"
    android:orientation="vertical">

    <!-- ViewPager2 Slider -->
    <androidx.viewpager2.widget.ViewPager2
        android:id="@+id/viewPager"
        android:layout_width="match_parent"
        android:layout_height="200dp" />

    <!-- Custom Smooth Page Indicator -->
    <com.example.fitness.myapplication.core.designsystem.component.SmoothPageIndicatorView
        android:id="@+id/pageIndicator"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp" />

</LinearLayout>
```

---

## ⚡ 3. Gắn Indicator Với ViewPager2 Trong Activity / Fragment

```kotlin
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Gắn Adapter cho ViewPager2
        val adapter = BannerAdapter(getBanners())
        binding.viewPager.adapter = adapter

        // 2. Kết nối SmoothPageIndicatorView với ViewPager2
        binding.pageIndicator.attachToViewPager2(binding.viewPager)
    }
}
```

---

## 💡 Tóm Tắt

Cả 2 tài liệu hướng dẫn hiện tại đã bao phủ trọn vẹn cả **Jetpack Compose** và **Android XML Layout (View System)**:

| Tính Năng | Jetpack Compose | Android XML (View System) |
| :--- | :--- | :--- |
| **Auto-Scroll Pager** | `AutoScrollPager.kt` (Dùng `LaunchedEffect` & `HorizontalPager`) | `AutoScrollViewPagerHelper.kt` (Dùng `Handler` & `ViewPager2`) |
| **Smooth Indicator** | `SmoothPageIndicator.kt` (Dùng `animateDpAsState` & `Row`) | `SmoothPageIndicatorView.kt` (Dùng `Custom View` & `Canvas`) |
