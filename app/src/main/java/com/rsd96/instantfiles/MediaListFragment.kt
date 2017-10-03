package com.rsd96.instantfiles

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.Toast
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.files_list_view.*
import java.io.File

/**
 * Created by Ramshad on 8/8/15.
 */

class MediaListFragment : Fragment() {

    companion object {
        var loadFiles = false
    }
    private val TAG = "MediaListFragment"
    private var flag = true
    internal val dialogListItems = arrayOf<CharSequence>("Rename", "Delete", "Properties")
    @get:JvmName("getContext_") private var context: Context? = null // annotation to avoid conflicts caused due to genration of getters by kotlin compiler
    lateinit var disposable: Disposable

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView()")

        val view = inflater?.inflate(R.layout.files_list_view, container, false)
        context = activity.applicationContext

        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {

        listView.setOnItemClickListener { parent, view, position, id ->
            val mediaListAdapter = parent.adapter as MediaListAdapter
            // get selected item on the list
            val mediaFileInfo = mediaListAdapter.getItem(position) as File
            Utilities.launchFiles(activity.baseContext, mediaFileInfo.toString())
        }

        listView.setOnItemLongClickListener { parent, view, position, id ->
            val mediaListAdapter = parent.adapter as MediaListAdapter
            val mediaFileInfo = File(mediaListAdapter.getItem(position).toString())
            val builder = AlertDialog.Builder(activity)
            builder.setItems(dialogListItems) { dialog, which ->
                when (which) {
                    0 -> {
                        val et = EditText(context)
                        et.setTextColor(Color.RED)
                        DialogBoxes.dialogBox(activity, "Rename to:", null, et, "Rename", { dialog, which ->
                            val f = mediaFileInfo.absolutePath.toString()
                            val from = File(mediaFileInfo.absolutePath.toString())
                            val ext = MimeTypeMap.getFileExtensionFromUrl(mediaFileInfo.name.substring(mediaFileInfo.name.lastIndexOf(".")))
                            val dir = File(f.substring(0, f.lastIndexOf("/")))
                            val to = File(dir, et.text.toString() + "." + ext)
                            renameFile(from, to, position)
                        })
                    }

                    1 -> DialogBoxes.dialogBox(activity, "Delete File", "Are you sure ?", null, "Delete", { dialog, which ->
                        val f = File(mediaFileInfo.toString())
                        deleteFile(f, position)
                    })

                    2 -> {
                        var hidden: Boolean? = null
                        hidden = mediaFileInfo.isHidden

                        DialogBoxes.dialog(activity, mediaFileInfo.name, mediaFileInfo.toString(), mediaFileInfo.length(), hidden)
                    }
                }
            }
            val alertDialog = builder.create()
            alertDialog.show()
            true
        }

        if (flag) {
            loadList()
            flag = false
        } else {
            listView.adapter = MainActivity.mediaListAdapter
        }
    }

    private fun renameFile(from: File, to: File, pos: Int) {
        if (to.exists()) {
            Toast.makeText(context, "File already exists!", Toast.LENGTH_SHORT).show()
        } else {
            if (from.renameTo(to)) {
                MainActivity.mediaListAdapter?.mediaList?.removeAt(pos)
                MainActivity.mediaListAdapter?.notifyDataSetChanged()
                /*LoadList().execute()*/
                Toast.makeText(context, "Rename Successful!", Toast.LENGTH_SHORT).show()
                scanFile(context, to)
            } else
                Toast.makeText(context, "Rename Failed!", Toast.LENGTH_SHORT).show()
        }

    }

    private fun deleteFile(f: File, pos: Int) {
        if (f.delete()) {
            MainActivity.mediaListAdapter?.mediaList?.removeAt(pos)
            MainActivity.mediaListAdapter?.notifyDataSetChanged()
            /*LoadList().execute()*/
            Toast.makeText(context, R.string.del_s, Toast.LENGTH_SHORT).show()
            scanFile(context, f)
        } else
            Toast.makeText(context, "Delete Failed!", Toast.LENGTH_SHORT).show()
    }

    fun scanFile(context: Context?, f: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val mediaScanIntent = Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val contentUri = Uri.fromFile(f)
            mediaScanIntent.data = contentUri
            context!!.sendBroadcast(mediaScanIntent)
        } else {
            context!!.sendBroadcast(Intent(
                    Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://" + f.toString())))
        }
    }


    fun loadList() {
         disposable = Observable
                .create(ObservableOnSubscribe<MutableList<File>> { emitter ->
                    //show prog. bar
                    progressBar.visibility = View.VISIBLE
                    progressBar.isIndeterminate = true
                    try {
                        emitter.onNext(getMediaList())
                        emitter.onComplete()
                    } catch (e: Exception) {
                        emitter.onError(e)
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { result ->
                    if(progressBar.visibility == View.VISIBLE)
                        progressBar.visibility = View.GONE
                    MainActivity.mediaListAdapter = context?.let { MediaListAdapter(it, result) }
                    listView.adapter = MainActivity.mediaListAdapter
                }
    }

    fun getMediaList() : MutableList<File> {
        val db = DatabaseHelper.getInstance(activity.applicationContext)
        val dbMediaList = db.getFileData(Types.MEDIA)
        if(dbMediaList.isEmpty()) {
            return dbMediaList
        } else {
            return dbMediaList
        }
    }

    /*inner class LoadList : AsyncTask<String, Void, MutableList<File>>() {

        internal var dbHelper = DatabaseHelper.getInstance(activity)

        override fun onPreExecute() {
            //mediaListFromDB = dbHelper.getDataFromDB(DatabaseHelper.TABLE_MEDIA_NAME)

            progressBar.visibility = View.VISIBLE
            progressBar.isIndeterminate = true

        }

        override fun doInBackground(vararg params: String): MutableList<File> {
            val db = DatabaseHelper.getInstance(activity.applicationContext)
            val dbMediaList = db.getFileData(Types.MEDIA)
            for(i in dbMediaList)
                Log.d(TAG, "Media DB data : $i")
            if(dbMediaList.isEmpty()) {
                if (!MainActivity.loadingFiles) {
                    MainActivity.loadingFiles = true
                    Log.d(TAG, "loadingFiles-true")
                    Log.d(TAG, "loading Files ... ")
                    Utilities.setFiles(MainActivity.root)
                    MainActivity.loadingFiles = false
                    Log.d(TAG, "Files loaded")
                } else {
                    // if document fragment is already started setFiles() , then just sleep till its done.
                    try {
                        while (MainActivity.loadingFiles) {
                            Thread.sleep(1000)
                            Log.d(TAG, "sleeping")
                        }
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
                AddMediaToDB().execute()
                return Utilities.mediaFileList
            } else {
                return dbMediaList
            }
        }

        override fun onPostExecute(results: MutableList<File>) {
            MainActivity.mediaListAdapter = context?.let { MediaListAdapter(it, results) }
            listView.adapter = MainActivity.mediaListAdapter

            progressBar.visibility = View.GONE
        }
    }*/


    inner class AddMediaToDB() : AsyncTask<Void, Void, Void>() {
        val db = DatabaseHelper.getInstance(activity.applicationContext)
        override fun doInBackground(vararg params: Void?): Void? {

            for (i in Utilities.mediaFileList) {
                db.insertData(i.absolutePath, Types.MEDIA)
            }
            return null
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("MF -LIFE CYCLE", "onResume()")
    }

    override fun onStop() {
        super.onStop()
        Log.d("MF -LIFE CYCLE", "onsStop()")
    }

    override fun onPause() {
        super.onPause()
        Log.d("MF -LIFE CYCLE", "onPause()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MF -LIFE CYCLE", "onDestroy()")
        disposable.dispose()
    }
}
