package com.zacharee1.kinematics.views

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.core.text.HtmlCompat

class HtmlTextView : TextView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(
                if (text != null) HtmlCompat.fromHtml(text.toString(), 0) else text,
                type
        )
    }
}