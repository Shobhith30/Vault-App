package com.example.vaultapplication

import android.content.res.ColorStateList
import android.widget.ProgressBar

object UpdatePasswordStrength {

    fun updatePasswordStrengthView(password: String, progressBar: ProgressBar) {

        if (password.isEmpty()) {
            progressBar.progress = 0
            return
        }

        val str = PasswordStrength.calculateStrength(password)

        progressBar.progressTintList = ColorStateList.valueOf(str.color)
        if (str == PasswordStrength.WEAK) {
            progressBar.progress = 25
        } else if (str == PasswordStrength.MEDIUM) {
            progressBar.progress = 50
        } else if (str == PasswordStrength.STRONG) {
            progressBar.progress = 75
        } else {
            progressBar.progress = 100
        }
    }
}