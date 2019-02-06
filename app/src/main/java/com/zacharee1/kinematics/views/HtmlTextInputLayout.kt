package com.zacharee1.kinematics.views

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputLayout
import ru.noties.markwon.Markwon

class HtmlTextInputLayout : TextInputLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    override fun setHint(hint: CharSequence?) {
        super.setHint(
                if (hint != null) Markwon.markdown(context, hint.toString()) else hint
        )
    }

    override fun setHelperText(helperText: CharSequence?) {
        super.setHelperText(
                if (helperText != null) Markwon.markdown(context, helperText.toString()) else helperText
        )
    }
}