package com.rsd96.instantfiles

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.view.View
import android.widget.TextView


/**
 * Created by Ramshad on 8/29/15.
 */
object DialogBoxes {




    fun dialog(context: Context, name: String, path: String, size: Long, hidden: Boolean?) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.properties_dialog)
        dialog.setTitle("Properties")

        val tvName: TextView = dialog.findViewById(R.id.tvName) as TextView
        val tvPath: TextView = dialog.findViewById(R.id.tvPath) as TextView
        val tvSize: TextView = dialog.findViewById(R.id.tvSize) as TextView
        val tvHidden: TextView = dialog.findViewById(R.id.tvHidden) as TextView



        var value: String? = null
        val fileSize = size / 1024//call function and convert bytes into Kb
        if (fileSize >= 1024)
            value = (fileSize / 1024).toString() + " Mb"
        else
            value = fileSize.toString() + " Kb"

        tvName.text = name
        tvPath.text = path
        tvSize.text = value
        tvHidden.text = hidden!!.toString()

        dialog.show()
    }

    fun dialogBox(context: Context, title: String?, message: String?, view: View?, positiveBtn: String?, listener: (Any, Any) -> Unit) {

        val builder = AlertDialog.Builder(context)
        if (title != null)
            builder.setTitle(title)
        if (message != null)
            builder.setMessage(message)
        builder.setNegativeButton(R.string.cancel) { dialog, which -> dialog.dismiss() }
        if (positiveBtn != null)
            builder.setPositiveButton(positiveBtn, listener)
        if (view != null)
            builder.setView(view)
        val dialog = builder.create()
        dialog.show()
    }
}
