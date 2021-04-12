package io.github.kaczmarek.stepbystep.ui.settings

import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updatePadding
import io.github.kaczmarek.stepbystep.R
import io.github.kaczmarek.stepbystep.ui.base.BaseFragment
import io.github.kaczmarek.stepbystep.utils.AppInsets

class SettingsFragment : BaseFragment(R.layout.fragment_settings) {

    private lateinit var clContainer: ConstraintLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clContainer = view.findViewById(R.id.cl_settings_container)
        toolbar?.title = getString(R.string.common_settings)
        showBackArrowButton()
    }

    override fun onApplyWindowInsets(view: View, insets: AppInsets) {
        clContainer.updatePadding(top = insets.top, bottom = insets.bottom)
    }

    companion object {
        const val TAG = "SettingsFragment"
    }
}