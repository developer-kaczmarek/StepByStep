package io.github.kaczmarek.stepbystep.ui.tracker

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.GnssStatus
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.view.View
import android.widget.Chronometer
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textview.MaterialTextView
import io.github.kaczmarek.stepbystep.R
import io.github.kaczmarek.stepbystep.ui.base.BaseFragment
import io.github.kaczmarek.stepbystep.ui.main.MainActivity
import io.github.kaczmarek.stepbystep.services.TrackerRecordListener
import io.github.kaczmarek.stepbystep.utils.AppInsets
import io.github.kaczmarek.stepbystep.utils.Utils.addLengthUnit
import io.github.kaczmarek.stepbystep.utils.Utils.conversionInKmH
import io.github.kaczmarek.stepbystep.utils.Utils.isLocationPermissionGranted
import io.github.kaczmarek.stepbystep.utils.getFormattedTime
import moxy.ktx.moxyPresenter
import java.text.SimpleDateFormat
import java.util.*

class TrackerFragment : BaseFragment(R.layout.fragment_tracker), TrackerView, View.OnClickListener {

    private var trackerRecordListener: TrackerRecordListener? = null
    private var gnssStatusCallback: GnssStatus.Callback? = null
    private var locationManager: LocationManager? = null
    private lateinit var clContainer: ConstraintLayout
    private lateinit var tvCurrentDistance: MaterialTextView
    private lateinit var tvGoalDistance: MaterialTextView
    private lateinit var tvCurrentSpeed: MaterialTextView
    private lateinit var tvMaxSpeed: MaterialTextView
    private lateinit var tvAverageSpeed: MaterialTextView
    private lateinit var tvAccuracy: MaterialTextView
    private lateinit var tvSatellites: MaterialTextView
    private lateinit var lpiGoalProgress: LinearProgressIndicator
    private lateinit var chmTimeRecord: Chronometer
    private lateinit var bStartRecord: MaterialButton
    private lateinit var bStopRecord: MaterialButton
    private lateinit var bPauseRecord: MaterialButton
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startOrResumeTrack()
        } else {
            showRequestPermissionRationale()
        }
    }
    private val openSettingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private val presenter by moxyPresenter { TrackerPresenter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        trackerRecordListener = context as MainActivity

        val currentDateTime = Date()
        toolbar?.title =
            SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault()).format(currentDateTime)
        showBackArrowButton()

        clContainer = view.findViewById(R.id.cl_tracker_container)

        tvCurrentDistance = view.findViewById(R.id.tv_tracker_current_distance)
        tvGoalDistance = view.findViewById(R.id.tv_tracker_goal_distance)
        tvCurrentSpeed = view.findViewById(R.id.tv_tracker_current_speed_value)
        tvMaxSpeed = view.findViewById(R.id.tv_tracker_max_speed_value)
        tvAverageSpeed = view.findViewById(R.id.tv_tracker_average_speed_value)
        tvAccuracy = view.findViewById(R.id.tv_tracker_accuracy_value)
        tvSatellites = view.findViewById(R.id.tv_tracker_satellites_value)
        chmTimeRecord = view.findViewById(R.id.chm_tracker_time_value)
        lpiGoalProgress = view.findViewById(R.id.lpi_tracker_goal_progress)
        bStartRecord = view.findViewById(R.id.b_tracker_start_record)
        bStopRecord = view.findViewById(R.id.b_tracker_stop_record)
        bPauseRecord = view.findViewById(R.id.b_tracker_pause_record)

        chmTimeRecord.setOnChronometerTickListener {
            it.text = it.context.getFormattedTime(SystemClock.elapsedRealtime() - it.base)
            presenter.getActualTrackInformation()
        }
        bStartRecord.setOnClickListener(this)
        bStopRecord.setOnClickListener(this)
        bPauseRecord.setOnClickListener(this)
        presenter.getGoalDistance()
        if (trackerRecordListener?.isRecording() == true) {
            startChronometer(trackerRecordListener?.getActualDuration() ?: 0L)
            presenter.getActualTrackInformation()
        } else {
            presenter.checkLastUnfinishedTrack()
        }
        changeRecordButtonsState()
    }

    override fun onDestroyView() {
        gnssStatusCallback?.let { locationManager?.unregisterGnssStatusCallback(it) }
        chmTimeRecord.stop()
        openSettingsLauncher.unregister()
        requestPermissionLauncher.unregister()
        super.onDestroyView()
    }

    override fun onApplyWindowInsets(view: View, insets: AppInsets) {
        clContainer.updatePadding(top = insets.top, bottom = insets.bottom)
    }

    override fun showInfoOfUnfinishedTrackIfPossible(
        realDistance: Float,
        currentSpeed: Float,
        maxSpeed: Float,
        averageSpeed: Double,
        duration: Long
    ) {
        tvCurrentDistance.text = realDistance.addLengthUnit()
        lpiGoalProgress.progress = realDistance.toInt()
        tvCurrentSpeed.text = currentSpeed.conversionInKmH()
        tvMaxSpeed.text = maxSpeed.conversionInKmH()
        tvAverageSpeed.text = averageSpeed.conversionInKmH()
        chmTimeRecord.text = chmTimeRecord.context.getFormattedTime(duration)
        val zero = getString(R.string.common_zero)
        tvAccuracy.text = getString(R.string.common_m, zero)
        tvSatellites.text = zero
    }

    override fun updateInfoAboutCurrentTrack(
        realDistance: Float,
        currentAccuracy: Float,
        currentSpeed: Float,
        maxSpeed: Float,
        averageSpeed: Double
    ) {
        tvCurrentDistance.text = realDistance.addLengthUnit()
        lpiGoalProgress.progress = realDistance.toInt()
        tvAccuracy.text = currentAccuracy.addLengthUnit()
        tvCurrentSpeed.text = currentSpeed.conversionInKmH()
        tvMaxSpeed.text = maxSpeed.conversionInKmH()
        tvAverageSpeed.text = averageSpeed.conversionInKmH()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.b_tracker_start_record -> {
                when {
                    isLocationPermissionGranted() -> {
                        startOrResumeTrack()
                    }
                    shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> showRequestPermissionRationale()
                    else -> requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
            R.id.b_tracker_stop_record -> {
                suspendTrackRecord(isFinished = true)
                gnssStatusCallback?.let { locationManager?.unregisterGnssStatusCallback(it) }

                changeRecordButtonsState()
            }
            R.id.b_tracker_pause_record -> {
                suspendTrackRecord(isFinished = false)
                gnssStatusCallback?.let { locationManager?.unregisterGnssStatusCallback(it) }
                changeRecordButtonsState()
            }
        }
    }

    override fun startTrackRecord(duration: Long) {
        trackerRecordListener?.startTrackRecording()
        startChronometer(duration)
        changeRecordButtonsState()
    }

    override fun showGoalDistance(goalDistance: Int) {
        lpiGoalProgress.max = goalDistance
        tvGoalDistance.text = goalDistance.addLengthUnit()
    }

    private fun suspendTrackRecord(isFinished: Boolean) {
        trackerRecordListener?.stopTrackRecording()
        chmTimeRecord.stop()
        presenter.saveCurrentTrack(SystemClock.elapsedRealtime() - chmTimeRecord.base, isFinished)
    }

    private fun changeRecordButtonsState() {
        bStartRecord.isVisible = trackerRecordListener?.isRecording() == false
        bStopRecord.isVisible = trackerRecordListener?.isRecording() == true
        bPauseRecord.isVisible = trackerRecordListener?.isRecording() == true
    }

    private fun showRequestPermissionRationale() {
        showAlertDialog(
            title = getString(R.string.access_fine_location_permission_title),
            message = getString(R.string.access_fine_location_permission_description),
            posBtnTxt = getString(R.string.common_settings),
            posBtnAction = {
                val intent = Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", context?.packageName, null)
                }
                openSettingsLauncher.launch(intent)
            },
            negBtnTxt = getString(R.string.common_cancel)
        )
    }

    private fun startChronometer(duration: Long) {
        with(chmTimeRecord) {
            base = SystemClock.elapsedRealtime() - duration
            start()
        }
    }

    private fun startOrResumeTrack() {
        presenter.startOrResumeTrack()
        registerGnssStatusCallback()
    }

    @SuppressLint("MissingPermission")
    private fun registerGnssStatusCallback() {
        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        gnssStatusCallback = object : GnssStatus.Callback() {
            override fun onSatelliteStatusChanged(status: GnssStatus) {
                super.onSatelliteStatusChanged(status)
                tvSatellites.text = status.satelliteCount.toString()
            }
        }
        gnssStatusCallback?.let { callback ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context?.let {
                    locationManager?.registerGnssStatusCallback(it.mainExecutor, callback)
                }
            } else {
                @Suppress("DEPRECATION")
                locationManager?.registerGnssStatusCallback(callback)
            }
        }
    }

    companion object {
        const val TAG = "TrackerFragment"
    }
}