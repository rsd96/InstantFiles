package com.rsd96.instantfiles

/**
 * Created by Ramshad on 8/8/15.
 */

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.util.*

class AppListAdapter(internal var context: Context, internal var appList: MutableList<ApplicationInfo>, pm: PackageManager) : BaseAdapter(), Filterable {
    var listofApps: MutableList<ApplicationInfo> // copy of original data
    private val TAG = "AppListAdapter"
    private var appListFilter: AppListFilter? = null
    internal var appNameList: ArrayList<String>

    init {
        Log.d(TAG, "AppListAdapter")
        listofApps = appList // copy of list of all apps
        pManager = pm
        appNameList = ArrayList()
        for (i in listofApps.indices) {
            appNameList.add(listofApps[i].loadLabel(pManager).toString())
        }
    }

    override fun getCount(): Int = listofApps.size
    override fun getItem(position: Int): Any = listofApps[position]
    override fun getItemId(position: Int): Long =  position.toLong()

    override fun getView(position: Int, cView: View?, parent: ViewGroup): View {

        val itemInfo = listofApps[position]
        var convertView: View? = cView
        val viewHolder: ViewHolder

        if (convertView == null) {
            val inflater = LayoutInflater.from(context)
            convertView = inflater.inflate(R.layout.list_item, null)

            viewHolder = ViewHolder()
            viewHolder.mFilenameTextView = convertView!!.findViewById(R.id.filename_text_view) as TextView
            viewHolder.mThumbnailImageView = convertView.findViewById(R.id.thumbnail_image_view) as ImageView
            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
        }

        viewHolder.mThumbnailImageView!!.setImageDrawable(itemInfo.loadIcon(pManager))
        viewHolder.mFilenameTextView!!.text = itemInfo.loadLabel(pManager)

        return convertView
    }

    // Filtering of list
    // called when user types somethign in edittext
    override fun getFilter(): Filter {
        if (appListFilter == null) {

            appListFilter = AppListFilter()
        }

        return appListFilter as AppListFilter
    }

    private inner class AppListFilter : Filter() {

        override fun performFiltering(constraint: CharSequence): Filter.FilterResults {
            Log.d(TAG, "performAppFiltering")
            val results = Filter.FilterResults()

            // checks each item of original list whether it starts with search text (constraints)
            // and if it does , its added to the new array list (FilteredList)
            if (constraint.length == 0) {
                results.count = appList.size
                results.values = appList
            } else {

                listofApps = appList.filter { it.loadLabel(pManager).toString().toLowerCase()
                                .contains(constraint.toString().toLowerCase()) } as MutableList<ApplicationInfo>

                results.count = listofApps.size
                results.values = listofApps
            }

            return results
        }

        override fun publishResults(constraint: CharSequence,
                                    results: Filter.FilterResults) {
            Log.d(TAG, "publishAppResults")
            // filtered list is added to orignal list and then the list is refreshed using "notifydatasetchanged()"
            listofApps = results.values as MutableList<ApplicationInfo>
            notifyDataSetChanged()
        }
    }

    internal class ViewHolder {
        var mThumbnailImageView: ImageView? = null
        var mFilenameTextView: TextView? = null
    }

    companion object {
        var pManager: PackageManager? = null
    }
}
