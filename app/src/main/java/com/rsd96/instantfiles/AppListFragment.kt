package com.rsd96.instantfiles

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.Observable
import kotlinx.android.synthetic.main.files_list_view.*

/**
 * Created by Ramshad on 8/7/15.
 */
class AppListFragment : Fragment() {

    private var flag = true
    @get:JvmName("getContext_") private var context: Context? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater?.inflate(R.layout.files_list_view, container, false)
        context = activity.applicationContext
        var appObservable : Observable<MutableList<ApplicationInfo>>

        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView.setOnItemClickListener { _, _, position, _ ->
            val appInfoAdapter = listView.adapter as AppListAdapter
            // get selected item on the list
            val appInfo = appInfoAdapter.getItem(position) as ApplicationInfo
            context?.let { Utilities.launchApp(it, activity.packageManager, appInfo.packageName) }
        }


        if (flag) {
            LoadList().execute()
            flag = false
        } else
            listView.adapter = MainActivity.appListAdapter
    }


    inner class LoadList : AsyncTask<String, Void, MutableList<ApplicationInfo>>() {


        override fun onPreExecute() {

            if(progressBar != null) {
                progressBar.visibility = View.VISIBLE
                progressBar.isIndeterminate = true
            }
        }


        override fun doInBackground(vararg params: String): MutableList<ApplicationInfo>? {
            return context?.let { Utilities.getInstalledApplication(it) }
        }

        override fun onPostExecute(results: MutableList<ApplicationInfo>) {
            if(progressBar != null)
                progressBar.visibility = View.GONE
            MainActivity.appListAdapter = AppListAdapter(activity.applicationContext, results, activity.packageManager)
            listView.adapter = MainActivity.appListAdapter
        }
    }


}
