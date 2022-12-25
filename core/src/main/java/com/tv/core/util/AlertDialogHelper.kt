package com.tv.core.util

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.ListAdapter
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tv.core.R

class AlertDialogHelper(
    private val context: Context,
    private val overrideThemeResId: Int = R.style.defaultAlertDialogStyle,
    private val title: String = "Dialog"
) {

    private lateinit var dialog: AlertDialog

    //This variable is equal by AlertDialog.BUTTON_POSITIVE
    //Don't use directly of AlertDialog.BUTTON_POSITIVE because it is ambiguity
    private val positionButtonId = -1

    fun create(
        adapter: ListAdapter,
        itemClickListener: DialogInterface.OnClickListener,
        positiveClickListener: DialogInterface.OnClickListener,
        positiveButtonText: String
    ) {
        dialog = MaterialAlertDialogBuilder(context, overrideThemeResId).setTitle(title)
            .setAdapter(
                adapter, itemClickListener
            )
            .setPositiveButton(positiveButtonText, positiveClickListener).create()

    }

    fun show() {
        dialog.show()
        dialog.getButton(positionButtonId).setTextColor(Color.WHITE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(0x99000000.toInt()))
    }

}