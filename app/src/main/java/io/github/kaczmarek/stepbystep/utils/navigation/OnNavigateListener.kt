package io.github.kaczmarek.stepbystep.utils.navigation

import androidx.fragment.app.Fragment

interface OnNavigateListener {
    fun onNavigate(fragmentInstance: Fragment, tag: String?, isAddToBackStack: Boolean = false)
}