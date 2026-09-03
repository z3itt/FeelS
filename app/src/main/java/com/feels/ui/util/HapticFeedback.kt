package com.feels.ui.util

import android.view.HapticFeedbackConstants
import android.view.View

fun View.performLightConfirmHaptic() {
    performHapticFeedback(HapticFeedbackConstants.CONFIRM)
}
