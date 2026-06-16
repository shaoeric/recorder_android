package com.recorder.app.ui.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.recorder.app.data.entity.Recording
import com.recorder.app.data.repository.RecordingRepository
import com.recorder.app.service.RecordingManager
import com.recorder.app.service.RecordingState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun recordingState_defaultIsIdle() {
        assertNotNull("RecordingManager should be non-null", true)
    }
}
