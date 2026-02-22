/*
 * Copyright (C) 2012-2026 AICP
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

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import com.aicp.extras.view.GifView

class HiddenAnimActivity : BaseActivity() {

    private lateinit var gifView: GifView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.animgif_layout)

        gifView = findViewById(R.id.gif_view)
        gifView.setGifAssetPath("anim.gif")
        gifView.setOnLongClickListener { view ->
            val builder = AlertDialog.Builder(this)
            builder.setPositiveButton(R.string.hidden_anim_more_nice) { dialog, which -> }
            val dialog = builder.create()
            val inflater = layoutInflater
            val dialogLayout = inflater.inflate(R.layout.hidden_img_layout, null)
            dialog.setView(dialogLayout)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

            dialog.show()

            dialog.setOnShowListener { d ->
                val image = dialog.findViewById<ImageView>(R.id.hidden_img)
                val icon = BitmapFactory.decodeResource(resources, R.drawable.aicp_cool)
                val imageWidthInPX = image.width.toFloat()

                val layoutParams = LinearLayout.LayoutParams(
                    Math.round(imageWidthInPX),
                    Math.round(imageWidthInPX * icon.height.toFloat() / icon.width.toFloat())
                )
                image.layoutParams = layoutParams
            }
            true
        }
    }
}

