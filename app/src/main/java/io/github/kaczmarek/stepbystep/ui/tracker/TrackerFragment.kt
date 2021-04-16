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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textview.MaterialTextView
import io.github.kaczmarek.stepbystep.R
import io.github.kaczmarek.stepbystep.services.isLocationPermissionGranted
import io.github.kaczmarek.stepbystep.ui.main.LocationServiceLifecycleListener
import io.github.kaczmarek.stepbystep.ui.base.BaseFragment
import io.github.kaczmarek.stepbystep.ui.main.MainActivity
import io.github.kaczmarek.stepbystep.utils.AppInsets
import io.github.kaczmarek.stepbystep.utils.Utils.addLengthUnit
import io.github.kaczmarek.stepbystep.utils.Utils.conversionInKmH
import moxy.ktx.moxyPresenter
import java.text.SimpleDateFormat
import java.util.*

class TrackerFragment : BaseFragment(R.layout.fragment_tracker), TrackerView, View.OnClickListener {

    private var locationServiceLifecycleListener: LocationServiceLifecycleListener? = null
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
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var openSettingsLauncher: ActivityResultLauncher<Intent>

    private val presenter by moxyPresenter { TrackerPresenter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationServiceLifecycleListener = context as MainActivity

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

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                startTrackRecord()
                registerGnssStatusCallback()
            } else {
                showRequestPermissionRationale()
            }
        }

        openSettingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        chmTimeRecord.setOnChronometerTickListener {
            presenter.getActualTrackInformation()
        }
        bStartRecord.setOnClickListener(this)
        bStopRecord.setOnClickListener(this)
        bPauseRecord.setOnClickListener(this)
        changeRecordButtonsState()
        presenter.initIndicatorsValue()
    }

    override fun onDestroyView() {
        gnssStatusCallback?.let { locationManager?.unregisterGnssStatusCallback(it) }
        openSettingsLauncher.unregister()
        requestPermissionLauncher.unregister()
        super.onDestroyView()
    }

    override fun onApplyWindowInsets(view: View, insets: AppInsets) {
        clContainer.updatePadding(top = insets.top, bottom = insets.bottom)
    }

    override fun initIndicatorsValue(
        realDistance: Float,
        goalDistance: Int,
        currentAccuracy: Float,
        currentSpeed: Float,
        maxSpeed: Float,
        averageSpeed: Double,
        time: Long,
        satellitesCount: Int
    ) {
        tvCurrentDistance.text = realDistance.addLengthUnit()
        with(lpiGoalProgress) {
            progress = realDistance.toInt()
            max = goalDistance
        }
        tvGoalDistance.text = goalDistance.addLengthUnit()
        tvAccuracy.text = currentAccuracy.addLengthUnit()
        tvCurrentSpeed.text = currentSpeed.conversionInKmH()
        tvMaxSpeed.text = maxSpeed.conversionInKmH()
        tvAverageSpeed.text = averageSpeed.conversionInKmH()
        tvSatellites.text = satellitesCount.toString()
    }

    override fun updateParamsValue(
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
                    isLocationPermissionGranted(requireContext()) -> {
                        startTrackRecord()
                        registerGnssStatusCallback()
                        changeRecordButtonsState()
                    }
                    shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> showRequestPermissionRationale()
                    else -> requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
            R.id.b_tracker_stop_record -> {
                gnssStatusCallback?.let { locationManager?.unregisterGnssStatusCallback(it) }
                suspendTrackRecord(isFinished = true)
                changeRecordButtonsState()
            }
            R.id.b_tracker_pause_record -> {
                gnssStatusCallback?.let { locationManager?.unregisterGnssStatusCallback(it) }
                suspendTrackRecord(isFinished = false)
                changeRecordButtonsState()
            }
        }
    }

    private fun startTrackRecord() {
        locationServiceLifecycleListener?.startService()
        with(chmTimeRecord) {
            base = SystemClock.elapsedRealtime()
            start()
        }
    }

    private fun suspendTrackRecord(isFinished: Boolean) {
        locationServiceLifecycleListener?.stopService()
        chmTimeRecord.stop()
        presenter.saveCurrentTrack(chmTimeRecord.base, isFinished)
    }

    private fun changeRecordButtonsState() {
        bStartRecord.isVisible = locationServiceLifecycleListener?.isServiceRunning() == false
        bStopRecord.isVisible = locationServiceLifecycleListener?.isServiceRunning() == true
        bPauseRecord.isVisible = locationServiceLifecycleListener?.isServiceRunning() == true
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