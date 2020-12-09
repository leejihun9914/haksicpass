//포인트 충전 페이지
package com.ljh.hasicpass_android

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_charge.*

class ChargeActivity : AppCompatActivity() {
    private lateinit var auth : FirebaseAuth
    var point = 0
    private var check = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charge)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        auth = FirebaseAuth.getInstance()
        val database = Firebase.database
        val myRef = database.getReference("users")
        val user = auth.currentUser

        myRef.child(user!!.uid).child("포인트").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<Int>()
                point = value!!
                tv_apoint.text = point.plus(Integer.parseInt(et_c_point.text.toString())).toString()+" P"
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })


        //메인페이지로 돌아가기
        btn_back2.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        //금액 추가 버튼
        btn_add_10k.setOnClickListener {
            addedPrice(10000)
        }
        btn_add_5k.setOnClickListener {
            addedPrice(5000)
        }
        btn_add_1k.setOnClickListener {
            addedPrice(1000)
        }

        //결제수단
        //무통장 입금
        btn_bank.setOnClickListener {
            creditLayout.visibility = View.INVISIBLE
            recolor()
            layout_bank.setBackgroundResource(R.drawable.check)
            check = "1"
            bankLayout.visibility = View.VISIBLE
        }
        //신용카드
        btn_creditcard.setOnClickListener {
            bankLayout.visibility = View.INVISIBLE
            recolor()
            Toast.makeText(this, "미구현", Toast.LENGTH_SHORT).show()
            layout_credit.setBackgroundResource(R.drawable.check)
            check = "2"
            creditLayout.visibility = View.VISIBLE
        }

        //충전버튼
        btn_c_charge.setOnClickListener {
            if (et_c_point.text.toString() == "0") {
                Toast.makeText(this, "충전금액을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
            if (check != "1" && check != "2") {
                Toast.makeText(this, "결제수단을 선택해주세요.", Toast.LENGTH_SHORT).show()
            }
            if(!cb_agree.isChecked){
                Toast.makeText(this, "약관에 동의해주세요.", Toast.LENGTH_SHORT).show()
            }else {
                val amount = point + Integer.parseInt(et_c_point.text.toString())
                myRef.child(user.uid).child("포인트").setValue(amount)
                finish()
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
            }
        }
    }

    //결제수단 테두리 원상복귀
    private fun recolor(){
        layout_bank.setBackgroundColor(Color.WHITE)
        layout_credit.setBackgroundColor(Color.WHITE)
    }

    @SuppressLint("SetTextI18n")
    private fun addedPrice(add : Int){
        tv_cpoint.visibility = View.VISIBLE
        tv_apoint.visibility = View.VISIBLE
        var total = Integer.parseInt(et_c_point.text.toString())
        total += add
        et_c_point.setText(total.toString())
        tv_apoint.text = point.plus(Integer.parseInt(et_c_point.text.toString())).toString()+" P"
    }
}