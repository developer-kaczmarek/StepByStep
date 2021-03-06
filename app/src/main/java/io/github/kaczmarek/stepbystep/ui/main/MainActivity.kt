package io.github.kaczmarek.stepbystep.ui.main

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.*
import android.view.*
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import io.github.kaczmarek.stepbystep.R
import io.github.kaczmarek.stepbystep.services.TrackerService
import io.github.kaczmarek.stepbystep.services.TrackerRecordListener
import io.github.kaczmarek.stepbystep.ui.base.BaseActivity
import io.github.kaczmarek.stepbystep.ui.settings.SettingsFragment
import io.github.kaczmarek.stepbystep.ui.statistics.StatisticsFragment
import io.github.kaczmarek.stepbystep.ui.tracker.TrackerFragment
import io.github.kaczmarek.stepbystep.utils.logDebug
import io.github.kaczmarek.stepbystep.utils.navigation.OnNavigateListener
import io.github.kaczmarek.stepbystep.utils.navigation.attachFragment
import moxy.ktx.moxyPresenter

class MainActivity : BaseActivity(R.layout.activity_main), MainView, OnNavigateListener,
    View.OnClickListener, TrackerRecordListener {

    private lateinit var dlMainContainer: DrawerLayout
    private lateinit var clMenuContainer: ConstraintLayout
    private lateinit var flContentContainer: FrameLayout
    private var actionBarDrawerToggle: ActionBarDrawerToggle? = null

    val presenter by moxyPresenter { MainPresenter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dlMainContainer = findViewById(R.id.dlMainContainer)
        clMenuContainer = findViewById(R.id.clMainGeneralMenu)
        flContentContainer = findViewById(R.id.flMainContentContainer)

        val stepsMenuItem = findViewById<TextView>(R.id.tvMainMenuItemSteps)
        val statisticsMenuItem = findViewById<TextView>(R.id.tvMainMenuItemStatistics)
        val settingsMenuItem = findViewById<TextView>(R.id.tvMainMenuItemSettings)
        val exitMenuItem = findViewById<TextView>(R.id.tvMainMenuItemExit)

        stepsMenuItem.setOnClickListener(this)
        statisticsMenuItem.setOnClickListener(this)
        settingsMenuItem.setOnClickListener(this)
        exitMenuItem.setOnClickListener(this)

        actionBarDrawerToggle = object : ActionBarDrawerToggle(
            this,
            dlMainContainer,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        ) {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                super.onDrawerSlide(drawerView, slideOffset)
                with(flContentContainer) {
                    translationX = drawerView.width * slideOffset
                    scaleX = 1 - (slideOffset / 6f)
                    scaleY = 1 - (slideOffset / 6f)
                }
            }
        }

        with(dlMainContainer) {
            setScrimColor(Color.TRANSPARENT)
            drawerElevation = 0f
            actionBarDrawerToggle?.let { addDrawerListener(it) }
        }
    }

    override fun onDestroy() {
        actionBarDrawerToggle?.let { dlMainContainer.removeDrawerListener(it) }
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            dlMainContainer.openDrawer(clMenuContainer)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun launchStepsScreen() {
        onNavigate(TrackerFragment(), TrackerFragment.TAG)
        changeVisibilitySelectors(TrackerFragment.TAG)
    }

    override fun onNavigate(fragmentInstance: Fragment, tag: String?, isAddToBackStack: Boolean) {
        supportFragmentManager.attachFragment(
            R.id.flMainContentContainer,
            fragmentInstance,
            tag,
            isAddToBackStack
        )
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tvMainMenuItemSteps -> {
                onNavigate(TrackerFragment(), TrackerFragment.TAG)
                changeVisibilitySelectors(TrackerFragment.TAG)
                dlMainContainer.closeDrawer(clMenuContainer)
            }
            R.id.tvMainMenuItemStatistics -> {
                onNavigate(StatisticsFragment(), StatisticsFragment.TAG)
                changeVisibilitySelectors(StatisticsFragment.TAG)
                dlMainContainer.closeDrawer(clMenuContainer)
            }
            R.id.tvMainMenuItemSettings -> {
                onNavigate(SettingsFragment(), SettingsFragment.TAG)
                changeVisibilitySelectors(SettingsFragment.TAG)
                dlMainContainer.closeDrawer(clMenuContainer)
            }
            R.id.tvMainMenuItemExit -> finish() // ???????????????? ???????????????????? ????????????, ???? ???????????? ???????? ????????????????????????
        }
    }

    /**
     * ?????????? ?????? ???????????? ?????????????? ???????????? ??????????.
     * ?????????? ?????????????? ???????????????????? ?????????????????????? ???????????????????? ???? ?????????????????? ??????????????????.
     */
    override fun startTrackRecording() {
        try {
            if (!isRecording()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(Intent(this, TrackerService::class.java))
                } else {
                    startService(Intent(this, TrackerService::class.java))
                }
            }
        } catch (e: Exception) {
            logDebug(TAG, e.message)
        }
    }

    /**
     * ?????????? ?????? ?????????????????? ?????????????? ???????????? ??????????.
     */
    override fun stopTrackRecording() {
        try {
            stopService(Intent(this, TrackerService::class.java))
        } catch (e: Exception) {
            logDebug(TAG, e.message)
        }
    }

    /**
     * ?????????? ?????? ?????????????????????? ???????????????? ???????????? ??????????.
     */
    @Suppress("DEPRECATION")
    override fun isRecording(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (TrackerService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }

    /**
     * ?????????? ?????? ?????????????????? ?????????????????? ?? ???????? ???? ???????????????????? (??????????????????) ???????????????? ????????
     * @selectedMenuItemTag ?????? ???????????????????? ??????????????????
     */
    private fun changeVisibilitySelectors(selectedMenuItemTag: String) {
        val stepsSelector = findViewById<View>(R.id.vMainMenuItemStepsSelector)
        val statisticsSelector = findViewById<View>(R.id.vMainMenuItemStatisticsSelector)
        val settingsSelector = findViewById<View>(R.id.vMainMenuItemSettingsSelector)
        stepsSelector.isVisible = selectedMenuItemTag == TrackerFragment.TAG
        statisticsSelector.isVisible = selectedMenuItemTag == StatisticsFragment.TAG
        settingsSelector.isVisible = selectedMenuItemTag == SettingsFragment.TAG
    }

    companion object {
        const val TAG = "MainActivity"
    }
}