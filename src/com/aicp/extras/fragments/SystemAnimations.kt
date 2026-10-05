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
package com.aicp.extras.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.BitmapFactory
import android.graphics.Point
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.os.UserHandle
import android.provider.Settings
import androidx.preference.ListPreference
import androidx.preference.Preference
import android.util.Log
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Enumeration
import java.util.List
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

import com.aicp.extras.BaseSettingsFragment
import com.aicp.extras.R
import com.aicp.extras.utils.FinishingAnimationDrawable
import com.aicp.extras.utils.SuShell
import com.aicp.extras.utils.SuTask
import com.aicp.extras.utils.Util

class SystemAnimations : BaseSettingsFragment() {

    private val TAG = SystemAnimations::class.java.simpleName


    override fun getPreferenceResource(): Int {
        return R.xml.system_animations
    }
}
