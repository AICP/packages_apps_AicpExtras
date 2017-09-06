package com.lordclockan.aicpextras.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lordclockan.R;

public class SeekBarPreferenceCham extends Preference implements SeekBar.OnSeekBarChangeListener {

    private final String TAG = getClass().getName();

    private static final String ANDROIDNS = "http://schemas.android.com/apk/res/android";
    private static final String AICPEXTRAS = "http://schemas.android.com/apk/res/com.lordclockan";
    private static final int DEFAULT_VALUE = 50;

    private int mMaxValue      = 100;
    private int mMinValue      = 0;
    private int mInterval      = 1;
    private int mDefaultValue  = -1;
    private int mCurrentValue;
    private String mUnitsLeft  = "";
    private String mUnitsRight = "";
    private SeekBar mSeekBar;
    private TextView mTitle;
    private ImageView mImagePlus;
    private ImageView mImageMinus;
    private Drawable mProgressThumb;

    private TextView mStatusText;
    private TextView mPopupValue;
    private boolean mTrackingTouch = false;
    private boolean mPopupAdded = false;
    private int mPopupWidth = 0;

    public SeekBarPreferenceCham(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPreference(context, attrs);
    }

    public SeekBarPreferenceCham(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initPreference(context, attrs);
    }

    private void initPreference(Context context, AttributeSet attrs) {
        setValuesFromXml(attrs, context);
        mSeekBar = new SeekBar(context, attrs);
        mSeekBar.setMax(mMaxValue - mMinValue);
        mSeekBar.setOnSeekBarChangeListener(this);
    }

    private void setValuesFromXml(AttributeSet attrs, Context context) {
        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.SeekBarPreference);

        mMaxValue = attrs.getAttributeIntValue(ANDROIDNS, "max", 100);
        mMinValue = attrs.getAttributeIntValue(AICPEXTRAS, "minimum", 0);
        mDefaultValue = attrs.getAttributeIntValue(AICPEXTRAS, "defaultVal", -1);
        mUnitsLeft = getAttributeStringValue(attrs, AICPEXTRAS, "unitsLeft", "");
        mUnitsRight = getAttributeStringValue(attrs, AICPEXTRAS, "unitsRight", "");
        Integer idR = a.getResourceId(R.styleable.SeekBarPreference_unitsRight, 0);
        if (idR > 0) {
            mUnitsRight = context.getResources().getString(idR);
        }
        Integer idL = a.getResourceId(R.styleable.SeekBarPreference_unitsLeft, 0);
        if (idL > 0) {
            mUnitsLeft = context.getResources().getString(idL);
        }
        try {
            String newInterval = attrs.getAttributeValue(AICPEXTRAS, "interval");
            if(newInterval != null)
                mInterval = Integer.parseInt(newInterval);
        }
        catch(Exception e) {
            Log.e(TAG, "Invalid interval value", e);
        }
    }

    private String getAttributeStringValue(AttributeSet attrs, String namespace, String name, String defaultValue) {
        String value = attrs.getAttributeValue(namespace, name);
        if(value == null)
            value = defaultValue;

        return value;
    }

    @Override
    public void onDependencyChanged(Preference dependency, boolean disableDependent) {
        super.onDependencyChanged(dependency, disableDependent);
        this.setShouldDisableView(true);
        if (mTitle != null)
            mTitle.setEnabled(!disableDependent);
        if (mSeekBar != null)
            mSeekBar.setEnabled(!disableDependent);
        if (mImagePlus != null)
            mImagePlus.setEnabled(!disableDependent);
        if (mImageMinus != null)
            mImageMinus.setEnabled(!disableDependent);
    }

    @Override
    protected View onCreateView(ViewGroup parent){
        super.onCreateView(parent);

        RelativeLayout layout =  null;
        try {
            LayoutInflater mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            layout = (RelativeLayout)mInflater.inflate(R.layout.seek_bar_preference, parent, false);
            mTitle = (TextView) layout.findViewById(android.R.id.title);
            mImagePlus = (ImageView) layout.findViewById(R.id.imagePlus);
            mImagePlus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mSeekBar.setProgress((mCurrentValue + mInterval) - mMinValue);
                }
            });
            mImagePlus.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mSeekBar.setProgress((mCurrentValue + (mMaxValue-mMinValue)/10) - mMinValue);
                    return true;
                }
            });
            mImageMinus = (ImageView) layout.findViewById(R.id.imageMinus);
            mImageMinus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mSeekBar.setProgress((mCurrentValue - mInterval) - mMinValue);
                }
            });
            mImageMinus.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mSeekBar.setProgress((mCurrentValue - (mMaxValue-mMinValue)/10) - mMinValue);
                    return true;
                }
            });
            mProgressThumb = mSeekBar.getThumb();
            mPopupValue = (TextView) mInflater.inflate(R.layout.seek_bar_value_popup, null, false);
            mPopupValue.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int width = mPopupValue.getWidth();
                        if (width != mPopupWidth) {
                            mPopupWidth = mPopupValue.getWidth();
                            startUpdateViewValue();
                        }
                    }
            });
            mStatusText = (TextView) layout.findViewById(R.id.seekBarPrefValue);
            mStatusText.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    final String defaultValue = getContext().getString(R.string.seekbar_default_value_set,
                            mDefaultValue);
                    if (mDefaultValue != -1) {
                        if (mDefaultValue != mCurrentValue) {
                            mSeekBar.setProgress(mDefaultValue);
                            Snackbar.make(view, defaultValue,
                                    Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        } else {
                            Snackbar.make(view, R.string.seekbar_default_value_already_set,
                                    Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    } else {
                        Snackbar.make(view, R.string.seekbar_no_default_value,
                                Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                    return true;
                }
            });
        }
        catch(Exception e)
        {
            Log.e(TAG, "Error creating seek bar preference", e);
        }
        return layout;
    }

    @Override
    public void onBindView(View view) {
        super.onBindView(view);
        try
        {
            // move our seekbar to the new view we've been given
            ViewParent oldContainer = mSeekBar.getParent();
            ViewGroup newContainer = (ViewGroup) view.findViewById(R.id.seekBarPrefBarContainer);

            if (oldContainer != newContainer) {
                // remove the seekbar from the old view
                if (oldContainer != null) {
                    ((ViewGroup) oldContainer).removeView(mSeekBar);
                }
                // remove the existing seekbar (there may not be one) and add ours
                newContainer.removeAllViews();
                newContainer.addView(mSeekBar, ViewGroup.LayoutParams.FILL_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        }
        catch(Exception ex) {
            Log.e(TAG, "Error binding view: " + ex.toString());
        }
        updateView(view);
    }

    /**
     * Update a SeekBarPreferenceCham view with our current state
     * @param view
     */
    protected void updateView(View view) {

        try {
            RelativeLayout layout = (RelativeLayout)view;
            mStatusText = (TextView)layout.findViewById(R.id.seekBarPrefValue);
            mStatusText.setText(String.valueOf(mCurrentValue));
            mStatusText.setMinimumWidth(30);
            mSeekBar.setProgress(mCurrentValue - mMinValue);

            TextView unitsRight = (TextView)layout.findViewById(R.id.seekBarPrefUnitsRight);
            unitsRight.setText(mUnitsRight);
            TextView unitsLeft = (TextView)layout.findViewById(R.id.seekBarPrefUnitsLeft);
            unitsLeft.setText(mUnitsLeft);
        }
        catch(Exception e) {
            Log.e(TAG, "Error updating seek bar preference", e);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int newValue = progress + mMinValue;
        if(newValue > mMaxValue)
            newValue = mMaxValue;
        else if(newValue < mMinValue)
            newValue = mMinValue;
        else if(mInterval != 1 && newValue % mInterval != 0)
            newValue = Math.round(((float)newValue)/mInterval)*mInterval;

        // change rejected, revert to the previous value
        if(!callChangeListener(newValue)){
            seekBar.setProgress(mCurrentValue - mMinValue);
            return;
        }
        // change accepted, store it
        mCurrentValue = newValue;
        if (mCurrentValue == mDefaultValue && mDefaultValue != -1) {
            mStatusText.setText(R.string.default_string);
            int redColor = getContext().getResources().getColor(R.color.seekbar_dot_color);
            mProgressThumb.setColorFilter(redColor, PorterDuff.Mode.SRC_IN);
        } else {
            mStatusText.setText(String.valueOf(newValue));
            mProgressThumb.clearColorFilter();
        }

        if (fromUser) {
            startUpdateViewValue();
        } else {
            stopUpdateViewValue();
        }

        persistInt(newValue);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        startUpdateViewValue();
        mTrackingTouch = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        notifyChanged();
        stopUpdateViewValue();
        mTrackingTouch = false;
    }

    @Override
    protected Object onGetDefaultValue(TypedArray ta, int index){
        int defaultValue = ta.getInt(index, DEFAULT_VALUE);
        return defaultValue;
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (restoreValue) {
            try {
                mCurrentValue = getPersistedInt(mCurrentValue);
                return;
            } catch (Exception e) {
                Log.e(TAG, "Invalid persisted value: " + e);
                try {
                    // Delete the invalid value, or next persistInt will fail
                    getSharedPreferences().edit()
                            .remove(getKey()).apply();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                // Don't return yet, try to get default value instead
            }
        }
        int temp = 0;
        try {
            temp = (Integer)defaultValue;
        }
        catch(Exception ex) {
            Log.e(TAG, "Invalid default value: " + defaultValue);
        }
        persistInt(temp);
        mCurrentValue = temp;
    }

    public void setValue(int value) {
        mCurrentValue = value;
    }

    private Drawable getSeekBarThumb() {
        return mProgressThumb;
    }

    private void startUpdateViewValue() {
        if (!mTrackingTouch) return;
        Rect thumbRect = getSeekBarThumb().getBounds();
        int[] seekbarPos = new int[2];
        int[] offsetPos = new int[2];
        mSeekBar.getLocationInWindow(seekbarPos);
        View mainContentView = mSeekBar.getRootView().findViewById(R.id.content_main);
        if (mainContentView == null) {
            mainContentView = mSeekBar.getRootView().findViewById(android.R.id.content);
        }
        if (mainContentView == null) {
            Log.w(TAG, "Could not find main content view to calculate value view offset");
            offsetPos[0] = 0;
            offsetPos[1] = 0;
        } else {
            mainContentView.getLocationInWindow(offsetPos);
        }
        mPopupValue.setText(mUnitsLeft + mCurrentValue + mUnitsRight);
        WindowManager.LayoutParams wp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        wp.gravity = Gravity.LEFT | Gravity.TOP;
        wp.x = thumbRect.centerX() + seekbarPos[0] - offsetPos[0] - (mPopupWidth-thumbRect.width()) / 2 +
                (int) getContext().getResources()
                        .getDimension(R.dimen.seek_bar_preference_cham_value_x_offset);
        wp.y = seekbarPos[1] - offsetPos[1] +
                (int) getContext().getResources()
                        .getDimension(R.dimen.seek_bar_preference_cham_value_y_offset);
        mPopupValue.setLayoutParams(wp);
        if (mPopupAdded) {
            wp = (WindowManager.LayoutParams) mPopupValue.getLayoutParams();
            ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
                    .updateViewLayout(mPopupValue, wp);
        } else {
            ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
                    .addView(mPopupValue, wp);
            mPopupAdded = true;
        }
        mPopupValue.setVisibility(View.VISIBLE);
    }

    private void stopUpdateViewValue() {
        if (!mPopupAdded) return;
        ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).removeView(mPopupValue);
        mPopupAdded = false;
    }
}
