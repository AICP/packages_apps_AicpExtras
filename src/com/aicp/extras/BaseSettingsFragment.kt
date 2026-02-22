/*
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
 */
package com.aicp.extras

import android.content.ContentResolver
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.android.settingslib.CustomDialogPreferenceCompat
import com.aicp.extras.utils.Util
import com.aicp.extras.widget.HighlightablePreferenceGroupAdapter
import com.aicp.gear.preference.AicpPreferenceFragment
import java.util.UUID

abstract class BaseSettingsFragment : AicpPreferenceFragment() {

    private val mDataSetObserver = object : AdapterDataObserver() {
        override fun onChanged() {
            onDataSetChanged()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            onDataSetChanged()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            onDataSetChanged()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            onDataSetChanged()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            onDataSetChanged()
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            onDataSetChanged()
        }
    }

    private var mMasterDependencyEnabled: Boolean? = null
    private var mInitialized = false
    private lateinit var mLayoutManager: LinearLayoutManager
    private var mAdapter: HighlightablePreferenceGroupAdapter? = null
    private var mPreferenceHighlighted = false
    private var mCurrentRootAdapter: Adapter<*>? = null
    private var mIsDataSetObserverRegistered = false

    protected abstract fun getPreferenceResource(): Int

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(getPreferenceResource())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mInitialized = true
        if (savedInstanceState != null) {
            mPreferenceHighlighted = savedInstanceState.getBoolean(SAVE_HIGHLIGHTED_KEY)
        }
        if (mMasterDependencyEnabled != null) {
            setMasterDependencyState(mMasterDependencyEnabled!!)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mAdapter?.let {
            outState.putBoolean(SAVE_HIGHLIGHTED_KEY, it.isHighlightRequested)
        }
    }

    override fun onResume() {
        super.onResume()
        highlightPreferenceIfNeeded()
    }

    override fun onBindPreferences() {
        registerObserverIfNeeded()
    }

    override fun onUnbindPreferences() {
        unregisterObserverIfNeeded()
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        return Util.onPreferenceTreeClick(this, preference) || super.onPreferenceTreeClick(preference)
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (preference.key == null) {
            preference.key = UUID.randomUUID().toString()
        }
        var f: DialogFragment? = null
        if (preference is CustomDialogPreferenceCompat) {
            f = CustomDialogPreferenceCompat.CustomPreferenceDialogFragment.newInstance(preference.key)
        } else {
            super.onDisplayPreferenceDialog(preference)
            return
        }
        f?.setTargetFragment(this, 0)
        f?.show(parentFragmentManager, "dialog_preference")
    }

    fun setMasterDependencyState(enabled: Boolean) {
        mMasterDependencyEnabled = enabled
        if (!mInitialized) {
            return
        }
        val preferenceScreen = preferenceScreen
        for (i in 0 until preferenceScreen.preferenceCount) {
            val preference = preferenceScreen.getPreference(i)
            if (preference.dependency == null) {
                preference.isEnabled = enabled
            }
        }
        if (enabled) {
            onMasterDependencyEnabled()
        }
    }

    fun registerObserverIfNeeded() {
        if (!mIsDataSetObserverRegistered) {
            (mCurrentRootAdapter as? Adapter<*>)?.unregisterAdapterDataObserver(mDataSetObserver)
            mCurrentRootAdapter = listView?.adapter
            (mCurrentRootAdapter as? Adapter<*>)?.registerAdapterDataObserver(mDataSetObserver)
            mIsDataSetObserverRegistered = true
            onDataSetChanged()
        }
    }

    fun unregisterObserverIfNeeded() {
        if (mIsDataSetObserverRegistered) {
            (mCurrentRootAdapter as? Adapter<*>)?.unregisterAdapterDataObserver(mDataSetObserver)
            mCurrentRootAdapter = null
            mIsDataSetObserverRegistered = false
        }
    }

    fun highlightPreferenceIfNeeded() {
        if (!isAdded) {
            return
        }
        mAdapter?.requestHighlight(view, listView)
    }

    protected fun onDataSetChanged() {
        highlightPreferenceIfNeeded()
    }

    override fun onCreateLayoutManager(): LayoutManager {
        mLayoutManager = LinearLayoutManager(requireContext())
        return mLayoutManager
    }

    override fun onCreateAdapter(preferenceScreen: PreferenceScreen): Adapter<*> {
        val arguments = arguments
        mAdapter = HighlightablePreferenceGroupAdapter(
            preferenceScreen,
            arguments?.getString(SettingsActivity.EXTRA_PREFERENCE_KEY),
            mPreferenceHighlighted
        )
        return mAdapter!!
    }

    protected fun isMasterDependencyEnabled(): Boolean {
        return mMasterDependencyEnabled != null && mMasterDependencyEnabled!!
    }

    protected fun getContentResolver(): ContentResolver {
        return requireActivity().contentResolver
    }

    protected open fun onMasterDependencyEnabled() {}

    companion object {
        private const val TAG = "BaseSettingsFragment"
        private const val SAVE_HIGHLIGHTED_KEY = "android:preference_highlighted"
    }
}

