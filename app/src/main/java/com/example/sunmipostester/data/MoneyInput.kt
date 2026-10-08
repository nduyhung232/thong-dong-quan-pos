package com.example.sunmipostester.data

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

/** Adds comma thousands separators while retaining a plain-integer model value. */
object MoneyInput {
    fun attach(editText: EditText) {
        var formatting = false
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(editable: Editable?) {
                if (formatting) return
                val text = editable?.toString().orEmpty()
                val cursor = editText.selectionStart.coerceAtLeast(0)
                val digitsBeforeCursor = text.take(cursor).count(Char::isDigit)
                val digits = text.filter(Char::isDigit)
                val formatted = digits.toLongOrNull()?.let(::withCommas).orEmpty()
                formatting = true
                editText.setText(formatted)
                var newCursor = 0
                var seenDigits = 0
                while (newCursor < formatted.length && seenDigits < digitsBeforeCursor) {
                    if (formatted[newCursor].isDigit()) seenDigits++
                    newCursor++
                }
                editText.setSelection(newCursor.coerceIn(0, formatted.length))
                formatting = false
            }
        })
        editText.setText(editText.text?.toString())
    }

    fun parse(value: CharSequence?): Int? =
        value?.toString()?.filter(Char::isDigit)?.toLongOrNull()?.takeIf { it <= Int.MAX_VALUE }?.toInt()

    private fun withCommas(value: Long): String =
        value.toString().reversed().chunked(3).joinToString(",").reversed()
}
