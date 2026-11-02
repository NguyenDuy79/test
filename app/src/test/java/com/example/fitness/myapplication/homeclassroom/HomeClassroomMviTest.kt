package com.example.fitness.myapplication.homeclassroom

import com.example.fitness.myapplication.homeclassroom.core.network.ApiCaller
import com.example.fitness.myapplication.homeclassroom.core.network.TokenRefresher
import com.example.fitness.myapplication.homeclassroom.core.network.TokenStore
import com.example.fitness.myapplication.homeclassroom.data.model.Session
import com.example.fitness.myapplication.homeclassroom.data.model.SessionStatus
import com.example.fitness.myapplication.homeclassroom.data.remote.AuthApi
import com.example.fitness.myapplication.homeclassroom.data.remote.ContestApi
import com.example.fitness.myapplication.homeclassroom.data.remote.CourseApi
import com.example.fitness.myapplication.homeclassroom.data.remote.GamificationApi
import com.example.fitness.myapplication.homeclassroom.data.remote.SessionApi
import com.example.fitness.myapplication.homeclassroom.data.remote.UserApi
import com.example.fitness.myapplication.homeclassroom.data.remote.VirtualExamApi
import com.example.fitness.myapplication.homeclassroom.data.repository.ContestRepository
import com.example.fitness.myapplication.homeclassroom.data.repository.CourseRepository
import com.example.fitness.myapplication.homeclassroom.data.repository.GamificationRepository
import com.example.fitness.myapplication.homeclassroom.data.repository.SessionRepository
import com.example.fitness.myapplication.homeclassroom.data.repository.SummerCampRepository
import com.example.fitness.myapplication.homeclassroom.data.repository.UserRepository
import com.example.fitness.myapplication.homeclassroom.data.repository.VirtualExamRepository
import com.example.fitness.myapplication.homeclassroom.presentation.HomeClassroomViewModel
import com.example.fitness.myapplication.homeclassroom.presentation.mvi.HomeClassroomIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Proxy

/**
 * Kiểm thử toàn diện kiến trúc MVI (Model-View-Intent) của Home Classroom:
 * 1. Intent phát sinh -> ViewModel xử lý
 * 2. State cập nhật đúng tính chất Single Source of Truth
 * 3. Side-effects phát qua Channel
 * 4. Đồng hồ đếm ngược (Countdown Timer) chạy mượt mà
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeClassroomMviTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: HomeClassroomViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Tạo Dummy API Proxies không thực sự gọi mạng trong Test
        val sessionApi = createDummyProxy<SessionApi>()
        val courseApi = createDummyProxy<CourseApi>()
        val userApi = createDummyProxy<UserApi>()
        val contestApi = createDummyProxy<ContestApi>()
        val virtualExamApi = createDummyProxy<VirtualExamApi>()
        val gamificationApi = createDummyProxy<GamificationApi>()

        val dummyCaller = ApiCaller(tokenRefresher = null)

        viewModel = HomeClassroomViewModel(
            sessionRepository = SessionRepository(sessionApi, dummyCaller),
            summerCampRepository = SummerCampRepository(sessionApi, dummyCaller),
            courseRepository = CourseRepository(courseApi, dummyCaller),
            userRepository = UserRepository(userApi, contestApi, dummyCaller),
            contestRepository = ContestRepository(contestApi, dummyCaller),
            virtualExamRepository = VirtualExamRepository(virtualExamApi, dummyCaller),
            gamificationRepository = GamificationRepository(gamificationApi, dummyCaller),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testLoadMockDataIntent_populatesState() = runTest(testDispatcher) {
        // Gửi Intent nạp dữ liệu mẫu
        viewModel.processIntent(HomeClassroomIntent.LoadMockData)
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.state.value

        // Xác thực Profile & Lớp học
        assertEquals("Nguyễn Minh Anh", state.currentProfile.name)
        assertEquals(3, state.currentProfile.getGradeId())
        assertEquals(150, state.profileLoyalty.cup)
        assertEquals(3, state.unreadNotificationCount)

        // Xác thực Danh sách buổi học
        assertTrue(state.listSessionsCurrentChooseMass.isNotEmpty())
        assertTrue(state.listSessionsCurrentChooseTutor.isNotEmpty())

        // Xác thực Session 1001 đang diễn ra
        val liveSession = state.listSessionsCurrentChooseMass.first { it.id == 1001 }
        assertEquals(SessionStatus.IN_PROGRESS, liveSession.status)
        assertEquals(true, state.checkProgress[1001])

        // Xác thực Countdown cho Session 1002 (khởi tạo 600 giây)
        assertEquals(600, state.timeCountDown[1002])
    }

    @Test
    fun testCountdownTimer_decrementsEverySecond() = runTest(testDispatcher) {
        viewModel.processIntent(HomeClassroomIntent.LoadMockData)
        testDispatcher.scheduler.runCurrent()

        assertEquals(600, viewModel.state.value.timeCountDown[1002])

        // Cho thời gian trôi qua 3 giây
        advanceTimeBy(3000L)
        testDispatcher.scheduler.runCurrent()

        assertEquals(597, viewModel.state.value.timeCountDown[1002])
    }

    @Test
    fun testClickSessionAndDismissIntent_updatesActiveDialog() = runTest(testDispatcher) {
        val testSession = Session(id = 8888, title = "Buổi học kiểm thử")

        // 1. Click vào session
        viewModel.processIntent(HomeClassroomIntent.ClickSession(testSession))
        assertNotNull(viewModel.state.value.activeDialogSession)
        assertEquals(8888, viewModel.state.value.activeDialogSession?.id)

        // 2. Dismiss dialog
        viewModel.processIntent(HomeClassroomIntent.DismissMessage)
        assertNull(viewModel.state.value.activeDialogSession)
    }

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T> createDummyProxy(): T {
        return Proxy.newProxyInstance(
            T::class.java.classLoader,
            arrayOf(T::class.java)
        ) { _, _, _ -> null } as T
    }
}
