/*
 * Copyright (C) 2016-2017 SlimRoms Project
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

package slim.utils;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import com.android.internal.util.XmlUtils;

import java.util.HashMap;
import java.util.Map;

public class AttributeHelper {

    class AttributeInfo {
        int resId;
        String name;
        String value;

        AttributeInfo(int resId, String name, String value) {
            this.resId = resId;
            this.name = name;
            this.value = value;
        }
    }

    private int[] mStyleRes;

    Map<Integer, AttributeInfo> mMap = new HashMap<>();

    Resources mResources;
    DisplayMetrics mMetrics;

    public AttributeHelper(Context context, AttributeSet attrs, int[] styleRes) {
        loadMap(attrs);
        mResources = context.getResources();
        mMetrics = mResources.getDisplayMetrics();

        mStyleRes = styleRes;
    }

    private int getIdForIndex(int index) {
        return mStyleRes[index];
    }

    public boolean getBoolean(int index, boolean defValue) {
        AttributeInfo info = mMap.get(getIdForIndex(index));
        if (info == null || TextUtils.isEmpty(info.value)) return defValue;
        if (info.value.equals("true")) {
            return true;
        } else if (info.value.equals("false")) {
            return false;
        } else {
            return defValue;
        }
    }

    public int getColor(int index, int defValue) {
        AttributeInfo info = mMap.get(getIdForIndex(index));
        if (info != null) {
            if (info.value.startsWith("@")) {
                int id = getResourceId(index, defValue);
                try {
                    return mResources.getColor(id);
                } catch (Exception e) {
                    return defValue;
                }
            }
        }
        return defValue;
    }

    public int getDimensionPixelSize(int index, int defValue) {
        AttributeInfo info = mMap.get(getIdForIndex(index));
        if (info != null) {
            if (info.value.endsWith("dp")) {
                String v = info.value.replace("dp", "");
                int iv;
                try {
                    iv = Integer.parseInt(v);
                } catch (Exception e) {
                    return defValue;
                }
                return TypedValue.complexToDimensionPixelSize(iv, mMetrics);
            }
        }
        return defValue;
    }

    public float getFloat(int index, float defValue) {
        AttributeInfo info = mMap.get(getIdForIndex(index));
        try {
            return Float.parseFloat(info.value);
        } catch (Exception e) {
            return defValue;
        }
    }

    public int getInt(int index, int defValue) {
        AttributeInfo info = mMap.get(getIdForIndex(index));
        if (info == null) return defValue;
        if (info.value.startsWith("@")) {
            int id = getResourceId(index, defValue);
            try {
                return mResources.getInteger(id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            return Integer.parseInt(info.value);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                return XmlUtils.convertValueToInt(info.value, defValue);
            } catch (Exception ex) {
                ex.printStackTrace();
                return defValue;
            }
        }
    }

    public int getResourceId(int index, int defValue) {
        AttributeInfo info = mMap.get(getIdForIndex(index));
        try {
            return Integer.parseInt(info.value.substring(1));
        } catch (Exception e) {
            return defValue;
        }
    }

    public String getString(int index) {
        AttributeInfo info = mMap.get(getIdForIndex(index));
        try {
            return info.value;
        } catch (Exception e) {
            return null;
        }
    }

    private void loadMap(AttributeSet attrs) {
        int count = attrs.getAttributeCount();
        for (int i = 0; i < count; i++) {
            String name = attrs.getAttributeName(i);
            String value = attrs.getAttributeValue(i);
            int resId = attrs.getAttributeNameResource(i);
            mMap.put(resId, new AttributeInfo(resId, name, value));
        }
    }
}
