package com.zacharee1.kinematics.utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

val Context.sharedPreferences: SharedPreferences
    get() = PreferenceManager.getDefaultSharedPreferences(this)