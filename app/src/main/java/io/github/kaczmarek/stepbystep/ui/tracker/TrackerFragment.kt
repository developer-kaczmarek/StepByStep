package io.github.kaczmarek.stepbystep.ui.tracker

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.GnssStatus
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Chronometer
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
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
import moxy.ktx.moxyPresenter
import java.text.SimpleDateFormat
import java.util.*

class TrackerFragment : BaseFragment(R.layout.fragment_tracker), TrackerView {

    private var isStoppedRecord = true
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
    private lateinit var bChangeRecordState: MaterialButton
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

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
        bChangeRecordState = view.findViewById(R.id.b_tracker_change_record_state)

        requestPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                changeTrackRecordState()
                registerGnssStatusCallback()
            } else {
                // Показать диалоговое окно, что кина не будет
            }
        }
        chmTimeRecord.setOnChronometerTickListener {
            presenter.getActualTrackInformation()
        }
        with(bChangeRecordState) {
            setText(R.string.fragment_steps_start_recording_description)
            setOnClickListener {
                if (isStoppedRecord) {
                    when {
                        isLocationPermissionGranted(requireContext()) -> {
                            changeTrackRecordState()
                            registerGnssStatusCallback()
                        }
                        shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {

                        }
                        else -> requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                } else {
                    changeTrackRecordState()
                }
            }
        }

        presenter.initIndicatorsValue()
    }

    override fun onDestroyView() {
        gnssStatusCallback?.let { locationManager?.unregisterGnssStatusCallback(it) }
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
        tvCurrentDistance.text = realDistance.toString()
        with(lpiGoalProgress) {
            progress = realDistance.toInt()
            max = goalDistance
        }
        tvAccuracy.text = currentAccuracy.toString()
        tvCurrentSpeed.text = currentSpeed.toString()
        tvMaxSpeed.text = maxSpeed.toString()
        tvAverageSpeed.text = averageSpeed.toString()
        tvSatellites.text = satellitesCount.toString()
    }

    override fun updateParamsValue(
            realDistance: Float,
            currentAccuracy: Float,
            currentSpeed: Float,
            maxSpeed: Float,
            averageSpeed: Double
    ) {
        tvCurrentDistance.text = realDistance.toString()
        lpiGoalProgress.progress = realDistance.toInt()
        tvAccuracy.text = currentAccuracy.toString()
        tvCurrentSpeed.text = currentSpeed.toString()
        tvMaxSpeed.text = maxSpeed.toString()
        tvAverageSpeed.text = averageSpeed.toString()
    }

    private fun changeTrackRecordState() {
        if (isStoppedRecord) {
            locationServiceLifecycleListener?.startService()
            bChangeRecordState.setText(R.string.fragment_steps_stop_recording_description)
            with(chmTimeRecord) {
                base = SystemClock.elapsedRealtime()
                start()
            }
        } else {
            locationServiceLifecycleListener?.stopService()
            bChangeRecordState.setText(R.string.fragment_steps_start_recording_description)
            chmTimeRecord.stop()
            //Запись в бд показателей сессии
        }
        isStoppedRecord = !isStoppedRecord
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