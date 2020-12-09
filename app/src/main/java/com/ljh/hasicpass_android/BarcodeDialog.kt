package com.ljh.hasicpass_android

import android.app.Dialog
import android.content.Context
import android.view.Window
import kotlinx.android.synthetic.main.activity_barcode.*

class BarcodeDialog (context: Context): Dialog(context){
    init{
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_barcode)
        img_a_barcode.setImageResource(R.drawable.noticker)
    }
}