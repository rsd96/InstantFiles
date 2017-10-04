package com.rsd96.instantfiles

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.AdapterView
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
class DocumentListFragment : Fragment(), AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener{


    private val TAG = "DocumentListFragment"
    private var flag = true
    internal var context: Context? = null
    lateinit var disposable: Disposable


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.files_list_view, container, false)
        context = activity.applicationContext

        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView.adapter = MainActivity.documentListAdapter
        listView.onItemClickListener = this
        listView.onItemLongClickListener = this

        loadList()
    }

    fun loadList() {
        disposable = Observable
                .create(ObservableOnSubscribe<MutableList<File>> { emitter ->
                    //show prog. bar
                    progressBar.visibility = View.VISIBLE
                    progressBar.isIndeterminate = true
                    try {
                        emitter.onNext(getDocList())
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
                    MainActivity.documentListAdapter = context?.let { DocumentListAdapter(it, result) }
                    listView.adapter = MainActivity.documentListAdapter
                    MainActivity.documentListAdapter?.notifyDataSetChanged()
                }
    }

    fun getDocList() : MutableList<File> {
        val db = DatabaseHelper.getInstance(activity.applicationContext)
        val dbDocList = db.getFileData(Types.DOCUMENT)
        if(dbDocList.isEmpty()) {
            return dbDocList
        } else {
            return dbDocList
        }
    }

    inner class AddDocsToDB() : AsyncTask<Void, Void, Void>() {
        val db = DatabaseHelper.getInstance(activity.applicationContext)
        override fun doInBackground(vararg params: Void?): Void? {
            for (i in Utilities.documentFileList) {
                db.insertData(i.absolutePath, Types.DOCUMENT)
            }
            return null
        }
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val documentListAdapter = parent.adapter as DocumentListAdapter
        // get selected item on the list
        val mediaFileInfo = documentListAdapter.getItem(position) as File
        Utilities.launchFiles(activity.baseContext, mediaFileInfo.toString())
    }

    override fun onItemLongClick(parent: AdapterView<*>, view: View, position: Int, id: Long): Boolean {

        val dialogListItems = arrayOf<CharSequence>("Rename", "Delete", "Properties")
        val documentListAdapter = parent.adapter as DocumentListAdapter
        val documentFileInfo = documentListAdapter.getItem(position) as File
        val builder = AlertDialog.Builder(activity)
        builder.setItems(dialogListItems) { dialog, which ->
            when (which) {

                0 -> {
                    val et = EditText(context)
                    DialogBoxes.dialogBox(activity, "Rename to:", null, et, "Rename") { dialog, which ->
                        val f = documentFileInfo.absolutePath.toString()
                        val from = File(documentFileInfo.absolutePath.toString())
                        val ext = MimeTypeMap.getFileExtensionFromUrl(documentFileInfo.name.substring(documentFileInfo.name.lastIndexOf(".")))
                        val dir = File(f.substring(0, f.lastIndexOf("/")))
                        val to = File(dir, et.text.toString() + "." + ext)
                        renameFile(from, to, position)
                    }
                }
                1 -> DialogBoxes.dialogBox(activity, "Delete File", "Are you sure ?", null, "Delete") { dialog, which ->
                    val f = File(documentFileInfo.toString())
                    deleteFile(f, position)
                }

                2 -> {
                    var hidden: Boolean? = null
                    hidden = documentFileInfo.isHidden

                    DialogBoxes.dialog(activity, documentFileInfo.name, documentFileInfo.toString(), documentFileInfo.length(), hidden)
                }
            }
        }
        val alertDialog = builder.create()
        alertDialog.show()
        return true
    }

    private fun renameFile(from: File, to: File, pos: Int) {
        if (to.exists()) {
            Toast.makeText(context, "File already exists!", Toast.LENGTH_SHORT).show()
        } else {
            if (from.renameTo(to)) {
                MainActivity.documentListAdapter?.documentList?.removeAt(pos)
                MainActivity.documentListAdapter?.notifyDataSetChanged()
                //LoadList().execute()
                context?.let { scanFile(it, to) }
                Toast.makeText(context, R.string.rename_s, Toast.LENGTH_SHORT).show()
            } else
                Toast.makeText(context, R.string.rename_f, Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteFile(f: File, pos: Int) {
        if (f.delete()) {
            Utilities.documentFileList.removeAt(pos)
            MainActivity.documentListAdapter?.notifyDataSetChanged()
            //LoadList().execute()
            Toast.makeText(context, R.string.del_s, Toast.LENGTH_SHORT).show()
            context?.let { scanFile(it, f) }
        } else
            Toast.makeText(context, R.string.del_f, Toast.LENGTH_SHORT).show()
    }

    fun scanFile(context: Context, f: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val mediaScanIntent = Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val contentUri = Uri.fromFile(f)
            mediaScanIntent.data = contentUri
            context.sendBroadcast(mediaScanIntent)
        } else {
            context.sendBroadcast(Intent(
                    Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://" + f.toString())))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }
}
