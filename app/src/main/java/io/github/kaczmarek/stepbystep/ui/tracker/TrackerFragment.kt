package io.github.kaczmarek.stepbystep.ui.tracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Chronometer
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textview.MaterialTextView
import io.github.kaczmarek.stepbystep.R
import io.github.kaczmarek.stepbystep.ui.base.BaseFragment
import io.github.kaczmarek.stepbystep.utils.AppInsets
import moxy.ktx.moxyPresenter
import java.text.SimpleDateFormat
import java.util.*

class TrackerFragment : BaseFragment(R.layout.fragment_tracker), TrackerView {

    private var isStoppedRecord = true
    private var stepsCounter = 0
    private var distanceCounter = 0F
    private var energyCounter = 0F
    private lateinit var clContainer: ConstraintLayout
    private lateinit var tvDistanceValue: MaterialTextView
    private lateinit var tvEnergyValue: MaterialTextView
    private lateinit var tvCurrentCountSteps: MaterialTextView
    private lateinit var tvGoal: MaterialTextView
    private lateinit var chmTimeValue: Chronometer
    private lateinit var cpiStepsProgress: CircularProgressIndicator

    private val presenter by moxyPresenter { TrackerPresenter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clContainer = view.findViewById(R.id.cl_tracker_container)

        val currentDateTime = Date()
        toolbar?.title = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault()).format(currentDateTime)
        showBackArrowButton()

        tvDistanceValue = view.findViewById(R.id.tv_tracker_distance_value)
        tvEnergyValue = view.findViewById(R.id.tv_tracker_energy_value)
        tvCurrentCountSteps = view.findViewById(R.id.tv_tracker_current_counter)
        tvGoal = view.findViewById(R.id.tv_tracker_goal)
        chmTimeValue = view.findViewById(R.id.chm_tracker_time_value)
        cpiStepsProgress = view.findViewById(R.id.cpi_tracker_progress)
        val bStepsChangeRecordState = view.findViewById<MaterialButton>(R.id.b_tracker_change_record_state)

        with(bStepsChangeRecordState) {
            setText(R.string.fragment_steps_start_recording_description)
            setOnClickListener {
                if (isStoppedRecord) {

                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED) {
                        //  requestPermissions(arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), REQUEST_CODE_PERMISSION_RECOGNITION)
                    } else {
                        bStepsChangeRecordState.setText(R.string.fragment_steps_stop_recording_description)
                        chmTimeValue.base = SystemClock.elapsedRealtime()
                        chmTimeValue.start()
                    }
                } else {
                    bStepsChangeRecordState.setText(R.string.fragment_steps_start_recording_description)
                    chmTimeValue.stop()
                    //Запись в бд показателей сессии
                }
                isStoppedRecord = !isStoppedRecord
            }
        }

        presenter.initIndicatorsValue()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED
        ) {

        } else {
            // Сообщить о невозможности работы приложения
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onApplyWindowInsets(view: View, insets: AppInsets) {
        clContainer.updatePadding(top = insets.top, bottom = insets.bottom)
    }

    override fun initIndicatorsValue(steps: Int, goalSteps: Int, distance: Float, energy: Float, time: Long) {
        stepsCounter = steps
        distanceCounter = distance
        energyCounter = energy
        tvDistanceValue.text = getString(R.string.common_km, distanceCounter.toString())
        tvEnergyValue.text = getString(R.string.common_cal, energyCounter.toString())
        tvCurrentCountSteps.text = stepsCounter.toString()
        with(cpiStepsProgress) {
            progress = steps
            max = goalSteps
        }
        tvGoal.text = getString(R.string.fragment_steps_goal_description, goalSteps.toString())
    }

    override fun resetIndicators(steps: Int, distance: Float, energy: Float, time: Long) {
        stepsCounter = steps
        distanceCounter = distance
        energyCounter = energy
        tvDistanceValue.text = getString(R.string.common_km, distanceCounter.toString())
        tvEnergyValue.text = getString(R.string.common_cal, energyCounter.toString())
        tvCurrentCountSteps.text = stepsCounter.toString()
        cpiStepsProgress.progress = steps
        chmTimeValue.base = SystemClock.elapsedRealtime()
        chmTimeValue.start()
    }

    companion object {
        const val TAG = "TrackerFragment"
    }
}