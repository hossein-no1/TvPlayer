package com.tv.core.util.ui

import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.ColorDrawable
import android.widget.ListAdapter
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tv.core.R

class AlertDialogHelper(
    private val context: Context,
    private val resIdStyle: Int = R.style.defaultAlertDialogStyle,
    private val title: String = "Dialog"
) {

    private lateinit var dialog: AlertDialog

    fun create(
        adapter: ListAdapter,
        itemClickListener: DialogInterface.OnClickListener,
        positiveClickListener: DialogInterface.OnClickListener,
        positiveButtonText: String
    ) {
        dialog = MaterialAlertDialogBuilder(context, resIdStyle).setTitle(title)
            .setAdapter(
                adapter, itemClickListener
            )
            .setPositiveButton(positiveButtonText, positiveClickListener).create()

    }

    fun show() {
        dialog.show()
        dialog.window?.setBackgroundDrawable(ColorDrawable(0x99000000.toInt()))
    }

}