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
import android.content.res.TypedArray;
import android.graphics.Color;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.aicp.extras.R;
import com.android.settingslib.Utils;
import com.android.settingslib.widget.LayoutPreference;

public class FODIconPicker extends LayoutPreference {

    private Context mContext;

    private static ImageButton Button0;
    private static ImageButton Button1;
    private static ImageButton Button2;
    private static ImageButton Button3;
    private static ImageButton Button4;
    private static ImageButton Button5;
    private static ImageButton Button6;
    private static ImageButton Button7;
    private static ImageButton Button8;
    private static ImageButton Button9;
    private static ImageButton Button10;
    private static ImageButton Button11;
    private static ImageButton Button12;
    private static ImageButton Button13;
    private static ImageButton Button14;
    private static ImageButton Button15;
    private static ImageButton Button16;
    private static ImageButton Button17;
    private static ImageButton Button18;
    private static ImageButton Button19;
    private static ImageButton Button20;
    private static ImageButton Button21;
    private static ImageButton Button22;
    private static ImageButton Button23;

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
        Button0 = findViewById(R.id.fodicon0_button);
        Button1 = findViewById(R.id.fodicon1_button);
        Button2 = findViewById(R.id.fodicon2_button);
        Button3 = findViewById(R.id.fodicon3_button);
        Button4 = findViewById(R.id.fodicon4_button);
        Button5 = findViewById(R.id.fodicon5_button);
        Button6 = findViewById(R.id.fodicon6_button);
        Button7 = findViewById(R.id.fodicon7_button);
        Button8 = findViewById(R.id.fodicon8_button);
        Button9 = findViewById(R.id.fodicon9_button);
        Button10 = findViewById(R.id.fodicon10_button);
        Button11 = findViewById(R.id.fodicon11_button);
        Button12 = findViewById(R.id.fodicon12_button);
        Button13 = findViewById(R.id.fodicon13_button);
        Button14 = findViewById(R.id.fodicon14_button);
        Button15 = findViewById(R.id.fodicon15_button);
        Button16 = findViewById(R.id.fodicon16_button);
        Button17 = findViewById(R.id.fodicon17_button);
        Button18 = findViewById(R.id.fodicon18_button);
        Button19 = findViewById(R.id.fodicon19_button);
        Button20 = findViewById(R.id.fodicon20_button);
        Button21 = findViewById(R.id.fodicon21_button);
        Button22 = findViewById(R.id.fodicon22_button);
        Button23 = findViewById(R.id.fodicon23_button);

        int defaultfodicon = Settings.System.getInt(
                mContext.getContentResolver(), Settings.System.FOD_ICON, 0);

        switch(defaultfodicon){
            case 0:
                updateHighlightedItem(Button0);
                break;
            case 1:
                updateHighlightedItem(Button1);
                break;
            case 2:
                updateHighlightedItem(Button2);
                break;
            case 3:
                updateHighlightedItem(Button3);
                break;
            case 4:
                updateHighlightedItem(Button4);
                break;
            case 5:
                updateHighlightedItem(Button5);
                break;
            case 6:
                updateHighlightedItem(Button6);
                break;
            case 7:
                updateHighlightedItem(Button7);
                break;
            case 8:
                updateHighlightedItem(Button8);
                break;
            case 9:
                updateHighlightedItem(Button9);
                break;
            case 10:
                updateHighlightedItem(Button10);
                break;
            case 11:
                updateHighlightedItem(Button11);
                break;
            case 12:
                updateHighlightedItem(Button12);
                break;
            case 13:
                updateHighlightedItem(Button13);
                break;
            case 14:
                updateHighlightedItem(Button14);
                break;
            case 15:
                updateHighlightedItem(Button15);
                break;
            case 16:
                updateHighlightedItem(Button16);
                break;
            case 17:
                updateHighlightedItem(Button17);
                break;
            case 18:
                updateHighlightedItem(Button18);
                break;
            case 19:
                updateHighlightedItem(Button19);
                break;
            case 20:
                updateHighlightedItem(Button20);
                break;
            case 21:
                updateHighlightedItem(Button21);
                break;
            case 22:
                updateHighlightedItem(Button22);
                break;
            case 23:
                updateHighlightedItem(Button23);
                break;
        }

        Button0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSettings(0);
                updateHighlightedItem(Button0);
            }
        });
        Button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSettings(1);
                updateHighlightedItem(Button1);
            }
        });
        Button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSettings(2);
                updateHighlightedItem(Button2);
            }
        });
        Button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSettings(3);
                updateHighlightedItem(Button3);
            }
        });
        Button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSettings(4);
                updateHighlightedItem(Button4);
            }
        });
        Button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSettings(5);
                updateHighlightedItem(Button5);
            }
        });
        Button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSettings(6);
                updateHighlightedItem(Button6);
            }
        });
        Button7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSettings(7);
                updateHighlightedItem(Button7);
            }
        });
        Button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSettings(8);
                updateHighlightedItem(Button8);
            }
        });
        Button9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSettings(9);
                updateHighlightedItem(Button9);
            }
        });
        Button10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSettings(10);
                updateHighlightedItem(Button10);
            }
        });
        Button11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSettings(11);
                updateHighlightedItem(Button11);
            }
        });
        Button12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSettings(12);
                updateHighlightedItem(Button12);
            }
        });
        Button13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSettings(13);
                updateHighlightedItem(Button13);
            }
        });
        Button14.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSettings(14);
                updateHighlightedItem(Button14);
            }
        });
        Button15.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSettings(15);
                updateHighlightedItem(Button15);
            }
        });
        Button16.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSettings(16);
                updateHighlightedItem(Button16);
            }
        });
        Button17.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSettings(17);
                updateHighlightedItem(Button17);
            }
        });
        Button18.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSettings(18);
                updateHighlightedItem(Button18);
            }
        });
        Button19.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSettings(19);
                updateHighlightedItem(Button19);
            }
        });
        Button20.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSettings(20);
                updateHighlightedItem(Button20);
            }
        });Button21.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSettings(21);
                updateHighlightedItem(Button21);
            }
        });Button22.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSettings(22);
                updateHighlightedItem(Button22);
            }
        });Button23.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSettings(23);
                updateHighlightedItem(Button23);
            }
        });
    }

    private void updateSettings(int fodicon) {
        Settings.System.putInt(mContext.getContentResolver(), Settings.System.FOD_ICON, fodicon);
    }

    private void updateHighlightedItem(ImageButton activebutton) {
        int defaultcolor = mContext.getResources().getColor(R.color.fod_item_background_stroke_color);
        ColorStateList defaulttint = ColorStateList.valueOf(defaultcolor);
        Button0.setBackgroundTintList(defaulttint);
        Button1.setBackgroundTintList(defaulttint);
        Button2.setBackgroundTintList(defaulttint);
        Button3.setBackgroundTintList(defaulttint);
        Button4.setBackgroundTintList(defaulttint);
        Button5.setBackgroundTintList(defaulttint);
        Button6.setBackgroundTintList(defaulttint);
        Button7.setBackgroundTintList(defaulttint);
        Button8.setBackgroundTintList(defaulttint);
        Button9.setBackgroundTintList(defaulttint);
        Button10.setBackgroundTintList(defaulttint);
        Button11.setBackgroundTintList(defaulttint);
        Button12.setBackgroundTintList(defaulttint);
        Button13.setBackgroundTintList(defaulttint);
        Button14.setBackgroundTintList(defaulttint);
        Button15.setBackgroundTintList(defaulttint);
        Button16.setBackgroundTintList(defaulttint);
        Button17.setBackgroundTintList(defaulttint);
        Button18.setBackgroundTintList(defaulttint);
        Button19.setBackgroundTintList(defaulttint);
        Button20.setBackgroundTintList(defaulttint);
        Button21.setBackgroundTintList(defaulttint);
        Button22.setBackgroundTintList(defaulttint);
        Button23.setBackgroundTintList(defaulttint);
        activebutton.setBackgroundTintList(Utils.getColorAttr(getContext(), android.R.attr.colorAccent));
    }
}
