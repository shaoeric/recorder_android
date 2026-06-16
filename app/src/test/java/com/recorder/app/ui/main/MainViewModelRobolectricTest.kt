package com.recorder.app.ui.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.recorder.app.RecorderApplication
import com.recorder.app.data.database.AppDatabase
import com.recorder.app.data.entity.Recording
import com.recorder.app.data.repository.RecordingRepository
import com.recorder.app.service.RecordingState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class MainViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var database: AppDatabase
    private lateinit var repository: RecordingRepository
    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        database = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            AppDatabase::class.java
        ).build()
        repository = RecordingRepository(
            database.recordingDao(),
            database.markerDao(),
            database.photoDao()
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        database.close()
    }

    @Test
    fun initialState_isIdle() {
        assertEquals(RecordingState.IDLE, viewModel?.recordingState?.value)
    }

    @Test
    fun allRecordings_isInitiallyEmpty() = runTest {
        val context = RuntimeEnvironment.getApplication() as RecorderApplication
        viewModel = MainViewModel(context)
        val recordings = viewModel.allRecordings
        advanceUntilIdle()
        assertEquals(0, recordings?.value?.size)
    }
}
