package com.pritex.ptx001

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout

class PtxKeyboardService : InputMethodService() {

    private var shift = false
    private val letterButtons = mutableListOf<Pair<Button, Char>>()

    override fun onCreateInputView(): View {
        letterButtons.clear()
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(0xFF222222.toInt())
        root.setPadding(4, 8, 4, 8)

        val rows = listOf("qwertyuiop", "asdfghjkl", "zxcvbnm")
        for ((index, row) in rows.withIndex()) {
            val rowLayout = newRow()
            if (index == 2) rowLayout.addView(makeKey("⇧", 1.5f) { toggleShift() })
            for (ch in row) {
                val b = makeKey(ch.toString(), 1f) { typeLetter(ch) }
                letterButtons.add(Pair(b, ch))
                rowLayout.addView(b)
            }
            if (index == 2) rowLayout.addView(makeKey("⌫", 1.5f) { deleteChar() })
            root.addView(rowLayout)
        }

        val bottom = newRow()
        bottom.addView(makeKey(",", 1f) { commit(",") })
        bottom.addView(makeKey("space", 5f) { commit(" ") })
        bottom.addView(makeKey(".", 1f) { commit(".") })
        bottom.addView(makeKey("↵", 1.5f) { pressEnter() })
        root.addView(bottom)
        return root
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun newRow(): LinearLayout {
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        return row
    }

    private fun makeKey(label: String, weight: Float, onClick: () -> Unit): Button {
        val b = Button(this)
        b.text = label
        b.isAllCaps = false
        b.setOnClickListener { onClick() }
        b.layoutParams = LinearLayout.LayoutParams(0, dp(50), weight)
        return b
    }

    private fun commit(text: String) {
        currentInputConnection?.commitText(text, 1)
    }

    private fun typeLetter(ch: Char) {
        val s = if (shift) ch.uppercaseChar().toString() else ch.toString()
        commit(s)
        if (shift) {
            shift = false
            refreshLetters()
        }
    }

    private fun toggleShift() {
        shift = !shift
        refreshLetters()
    }

    private fun refreshLetters() {
        for ((b, ch) in letterButtons) {
            b.text = if (shift) ch.uppercaseChar().toString() else ch.toString()
        }
    }

    private fun deleteChar() {
        currentInputConnection?.deleteSurroundingText(1, 0)
    }

    private fun pressEnter() {
        val ic = currentInputConnection ?: return
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
    }
}
