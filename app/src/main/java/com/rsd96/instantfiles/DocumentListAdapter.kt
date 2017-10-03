package com.rsd96.instantfiles

/**
 * Created by Ramshad on 8/8/15.
 */

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.io.File
import java.util.*

class DocumentListAdapter(internal var context: Context, internal var documentList: MutableList<File>) : BaseAdapter(), Filterable {

    private val TAG = "DocumentListAdapter"
    internal var listOfDocument: MutableList<File>
    private var documentListFilter: DocumentListFilter? = null

    init {
        Log.d(TAG, "MediaListAdapter")
        listOfDocument = documentList
    }

    override fun getCount(): Int {
        return documentList.size
    }

    override fun getItem(position: Int): Any {
        return documentList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val documentName = documentList[position].name
        var v: View? = convertView
        val viewHolder: ViewHolder

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

        viewHolder.mThumbnailImageView!!.visibility = View.GONE
        viewHolder.mFilenameTextView!!.text = documentName

        return v
    }

    override fun getFilter(): Filter {
        // TODO Auto-generated method stub
        if (documentListFilter == null) {

            documentListFilter = DocumentListFilter()
        }

        return documentListFilter as DocumentListFilter
    }

    private inner class DocumentListFilter : Filter() {

        override fun performFiltering(constraint: CharSequence): Filter.FilterResults {
            // TODO Auto-generated method stub
            Log.d(TAG, "performDocumentFiltering")
            val results = Filter.FilterResults()
            val filteredList = ArrayList<File>()
            // checks each item of original list whether it starts with search text (constraints)
            // and if it does , its added to the new array list (FilteredList)
            if (constraint.length == 0) {
                results.count = listOfDocument.size
                results.values = listOfDocument
            } else {
                documentList = listOfDocument // applist is reset to original list of all apps ,
                //if not some data will be lost during filtering
                for (i in documentList.indices) {
                    //String appNameList = mediaList.get(i).getName();
                    if (documentList[i].name.toLowerCase().contains(
                            constraint.toString().toLowerCase())) {
                        // data from applist that starts with typed text is added to newly created list (filteredList)
                        filteredList.add(documentList[i])
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
            documentList = results.values as MutableList<File>
            notifyDataSetChanged()
        }

    }

    internal class ViewHolder {
        var mThumbnailImageView: ImageView? = null
        var mFilenameTextView: TextView? = null
    }
}