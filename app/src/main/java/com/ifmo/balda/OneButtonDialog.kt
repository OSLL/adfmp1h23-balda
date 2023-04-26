package com.ifmo.balda

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class OneButtonDialog(
  private val message: Int,
  private val buttonText: Int = R.string.ok,
  private val onButtonClick: () -> Unit = { },
  private val onClose: () -> Unit = { }
) : DialogFragment() {
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = activity?.let {
    with(AlertDialog.Builder(it)) {
      setMessage(message)
      setNeutralButton(buttonText) { _, _ -> onButtonClick() }
      setOnCancelListener { onClose() }
      create()
    }
  } ?: throw IllegalStateException("Activity cannot be null")
}
