# CarouselSlider: Giải thích toàn bộ file, từ UI xuống tới hàm

File code: `app/src/main/java/com/example/fitness/myapplication/core/designsystem/component/CarouselSlider.kt`

Tài liệu đi theo thứ tự **từ ngoài vào trong**: bắt đầu từ cái người dùng nhìn thấy trên màn hình, rồi đi dần xuống từng hàm bên dưới.

## Mục lục

- [Phần 0: Bức tranh tổng thể](#phần-0-bức-tranh-tổng-thể)
- **Tầng UI (thứ nhìn thấy được)**
  - [Phần 1: Màn hình dùng component — `OnboardingScreen`](#phần-1-màn-hình-dùng-component--onboardingscreen)
  - [Phần 2: `CarouselSliderWithText` — ảnh + chấm + chữ](#phần-2-carouselsliderwithtext--ảnh--chấm--chữ)
  - [Phần 3: `CarouselSlider` — lớp bọc cho danh sách](#phần-3-carouselslider--lớp-bọc-cho-danh-sách)
  - [Phần 4: `CarouselSliderBuilder` (phần giao diện) — vẽ từng trang](#phần-4-carouselsliderbuilder-phần-giao-diện--vẽ-từng-trang)
- **Tầng logic (thứ chạy ngầm)**
  - [Phần 5: Trang thật và trang ảo — cuộn vô hạn](#phần-5-trang-thật-và-trang-ảo--cuộn-vô-hạn)
  - [Phần 6: Các biến trạng thái](#phần-6-các-biến-trạng-thái)
  - [Phần 7: Vòng lặp AutoPlay](#phần-7-vòng-lặp-autoplay)
  - [Phần 8: Phát hiện ngón tay chạm](#phần-8-phát-hiện-ngón-tay-chạm)
  - [Phần 9: Callback `onPageChanged` và `reason`](#phần-9-callback-onpagechanged-và-reason)
- **Tầng cấu hình và điều khiển**
  - [Phần 10: `CarouselController` — điều khiển bằng code](#phần-10-carouselcontroller--điều-khiển-bằng-code)
  - [Phần 11: `CarouselOptions` — bảng cấu hình](#phần-11-carouseloptions--bảng-cấu-hình)
  - [Phần 12: Các `enum`](#phần-12-các-enum)
  - [Phần 13: `CarouselSliderPreview`](#phần-13-carouselsliderpreview)
- [Phần 14: Bản cũ sai ở đâu và cách kiểm tra](#phần-14-bản-cũ-sai-ở-đâu-và-cách-kiểm-tra)

---

## Phần 0: Bức tranh tổng thể

Trên màn Onboarding, người dùng thấy:

```
┌──────────────────────────────────────┐
│  ┌──┐ ┌──────────────────────┐ ┌──┐  │  ← Ảnh (Carousel): ảnh giữa to,
│  │  │ │                      │ │  │  │    2 ảnh bên hé ra và nhỏ hơn
│  │B │ │        ẢNH A         │ │C │  │
│  │  │ │                      │ │  │  │
│  └──┘ └──────────────────────┘ └──┘  │
│               ● ━━ ●                 │  ← Chấm chỉ báo trang
│        Học Tiếng Anh Với AI          │  ← Chữ đổi theo ảnh
│   Luyện phản xạ 1:1 cùng AI Edupia.  │
└──────────────────────────────────────┘
```

Các hàm gọi nhau theo thứ tự sau (mũi tên = "gọi tới"):

```
OnboardingScreen                       (màn hình của app)
   └─► CarouselSliderWithText          Phần 2: ghép ảnh + chấm + chữ
         ├─► CarouselSlider            Phần 3: nhận List<T>
         │     └─► CarouselSliderBuilder   Phần 4–9: bộ não thật sự
         │           ├─ HorizontalPager / VerticalPager  (vẽ trang)
         │           ├─ Vòng lặp AutoPlay               (tự chuyển trang)
         │           ├─ Bộ nghe ngón tay                (dừng khi chạm)
         │           └─ Gửi onPageChanged               (báo ra ngoài)
         ├─► SmoothPageIndicator       (chấm chỉ báo, file khác)
         └─► AnimatedContent           (chữ bên dưới)

CarouselController   Phần 10: "điều khiển từ xa", gọi nextPage(), animateToPage()...
CarouselOptions      Phần 11: toàn bộ cài đặt
```

---

# TẦNG UI

## Phần 1: Màn hình dùng component — `OnboardingScreen`

File: `feature/onboarding/OnboardingScreen.kt`. Đây là nơi **sử dụng** carousel, để thấy ta cần truyền vào những gì:

```kotlin
val carouselController = rememberCarouselController()
```
Tạo một "điều khiển từ xa" để lát nữa chuyển trang bằng code (xem Phần 10).

```kotlin
LaunchedEffect(effectFlow) {
    effectFlow?.collectLatest { effect ->
        when (effect) {
            is OnboardingEffect.ScrollToPage -> {
                carouselController.animateToPage(effect.page)
            }
```
ViewModel bảo "sang trang X" thì màn hình gọi `animateToPage(X)`. Ở đây **X là trang thật** (0, 1, 2…).

```kotlin
CarouselSliderWithText(
    items = state.pages,                  // danh sách dữ liệu
    controller = carouselController,
    options = CarouselOptions(
        height = 240.dp,                  // carousel cao 240dp
        viewportFraction = 0.85f,         // ảnh giữa rộng 85%, 2 bên hé 7.5%
        enlargeCenterPage = true,         // ảnh giữa to, 2 bên nhỏ
        enlargeFactor = 0.25f,            // 2 bên nhỏ đi 25%
        autoPlay = true,                  // tự chuyển trang
        autoPlayInterval = 3500L,         // mỗi 3.5 giây
        enableInfiniteScroll = true,      // lặp vòng tròn
        initialPage = state.currentPage,
        onPageChanged = { index, _ ->
            onIntent(OnboardingIntent.PageChanged(index))  // báo ViewModel
        },
    ),
    indicatorEffect = IndicatorEffect.WORM,
    itemContent = { item, _, _ -> Image(...) },        // cách vẽ 1 ảnh
    bottomTextContent = { item, _ -> ... },             // cách vẽ chữ
)
```

---

## Phần 2: `CarouselSliderWithText` — ảnh + chấm + chữ

Component "trọn gói" mà màn hình gọi tới.

### 2.1. Tham số

```kotlin
@Composable
fun <T> CarouselSliderWithText(
    items: List<T>,
```
`<T>` nghĩa là **kiểu dữ liệu nào cũng được** (ví dụ `OnboardingPage`, `Banner`…). `items` là danh sách cần hiển thị.

```kotlin
    modifier: Modifier = Modifier,
    options: CarouselOptions = CarouselOptions(
        autoPlay = true,
        autoPlayInterval = 3500L,
        viewportFraction = 0.85f,
        enlargeCenterPage = true,
    ),
```
Cấu hình mặc định nếu người gọi không truyền: tự chạy mỗi 3,5s, ảnh giữa rộng 85% và phóng to.

```kotlin
    controller: CarouselController = rememberCarouselController(),
    indicatorEffect: IndicatorEffect = IndicatorEffect.WORM,
```
Không truyền controller thì tự tạo. Kiểu chấm mặc định là `WORM` (chấm kéo dài như con sâu khi vuốt).

```kotlin
    itemContent: @Composable (item: T, index: Int, realIndex: Int) -> Unit,
    bottomTextContent: @Composable (item: T, realIndex: Int) -> Unit,
) {
```
Hai "khuôn vẽ" do người dùng component tự viết:
- `itemContent`: vẽ **một ảnh/trang** như thế nào.
- `bottomTextContent`: vẽ **phần chữ** bên dưới như thế nào.

### 2.2. Thân hàm

```kotlin
    if (items.isEmpty()) return
```
Danh sách rỗng → không vẽ gì.

```kotlin
    var currentRealIndex by remember { mutableIntStateOf(options.initialPage.coerceIn(0, items.size - 1)) }
```
Ghi nhớ **trang thật đang hiển thị**, để phần chữ biết hiện chữ của trang nào.
- `remember`: giữ giá trị qua các lần vẽ lại.
- `mutableIntStateOf`: khi giá trị đổi, phần chữ tự vẽ lại.
- `coerceIn(0, items.size - 1)`: chặn trong khoảng hợp lệ (truyền `initialPage = 99` cũng không crash).

```kotlin
    val combinedOptions = options.copy(
        onPageChanged = { index, reason ->
            currentRealIndex = index
            options.onPageChanged?.invoke(index, reason)
        },
    )
```
"Chen" thêm việc vào `onPageChanged`. Mỗi khi đổi trang:
1. Cập nhật `currentRealIndex` để chữ đổi theo.
2. Vẫn gọi tiếp `onPageChanged` gốc của người dùng (nếu có), không làm mất callback của họ.

`copy(...)` tạo bản sao của `options`, chỉ thay đúng trường `onPageChanged`.

```kotlin
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
```
Xếp 3 khối theo **chiều dọc**, căn giữa ngang.

**Khối 1: Ảnh**
```kotlin
        CarouselSlider(
            items = items,
            options = combinedOptions,
            controller = controller,
            itemContent = itemContent,
        )
        Spacer(modifier = Modifier.height(16.dp))
```
Gọi xuống `CarouselSlider` (Phần 3), dùng `combinedOptions` để nhận được thông báo đổi trang. Sau đó chừa khoảng trống 16dp.

**Khối 2: Chấm chỉ báo**
```kotlin
        controller.pagerState?.let { state ->
            SmoothPageIndicator(
                pagerState = state,
                pageCount = items.size,
                effect = indicatorEffect,
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
```
- `controller.pagerState` được `CarouselSliderBuilder` gắn vào ngay lúc vẽ khối 1 (Phần 4.2). Vì khối 1 vẽ trước, đến đây đã có giá trị.
- `?.let { }`: chỉ vẽ khi `pagerState` khác `null`.
- `pageCount = items.size`: truyền **số trang thật** (3), không phải ~2 tỷ trang ảo. `SmoothPageIndicator` tự đổi trang ảo về trang thật.

**Khối 3: Chữ**
```kotlin
        AnimatedContent(
            targetState = currentRealIndex,
```
`AnimatedContent` chạy hiệu ứng mỗi khi `targetState` đổi, tức mỗi khi sang trang mới.

```kotlin
            transitionSpec = {
                (fadeIn(animationSpec = tween(350)) + slideInVertically { height -> height / 2 }) togetherWith
                    (fadeOut(animationSpec = tween(350)) + slideOutVertically { height -> -height / 2 })
            },
```
Hiệu ứng chuyển chữ, **cùng lúc** (`togetherWith`):
- Chữ mới: hiện dần (`fadeIn` 350ms) + trượt từ **dưới lên** (bắt đầu thấp hơn nửa chiều cao).
- Chữ cũ: mờ dần (`fadeOut`) + trượt **lên trên** rồi biến mất.

```kotlin
            label = "carousel_text_animation",
        ) { targetIndex ->
            val safeIndex = targetIndex.coerceIn(0, items.size - 1)
            bottomTextContent(items[safeIndex], safeIndex)
        }
```
- `label`: tên hiển thị trong công cụ debug animation của Android Studio.
- `safeIndex`: chặn chỉ số hợp lệ phòng khi danh sách bị đổi ngắn đi.
- Gọi "khuôn vẽ chữ" của người dùng.

---

## Phần 3: `CarouselSlider` — lớp bọc cho danh sách

```kotlin
@Composable
fun <T> CarouselSlider(
    items: List<T>,
    modifier: Modifier = Modifier,
    options: CarouselOptions = CarouselOptions(),
    controller: CarouselController = rememberCarouselController(),
    itemContent: @Composable (item: T, index: Int, realIndex: Int) -> Unit,
) {
    CarouselSliderBuilder(
        itemCount = items.size,
        modifier = modifier,
        options = options,
        controller = controller,
        itemContent = { index, realIndex ->
            if (realIndex in items.indices) {
                itemContent(items[realIndex], index, realIndex)
            }
        },
    )
}
```
Lớp bọc mỏng, cho người dùng **truyền thẳng danh sách** thay vì phải tự tra cứu theo số.
- `CarouselSliderBuilder` chỉ biết **số lượng** (`itemCount`) và gọi lại với số trang.
- Hàm này "dịch" số trang thành **phần tử thật**: `items[realIndex]`.
- `realIndex in items.indices`: kiểm tra số hợp lệ trước khi lấy, tránh crash.

Ba tham số trong `itemContent(item, index, realIndex)`:

| Tên | Ý nghĩa | Ví dụ (3 ảnh, cuộn vô hạn) |
| :--- | :--- | :--- |
| `item` | Phần tử dữ liệu | `OnboardingPage("Học Tiếng Anh…")` |
| `index` | Số trang **ảo** trong Pager | `1073741823` |
| `realIndex` | Số trang **thật** trong danh sách | `1` |

> Giống Flutter: `CarouselSlider(items: ...)` thì gọi `CarouselSlider.builder(itemCount: ...)` bên trong.

---

## Phần 4: `CarouselSliderBuilder` (phần giao diện) — vẽ từng trang

Đây là **bộ não**. Phần này giải thích nửa **giao diện** (cuối hàm). Nửa **logic** (đầu hàm) nằm ở Phần 5–9.

### 4.1. Khung chứa: chiều cao + bộ nghe ngón tay

```kotlin
val containerModifier = modifier
    .then(
        if (options.height != null) {
            Modifier.height(options.height)
        } else {
            Modifier.aspectRatio(options.aspectRatio)
        },
    )
    .pointerInput(Unit) { ... }   // bộ nghe ngón tay, xem Phần 8
```
Quyết định **kích thước** carousel:
- Có `height` (ví dụ 240dp) → cao đúng 240dp.
- Không có → cao theo **tỉ lệ khung hình**, mặc định 16:9 (rộng 360dp thì cao ~202dp).

### 4.2. Đo kích thước cha và tính khoảng hé 2 bên

```kotlin
BoxWithConstraints(
    modifier = containerModifier,
) {
    val parentWidth = maxWidth
    val parentHeight = maxHeight
```
`BoxWithConstraints` cho biết khung chứa **rộng và cao bao nhiêu**. Cần biết vậy mới tính được khoảng hé 2 bên.

```kotlin
    val horizontalPadding = if (options.scrollDirection == CarouselScrollDirection.HORIZONTAL && options.viewportFraction < 1f) {
        parentWidth * ((1f - options.viewportFraction) / 2f)
    } else {
        0.dp
    }
```
Đây là cách tạo hiệu ứng **"hé lộ trang bên cạnh"**:
```
viewportFraction = 0.85 → ảnh giữa chiếm 85%
Phần còn lại: 1 - 0.85 = 15% → chia đôi → mỗi bên 7.5%

│◄7.5%►│◄─────── 85% ───────►│◄7.5%►│
│ hé B │        ẢNH A        │ hé C │
```
Với màn 400dp: `400 × 0.075 = 30dp` mỗi bên.

```kotlin
    val verticalPadding = if (options.scrollDirection == CarouselScrollDirection.VERTICAL && options.viewportFraction < 1f) {
        parentHeight * ((1f - options.viewportFraction) / 2f)
    } else {
        0.dp
    }
```
Y hệt, nhưng cho chế độ **cuộn dọc** (hé trên/dưới).

```kotlin
    val contentPadding = PaddingValues(
        horizontal = horizontalPadding,
        vertical = verticalPadding,
    )
    val pageModifier = Modifier.fillMaxSize()
```
Gom lại thành `PaddingValues` để đưa vào Pager. Pager chiếm toàn bộ khung (`fillMaxSize`).

### 4.3. Pager: thành phần vuốt trang của Compose

```kotlin
    if (options.scrollDirection == CarouselScrollDirection.HORIZONTAL) {
        HorizontalPager(
            state = pagerState,
            modifier = pageModifier,
            contentPadding = contentPadding,
            reverseLayout = options.reverse,
        ) { page ->
```
- Cuộn ngang → `HorizontalPager`. Cuộn dọc → `VerticalPager` (code y hệt, ở nhánh `else`).
- `state = pagerState`: "bộ nhớ" của Pager (đang ở trang nào, đang cuộn không…), tạo ở Phần 5.
- `contentPadding`: khoảng hé 2 bên vừa tính.
- `reverseLayout`: `true` thì đảo ngược thứ tự trang.
- `{ page -> ... }`: Pager gọi khối này **cho mỗi trang cần vẽ** (thường là trang giữa + 2 trang bên). `page` là số **trang ảo**.

```kotlin
            val realIndex = (page % itemCount + itemCount) % itemCount
```
Đổi trang ảo → trang thật (giải thích ở Phần 5).

### 4.4. Hiệu ứng phóng to trang giữa

```kotlin
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (options.enlargeCenterPage) {
                            Modifier.graphicsLayer { ... }
                        } else {
                            Modifier
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                itemContent(page, realIndex)
            }
```
Mỗi trang nằm trong một `Box`. Nếu bật `enlargeCenterPage` thì gắn thêm `graphicsLayer` để co giãn. Cuối cùng gọi `itemContent` để vẽ nội dung.

> `graphicsLayer` co giãn/làm mờ ở **bước vẽ**, không bắt Compose đo đạc lại bố cục, nên rất mượt khi chạy mỗi khung hình lúc vuốt.

**Tính khoảng cách tới tâm:**
```kotlin
                Modifier.graphicsLayer {
                    val pageOffset = (
                        (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                    ).let { abs(it) }
```
`pageOffset` = trang này **cách tâm màn hình bao xa**, tính bằng "số trang":
- `currentPage - page`: chênh lệch giữa trang đang hiển thị và trang đang vẽ.
- `currentPageOffsetFraction`: đã vuốt được bao nhiêu phần của trang (từ -0.5 đến 0.5).
- `abs(...)`: lấy trị tuyệt đối (trái hay phải đều tính là "xa").

```
Đang đứng yên ở A:   A → 0      B, C → 1
Vuốt nửa đường A→B:  A → 0.5    B → 0.5
```

**Tính tỉ lệ co:**
```kotlin
                    val scaleFraction = 1f - (pageOffset * options.enlargeFactor).coerceIn(0f, 1f)
```
Với `enlargeFactor = 0.25`:
- Trang giữa (`pageOffset = 0`): `1 - 0 = 1.0` → kích thước gốc 100%.
- Trang bên (`pageOffset = 1`): `1 - 0.25 = 0.75` → nhỏ còn 75%.
- Đang vuốt nửa đường (`0.5`): `0.875` → co giãn **liên tục, mượt** theo ngón tay.
- `coerceIn(0f, 1f)`: không bao giờ co quá 100% (kích thước âm).

**Áp dụng theo kiểu:**
```kotlin
                    when (options.enlargeStrategy) {
                        CenterPageEnlargeStrategy.SCALE -> {
                            scaleX = scaleFraction
                            scaleY = scaleFraction
                        }
```
`SCALE`: co **cả ngang lẫn dọc**, như thu nhỏ cả bức ảnh.

```kotlin
                        CenterPageEnlargeStrategy.HEIGHT -> {
                            scaleY = scaleFraction
                        }
```
`HEIGHT`: chỉ co **chiều cao**, chiều ngang giữ nguyên.

```kotlin
                        CenterPageEnlargeStrategy.ZOOM -> {
                            scaleX = scaleFraction
                            scaleY = scaleFraction
                            alpha = lerp(
                                start = 0.5f,
                                stop = 1.0f,
                                fraction = 1f - pageOffset.coerceIn(0f, 1f),
                            )
                        }
                    }
```
`ZOOM`: co như `SCALE` **và làm mờ** trang bên.
`lerp(0.5, 1.0, x)` = "lấy giá trị ở vị trí x trên đoạn 0.5 → 1.0":
- Trang giữa: `x = 1` → `alpha = 1.0` (rõ hoàn toàn).
- Trang bên: `x = 0` → `alpha = 0.5` (mờ một nửa).

---

# TẦNG LOGIC

Tất cả các phần dưới đây nằm ở **đầu** hàm `CarouselSliderBuilder`, chạy trước phần giao diện ở Phần 4.

## Phần 5: Trang thật và trang ảo — cuộn vô hạn

Có 3 ảnh A, B, C. Muốn vuốt mãi không hết (A → B → C → A → …), ta "giả vờ" là có rất nhiều trang:
```
Trang ảo:   ... 1073741822  1073741823  1073741824  1073741825 ...
Trang thật: ...     A           B           C           A      ...
```

```kotlin
if (itemCount <= 0) return
```
Không có phần tử thì không vẽ.

```kotlin
val isInfinite = options.enableInfiniteScroll && itemCount > 1
```
Chỉ cuộn vô hạn khi **có từ 2 phần tử trở lên** (1 ảnh thì chẳng có gì để lặp).

```kotlin
val totalPages = if (isInfinite) Int.MAX_VALUE else itemCount
```
Vô hạn → Pager có ~2,1 tỷ trang. Không vô hạn → đúng số phần tử.

```kotlin
val initialVirtualPage = if (isInfinite) {
    val half = Int.MAX_VALUE / 2
    half - (half % itemCount) + options.initialPage.coerceIn(0, itemCount - 1)
} else {
    options.initialPage.coerceIn(0, itemCount - 1)
}
```
Bắt đầu **ở giữa** dải số, để vuốt trái hay phải đều còn ~1 tỷ trang:
- `half - (half % itemCount)`: làm tròn xuống số **chia hết cho `itemCount`**, tức một trang ảo ứng với trang thật số 0 (A).
- `+ initialPage`: dịch tới trang muốn hiển thị đầu tiên.

```kotlin
val pagerState = rememberPagerState(
    initialPage = initialVirtualPage,
    pageCount = { totalPages },
)
```
Tạo "bộ nhớ" cho Pager. Nó biết: đang ở trang nào (`currentPage`), trang đã dừng hẳn (`settledPage`), có đang cuộn không (`isScrollInProgress`)…

```kotlin
controller.pagerState = pagerState
controller.itemCountProvider = { itemCount }
controller.infiniteScrollProvider = { isInfinite }
```
"Nối dây" cho điều khiển từ xa: đưa `pagerState` và thông tin số lượng cho `CarouselController`, để nó gọi được `nextPage()`, `animateToPage()`…

**Công thức đổi trang ảo → trang thật** (dùng ở rất nhiều chỗ):
```kotlin
val realIndex = (page % itemCount + itemCount) % itemCount
```
Ví dụ với 3 phần tử: `1073741825 % 3 = 0` → A. Phần `+ itemCount) % itemCount` để kết quả **không bao giờ âm** (trong Kotlin, `-1 % 3 = -1`).

---

## Phần 6: Các biến trạng thái

```kotlin
val isDragged by pagerState.interactionSource.collectIsDraggedAsState()
```
`true` khi người dùng **đang kéo** slider. Compose tự cập nhật biến này.

```kotlin
var isTouched by remember { mutableStateOf(false) }
```
`true` khi **có ngón tay đặt trên slider**, kể cả chỉ chạm giữ mà chưa kéo. Biến này được bật/tắt ở Phần 8.
> Vì sao cần cả hai? Flutter `pauseAutoPlayOnTouch` dừng ngay khi **chạm**. Nếu chỉ dùng `isDragged` thì người dùng chạm giữ để xem ảnh, slider vẫn tự trôi đi.

```kotlin
val currentItemCount by rememberUpdatedState(itemCount)
val currentOnPageChanged by rememberUpdatedState(options.onPageChanged)
```
Các coroutine ở Phần 7 và 9 chạy rất lâu và **không khởi động lại** mỗi lần vẽ lại. Nếu đọc thẳng `itemCount` hay `options.onPageChanged`, chúng sẽ giữ **giá trị cũ** từ lần đầu. `rememberUpdatedState` như một "hộp" luôn chứa giá trị mới nhất.

---

## Phần 7: Vòng lặp AutoPlay

### 7.1. Khi nào vòng lặp khởi động lại?

```kotlin
LaunchedEffect(
    pagerState,
    options.autoPlay,
    controller.isAutoPlayPlaying,
    options.autoPlayInterval,
    options.autoPlayAnimationDuration,
    options.autoPlayCurve,
    options.pauseAutoPlayOnTouch,
    isInfinite,
) {
```
`LaunchedEffect` chạy một coroutine. Khi **bất kỳ key nào đổi**, coroutine cũ bị huỷ và chạy lại. Các key ở đây đều là **cấu hình**, chỉ đổi khi đổi cài đặt hoặc gọi `stopAutoPlay()` / `startAutoPlay()`.

> ⚠️ **Tuyệt đối không** đưa `pagerState.currentPage` vào key. Đây là lỗi của bản cũ (Phần 14).

```kotlin
    if (!options.autoPlay || !controller.isAutoPlayPlaying) return@LaunchedEffect
```
AutoPlay đang tắt → thoát.

### 7.2. Điều kiện "được phép đếm giờ"

```kotlin
    fun canTick(): Boolean =
        !pagerState.isScrollInProgress &&
            !(options.pauseAutoPlayOnTouch && (isDragged || isTouched))
```
Chỉ đếm khi **cả hai** đúng:
- Slider **đứng yên**: không bị kéo, không trôi theo đà, không chạy animation.
- Nếu bật "dừng khi chạm" thì không được có ngón tay nào trên slider.

### 7.3. Ba bước của vòng lặp

```kotlin
    while (true) {
```
Lặp mãi. Không bị treo, vì mỗi vòng đều có lệnh **chờ**. Đóng màn hình → `LaunchedEffect` bị huỷ → vòng lặp dừng ngay tại lệnh chờ.

**Bước 1: Chờ slider rảnh**
```kotlin
        snapshotFlow { canTick() && currentItemCount > 1 }.first { it }
```
- `snapshotFlow { }` theo dõi các State bên trong và phát giá trị mới mỗi khi chúng đổi.
- `.first { it }` **đứng chờ** đến khi giá trị là `true`.

**Bước 2: Đếm ngược, bị làm phiền thì đếm lại**
```kotlin
        val startPage = pagerState.settledPage
```
Ghi nhớ trang đang đứng. `settledPage` là trang **đã dừng hẳn**. Khác với `currentPage` (đổi ngay khi trượt quá nửa trang), nó không đổi giữa chừng.

```kotlin
        val interrupted = withTimeoutOrNull(options.autoPlayInterval) {
            snapshotFlow { canTick() && pagerState.settledPage == startPage }.first { !it }
        }
        if (interrupted != null) continue
```
- `withTimeoutOrNull(3500)`: chạy khối bên trong **tối đa 3,5 giây**. Hết giờ mà chưa xong thì trả về `null`.
- Khối bên trong chờ chuyện "làm phiền": bị chạm, bị kéo, hoặc trang bị đổi.

| Kết quả | Ý nghĩa | Làm gì |
| :--- | :--- | :--- |
| `null` | Yên ổn đủ thời gian | Sang bước 3 |
| khác `null` | Bị làm phiền giữa chừng | `continue` → bước 1, **đếm lại từ 0** |

**Bước 3: Chuyển trang**
```kotlin
        val count = currentItemCount
        val target = if (isInfinite) {
            pagerState.currentPage + 1
        } else {
            (pagerState.currentPage + 1) % count
        }
```
- Vô hạn: sang trang ảo kế tiếp (C → A vẫn là đi tới).
- Không vô hạn: đến trang cuối thì `% count` quay về 0, giống Flutter.

```kotlin
        controller.lastReason = CarouselPageChangedReason.TIMED
```
Đánh dấu lần đổi trang này là do **hết giờ**.

```kotlin
        try {
            pagerState.animateScrollToPage(
                page = target,
                animationSpec = tween(
                    durationMillis = options.autoPlayAnimationDuration.toInt(),
                    easing = options.autoPlayCurve,
                ),
            )
        } catch (e: CancellationException) {
            currentCoroutineContext().ensureActive()
        }
    }
}
```
- `animateScrollToPage`: trượt sang trang mới và **chờ đến khi trượt xong**.
- Nếu người dùng vuốt chen ngang, Compose ưu tiên ngón tay và huỷ animation bằng `CancellationException`. Bắt lỗi này thì vòng lặp **không chết**, quay lại bước 1.
- `ensureActive()`: nếu chính `LaunchedEffect` bị huỷ (đóng màn hình) thì ném lỗi tiếp để thoát hẳn.

### 7.4. Dòng thời gian

```
Bình thường (interval 3.5s, animation 0.8s):
0s ────────── 3.5s ─ 4.3s ────────── 7.8s ─ 8.6s
[A đứng yên]  ►trượt►  [B đứng yên]  ►trượt►  [C]

Chạm giữ từ 2s đến 3s:
0s ── 2s ═══ 3s ────────── 6.5s ─ 7.3s
[A]  [chạm]  [A đứng yên]  ►trượt►  [B]
             └ đếm lại 3.5s từ lúc thả tay
```

---

## Phần 8: Phát hiện ngón tay chạm

Gắn vào `containerModifier` (Phần 4.1):

```kotlin
.pointerInput(Unit) {
    awaitEachGesture {
```
Gắn bộ nghe cảm ứng vào khung slider. `awaitEachGesture` lặp lại cho **mỗi lần chạm** (đặt tay → nhấc tay).

```kotlin
        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        isTouched = true
```
- Chờ ngón tay đầu tiên đặt xuống → `isTouched = true`.
- `PointerEventPass.Initial`: nghe sự kiện **trước** Pager. Ta chỉ **nghe**, không "ăn" sự kiện, nên Pager vẫn vuốt bình thường.
- `requireUnconsumed = false`: vẫn nhận kể cả khi chỗ khác đã xử lý.

```kotlin
        try {
            do {
                val event = awaitPointerEvent(PointerEventPass.Initial)
            } while (event.changes.any { it.pressed })
        } finally {
            isTouched = false
        }
```
- Đọc sự kiện liên tục, đến khi **không còn ngón nào ấn** thì thoát.
- `finally`: luôn trả `isTouched` về `false`, kể cả khi cử chỉ bị huỷ, để AutoPlay không kẹt mãi ở trạng thái "đang chạm".

---

## Phần 9: Callback `onPageChanged` và `reason`

### 9.1. Người dùng bắt đầu vuốt → `MANUAL`

```kotlin
LaunchedEffect(pagerState) {
    snapshotFlow { isDragged }.collect { dragging ->
        if (dragging) controller.lastReason = CarouselPageChangedReason.MANUAL
    }
}
```
Vuốt mạnh rồi thả tay, slider còn **trôi theo đà** sang trang khác (lúc này `isDragged = false`). Lần đổi trang đó vẫn phải báo `MANUAL`, nên ta đặt `MANUAL` ngay **khi bắt đầu kéo**.

### 9.2. Gọi callback

```kotlin
LaunchedEffect(pagerState) {
    snapshotFlow { pagerState.currentPage }.drop(1).collectLatest { page ->
```
- Theo dõi `currentPage`. Mỗi lần đổi là một lần báo ra ngoài.
- `.drop(1)`: bỏ giá trị đầu tiên. Lúc vừa hiển thị, trang chưa hề "đổi". Flutter cũng không gọi lúc khởi tạo.

```kotlin
        val count = currentItemCount
        val realIndex = (page % count + count) % count
```
Đổi sang trang thật trước khi báo.

```kotlin
        val reason = if (isDragged) {
            CarouselPageChangedReason.MANUAL
        } else {
            controller.lastReason
        }
        currentOnPageChanged?.invoke(realIndex, reason)
    }
}
```
- Đang kéo → `MANUAL`.
- Không → lấy nguồn được đánh dấu gần nhất.
- **Không reset** `lastReason` sau khi gọi: một lần cuộn có thể đi qua nhiều trang (ví dụ C → B → A khi tự quay về đầu), và tất cả phải cùng một `reason`.

| Nguồn cuộn | Nơi đặt `lastReason` |
| :--- | :--- |
| AutoPlay | Phần 7, bước 3 → `TIMED` |
| `controller.*` | Phần 10 → `CONTROLLER` |
| Người dùng vuốt | Phần 9.1 → `MANUAL` |

---

# TẦNG CẤU HÌNH VÀ ĐIỀU KHIỂN

## Phần 10: `CarouselController` — điều khiển bằng code

### 10.1. Tạo controller

```kotlin
@Composable
fun rememberCarouselController(): CarouselController {
    val coroutineScope = rememberCoroutineScope()
    return remember(coroutineScope) {
        CarouselController(coroutineScope)
    }
}
```
- `rememberCoroutineScope()`: lấy một "phạm vi chạy coroutine" gắn với màn hình. Màn hình đóng thì mọi animation đang chạy tự huỷ.
- `remember`: chỉ tạo controller **một lần**, không tạo mới mỗi lần vẽ lại.

### 10.2. Bên trong class

```kotlin
class CarouselController internal constructor(
    private val coroutineScope: CoroutineScope,
) {
```
`internal constructor`: bên ngoài module không tự `CarouselController(...)` được, bắt buộc dùng `rememberCarouselController()`.

```kotlin
    internal var pagerState: PagerState? = null
    internal var itemCountProvider: (() -> Int)? = null
    internal var infiniteScrollProvider: (() -> Boolean)? = null
```
"Dây nối" tới slider, được `CarouselSliderBuilder` gắn vào (Phần 5). Ban đầu là `null` vì slider chưa vẽ.

```kotlin
    internal var lastReason: CarouselPageChangedReason = CarouselPageChangedReason.MANUAL
```
Nguồn của lần cuộn gần nhất (Phần 9).

```kotlin
    var isAutoPlayPlaying: Boolean by mutableStateOf(true)
        internal set
```
AutoPlay đang bật hay tắt.
- `mutableStateOf`: đổi giá trị → `LaunchedEffect` ở Phần 7 (có key này) tự khởi động lại hoặc dừng.
- `internal set`: bên ngoài chỉ **đọc** được, muốn đổi phải gọi `startAutoPlay()` / `stopAutoPlay()`.

```kotlin
    val realCurrentPage: Int
        get() {
            val state = pagerState ?: return 0
            val count = itemCountProvider?.invoke() ?: return 0
            if (count <= 0) return 0
            return (state.currentPage % count + count) % count
        }
```
Trang **thật** đang hiển thị. Chưa nối dây thì trả về 0.

### 10.3. Đổi trang thật sang trang ảo

```kotlin
    private fun toVirtualPage(state: PagerState, page: Int): Int {
        val count = itemCountProvider?.invoke() ?: return page
        if (count <= 0) return page
        val target = page.coerceIn(0, count - 1)
        val isInfinite = infiniteScrollProvider?.invoke() ?: false
        if (!isInfinite) return target
        return state.currentPage + (target - realCurrentPage)
    }
```
Màn hình gọi `animateToPage(2)` với ý "ảnh thứ 3". Nhưng khi cuộn vô hạn, Pager đang ở trang ảo ~**1 tỷ**. Đưa thẳng số 2 vào thì Pager cố cuộn lùi 1 tỷ trang!

Công thức: **trang ảo hiện tại + khoảng cách giữa hai trang thật**.
```
Đang ở ảo 1073741823 (thật = 1), muốn tới thật = 2:
1073741823 + (2 - 1) = 1073741824  → chỉ trượt 1 trang ✅
```

### 10.4. Các lệnh điều khiển

```kotlin
    fun animateToPage(page: Int, durationMs: Long = 800L, easing: Easing = FastOutSlowInEasing) {
        val state = pagerState ?: return
        coroutineScope.launch {
            lastReason = CarouselPageChangedReason.CONTROLLER
            state.animateScrollToPage(
                page = toVirtualPage(state, page),
                animationSpec = tween(durationMillis = durationMs.toInt(), easing = easing),
            )
        }
    }
```
Trượt mượt tới **trang thật** `page`.
- Chưa nối dây (`pagerState == null`) → bỏ qua.
- `coroutineScope.launch`: animation là hàm `suspend`, phải chạy trong coroutine. Nhờ vậy gọi được từ nút bấm thông thường.
- Đánh dấu `CONTROLLER` rồi trượt.

```kotlin
    fun nextPage(durationMs: Long = 800L, easing: Easing = FastOutSlowInEasing) {
        ...
        if (isInfinite && count > 1) {
            state.animateScrollToPage(page = state.currentPage + 1, ...)
        } else {
            val next = (state.currentPage + 1) % count
            state.animateScrollToPage(page = next, ...)
        }
    }
```
Sang trang kế. Vô hạn → +1 trang ảo. Không vô hạn → đến cuối thì về 0.

```kotlin
    fun previousPage(durationMs: Long = 800L, easing: Easing = FastOutSlowInEasing) {
        ...
        if (isInfinite && count > 1) {
            state.animateScrollToPage(page = state.currentPage - 1, ...)
        } else {
            val prev = if (state.currentPage - 1 < 0) count - 1 else state.currentPage - 1
            state.animateScrollToPage(page = prev, ...)
        }
    }
```
Về trang trước. Không vô hạn mà đang ở trang 0 → nhảy tới trang cuối.

```kotlin
    fun jumpToPage(page: Int) {
        val state = pagerState ?: return
        coroutineScope.launch {
            lastReason = CarouselPageChangedReason.CONTROLLER
            state.scrollToPage(toVirtualPage(state, page))
        }
    }
```
Nhảy **ngay lập tức** (không animation) tới trang thật `page`.

```kotlin
    fun startAutoPlay() { isAutoPlayPlaying = true }
    fun stopAutoPlay()  { isAutoPlayPlaying = false }
```
Bật/tắt AutoPlay. Vì `isAutoPlayPlaying` là key của `LaunchedEffect` (Phần 7), đổi nó thì vòng lặp tự dừng hoặc chạy lại.

---

## Phần 11: `CarouselOptions` — bảng cấu hình

```kotlin
data class CarouselOptions(...)
```
`data class` là class chỉ để chứa dữ liệu, có sẵn `copy()` (dùng ở Phần 2.2).

| Tham số | Mặc định | Ý nghĩa | Dùng ở |
| :--- | :--- | :--- | :--- |
| `height` | `null` | Chiều cao cố định. `null` → tính theo `aspectRatio` | Phần 4.1 |
| `aspectRatio` | `16f / 9f` | Tỉ lệ rộng/cao khi không có `height` | Phần 4.1 |
| `viewportFraction` | `0.8f` | Trang giữa chiếm bao nhiêu % chiều rộng | Phần 4.2 |
| `initialPage` | `0` | Trang hiển thị đầu tiên | Phần 5 |
| `enableInfiniteScroll` | `true` | Cuộn vòng tròn | Phần 5 |
| `reverse` | `false` | Đảo ngược thứ tự trang | Phần 4.3 |
| `autoPlay` | `false` | Tự chuyển trang | Phần 7 |
| `autoPlayInterval` | `4000L` | Thời gian đứng ở mỗi trang (ms) | Phần 7 |
| `autoPlayAnimationDuration` | `800L` | Thời gian trượt (ms) | Phần 7 |
| `autoPlayCurve` | `FastOutSlowInEasing` | Đường cong tốc độ: nhanh lúc đầu, chậm dần lúc cuối | Phần 7 |
| `enlargeCenterPage` | `false` | Phóng to trang giữa | Phần 4.4 |
| `enlargeFactor` | `0.3f` | Trang bên nhỏ đi bao nhiêu (0.3 = 30%) | Phần 4.4 |
| `enlargeStrategy` | `SCALE` | Kiểu co giãn | Phần 4.4 |
| `onPageChanged` | `null` | Callback `(trang thật, nguồn)` khi đổi trang | Phần 9 |
| `scrollDirection` | `HORIZONTAL` | Cuộn ngang hay dọc | Phần 4.2, 4.3 |
| `pauseAutoPlayOnTouch` | `true` | Dừng AutoPlay khi chạm | Phần 7, 8 |
| `padEnds` | `true` | ⚠️ **Đã khai báo nhưng chưa được dùng ở đâu**. Đổi giá trị không có tác dụng | — |

---

## Phần 12: Các `enum`

`enum` là danh sách các lựa chọn cố định.

```kotlin
enum class CarouselPageChangedReason {
    TIMED,      // Đổi trang do AutoPlay hết giờ
    MANUAL,     // Đổi trang do người dùng vuốt
    CONTROLLER  // Đổi trang do gọi controller bằng code
}
```
Được gửi kèm `onPageChanged` để biết **ai** làm đổi trang. Ví dụ: chỉ gửi analytics khi người dùng tự vuốt (`MANUAL`).

```kotlin
enum class CenterPageEnlargeStrategy {
    SCALE,  // Co cả ngang và dọc
    HEIGHT, // Chỉ co chiều cao
    ZOOM    // Co + làm mờ
}
```
Kiểu phóng to trang giữa (Phần 4.4).

```kotlin
enum class CarouselScrollDirection {
    HORIZONTAL, // Ngang
    VERTICAL    // Dọc
}
```
Hướng cuộn (Phần 4.3).

---

## Phần 13: `CarouselSliderPreview`

```kotlin
@Preview(showBackground = true, name = "CarouselSlider Preview")
@Composable
fun CarouselSliderPreview() {
    val sampleList = listOf(
        SampleOnboardingItem("Học Tiếng Anh Với AI", "Luyện phản xạ 1:1 cùng AI Edupia.", R.drawable.image_login),
        ...
    )
    EdupiaTheme {
        Box(...) {
            CarouselSliderWithText(
                items = sampleList,
                options = CarouselOptions(height = 220.dp, autoPlay = true, ...),
                itemContent = { item, _, _ -> Image(...) },
                bottomTextContent = { item, _ -> Column { Text(title); Text(description) } },
            )
        }
    }
}
```
- `@Preview`: Android Studio vẽ thử component ở khung **Design/Split**, không cần chạy app.
- `SampleOnboardingItem`: dữ liệu mẫu (khai báo ở `AutoScrollPager.kt`).
- `EdupiaTheme { }`: bọc theme để màu và chữ đúng thiết kế.
- `_`: bỏ qua tham số không dùng.

> Preview tĩnh không chạy AutoPlay. Bấm nút **Interactive Mode** (▶) trên Preview để xem nó tự trượt.

---

## Phần 14: Bản cũ sai ở đâu và cách kiểm tra

### Lỗi đã sửa

**1. Animation bị huỷ giữa chừng (nặng nhất)**

Code cũ:
```kotlin
LaunchedEffect(..., isDragged, pagerState.currentPage, ...) {
    delay(options.autoPlayInterval)
    pagerState.animateScrollToPage(pagerState.currentPage + 1, ...)
}
```
```
1. Chờ 4s → bắt đầu trượt A → B
2. Trượt quá nửa đường → currentPage đổi A → B
3. currentPage là KEY → LaunchedEffect bị HUỶ và chạy lại
4. Huỷ coroutine = huỷ luôn animation → slider khựng, lệch nửa trang ❌
5. Effect mới chờ 4s rồi trượt tiếp từ chỗ lệch → nhảy cóc ❌
```
→ Sửa bằng vòng lặp `while` + `snapshotFlow` (Phần 7).

**2. Chỉ dừng khi kéo, chạm giữ thì vẫn tự trôi** → thêm `isTouched` (Phần 8).

**3. `onPageChanged` dùng callback cũ và bị gọi lúc khởi tạo** → `rememberUpdatedState` (Phần 6) + `drop(1)` (Phần 9.2).

**4. `reason` sai khi đi qua nhiều trang hoặc trôi theo đà** → không reset sau callback, reset khi bắt đầu kéo (Phần 9).

**5. `animateToPage` / `jumpToPage` đưa trang thật thẳng vào Pager** → `toVirtualPage` (Phần 10.3).

### Kiểm tra bằng tay trên màn Onboarding

| # | Thao tác | Kết quả đúng |
| :-- | :--- | :--- |
| 1 | Để yên | Cứ ~3,5s trượt mượt sang trang kế, **không khựng giữa chừng** |
| 2 | Đến trang cuối, để yên | Trượt tiếp sang trang đầu cùng chiều (không tua ngược) |
| 3 | Chạm giữ trên ảnh (không kéo) | Slider **đứng yên** suốt lúc giữ |
| 4 | Thả tay | Đợi đủ 3,5s **tính từ lúc thả** mới trượt |
| 5 | Vuốt sang trang khác | Đếm lại, 3,5s sau mới tự trượt |
| 6 | Vuốt chen ngang lúc đang tự trượt | Theo tay người dùng; thả ra thì AutoPlay vẫn chạy tiếp |
| 7 | Gửi `OnboardingEffect.ScrollToPage(n)` | Trượt thẳng đến trang `n`, chỉ đi qua vài trang |
| 8 | Chữ và chấm bên dưới | Luôn khớp với ảnh đang hiển thị |
