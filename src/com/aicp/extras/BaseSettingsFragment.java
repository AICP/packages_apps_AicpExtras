/*
 * Copyright (C) 2017 AICP
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


package com.aicp.extras;

import static com.aicp.extras.SettingsActivity.EXTRA_PREFERENCE_KEY;

import android.app.DialogFragment;
import android.content.ContentResolver;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.settingslib.CustomDialogPreferenceCompat;

import com.aicp.extras.utils.Util;
import com.aicp.extras.widget.HighlightablePreferenceGroupAdapter;
import com.aicp.gear.preference.AicpPreferenceFragment;

import java.util.UUID;

public abstract class BaseSettingsFragment extends AicpPreferenceFragment {

    private static final String TAG = BaseSettingsFragment.class.getSimpleName();

    private static final String SAVE_HIGHLIGHTED_KEY = "android:preference_highlighted";

    private Boolean mMasterDependencyEnabled;
    private boolean mInitialized = false;

    private LinearLayoutManager mLayoutManager;

    private HighlightablePreferenceGroupAdapter mAdapter;
    private boolean mPreferenceHighlighted = false;

    private RecyclerView.Adapter mCurrentRootAdapter;
    private boolean mIsDataSetObserverRegistered = false;
    private RecyclerView.AdapterDataObserver mDataSetObserver =
            new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    onDataSetChanged();
                }

                @Override
                public void onItemRangeChanged(int positionStart, int itemCount) {
                    onDataSetChanged();
                }

                @Override
                public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
                    onDataSetChanged();
                }

                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    onDataSetChanged();
                }

                @Override
                public void onItemRangeRemoved(int positionStart, int itemCount) {
                    onDataSetChanged();
                }

                @Override
                public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                    onDataSetChanged();
                }
            };

    protected abstract int getPreferenceResource();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(getPreferenceResource());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInitialized = true;
        if (savedInstanceState != null) {
            mPreferenceHighlighted = savedInstanceState.getBoolean(SAVE_HIGHLIGHTED_KEY);
        }
        //HighlightablePreferenceGroupAdapter.adjustInitialExpandedChildCount(this /* host */);
        if (mMasterDependencyEnabled != null) {
            setMasterDependencyState(mMasterDependencyEnabled);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mAdapter != null) {
            outState.putBoolean(SAVE_HIGHLIGHTED_KEY, mAdapter.isHighlightRequested());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        highlightPreferenceIfNeeded();
    }

    @Override
    protected void onBindPreferences() {
        registerObserverIfNeeded();
    }

    @Override
    protected void onUnbindPreferences() {
        unregisterObserverIfNeeded();
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return Util.onPreferenceTreeClick(this, preference)
                || super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (preference.getKey() == null) {
            // Auto-key preferences that don't have a key, so the dialog can find them.
            preference.setKey(UUID.randomUUID().toString());
        }
        DialogFragment f = null;
        if (preference instanceof CustomDialogPreferenceCompat) {
            f = CustomDialogPreferenceCompat.CustomPreferenceDialogFragment
                    .newInstance(preference.getKey());
        } else {
            super.onDisplayPreferenceDialog(preference);
            return;
        }
        f.setTargetFragment(this, 0);
        f.show(getFragmentManager(), "dialog_preference");
    }

    public void setMasterDependencyState(boolean enabled) {
        mMasterDependencyEnabled = enabled;
        if (!mInitialized) {
            return;
        }
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        for (int i = 0; i < preferenceScreen.getPreferenceCount(); i++) {
            Preference preference = preferenceScreen.getPreference(i);
            // Preferences that have a dependency declared will be disabled by disabling
            // the dependency, so only disable those that don't have one
            if (preference.getDependency() == null) {
                preference.setEnabled(enabled);
            }
        }
    }

    public void registerObserverIfNeeded() {
        if (!mIsDataSetObserverRegistered) {
            if (mCurrentRootAdapter != null) {
                mCurrentRootAdapter.unregisterAdapterDataObserver(mDataSetObserver);
            }
            mCurrentRootAdapter = getListView().getAdapter();
            mCurrentRootAdapter.registerAdapterDataObserver(mDataSetObserver);
            mIsDataSetObserverRegistered = true;
            onDataSetChanged();
        }
    }

    public void unregisterObserverIfNeeded() {
        if (mIsDataSetObserverRegistered) {
            if (mCurrentRootAdapter != null) {
                mCurrentRootAdapter.unregisterAdapterDataObserver(mDataSetObserver);
                mCurrentRootAdapter = null;
            }
            mIsDataSetObserverRegistered = false;
        }
    }

    public void highlightPreferenceIfNeeded() {
        if (!isAdded()) {
            return;
        }
        if (mAdapter != null) {
            mAdapter.requestHighlight(getView(), getListView());
        }
    }

    protected void onDataSetChanged() {
        highlightPreferenceIfNeeded();
        //updateEmptyView();
    }

    @Override
    public RecyclerView.LayoutManager onCreateLayoutManager() {
        mLayoutManager = new LinearLayoutManager(getContext());
        return mLayoutManager;
    }

    @Override
    protected RecyclerView.Adapter onCreateAdapter(PreferenceScreen preferenceScreen) {
        final Bundle arguments = getArguments();
        mAdapter = new HighlightablePreferenceGroupAdapter(preferenceScreen,
                arguments == null ? null : arguments.getString(EXTRA_PREFERENCE_KEY),
                mPreferenceHighlighted);
        return mAdapter;
    }

    protected boolean isMasterDependencyEnabled() {
        return mMasterDependencyEnabled;
    }

    protected ContentResolver getContentResolver() {
        return getActivity().getContentResolver();
    }
}
