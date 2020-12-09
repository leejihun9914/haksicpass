//접근 권한 부여
package com.ljh.hasicpass_android

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import java.util.ArrayList

class LaunchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)

        TedPermission.with(this)
            .setPermissionListener(object : PermissionListener{
                override fun onPermissionGranted() {
                    startActivity(Intent(this@LaunchActivity, CameraActivity::class.java))
                    finish()
                }

                override fun onPermissionDenied(deniedPermissions: ArrayList<String>?) {
                    for(i in deniedPermissions!!)
                        Log.d("Error", i)
                }

            })
            .setDeniedMessage("페이스로그인을 이용하시려면 권한을 허가하셔야합니다.")
            .setPermissions(Manifest.permission.CAMERA)
            .check()
    }
}