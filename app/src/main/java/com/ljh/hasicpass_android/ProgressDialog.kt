package com.ljh.hasicpass_android

import android.app.Dialog
import android.content.Context
import android.view.Window

class ProgressDialog(context: Context): Dialog(context) {
    init{
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_camera)
    }
}