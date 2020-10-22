/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.aicp.extras.preference;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.SystemProperties;
import androidx.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Switch;

import com.aicp.extras.R;

/**
 * A custom preference that provides inline switch toggle. It has a mandatory field for title, and
 * optional fields for icon and sub-text.
 */
public class MasterSwitchPreference extends TwoTargetPreference {

    private Context mContext;
    private Switch mSwitch;
    private boolean mChecked;
    private boolean mEnableSwitch = true;
    private boolean mDefaultValue;

    private MasterSwitchPreferenceDependencyHandler mDependencyHandler;
    private int mThereCanBeOnlyOneGroupId = 0;
    private boolean mThereShouldBeOne = false;

    private View mTwoTargetDivider;
    private View mWidgetView;
    private View mBaseView;
    private View mMainView;
    private boolean mPlainSwitch = false;

    public MasterSwitchPreference(Context context, AttributeSet attrs,
                                  int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    public MasterSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public MasterSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MasterSwitchPreference(Context context) {
        super(context);
        init(context, null);
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.MasterSwitchPreference);
        mThereCanBeOnlyOneGroupId = a.getInt(
                R.styleable.MasterSwitchPreference_thereCanBeOnlyOneGroupId,
                mThereCanBeOnlyOneGroupId);
        mThereShouldBeOne = a.getBoolean(R.styleable.MasterSwitchPreference_thereShouldBeOneSwitch,
                mThereShouldBeOne);
        setPlainSwitch(
                a.getBoolean(R.styleable.MasterSwitchPreference_plainSwitch,
                    mPlainSwitch));
        a.recycle();
    }

    @Override
    protected int getSecondTargetResId() {
        return R.layout.preference_widget_master_switch;
    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSwitch != null && !mSwitch.isEnabled()) {
                    return;
                }
                if (!mChecked) {
                    mDependencyHandler.onEnablePref(mThereCanBeOnlyOneGroupId, getKey());
                } else if (mDependencyHandler != null && mThereShouldBeOne &&
                        !mDependencyHandler.isAnotherEnabled(
                                mThereCanBeOnlyOneGroupId, getKey())) {
                    // It might not be safe to disable, so ask the user to make sure
                    mDependencyHandler.showConfirmDisableDialog(mContext,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Continue with disabling the preference
                                    setChecked(false);
                                    if (!callChangeListener(mChecked)) {
                                        setChecked(!mChecked);
                                    } else {
                                        persistBoolean(mChecked);
                                    }
                                }
                            },
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Only close dialog
                                }
                            });
                    return;
                }
                setChecked(!mChecked);
                if (!callChangeListener(mChecked)) {
                    setChecked(!mChecked);
                } else {
                    persistBoolean(mChecked);
                }
            }
        };


    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        mWidgetView = holder.findViewById(android.R.id.widget_frame);
        mBaseView = holder.itemView;
        mMainView = holder.findViewById(R.id.main_view);
        if (mWidgetView != null) {
            mWidgetView.setOnClickListener(mClickListener);
        }

        mSwitch = (Switch) holder.findViewById(R.id.switchWidget);
        if (mSwitch != null) {
            mSwitch.setContentDescription(getTitle());
            mSwitch.setChecked(mChecked);
            mSwitch.setEnabled(mEnableSwitch);
        }
        mTwoTargetDivider = holder.findViewById(R.id.two_target_divider);
        setPlainSwitch(mPlainSwitch);
    }

    @Override
    protected void onClick() {
        super.onClick();

        if (mPlainSwitch) {
            mClickListener.onClick(null);
        }
    }


    public void setDependencyHandler(MasterSwitchPreferenceDependencyHandler dependencyHandler) {
        mDependencyHandler = dependencyHandler;
    }

    public void setThereCanBeOnlyOneGroupId(int id) {
        mThereCanBeOnlyOneGroupId = id;
    }

    public int getThereCanBeOnlyOneGroupId() {
        return mThereCanBeOnlyOneGroupId;
    }

    public void setThereShouldBeOneSwitch(boolean enabled) {
        mThereShouldBeOne = enabled;
    }

    public boolean getThereShouldBeOneSwitch() {
        return mThereShouldBeOne;
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
        if (mSwitch != null) {
            mSwitch.setChecked(checked);
        }
    }

    public void setCheckedPersisting(boolean checked) {
        setChecked(checked);
        persistBoolean(checked);
    }

    public void setSwitchEnabled(boolean enabled) {
        mEnableSwitch = enabled;
        if (mSwitch != null) {
            mSwitch.setEnabled(enabled);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        // This is called in super constructor, so we cannot load required
        // attrs for this method from init() -> do here (and use Preference stylables only)
        String systemPropDefaultOverride =
                a.getString(R.styleable.Preference_systemPropDefaultOverride);

        if (systemPropDefaultOverride != null) {
            int sep1 = systemPropDefaultOverride.indexOf('?');
            int sep2 = systemPropDefaultOverride.indexOf(':');
            String override = SystemProperties.get(systemPropDefaultOverride.substring(0, sep1));
            String onValue = systemPropDefaultOverride.substring(sep1+1, sep2);
            String offValue = systemPropDefaultOverride.substring(sep2+1);
            if (onValue.equals(override)) {
                return true;
            } else if (offValue.equals(override)) {
                return false;
            } // else: don't override
        }
        return mDefaultValue = a.getBoolean(index, false);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setChecked(restoreValue ? getPersistedBoolean((Boolean) defaultValue)
                : (Boolean) defaultValue);
    }

    /**
     * Call from outside when value might have changed.
     */
    void reloadValue() {
        boolean newValue = getPersistedBoolean(mChecked);
        if (newValue != mChecked) {
            // Update listener so it knows the value has changed e.g. on resume,
            // but ignore return result: we don't allow listener to prevent change
            // since it already has changed
            callChangeListener(newValue);
            // Update UI
            setChecked(newValue);
        }
    }

    /**
     * Get default value for external use.
     */
    public boolean getDefaultValue() {
        return mDefaultValue;
    }

    public void setPlainSwitch(boolean plainSwitch) {
        mPlainSwitch = plainSwitch;
        if (mTwoTargetDivider != null) {
            mTwoTargetDivider.setVisibility(plainSwitch ? View.GONE : View.VISIBLE);
        }
        int[] attrs = new int[] {
            android.R.attr.selectableItemBackground,
        };
        TypedArray ta = mContext.getTheme().obtainStyledAttributes(attrs);
        int selectableItemBackground = ta.getResourceId(0, 0);
        ta.recycle();
        if (mWidgetView != null) {
            // When imitating a plain switch, whole preference is clickable,
            // so disable individual clickabilities for better visual appearance
            if (plainSwitch) {
                mWidgetView.setClickable(false);
                mWidgetView.setBackgroundColor(Color.TRANSPARENT);
                mBaseView.setBackgroundResource(selectableItemBackground);
                mMainView.setBackgroundColor(Color.TRANSPARENT);
            } else {
                mWidgetView.setClickable(true);
                mWidgetView.setBackground(null);
                mBaseView.setBackgroundColor(Color.TRANSPARENT);
                mMainView.setBackgroundResource(selectableItemBackground);
            }
        }
    }
}
