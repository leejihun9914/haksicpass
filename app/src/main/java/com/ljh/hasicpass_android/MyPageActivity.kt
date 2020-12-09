package com.ljh.hasicpass_android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_my_page.*

class MyPageActivity : AppCompatActivity() {
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        auth = FirebaseAuth.getInstance()
        val database = Firebase.database
        val myRef = database.getReference("users")
        val user = auth.currentUser

        myRef.child(user!!.uid).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currentUser = dataSnapshot.getValue<User>()
                var str = ""
                for (i in 1..currentUser!!.비밀번호!!.length)
                    str += '*'
                tv_mp_pwd.text = str
                tv_mp_name.text = currentUser.이름
                tv_mp_email.text = currentUser.이메일
                tv_mp_pwd.text = str
                tv_mp_tel.text = currentUser.전화번호
                
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        //뒤로가기 버튼
        btn_mp_back.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }
}