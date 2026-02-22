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
package com.aicp.extras.utils

import android.content.Context
import android.os.AsyncTask
import android.widget.Toast
import com.aicp.extras.R

abstract class SuTask<Params>(private val mContext: Context) : AsyncTask<Params, Void, Boolean>() {

    @Throws(SuShell.SuDeniedException::class)
    protected abstract fun sudoInBackground(vararg params: Params)

    override fun doInBackground(vararg params: Params): Boolean {
        return try {
            sudoInBackground(*params)
            true
        } catch (e: SuShell.SuDeniedException) {
            false
        }
    }

    override fun onPostExecute(result: Boolean) {
        super.onPostExecute(result)
        if (!result) {
            Toast.makeText(
                mContext,
                mContext.getString(R.string.cannot_get_su),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

