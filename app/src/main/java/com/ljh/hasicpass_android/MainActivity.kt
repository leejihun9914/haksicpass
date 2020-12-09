//로그인 페이지
package com.ljh.hasicpass_android

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(){
    private lateinit var auth: FirebaseAuth
    private lateinit var customProgressDialog : ProgressDialog
    private var time3: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        auth = FirebaseAuth.getInstance()

        customProgressDialog = ProgressDialog(this)
        customProgressDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        customProgressDialog.setCancelable(false)

        //회원가입
        btn_register.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        //로그인
        btn_login.setOnClickListener {
            if (et_id.text.toString().isEmpty() || et_password.text.toString().isEmpty()){
                Toast.makeText(this, "아이디와 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
            } else {
                customProgressDialog.show()
                auth.signInWithEmailAndPassword(et_id.text.toString(), et_password.text.toString())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            customProgressDialog.dismiss()
                            val intent = Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                        } else {
                            customProgressDialog.dismiss()
                            tv_login_check.text = "아이디 혹은 비밀번호가 일치하지 않습니다"
                            et_id.setText("")
                            et_password.setText("")
                        }
                    }
            }
        }
        //페이스 로그인
        btn_face_login.setOnClickListener {
            val intent = Intent(this, LaunchActivity::class.java)
            startActivity(intent)
        }
    }
    //뒤로가기 방지
    override fun onBackPressed() {
        val time1 = System.currentTimeMillis()
        val time2 = time1 - time3
        if (time2 in 0..2000) {
            finish()
        }
        else {
            time3 = time1
            Toast.makeText(applicationContext, "한번 더 누르시면 종료됩니다.",Toast.LENGTH_SHORT).show()
        }
    }
}