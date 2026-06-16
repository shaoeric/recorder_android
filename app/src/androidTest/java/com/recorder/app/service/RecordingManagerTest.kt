package com.recorder.app.service

import android.Manifest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@RunWith(AndroidJUnit4::class)
class RecordingManagerTest {

    @get:Rule
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.RECORD_AUDIO)

    private lateinit var recordingManager: RecordingManager
    private lateinit var outputFile: File

    @Before
    fun setup() {
        recordingManager = RecordingManager(androidx.test.core.app.ApplicationProvider.getApplicationContext())
        outputFile = File.createTempFile("test_recording", ".aac")
        outputFile.deleteOnExit()
    }

    @After
    fun tearDown() {
        recordingManager.release()
        if (outputFile.exists()) {
            outputFile.delete()
        }
    }

    @Test
    fun initialState_isIdle() = runBlocking {
        assertEquals(RecordingState.IDLE, recordingManager.state.first())
    }

    @Test
    fun startRecording_changesStateToRecording() = runBlocking {
        recordingManager.startRecording(outputFile)
        assertEquals(RecordingState.RECORDING, recordingManager.state.first())
    }

    @Test
    fun startRecording_setsFilePath() = runBlocking {
        recordingManager.startRecording(outputFile)
        assertEquals(outputFile.absolutePath, recordingManager.currentFilePath)
    }

    @Test
    fun startRecording_createsOutputFile() = runBlocking {
        recordingManager.startRecording(outputFile)
        assertTrue(outputFile.exists())
    }

    @Test
    fun stopRecording_afterStart_returnsToIdle() = runBlocking {
        recordingManager.startRecording(outputFile)
        recordingManager.stopRecording()
        assertEquals(RecordingState.IDLE, recordingManager.state.first())
    }

    @Test
    fun stopRecording_returnsDurationGreaterThanZero() = runBlocking {
        recordingManager.startRecording(outputFile)
        Thread.sleep(500)
        val duration = recordingManager.stopRecording()
        assertTrue(duration > 0)
    }

    @Test
    fun pauseAndResumeRecording_changesStates() = runBlocking {
        recordingManager.startRecording(outputFile)
        assertEquals(RecordingState.RECORDING, recordingManager.state.first())

        recordingManager.pauseRecording()
        assertEquals(RecordingState.PAUSED, recordingManager.state.first())

        recordingManager.resumeRecording()
        assertEquals(RecordingState.RECORDING, recordingManager.state.first())
    }

    @Test
    fun getElapsedTimeMs_increasesOverTime() = runBlocking {
        recordingManager.startRecording(outputFile)
        Thread.sleep(200)
        val time1 = recordingManager.getElapsedTimeMs()
        Thread.sleep(200)
        val time2 = recordingManager.getElapsedTimeMs()
        assertTrue(time2 > time1)
        recordingManager.stopRecording()
    }

    @Test
    fun release_returnsToIdle() = runBlocking {
        recordingManager.startRecording(outputFile)
        recordingManager.release()
        assertEquals(RecordingState.IDLE, recordingManager.state.first())
    }
}
