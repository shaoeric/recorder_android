package com.recorder.app.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class RecordingStateTest {

    @Test
    fun recordingState_values() {
        assertEquals(3, RecordingState.entries.size)
        assertEquals(RecordingState.IDLE, RecordingState.valueOf("IDLE"))
        assertEquals(RecordingState.RECORDING, RecordingState.valueOf("RECORDING"))
        assertEquals(RecordingState.PAUSED, RecordingState.valueOf("PAUSED"))
    }

    @Test
    fun recordingState_notEqual() {
        assertNotEquals(RecordingState.IDLE, RecordingState.RECORDING)
        assertNotEquals(RecordingState.RECORDING, RecordingState.PAUSED)
        assertNotEquals(RecordingState.PAUSED, RecordingState.IDLE)
    }
}
