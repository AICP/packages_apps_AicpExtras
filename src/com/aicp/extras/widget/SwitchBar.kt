/*
 * Copyright (C) 2014 The Android Open Source Project
 * Copyright (C) 2026 AICP
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
package com.aicp.extras.widget

import android.content.Context
import android.content.res.TypedArray
import android.os.Parcel
import android.os.Parcelable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.TextAppearanceSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.StyleableRes
import com.aicp.extras.R

class SwitchBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes), CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    interface OnSwitchChangeListener {
        fun onSwitchChanged(switchView: CompoundButton, isChecked: Boolean)
    }

    companion object {
        @StyleableRes
        private val XML_ATTRIBUTES = intArrayOf(
            R.attr.switchBarMarginStart,
            R.attr.switchBarMarginEnd,
            R.attr.switchBarBackgroundColor,
            R.attr.switchBarBackgroundActivatedColor
        )
    }

    private val mSwitchChangeListeners: MutableList<OnSwitchChangeListener> = ArrayList()
    private val mSummarySpan: TextAppearanceSpan

    private lateinit var mSwitch: ToggleSwitch
    private lateinit var mTextView: TextView
    private var mLabel: String
    private var mSummary: String? = null
    @ColorInt
    private var mBackgroundColor: Int
    @ColorInt
    private var mBackgroundActivatedColor: Int

    init {
        LayoutInflater.from(context).inflate(R.layout.switch_bar, this)

        val a = context.obtainStyledAttributes(attrs, XML_ATTRIBUTES)
        val switchBarMarginStart = a.getDimension(0, 0f).toInt()
        val switchBarMarginEnd = a.getDimension(1, 0f).toInt()
        mBackgroundColor = a.getColor(2, 0)
        mBackgroundActivatedColor = a.getColor(3, 0)
        a.recycle()

        mTextView = findViewById(R.id.switch_text)
        mTextView.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        mLabel = context.getString(R.string.switch_off_text)
        mSummarySpan = TextAppearanceSpan(context, R.style.TextAppearance_Small_SwitchBar)
        updateText()
        val lp = mTextView.layoutParams as MarginLayoutParams
        lp.marginStart = switchBarMarginStart

        mSwitch = findViewById(R.id.switch_widget)
        mSwitch.isSaveEnabled = false
        mSwitch.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        val switchLp = mSwitch.layoutParams as MarginLayoutParams
        switchLp.marginEnd = switchBarMarginEnd
        setBackgroundColor(mBackgroundColor)

        addOnSwitchChangeListener(object : OnSwitchChangeListener {
            override fun onSwitchChanged(switchView: CompoundButton, isChecked: Boolean) {
                setTextViewLabelAndBackground(isChecked)
            }
        })

        setOnClickListener(this)

        // Default is hide
        visibility = View.GONE
    }

    private fun setTextViewLabelAndBackground(isChecked: Boolean) {
        mLabel = context.getString(if (isChecked) R.string.switch_on_text else R.string.switch_off_text)
        setBackgroundColor(if (isChecked) mBackgroundActivatedColor else mBackgroundColor)
        updateText()
    }

    fun setSummary(summary: String?) {
        mSummary = summary
        updateText()
    }

    private fun updateText() {
        if (TextUtils.isEmpty(mSummary)) {
            mTextView.text = mLabel
            return
        }
        val ssb = SpannableStringBuilder(mLabel).append('\n')
        val start = ssb.length
        ssb.append(mSummary)
        ssb.setSpan(mSummarySpan, start, ssb.length, 0)
        mTextView.text = ssb
    }

    fun setChecked(checked: Boolean) {
        setTextViewLabelAndBackground(checked)
        mSwitch.isChecked = checked
    }

    fun setCheckedInternal(checked: Boolean) {
        setTextViewLabelAndBackground(checked)
        mSwitch.setCheckedInternal(checked)
    }

    fun isChecked(): Boolean {
        return mSwitch.isChecked
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        mTextView.isEnabled = enabled
        mSwitch.isEnabled = enabled
    }

    fun show() {
        if (!isShowing()) {
            visibility = View.VISIBLE
            mSwitch.setOnCheckedChangeListener(this)
        }
    }

    fun hide() {
        if (isShowing()) {
            visibility = View.GONE
            mSwitch.setOnCheckedChangeListener(null)
        }
    }

    fun isShowing(): Boolean {
        return visibility == View.VISIBLE
    }

    override fun onClick(v: View) {
        val isChecked = !mSwitch.isChecked
        setChecked(isChecked)
    }

    private fun propagateChecked(isChecked: Boolean) {
        for (listener in mSwitchChangeListeners) {
            listener.onSwitchChanged(mSwitch, isChecked)
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        propagateChecked(isChecked)
    }

    fun addOnSwitchChangeListener(listener: OnSwitchChangeListener) {
        if (mSwitchChangeListeners.contains(listener)) {
            throw IllegalStateException("Cannot add twice the same OnSwitchChangeListener")
        }
        mSwitchChangeListeners.add(listener)
    }

    fun removeOnSwitchChangeListener(listener: OnSwitchChangeListener) {
        if (!mSwitchChangeListeners.contains(listener)) {
            throw IllegalStateException("Cannot remove OnSwitchChangeListener")
        }
        mSwitchChangeListeners.remove(listener)
    }

    class SavedState : BaseSavedState {
        var isChecked: Boolean = false
        var isVisible: Boolean = false

        constructor(superState: Parcelable?) : super(superState)

        private constructor(`in`: Parcel) : super(`in`) {
            isChecked = `in`.readInt() != 0
            isVisible = `in`.readInt() != 0
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(if (isChecked) 1 else 0)
            out.writeInt(if (isVisible) 1 else 0)
        }

        override fun toString(): String {
            return ("SwitchBar.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " checked=" + isChecked
                    + " visible=" + isVisible + "}")
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(`in`: Parcel): SavedState {
                    return SavedState(`in`)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val ss = SavedState(superState)
        ss.isChecked = mSwitch.isChecked
        ss.isVisible = isShowing()
        return ss
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.superState)
        mSwitch.setCheckedInternal(ss.isChecked)
        setTextViewLabelAndBackground(ss.isChecked)
        visibility = if (ss.isVisible) View.VISIBLE else View.GONE
        mSwitch.setOnCheckedChangeListener(if (ss.isVisible) this else null)
        requestLayout()
    }

    override fun getAccessibilityClassName(): CharSequence {
        return CompoundButton::class.java.name
    }

    override fun onRequestSendAccessibilityEvent(child: View, event: AccessibilityEvent): Boolean {
        event.setSource(this)
        return true
    }

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.text = mTextView.text
        info.isCheckable = true
        info.isChecked = mSwitch.isChecked
    }

    override fun onInitializeAccessibilityEvent(event: AccessibilityEvent) {
        super.onInitializeAccessibilityEvent(event)
        event.contentDescription = mTextView.text
        event.isChecked = mSwitch.isChecked
    }
}

