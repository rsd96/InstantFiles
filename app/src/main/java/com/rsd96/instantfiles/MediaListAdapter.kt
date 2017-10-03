package com.rsd96.instantfiles

/**
 * Created by Ramshad on 8/8/15.
 */

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.*
import java.io.File
import java.util.*

class MediaListAdapter(internal var context: Context, internal var mediaList: MutableList<File>) : BaseAdapter(), Filterable {
    private val TAG = "MediaListAdapter"
    internal var listOfMedia: MutableList<File>
    private var mediaListFilter: MediaListFilter? = null
    internal var map = MimeTypeMap.getSingleton()


    init {
        Log.d(TAG, "MediaListAdapter")
        listOfMedia = mediaList
    }

    override fun getCount(): Int {
        return mediaList.size
    }

    override fun getItem(position: Int): Any {
        return mediaList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val mediaName = mediaList[position].name
        var v: View? = convertView
        val viewHolder: ViewHolder
        val ext = MimeTypeMap.getFileExtensionFromUrl(mediaName.substring(mediaName.lastIndexOf(".")))
        val mime = map.getMimeTypeFromExtension(ext)

        if (v == null) {
            val inflater = LayoutInflater.from(context)
            v = inflater.inflate(R.layout.list_item, null)

            viewHolder = ViewHolder()
            viewHolder.mFilenameTextView = v!!.findViewById(R.id.filename_text_view) as TextView
            viewHolder.mThumbnailImageView = v.findViewById(R.id.thumbnail_image_view) as ImageView
            v.tag = viewHolder
        } else {
            viewHolder = v.tag as ViewHolder
        }


        if (mime != null && (mime.contains("audio") || mime.contains("application") && mime.contains("ogg"))) {
            viewHolder.mThumbnailImageView!!.setImageResource(R.drawable.music_list_icon)
        } else if (mime!!.contains("image")) {
            viewHolder.mThumbnailImageView!!.setImageResource(R.drawable.image_list_icon)
        } else {
            viewHolder.mThumbnailImageView!!.setImageResource(R.drawable.video_list_icon)
        }
        viewHolder.mFilenameTextView!!.text = mediaName



        return v
    }

    override fun getFilter(): Filter {
        // TODO Auto-generated method stub
        if (mediaListFilter == null) {

            mediaListFilter = MediaListFilter()
        }

        return mediaListFilter as Filter
    }

    private inner class MediaListFilter : Filter() {

        override fun performFiltering(constraint: CharSequence): Filter.FilterResults {
            // TODO Auto-generated method stub
            Log.d(TAG, "performMediaFiltering")
            val results = Filter.FilterResults()
            val filteredList = ArrayList<File>()
            // checks each item of original list whether it starts with search text (constraints)
            // and if it does , its added to the new array list (FilteredList)
            if (constraint.length == 0) {
                results.count = listOfMedia.size
                results.values = listOfMedia
            } else {
                mediaList = listOfMedia // applist is reset to original list of all apps ,
                //if not some data will be lost during filtering
                for (i in mediaList.indices) {
                    //String appNameList = mediaList.get(i).getName();
                    if (mediaList[i].name.toLowerCase().contains(
                            constraint.toString().toLowerCase())) {
                        // data from applist that starts with typed text is added to newly created list (filteredList)
                        filteredList.add(mediaList[i])
                    }
                }
                results.count = filteredList.size
                results.values = filteredList
            }

            return results
        }

        override fun publishResults(constraint: CharSequence,
                                    results: Filter.FilterResults) {
            Log.d(TAG, "publishMediaResults")
            // filtered list is added to orignal list and then the list is refreshed using "notifydatasetchanged()"
            try {
                mediaList = results.values as MutableList<File>
            } catch (e: Exception) {
                e.printStackTrace()
            }
            notifyDataSetChanged()
        }

    }

    internal class ViewHolder {
        var mThumbnailImageView: ImageView? = null
        var mFilenameTextView: TextView? = null
    }
}