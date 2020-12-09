//메인 홈 페이지
package com.ljh.hasicpass_android

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager.widget.ViewPager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.zxing.BarcodeFormat
import com.google.zxing.oned.Code128Writer
import com.lakue.lakuepopupactivity.PopupActivity
import com.lakue.lakuepopupactivity.PopupGravity
import com.lakue.lakuepopupactivity.PopupResult
import com.lakue.lakuepopupactivity.PopupType
import kotlinx.android.synthetic.main.activity_barcode.*
import kotlinx.android.synthetic.main.activity_drawer.*
import kotlinx.android.synthetic.main.activity_home.*
import org.jetbrains.anko.doAsync
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.util.*


class HomeActivity : AppCompatActivity() {
    private lateinit var auth : FirebaseAuth
    private var baseUrl = "http://www.kopo.ac.kr/incheon/content.do?menu=6893"
    private var cday = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    private var resultMap = mutableMapOf<String, String>()
    var point = 0
    var exchange  = ""
    private var value = ""
    private lateinit var cbitmap: Bitmap
    @RequiresApi(Build.VERSION_CODES.O)
    var ptime: LocalDate = LocalDate.now()
    lateinit var customBarcodeDialog : BarcodeDialog
    internal lateinit var  viewpager : ViewPager

    private lateinit var drawerLayout : DrawerLayout
    private lateinit var drawerView : View

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        auth = FirebaseAuth.getInstance()
        val database = Firebase.database
        val myRef = database.getReference("users")
        val user = auth.currentUser

        if (resultCode == RESULT_OK) {
            if (requestCode == 2) {
                val result = data?.getSerializableExtra("result") as PopupResult
                if (result == PopupResult.LEFT) {
                    //구매확정 시
                    if(point>=1700) {
                        Toast.makeText(this, "구매완료", Toast.LENGTH_SHORT).show()
                        value = numberGen(13,1).toString()
                        myRef.child(user!!.uid).child("바코드번호")
                            .setValue(value)
                        myRef.child(user.uid).child("포인트")
                            .setValue(Integer.parseInt((point.minus(1700)).toString()))
                        myRef.child(user.uid).child("발급일자")
                            .setValue(ptime.toString())
                    }
                    else{
                        Toast.makeText(this, "잔액이 부족합니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        customBarcodeDialog = BarcodeDialog(this)
        customBarcodeDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        auth = FirebaseAuth.getInstance()
        val database = Firebase.database
        val myRef = database.getReference("users")
        val user = auth.currentUser

        //식단 위에 오늘 날짜 표시
        tv_today.text = ptime.toString()

        //식단 크롤링
        doAsync {
            val doc = Jsoup.connect(baseUrl).get()

            val breakfast = doc.select("table.tbl_table.menu").select("tr")[cday - 1].select("td").select(
                "span")[0]
            val lunch = doc.select("table.tbl_table.menu").select("tr")[cday - 1].select("td").select(
                "span")[1]
            val dinner = doc.select("table.tbl_table.menu").select("tr")[cday - 1].select("td").select(
                "span")[2]


            resultMap["조식"] = makeLineText(breakfast)
            resultMap["중식"] = makeLineText(lunch)
            resultMap["석식"] = makeLineText(dinner)

            //기본값으로 중식표시
            tv_hasic.text = resultMap["중식"].toString()
        }

        //식단 좌우 버튼
        btn_left.setOnClickListener {
            if(tv_hasic_title.text=="중식"){
                btn_left.visibility = View.INVISIBLE
                tv_hasic.text = resultMap["조식"].toString()
                tv_hasic_title.text = "조식"
            }else if(tv_hasic_title.text=="석식"){
                setvisible()
                tv_hasic.text = resultMap["중식"].toString()
                tv_hasic_title.text = "중식"
            }
        }

        btn_right.setOnClickListener {
            if(tv_hasic_title.text=="중식"){
                btn_right.visibility = View.INVISIBLE
                tv_hasic.text = resultMap["석식"].toString()
                tv_hasic_title.text = "석식"
            }else if(tv_hasic_title.text=="조식"){
                setvisible()
                tv_hasic.text = resultMap["중식"].toString()
                tv_hasic_title.text = "중식"
            }
        }

        //데이터 가져오기
        myRef.child(user!!.uid).addValueEventListener(object :
            ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currentUser = dataSnapshot.getValue<User>()
                val pointValue = currentUser!!.포인트!!
                val barcodeValue = currentUser.바코드번호

                ////식권 구매 포인트부분
                point = currentUser.포인트!!
                exchange = if ((pointValue.minus(1700)) < 0) {
                    "구매 불가"
                } else {
                    (pointValue.minus(1700)).toString() + "원"
                }

                //접속중인 유저 이름
                tv_h_name.text = currentUser.이름
                tv_m_name.text = currentUser.이름 + '님'

                //접속적인 유저 포인트
                tv_point.text = currentUser.포인트.toString()

                //바코드 번호
                tv_barcode_num.text = barcodeValue.toString()
                if(tv_barcode_num.text != ""){
                    displayBitmap()
                    btn_barcode_zoom.visibility = View.VISIBLE
                }
                else img_barcode.setImageResource(R.drawable.noticker)

                //바코드 다이얼로그 발급일자
                customBarcodeDialog.tv_buytime.text = "발급일자 : "+ currentUser.발급일자
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        //식권 구매 팝업
        btn_ticket.setOnClickListener {
            val intent = Intent(baseContext, PopupActivity::class.java)
            intent.putExtra("type", PopupType.SELECT)
            intent.putExtra("gravity", PopupGravity.LEFT)
            intent.putExtra("title", "구매하시겠습니까?")
            intent.putExtra("content", "구매 후 잔액: $exchange")
            intent.putExtra("buttonLeft", "구매")
            intent.putExtra("buttonRight", "취소")
            startActivityForResult(intent, 2)
        }

        //바코드 확대 버튼
        btn_barcode_zoom.setOnClickListener{
            customBarcodeDialog.img_a_barcode.setImageBitmap(cbitmap)
            customBarcodeDialog.show()
        }

        //포인트 충전 버튼
        btn_point.setOnClickListener {
            val intent = Intent(this, ChargeActivity::class.java)
            startActivity(intent)
        }

        // 메뉴 버튼
        btn_menu.setOnClickListener {
            drawerLayout.openDrawer(drawerView)
        }

        // 메뉴 안 마이페이지
        val mypage = findViewById<LinearLayout>(R.id.mypage)
        drawerLayout = findViewById(R.id.drawer_layout)
        drawerView = findViewById(R.id.drawerView)

        mypage.setOnClickListener {
            val intent = Intent(this, MyPageActivity::class.java)
            startActivity(intent)
        }
        viewpager = findViewById<ViewPager>(R.id.viewPager)
        val adapter = ViewPagerAdapter(this)
        viewpager.adapter = adapter

        //로그아웃 버튼
        btn_logout.setOnClickListener {
            logout()
        }
        btn_m_logout.setOnClickListener {
            logout()
        }
    }
    // 로그아웃
    private fun logout() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        val builder = AlertDialog.Builder(this)
        builder.setTitle("로그아웃")
        builder.setMessage("정말로 로그아웃 하시겠습니까?")
        builder.setPositiveButton(Html.fromHtml("<font color='#FF7F27'>예</font>")) { _, _ ->
            startActivity(intent)
            auth.signOut()
            finish()
            Toast.makeText(this, "정상적으로 로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("아니오") { _, _ ->

        }
        builder.show()
    }
    
    // 바코드 13자리 난수 생성
    private fun numberGen(len: Int, dupCd: Int): String? {
        val rand = Random()
        var numStr = "" //난수가 저장될 변수
        var i = 0
        while (i < len) {

            //0~9 까지 난수 생성
            val ran = Integer.toString(rand.nextInt(10))
            if (dupCd == 1) {
                //중복 허용시 numStr에 append
                numStr += ran
            } else if (dupCd == 2) {
                //중복을 허용하지 않을시 중복된 값이 있는지 검사한다
                if (!numStr.contains(ran)) {
                    //중복된 값이 없으면 numStr에 append
                    numStr += ran
                } else {
                    //생성된 난수가 중복되면 루틴을 다시 실행한다
                    i -= 1
                }
            }
            i++
        }
        return numStr
    }

    //바코드 비트맵 생성
    private fun createBarcodeBitmap(
        barcodeValue: String,
        @ColorInt barcodeColor: Int,
        @ColorInt backgroundColor: Int,
        widthPixels: Int,
        heightPixels: Int
    ): Bitmap {
        val bitMatrix = Code128Writer().encode(
            barcodeValue,
            BarcodeFormat.CODE_128,
            widthPixels,
            heightPixels
        )

        val pixels = IntArray(bitMatrix.width * bitMatrix.height)
        for (y in 0 until bitMatrix.height) {
            val offset = y * bitMatrix.width
            for (x in 0 until bitMatrix.width) {
                pixels[offset + x] =
                    if (bitMatrix.get(x, y)) barcodeColor else backgroundColor
            }
        }

        val bitmap = Bitmap.createBitmap(
            bitMatrix.width,
            bitMatrix.height,
            Bitmap.Config.ARGB_8888
        )
        bitmap.setPixels(
            pixels,
            0,
            bitMatrix.width,
            0,
            0,
            bitMatrix.width,
            bitMatrix.height
        )
        return bitmap
    }

    //바코드 이미지
    private fun displayBitmap() {
        val widthPixels = resources.getDimensionPixelSize(R.dimen.width_barcode)
        val heightPixels = resources.getDimensionPixelSize(R.dimen.height_barcode)

        cbitmap = createBarcodeBitmap(
            barcodeValue = tv_barcode_num.text.toString(),
            barcodeColor = getColor(android.R.color.background_dark),
            backgroundColor = getColor(android.R.color.white),
            widthPixels = widthPixels,
            heightPixels = heightPixels
        )

        img_barcode.setImageBitmap(cbitmap)
    }

    //가져온 식단 폼
    private fun makeLineText(element: Element) : String{
        val list = element.html().split(",").toTypedArray()

        var result = ""
        list.forEach {
            if(it == " ") return@forEach
            result+=it+"\n"
        }
        return result
    }

    private fun setvisible() {
        btn_left.visibility = View.VISIBLE
        btn_right.visibility = View.VISIBLE
    }

    //뒤로가기 방지
    override fun onBackPressed() {
        //super.onBackPressed();
    }
}
