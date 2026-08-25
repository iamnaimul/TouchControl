package com.naimul.touchcontrol

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.getSystemService
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class MainActivity : Activity() {

    companion object {
        private const val INACTIVITY_MS = 2000L
        private const val SWIPE_DISTANCE = 70f
        private const val DOUBLE_TAP_MS = 500L
        private const val LONG_PRESS_MS = 750L
        private const val FEEDBACK_HIDE_MS = 500L
        private const val PREFS = "touch_control"
        private const val SETUP_DONE = "setup_done"
        private const val BRIGHTNESS_STEP = 0.05f
    }

    private val handler = Handler(Looper.getMainLooper())
    private var overlay: View? = null
    private var feedback: TextView? = null
    private var lastTap = 0L
    private var downX = 0f
    private var downY = 0f
    private var dragging = false
    private var longPressTriggered = false
    private var gestureAxis = 0 // 0 = undecided, 1 = vertical (volume), 2 = horizontal (brightness)
    private var feedbackHide: Runnable? = null
    private var inactivity: Runnable? = null
    private var longPress: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onResume() {
        super.onResume()
        if (setupComplete()) activateControl() else showSetup()
    }

    override fun onPause() {
        // The overlay is a system window, independent of this Activity's
        // lifecycle. If we didn't remove it here, an invisible, full-screen,
        // touch-swallowing view could keep intercepting taps meant for the
        // home screen or whatever app the user switches to next, until the
        // inactivity timer happened to fire.
        removeOverlay(false)
        super.onPause()
    }

    private fun setupComplete(): Boolean {
        val saved = getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean(SETUP_DONE, false)
        if (!saved) return false
        return hasAllRequiredPermissions()
    }

    private fun hasAllRequiredPermissions(): Boolean =
        Settings.System.canWrite(this) &&
            Settings.canDrawOverlays(this) &&
            isDeviceAdminActive() &&
            isPowerAccessibilityEnabled()

    private fun showSetup() {
        removeOverlay(false)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(28), dp(28), dp(28), dp(28))
            setBackgroundColor(Color.WHITE)
        }

        val title = TextView(this).apply {
            text = "Touch Control\n\nOne-time setup"
            textSize = 24f
            gravity = Gravity.CENTER
        }

        val status = TextView(this).apply {
            textSize = 15f
            gravity = Gravity.CENTER
            setPadding(0, dp(20), 0, dp(20))
            text = permissionStatus()
        }

        val button = TextView(this).apply {
            text = "CONTINUE"
            textSize = 17f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.BLACK)
            setPadding(dp(40), dp(22), dp(40), dp(22))
            setOnClickListener { requestNextPermission() }
        }

        root.addView(title)
        root.addView(status)
        root.addView(button)
        setContentView(root)
    }

    private fun permissionStatus(): String = buildString {
        append(if (Settings.System.canWrite(this@MainActivity)) "✓ Modify system settings\n" else "1. Modify system settings\n")
        append(if (Settings.canDrawOverlays(this@MainActivity)) "✓ Display over other apps\n" else "2. Display over other apps\n")
        append(if (isDeviceAdminActive()) "✓ Device administrator\n" else "3. Device administrator\n")
        append(if (isPowerAccessibilityEnabled()) "✓ Power menu accessibility\n" else "4. Power menu accessibility")
    }

    private fun requestNextPermission() {
        when {
            !Settings.System.canWrite(this) ->
                startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:$packageName")))

            !Settings.canDrawOverlays(this) ->
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))

            !isDeviceAdminActive() ->
                startActivity(Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                    putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent())
                    putExtra(
                        DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                        "Double tap দিয়ে screen lock এবং power control ব্যবহার করতে এই permission প্রয়োজন।"
                    )
                })

            !isPowerAccessibilityEnabled() ->
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))

            else -> finishSetup()
        }
    }

    private fun finishSetup() {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(SETUP_DONE, true).apply()
        activateControl()
    }

    private fun activateControl() {
        if (!hasAllRequiredPermissions()) {
            showSetup()
            return
        }

        setContentView(View(this).apply { setBackgroundColor(Color.TRANSPARENT) })
        showOverlay()
    }

    private fun showOverlay() {
        removeOverlay(false)

        val root = android.widget.FrameLayout(this)
        val gestureView = object : View(this) {
            override fun onTouchEvent(e: MotionEvent): Boolean {
                resetInactivity()

                when (e.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        downX = e.x
                        downY = e.y
                        dragging = false
                        longPressTriggered = false
                        gestureAxis = 0

                        val r = Runnable {
                            if (!dragging && overlay != null) {
                                longPressTriggered = true
                                openPowerMenu()
                            }
                        }
                        longPress = r
                        handler.postDelayed(r, LONG_PRESS_MS)
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val dx = e.x - downX
                        val dy = e.y - downY

                        // Lock to one axis once the initial movement is clear.
                        // A single gesture can therefore control only volume
                        // OR brightness, never both.
                        if (gestureAxis == 0 &&
                            (abs(dx) >= SWIPE_DISTANCE || abs(dy) >= SWIPE_DISTANCE)
                        ) {
                            gestureAxis = if (abs(dy) > abs(dx)) 1 else 2
                            downX = e.x
                            downY = e.y
                            // A real drag is now confirmed: this must not also
                            // fire the long-press power menu, and must not
                            // count as a tap for the double-tap-lock check.
                            dragging = true
                            longPress?.let(handler::removeCallbacks)
                        } else if (gestureAxis == 1) {
                            val vertical = e.y - downY
                            if (abs(vertical) >= SWIPE_DISTANCE) {
                                changeVolume(if (vertical < 0) 1 else -1)
                                downY = e.y
                            }
                        } else if (gestureAxis == 2) {
                            val horizontal = e.x - downX
                            if (abs(horizontal) >= SWIPE_DISTANCE) {
                                changeBrightness(
                                    if (horizontal > 0) BRIGHTNESS_STEP else -BRIGHTNESS_STEP
                                )
                                downX = e.x
                            }
                        }
                        return true
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        longPress?.let(handler::removeCallbacks)

                        if (!dragging && !longPressTriggered && e.actionMasked == MotionEvent.ACTION_UP) {
                            val now = System.currentTimeMillis()
                            if (now - lastTap <= DOUBLE_TAP_MS) {
                                lastTap = 0L
                                lockScreen()
                            } else {
                                lastTap = now
                            }
                        }
                        return true
                    }
                }
                return true
            }
        }

        root.addView(gestureView, android.widget.FrameLayout.LayoutParams(-1, -1))

        val indicator = TextView(this).apply {
            textSize = 12f
            setTextColor(Color.WHITE)
            setPadding(dp(9), dp(5), dp(9), dp(5))
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                cornerRadius = dp(10).toFloat()
                setColor(Color.argb(150, 0, 0, 0))
            }
            visibility = View.GONE
        }

        root.addView(
            indicator,
            android.widget.FrameLayout.LayoutParams(dp(120), dp(34), Gravity.CENTER_VERTICAL or Gravity.END).apply {
                rightMargin = dp(8)
            }
        )
        feedback = indicator
        overlay = root

        val params = WindowManager.LayoutParams(
            -1,
            -1,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            android.graphics.PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START }

        getSystemService<WindowManager>()?.addView(root, params)
        resetInactivity()
    }

    private fun resetInactivity() {
        inactivity?.let(handler::removeCallbacks)
        val r = Runnable { closeAfterInactivity() }
        inactivity = r
        handler.postDelayed(r, INACTIVITY_MS)
    }

    private fun closeAfterInactivity() {
        removeOverlay(true)
    }

    private fun changeVolume(direction: Int) {
        val audio = getSystemService<AudioManager>() ?: return
        audio.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            if (direction > 0) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER,
            AudioManager.FLAG_SHOW_UI
        )
        val max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val current = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
        showFeedback("Volume ${percent(current, max)}%")
    }

    private fun changeBrightness(delta: Float) {
        ensureManualBrightnessMode()
        val current = Settings.System.getInt(
            contentResolver, Settings.System.SCREEN_BRIGHTNESS, 128
        ) / 255f
        val value = min(1f, max(0.01f, current + delta))
        Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, (value * 255f).roundToInt())
        window.attributes = window.attributes.apply { screenBrightness = value }
        showFeedback("Brightness ${(value * 100f).roundToInt()}%")
    }

    private fun ensureManualBrightnessMode() {
        // Most phones ship with adaptive (auto) brightness on. While that's
        // active, the system silently ignores writes to SCREEN_BRIGHTNESS,
        // so the gesture would look like it does nothing. We already hold
        // WRITE_SETTINGS, so switch to manual mode once, on first use.
        val mode = Settings.System.getInt(
            contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE,
            Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
        )
        if (mode != Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) {
            Settings.System.putInt(
                contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
        }
    }

    private fun showFeedback(text: String) {
        val v = feedback ?: return
        v.text = text
        v.visibility = View.VISIBLE
        feedbackHide?.let(v::removeCallbacks)
        val r = Runnable { v.visibility = View.GONE }
        feedbackHide = r
        v.postDelayed(r, FEEDBACK_HIDE_MS)
    }

    private fun lockScreen() {
        val dpm = getSystemService<DevicePolicyManager>() ?: return
        if (dpm.isAdminActive(adminComponent())) {
            removeOverlay(false)
            dpm.lockNow()
        } else {
            startActivity(Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent())
            })
        }
    }

    private fun openPowerMenu() {
        if (!PowerAccessibilityService.showPowerMenu()) {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    private fun adminComponent() = ComponentName(this, LockAdminReceiver::class.java)

    private fun isDeviceAdminActive(): Boolean =
        getSystemService<DevicePolicyManager>()?.isAdminActive(adminComponent()) == true

    private fun isPowerAccessibilityEnabled(): Boolean {
        val enabled = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            ?: return false
        val expected = ComponentName(this, PowerAccessibilityService::class.java).flattenToString()
        return enabled.split(':').any { TextUtils.equals(it, expected) }
    }

    private fun percent(value: Int, maxValue: Int): Int =
        if (maxValue <= 0) 0 else (value * 100f / maxValue).roundToInt()

    private fun removeOverlay(finish: Boolean) {
        inactivity?.let(handler::removeCallbacks)
        longPress?.let(handler::removeCallbacks)
        feedbackHide?.let(handler::removeCallbacks)
        inactivity = null
        longPress = null
        feedbackHide = null

        val v = overlay
        overlay = null
        feedback = null
        if (v != null) {
            getSystemService<WindowManager>()?.let { wm -> runCatching { wm.removeView(v) } }
        }
        if (finish && !isFinishing) finish()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).roundToInt()

    override fun onDestroy() {
        removeOverlay(false)
        super.onDestroy()
    }
}
