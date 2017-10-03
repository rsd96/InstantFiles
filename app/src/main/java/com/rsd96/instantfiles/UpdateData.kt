package com.rsd96.instantfiles

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import java.io.File

/**
 * Created by Ramshad on 9/30/17.
 */
object UpdateData {
    private val TAG = "UpdateData"

     class AddMediaToDB(context: Context) : AsyncTask<MutableList<File>, Void, Void>() {
        val db = DatabaseHelper.getInstance(context)
        override fun doInBackground(vararg params: MutableList<File>?): Void? {
            Log.d(TAG, "AddMediaToDB()")
            val list = params[0]
            if(MainActivity.mediaListAdapter != null) {
                if (list != null) {
                    val delFileList = MainActivity.mediaListAdapter?.mediaList?.filter { !(list.contains(it)) }
                    if (delFileList?.isNotEmpty()!!)
                        for (x in delFileList)
                            db.deleteData(x.absolutePath, Types.MEDIA)

                    val newFileList = list.filter { !(MainActivity.mediaListAdapter!!.mediaList.contains(it)) }
                    if (newFileList.isNotEmpty())
                        for (x in newFileList)
                            db.insertData(x.absolutePath, Types.MEDIA)
                }
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            if(MainActivity.mediaListAdapter != null)
                MainActivity.mediaListAdapter!!.notifyDataSetChanged()
        }
    }

    class AddDocsToDB(context: Context) : AsyncTask<MutableList<File>, Void, Void>() {
        val db = DatabaseHelper.getInstance(context)
        override fun doInBackground(vararg params: MutableList<File>?): Void? {
            Log.d(TAG, "AddDocsToDB()")
            val list = params[0]

            if(MainActivity.documentListAdapter != null) {
                if (list != null) {
                    val delFileList = MainActivity.documentListAdapter!!.documentList.filter { !(list.contains(it)) }
                    if (delFileList.isNotEmpty())
                        for (x in delFileList)
                            db.deleteData(x.absolutePath, Types.DOCUMENT)

                    val newFileList = list.filter { !(MainActivity.documentListAdapter!!.documentList.contains(it)) }
                    if (newFileList.isNotEmpty())
                        for (x in newFileList)
                            db.insertData(x.absolutePath, Types.DOCUMENT)
                }
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            if(MainActivity.documentListAdapter != null)
                MainActivity.documentListAdapter!!.notifyDataSetChanged()
        }
    }

}