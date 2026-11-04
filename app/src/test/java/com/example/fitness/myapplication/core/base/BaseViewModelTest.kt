package com.example.fitness.myapplication.core.base

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BaseViewModelTest {

    data class TestState(val text: String = "", val log: List<String> = emptyList()) : UiState

    sealed interface TestIntent : UiIntent {
        data class TextChanged(val text: String) : TestIntent
        data class Submit(val id: Int) : TestIntent
        data class Search(val id: Int) : TestIntent
    }

    sealed interface TestEffect : UiEffect

    class TestViewModel : BaseViewModel<TestState, TestIntent, TestEffect>(TestState()) {
        override fun handleIntent(intent: TestIntent) {
            when (intent) {
                is TestIntent.TextChanged -> setState { copy(text = intent.text) }
                is TestIntent.Submit -> launchDroppable("submit") { work(intent.id) }
                is TestIntent.Search -> launchLatest("search") { work(intent.id) }
            }
        }

        private suspend fun work(id: Int) {
            setState { copy(log = log + "start$id") }
            delay(5_000)
            setState { copy(log = log + "end$id") }
        }
    }

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `state update is synchronous and not blocked by a slow request`() = runTest(dispatcher) {
        val vm = TestViewModel()
        vm.onIntent(TestIntent.Submit(1))
        runCurrent()
        vm.onIntent(TestIntent.TextChanged("abc"))
        // Không cần runCurrent: setState chạy ngay trong onIntent.
        assertEquals("abc", vm.currentState.text)
        assertEquals(listOf("start1"), vm.currentState.log)
    }

    @Test
    fun `launchDroppable ignores intent while previous is running`() = runTest(dispatcher) {
        val vm = TestViewModel()
        vm.onIntent(TestIntent.Submit(1))
        runCurrent()
        vm.onIntent(TestIntent.Submit(2))
        advanceUntilIdle()
        assertEquals(listOf("start1", "end1"), vm.currentState.log)

        vm.onIntent(TestIntent.Submit(3)) // đã xong ⇒ được chạy lại
        advanceUntilIdle()
        assertEquals(listOf("start1", "end1", "start3", "end3"), vm.currentState.log)
    }

    @Test
    fun `launchLatest cancels previous`() = runTest(dispatcher) {
        val vm = TestViewModel()
        vm.onIntent(TestIntent.Search(1))
        runCurrent()
        vm.onIntent(TestIntent.Search(2))
        advanceUntilIdle()
        assertEquals(listOf("start1", "start2", "end2"), vm.currentState.log)
    }
}
