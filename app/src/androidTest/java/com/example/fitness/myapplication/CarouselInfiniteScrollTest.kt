package com.example.fitness.myapplication

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitness.myapplication.core.designsystem.component.CarouselController
import com.example.fitness.myapplication.core.designsystem.component.CarouselOptions
import com.example.fitness.myapplication.core.designsystem.component.CarouselSlider
import com.example.fitness.myapplication.core.designsystem.component.rememberCarouselController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Kiểm tra cuộn vô hạn kiểu "bản sao + nhảy ngầm" luôn kéo Pager về bản GỐC. */
@RunWith(AndroidJUnit4::class)
class CarouselInfiniteScrollTest {

    @get:Rule
    val rule = createComposeRule()

    private val items = listOf("A", "B", "C")
    private lateinit var controller: CarouselController

    private fun setUp(options: CarouselOptions) {
        rule.setContent {
            controller = rememberCarouselController()
            CarouselSlider(
                items = items,
                modifier = Modifier.fillMaxWidth().testTag("carousel"),
                options = options,
                controller = controller,
            ) { item, _, _ -> Text(item) }
        }
        rule.waitForIdle()
    }

    private val page get() = controller.pagerState!!.currentPage
    private val pageCount get() = controller.pagerState!!.pageCount

    // Bản gốc nằm ở đoạn giữa: [first, first + itemCount)
    private fun assertInOriginal(step: String) {
        val first = (pageCount / items.size / 2) * items.size
        assertTrue(
            "$step: page=$page không nằm trong bản gốc $first..${first + items.size - 1}",
            page in first until first + items.size,
        )
    }

    @Test
    fun swipeForward_manyTimes_staysInOriginal() {
        setUp(CarouselOptions(autoPlay = false))
        assertInOriginal("start")
        repeat(8) { i ->
            rule.onNodeWithTag("carousel").performTouchInput { swipeLeft(startX = centerX + width * 0.3f, endX = centerX - width * 0.3f) }
            rule.waitForIdle()
            assertInOriginal("sau lần vuốt tới #${i + 1}")
            assertEquals((i + 1) % 3, controller.realCurrentPage)
        }
    }

    @Test
    fun swipeBackward_manyTimes_staysInOriginal() {
        setUp(CarouselOptions(autoPlay = false))
        repeat(8) { i ->
            rule.onNodeWithTag("carousel").performTouchInput { swipeRight(startX = centerX - width * 0.3f, endX = centerX + width * 0.3f) }
            rule.waitForIdle()
            assertInOriginal("sau lần vuốt lùi #${i + 1}")
            assertEquals((-(i + 1) % 3 + 3) % 3, controller.realCurrentPage)
        }
    }

    @Test
    fun fastSwipes_withoutWaiting_doNotHitWall() {
        setUp(CarouselOptions(autoPlay = false))
        // Vuốt liên tục, không đợi Pager dừng giữa các lần vuốt
        repeat(15) {
            rule.onNodeWithTag("carousel").performTouchInput {
                swipeLeft(startX = centerX + width * 0.3f, endX = centerX - width * 0.3f, durationMillis = 80)
            }
        }
        rule.waitForIdle()
        assertInOriginal("sau 15 lần vuốt nhanh")
    }

    @Test
    fun controllerNext_manyTimes_staysInOriginal() {
        setUp(CarouselOptions(autoPlay = false))
        repeat(8) { i ->
            rule.runOnIdle { controller.nextPage(durationMs = 200) }
            rule.waitForIdle()
            assertInOriginal("sau nextPage #${i + 1}")
            assertEquals((i + 1) % 3, controller.realCurrentPage)
        }
    }
}
