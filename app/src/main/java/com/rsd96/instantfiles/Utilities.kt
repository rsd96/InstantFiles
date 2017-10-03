package com.rsd96.instantfiles

/**
 * Created by Ramshad on 8/8/15.
 */

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import java.io.File



object Utilities {

    val TAG = "Utilities"
    internal var mediaFileList: MutableList<File> = mutableListOf()
    internal var documentFileList: MutableList<File> = mutableListOf()
    internal var map = MimeTypeMap.getSingleton()


    // Get all installed application on mobile and checks if its launchable or
    // not then returns that list

    fun getInstalledApplication(c: Context): MutableList<ApplicationInfo> {
        Log.d(TAG, "getInstalledApplication()")

        val db = DatabaseHelper.getInstance(c)
        val dbList = db.getAppData(c)
        if (dbList.isNotEmpty()) {
            return dbList
        } else {
            val installedAppsList = c.packageManager
                    .getInstalledApplications(PackageManager.GET_META_DATA)

            var i = 0
            while (i < installedAppsList.size) {
                // checks if the app is launchable or not
                if (c.packageManager.getLaunchIntentForPackage(
                        installedAppsList[i].packageName) == null) {
                    //if not launchable it is removed from list
                    installedAppsList.removeAt(i)
                    //count decreased as list count decreases by one when a record is removed.
                    --i
                } else {
                    db.insertData(installedAppsList[i].packageName, Types.APP)
                }
                i++
            }
            return installedAppsList
        }
    }

    fun setFiles(dir: File) {
        Log.d(TAG, "setFiles()")
        val dirFile = dir.listFiles()
        if (dirFile != null && dirFile.isNotEmpty()) {
            for (i in dirFile.indices) {
                if (dirFile[i].isDirectory) {
                    setFiles(dirFile[i])
                } else {
                    if (dirFile[i].name.lastIndexOf(".") != -1) {
                        val ext = MimeTypeMap.getFileExtensionFromUrl(dirFile[i].name.substring(dirFile[i].name.lastIndexOf(".")))
                        val mime = map.getMimeTypeFromExtension(ext)
                        if (mime != null && (mime.contains("audio") || mime.contains("image") || mime.contains("video") || mime.contains("application") && mime.contains("ogg"))) {
                            /*if (!mediaFileList.contains(dirFile[i]))*/
                                mediaFileList.add(dirFile[i])

                        } else if (mime != null && (mime.contains("text") || mime.contains("application"))) {
                            if (!(mime.contains("xml") || mime.contains("ogg") || mime.contains("zip") || mime.contains("apk"))) {
                                /*if (!documentFileList.contains(dirFile[i]))*/
                                    documentFileList.add(dirFile[i])
                            }
                        } else {
                            continue
                        }
                    }
                }
            }
        }
    }

    fun launchApp(c: Context, pm: PackageManager, pkgName: String): Boolean {
        // query the intent for lauching
        val intent = pm.getLaunchIntentForPackage(pkgName)
        // if intent is available
        if (intent != null) {
            try {
                // launch application
                c.startActivity(intent)
                // if succeed
                return true

                // if fail
            } catch (ex: ActivityNotFoundException) {
                // quick message notification
                val toast = Toast.makeText(c, R.string.app_not_found_toast,
                        Toast.LENGTH_LONG)
                // display message
                toast.show()
            }

        }
        // by default, fail to launch
        return false
    }

    fun launchFiles(c: Context, filePath: String) {

        val file = File(filePath)
        val map = MimeTypeMap.getSingleton()
        val ext = MimeTypeMap.getFileExtensionFromUrl(file.name.substring(file.name.lastIndexOf(".")))
        var type: String? = map.getMimeTypeFromExtension(ext)
        if (type == null)
            type = "*/*"

        val intent = Intent(Intent.ACTION_VIEW)
        val data = Uri.fromFile(file)

        intent.setDataAndType(data, type)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        c.startActivity(intent)
    }
}