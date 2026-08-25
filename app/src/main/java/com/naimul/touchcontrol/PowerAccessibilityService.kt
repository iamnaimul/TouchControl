package com.naimul.touchcontrol

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class PowerAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() = Unit

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onDestroy() {
        if (instance === this) instance = null
        super.onDestroy()
    }

    companion object {
        private var instance: PowerAccessibilityService? = null
        fun showPowerMenu(): Boolean = instance?.performGlobalAction(GLOBAL_ACTION_POWER_DIALOG) == true
    }
}
