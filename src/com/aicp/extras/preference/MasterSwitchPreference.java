package com.aicp.extras.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.widget.SwitchCompat;
import androidx.preference.PreferenceViewHolder;
import androidx.preference.SwitchPreferenceCompat;

import com.aicp.extras.R;

/**
 * MasterSwitchPreference:
 * - Left side triggers fragment callback only
 * - Right side toggles the switch
 * - Divider between text and switch
 * - Persistency + "only-one-enabled" logic
 */
public class MasterSwitchPreference extends SwitchPreferenceCompat {

    private boolean mDefaultValue;
    private MasterSwitchPreferenceDependencyHandler mDependencyHandler;
    private int mThereCanBeOnlyOneGroupId = 0;
    private boolean mThereShouldBeOne = false;

    /** Optional callback for left click navigation */
    public interface OnFragmentClickListener {
        void onClick(MasterSwitchPreference preference);
    }

    private OnFragmentClickListener mOnFragmentClickListener;

    // ---------------- Constructors ----------------

    public MasterSwitchPreference(Context context) {
        super(context);
        init(context, null);
    }

    public MasterSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MasterSwitchPreference(Context context,
                                  AttributeSet attrs,
                                  int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    // ---------------- Initialization ----------------

    private void init(Context context, AttributeSet attrs) {
        setLayoutResource(R.layout.preference_master_switch);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(
                    attrs, R.styleable.MasterSwitchPreference);

            mThereCanBeOnlyOneGroupId = a.getInt(
                    R.styleable.MasterSwitchPreference_thereCanBeOnlyOneGroupId, 0);

            mThereShouldBeOne = a.getBoolean(
                    R.styleable.MasterSwitchPreference_thereShouldBeOneSwitch, false);

            a.recycle();
        }

        // "Only one enabled" logic
        setOnPreferenceChangeListener((preference, newValue) -> {
            boolean checked = (Boolean) newValue;

            if (!checked && mThereShouldBeOne && mDependencyHandler != null &&
                    !mDependencyHandler.isAnotherEnabled(
                            mThereCanBeOnlyOneGroupId, getKey())) {

                mDependencyHandler.showConfirmDisableDialog(
                        getContext(),
                        (dialog, which) -> {
                            setChecked(false);
                            persistBoolean(false);
                        },
                        (dialog, which) -> {
                            // only dismiss dialog
                        });

                return false;
            }

            if (checked && mDependencyHandler != null) {
                mDependencyHandler.onEnablePref(
                        mThereCanBeOnlyOneGroupId, getKey());
            }

            persistBoolean(checked);
            return true;
        });
    }

    // ---------------- View Binding ----------------

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        View mainArea = holder.findViewById(R.id.main_area); // left area
        View divider = holder.findViewById(R.id.divider);

        // Cast switch safely
        View rawSwitch = holder.findViewById(android.R.id.switch_widget);
        if (rawSwitch instanceof SwitchCompat) {
            SwitchCompat switchView = (SwitchCompat) rawSwitch;
            switchView.setClickable(true);
            switchView.setFocusable(true);
        }

        // Left area click → fragment callback only
        if (mainArea != null) {
            mainArea.setOnClickListener(v -> {
                if (mOnFragmentClickListener != null) {
                    mOnFragmentClickListener.onClick(this);
                }
            });
        }

        // Divider visible
        if (divider != null) {
            divider.setVisibility(View.VISIBLE);
        }

        // Disable whole row click
        holder.itemView.setOnClickListener(null);
    }

    // ---------------- Public API ----------------

    public void setCheckedPersisting(boolean checked) {
        setChecked(checked);
        persistBoolean(checked);
    }

    public void reloadValue() {
        boolean newValue = getPersistedBoolean(isChecked());
        if (newValue != isChecked()) {
            callChangeListener(newValue);
            setChecked(newValue);
        }
    }

    public void setDependencyHandler(
            MasterSwitchPreferenceDependencyHandler handler) {
        mDependencyHandler = handler;
    }

    public void setOnFragmentClickListener(OnFragmentClickListener listener) {
        mOnFragmentClickListener = listener;
    }

    public int getThereCanBeOnlyOneGroupId() {
        return mThereCanBeOnlyOneGroupId;
    }

    public boolean getThereShouldBeOneSwitch() {
        return mThereShouldBeOne;
    }

    public boolean getDefaultValue() {
        return mDefaultValue;
    }

    // ---------------- Default Handling ----------------

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        String systemPropDefaultOverride = a.getString(
                R.styleable.Preference_systemPropDefaultOverride);

        if (systemPropDefaultOverride != null) {
            int sep1 = systemPropDefaultOverride.indexOf('?');
            int sep2 = systemPropDefaultOverride.indexOf(':');

            if (sep1 > 0 && sep2 > sep1) {
                String override = SystemProperties.get(
                        systemPropDefaultOverride.substring(0, sep1));

                String onValue = systemPropDefaultOverride.substring(sep1 + 1, sep2);
                String offValue = systemPropDefaultOverride.substring(sep2 + 1);

                if (onValue.equals(override)) return true;
                if (offValue.equals(override)) return false;
            }
        }

        return mDefaultValue = a.getBoolean(index, false);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        boolean value = getPersistedBoolean(
                defaultValue != null && (Boolean) defaultValue);
        setChecked(value);
    }
}

