package com.example.vaultapplication

import android.graphics.Color

enum class PasswordStrength(val color: Int) {

    WEAK(Color.RED),
    MEDIUM(Color.argb(255, 220, 185, 0)),
    STRONG(Color.rgb(50,50,50)),
    VERY_STRONG(Color.rgb(0,100,0));


    companion object {

        private var REQUIRED_LENGTH = 8
        private var REQUIRE_SPECIAL_CHARACTERS = true
        private var REQUIRE_DIGITS = true
        private var REQUIRE_LOWER_CASE = true
        private var REQUIRE_UPPER_CASE = false

        fun calculateStrength(password: String): PasswordStrength {
            var currentScore = 0
            var sawUpper = false
            var sawLower = false
            var sawDigit = false
            var sawSpecial = false


            for (element in password) {

                if (!sawSpecial && !Character.isLetterOrDigit(element)) {
                    currentScore += 1
                    sawSpecial = true
                } else {
                    if (!sawDigit && Character.isDigit(element)) {
                        currentScore += 1
                        sawDigit = true
                    } else {
                        if (!sawUpper || !sawLower) {
                            if (Character.isUpperCase(element))
                                sawUpper = true
                            else
                                sawLower = true
                            if (sawUpper && sawLower)
                                currentScore += 1
                        }
                    }
                }

            }

            if (password.length > REQUIRED_LENGTH) {
                currentScore = if (REQUIRE_SPECIAL_CHARACTERS && !sawSpecial
                    || REQUIRE_UPPER_CASE && !sawUpper
                    || REQUIRE_LOWER_CASE && !sawLower
                    || REQUIRE_DIGITS && !sawDigit) {
                    1
                } else {
                    3
                }
            } else {
                if (REQUIRE_SPECIAL_CHARACTERS && !sawSpecial
                    || REQUIRE_UPPER_CASE && !sawUpper
                    || REQUIRE_LOWER_CASE && !sawLower
                    || REQUIRE_DIGITS && !sawDigit) {
                    currentScore = 0
                } else {
                    currentScore = 1
                }

            }

            when (currentScore) {
                0 -> return WEAK
                1 -> return MEDIUM
                2 -> return STRONG
                3 -> return VERY_STRONG
            }

            return VERY_STRONG
        }
    }

}