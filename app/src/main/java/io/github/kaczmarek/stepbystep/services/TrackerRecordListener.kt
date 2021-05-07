package io.github.kaczmarek.stepbystep.services

interface TrackerRecordListener {
    fun startTrackRecording()
    fun stopTrackRecording()
    fun isRecording(): Boolean
    fun getActualDuration(): Long
}