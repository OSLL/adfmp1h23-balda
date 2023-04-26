package com.ifmo.balda

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class TwoButtonsDialog(
  private val message: Int,
  private val yesButtonText: Int = R.string.yes,
  private val noButtonText: Int = R.string.no,
  private val onPositive: () -> Unit = { },
  private val onNegative: () -> Unit = { },
  private val onClose: () -> Unit = { }
) : DialogFragment() {
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = activity?.let {
    with(AlertDialog.Builder(it)) {
      setMessage(message)
      setPositiveButton(yesButtonText) { _, _ -> onPositive() }
      setNegativeButton(noButtonText) { _, _ -> onNegative() }
      setOnCancelListener { onClose() }
      create()
    }
  } ?: throw IllegalStateException("Activity cannot be null")
}
