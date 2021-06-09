package com.jankku.alphawall.util

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

class Keyboard {
    companion object {
        fun EditText.showKeyboard() {
            post {
                if (this.requestFocus()) {
                    val imm =
                        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
                }
            }
        }

        fun EditText.hideKeyboard() {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(this.windowToken, 0)
        }
    }
}