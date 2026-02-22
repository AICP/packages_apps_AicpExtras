/*
 * Copyright (C) 2016 The DirtyUnicorns Project
 * Copyright (C) 2017-2026 AICP
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * GridView displaying all the available icons in a Icon pack.
 */
package com.aicp.extras

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.res.AssetManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.LruCache
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.util.*

class IconPackGridActivity : AppCompatActivity() {

    private lateinit var mAdapter: IconGridAdapter
    private lateinit var mGridData: ArrayList<IconInfo>
    private lateinit var mGridView: GridView
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mMemoryCache: LruCache<String, Bitmap>
    private lateinit var mPackageName: String
    private lateinit var mIconRes: Resources

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPackageName = intent.getStringExtra("icon_package_name") ?: run {
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        try {
            val pm = packageManager
            val title = pm.getApplicationInfo(mPackageName, 0).loadLabel(pm).toString()
            if (title.isNotEmpty()) {
                setTitle(title)
            }
        } catch (e: Exception) {
            setTitle(R.string.icon_pack_picker_dialog_title)
        }

        try {
            val info = packageManager.getPackageInfo(mPackageName, 0)
            val iconApk = info.applicationInfo?.publicSourceDir ?: run {
                setResult(RESULT_CANCELED)
                finish()
                return
            }
            val assets = AssetManager()
            assets.addAssetPath(iconApk)
            val dm = resources.displayMetrics
            val config = resources.configuration
            mIconRes = Resources(assets, dm, config)
        } catch (e: Exception) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setContentView(R.layout.icon_picker_grid)

        val memClass = (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).memoryClass
        val cacheSize = 1024 * 1024 * memClass / 8
        mMemoryCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount
            }
        }

        mGridView = findViewById(R.id.icon_grid)
        mProgressBar = findViewById(R.id.progressBar)
        mGridData = ArrayList()
        mAdapter = IconGridAdapter(this)
        mGridView.adapter = mAdapter
        mGridView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val info = mGridData[position]
            val resultIntent = Intent().apply {
                putExtra("icon_data_type", "iconpack")
                putExtra("icon_data_package", mPackageName)
                putExtra("icon_data_name", info.name)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        AsyncIconLoaderTask().execute(mPackageName)
        mProgressBar.visibility = View.VISIBLE
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            setResult(RESULT_CANCELED)
            finish()
            return true
        }
        return false
    }

    private inner class AsyncIconLoaderTask : AsyncTask<String, Void, Boolean>() {
        override fun doInBackground(vararg params: String): Boolean {
            val packageName = params[0]
            val set = HashSet<String>()

            try {
                val appfilterstream = mIconRes.assets.open("drawable.xml")
                set.addAll(xmlInputStreamToSet(appfilterstream, packageName))
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                val xmlId = mIconRes.getIdentifier("drawable", "xml", packageName)
                if (xmlId != 0) {
                    set.addAll(xmlPullParserToSet(mIconRes.getXml(xmlId)))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            for (drawableName in set) {
                val resId = mIconRes.getIdentifier(drawableName, "drawable", packageName)
                if (resId > 0) {
                    val d = mIconRes.getDrawable(resId, null)
                    if (d != null) {
                        val info = IconInfo()
                        info.id = resId
                        info.name = drawableName
                        mGridData.add(info)
                    }
                }
            }

            Collections.sort(mGridData, Comparator { lhs, rhs -> lhs.name.compareTo(rhs.name) })
            return mGridData.isNotEmpty()
        }

        override fun onPostExecute(result: Boolean) {
            if (result) {
                mAdapter.setGridData(mGridData)
            } else {
                setResult(RESULT_CANCELED)
                finish()
            }
            mProgressBar.visibility = View.GONE
        }

        private fun xmlInputStreamToSet(inputStream: InputStream, packageName: String): Set<String> {
            val set = HashSet<String>()
            try {
                val factory = XmlPullParserFactory.newInstance()
                factory.isNamespaceAware = true
                val xpp = factory.newPullParser()
                xpp.setInput(inputStream, "utf-8")
                set.addAll(xmlPullParserToSet(xpp))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return set
        }

        private fun xmlPullParserToSet(xpp: XmlPullParser): Set<String> {
            val set = HashSet<String>()
            try {
                var eventType = xpp.eventType
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.name == "item") {
                            for (i in 0 until xpp.attributeCount) {
                                if (xpp.getAttributeName(i).startsWith("drawable")) {
                                    val drawableName = xpp.getAttributeValue(i)
                                    set.add(drawableName)
                                }
                            }
                        }
                    }
                    eventType = xpp.next()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return set
        }
    }

    private inner class IconGridAdapter(private val context: Context) : BaseAdapter() {
        private val mInflater: LayoutInflater = LayoutInflater.from(context)
        private var mGridDataInternal: ArrayList<IconInfo> = ArrayList()

        fun setGridData(gridData: ArrayList<IconInfo>) {
            mGridDataInternal = gridData
            notifyDataSetChanged()
        }

        override fun getCount(): Int {
            return mGridDataInternal.size
        }

        override fun getItem(position: Int): IconInfo {
            return mGridDataInternal[position]
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var view = convertView
            val holder: ViewHolder

            if (view == null) {
                view = mInflater.inflate(R.layout.icon_picker_item, parent, false)
                holder = ViewHolder(view.findViewById(R.id.grid_item_image))
                view.tag = holder
            } else {
                holder = view.tag as ViewHolder
            }

            val info = getItem(position)
            var b = mMemoryCache.get(info.name)
            var d: Drawable? = null
            if (b == null) {
                try {
                    d = mIconRes.getDrawable(info.id, null)
                    val tmp = drawableToBitmap(d)
                    if (tmp != null) {
                        mMemoryCache.put(info.name, tmp)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                d = BitmapDrawable(resources, b)
            }
            holder.icon.setImageDrawable(d)
            return view!!
        }
    }

    private class ViewHolder(val icon: ImageView)

    private class IconInfo {
        var id: Int = 0
        lateinit var name: String
    }

    companion object {
        fun drawableToBitmap(drawable: Drawable?): Bitmap? {
            if (drawable == null) {
                return null
            } else if (drawable is BitmapDrawable) {
                return drawable.bitmap
            }
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }
    }
}

