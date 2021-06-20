/*
 * Copyright (C) 2020 crDroid Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aicp.extras.preference;

import android.content.Context;
import android.content.res.ColorStateList;
import android.provider.Settings;
import android.util.AttributeSet;
import android.widget.ImageButton;

import com.android.settingslib.Utils;
import com.android.settingslib.widget.LayoutPreference;

import com.aicp.extras.R;

import java.util.ArrayList;

// TODO Switch to ThemePicker's CustomizationOption
public class FODIconPicker extends LayoutPreference {

    private Context mContext;

    private ArrayList<ImageButton> mButtons = new ArrayList<ImageButton>();

    private static final String TAG = "FODIconPicker";

    public FODIconPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setView();
    }

    public FODIconPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void setView() {
        mButtons.add(findViewById(R.id.fodicon0_button));
        mButtons.add(findViewById(R.id.fodicon1_button));
        mButtons.add(findViewById(R.id.fodicon2_button));
        mButtons.add(findViewById(R.id.fodicon3_button));
        mButtons.add(findViewById(R.id.fodicon4_button));
        mButtons.add(findViewById(R.id.fodicon5_button));
        mButtons.add(findViewById(R.id.fodicon6_button));
        mButtons.add(findViewById(R.id.fodicon7_button));
        mButtons.add(findViewById(R.id.fodicon8_button));
        mButtons.add(findViewById(R.id.fodicon9_button));
        mButtons.add(findViewById(R.id.fodicon10_button));
        mButtons.add(findViewById(R.id.fodicon11_button));
        mButtons.add(findViewById(R.id.fodicon12_button));
        mButtons.add(findViewById(R.id.fodicon13_button));
        mButtons.add(findViewById(R.id.fodicon14_button));
        mButtons.add(findViewById(R.id.fodicon15_button));
        mButtons.add(findViewById(R.id.fodicon16_button));
        mButtons.add(findViewById(R.id.fodicon17_button));
        mButtons.add(findViewById(R.id.fodicon18_button));
        mButtons.add(findViewById(R.id.fodicon19_button));
        mButtons.add(findViewById(R.id.fodicon20_button));
        mButtons.add(findViewById(R.id.fodicon21_button));
        mButtons.add(findViewById(R.id.fodicon22_button));
        mButtons.add(findViewById(R.id.fodicon23_button));

        updateHighlightedItem(mButtons.get(Settings.System.getInt(
                mContext.getContentResolver(), Settings.System.FOD_ICON, 0)));

        for (int i = 0; i < mButtons.size(); i++) {
            final ImageButton tempButton = mButtons.get(i);
            final int finalI = i;
            tempButton.setOnClickListener(view -> {
                updateSettings(finalI);
                updateHighlightedItem(tempButton);

            });
        }
    }

    private void updateSettings(int fodicon) {
        Settings.System.putInt(mContext.getContentResolver(), Settings.System.FOD_ICON, fodicon);
    }

    private void updateHighlightedItem(ImageButton activebutton) {
        int defaultcolor = mContext.getResources().getColor(R.color.fod_item_background_stroke_color);
        ColorStateList defaulttint = ColorStateList.valueOf(defaultcolor);
        for (int i = 0; i < mButtons.size(); i++) {
            mButtons.get(i).setBackgroundTintList(defaulttint);
        }
        activebutton.setBackgroundTintList(Utils.getColorAttr(getContext(), android.R.attr.colorAccent));
    }
}
