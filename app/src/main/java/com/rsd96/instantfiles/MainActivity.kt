package com.rsd96.instantfiles

import android.os.Bundle
import android.os.Environment
import android.os.FileObserver
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var mTabs: TabLayout
    private var tabsviewPager: ViewPager? = null // For swiping between tabs
    private var mTabsAdapter: TabsAdapter? = null
    val path: String = Environment.getExternalStorageDirectory().absolutePath
    val mask: Int = FileObserver.CREATE


    companion object {
        private val TAG = "MainActivity"
        var appListAdapter: AppListAdapter? = null
        var mediaListAdapter: MediaListAdapter? = null
        var documentListAdapter: DocumentListAdapter? = null
        var root: File = File(Environment.getExternalStorageDirectory().absolutePath)
        var loadingFiles = false  // boolean to indicate that media and document file list is being made
        var filesLoaded = false // to ensure files are loaded only once
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        defVars()
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        //creating the tabs and adding them to adapter class
        mTabsAdapter!!.addFragment(AppListFragment(), getString(R.string.applications))
        mTabsAdapter!!.addFragment(MediaListFragment(), getString(R.string.media))
        mTabsAdapter!!.addFragment(DocumentListFragment(), getString(R.string.documents))


        //setup viewpager to give swipe effect
        tabsviewPager!!.adapter = mTabsAdapter
        tabsviewPager!!.currentItem = 0

        mTabs.setupWithViewPager(tabsviewPager!!)


        // Search function
        RxTextView.afterTextChangeEvents(etSearch)
                .debounce(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { Log.d(TAG, "textFilter Error : ${it.message}")  }
                .subscribe { tvChangeEvent ->
                    Log.d(TAG, "Output : " + tvChangeEvent.view().text)
                    val s = tvChangeEvent.view().text.trim()
                    appListAdapter?.filter?.filter(s)
                    mediaListAdapter?.filter?.filter(s)
                    documentListAdapter?.filter?.filter(s)
                }

        // Check files and load data
        Completable.fromAction { Utilities.setFiles(root) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { Log.d(TAG, "Rx Reading error") }
                .doOnComplete {
                    UpdateData.AddMediaToDB(applicationContext).execute(Utilities.mediaFileList)
                    UpdateData.AddDocsToDB(applicationContext).execute(Utilities.documentFileList)
                }
                .subscribe()
    }


    fun defVars() {
        tabsviewPager = findViewById(R.id.tabspager) as ViewPager
        mTabsAdapter = TabsAdapter(supportFragmentManager)
        mTabs = findViewById(R.id.tabs) as TabLayout
    }

    override fun onPostResume() {
        super.onPostResume()
        Log.d("LIFE CYCLE", "onPostResume()")

    }

    override fun onStop() {
        super.onStop()
        Log.d("LIFE CYCLE", "onsStop()")
    }

    override fun onPause() {
        super.onPause()
        Log.d("LIFE CYCLE", "onPause()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("LIFE CYCLE", "onDestroy()")
    }
}
