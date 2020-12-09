//회원가입 페이지
package com.ljh.hasicpass_android

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.android.synthetic.main.activity_register.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class RegisterActivity : AppCompatActivity() {
    private val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var curPhotoPath: String
    private lateinit var auth : FirebaseAuth
    private var firebaseStore: FirebaseStorage? = null
    private var storageReference: StorageReference? = null

    var hid : String? = ""
    var hid_check : Boolean? = false
    private lateinit var mbitmap : Bitmap

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {

            val matrix = Matrix()
            matrix.setScale(-1f,1f)
            var bitmap: Bitmap
            val file = File(curPhotoPath)


            if (Build.VERSION.SDK_INT < 28) {
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.fromFile(file))
//                var height = bitmap.height
                val width = bitmap.width

                if(width>1200){
                    bitmap = Bitmap.createScaledBitmap(bitmap, 1080, 1440, true)
                }
                mbitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)
            } else {
                val decode = ImageDecoder.createSource(
                    contentResolver,
                    Uri.fromFile(file)
                )
                bitmap = ImageDecoder.decodeBitmap(decode)
//                var height = bitmap.height
                val width = bitmap.width

                if(width>1200){
                    bitmap = Bitmap.createScaledBitmap(bitmap, 1080, 1440, true)
                }
                mbitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)
            }
            tv_face_check.text = "얼굴등록이 완료되었습니다"
        } else{
            Toast.makeText(applicationContext, "얼굴등록 취소", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        val database = Firebase.database
        val myRef = database.getReference("users")

        firebaseStore = FirebaseStorage.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        auth =FirebaseAuth.getInstance()

        btn_id_check.setOnClickListener {
            hid_check = false
            myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if(et_r_id.text.toString() == ""){
                        Toast.makeText(applicationContext,"학번을 입력해주세요", Toast.LENGTH_SHORT).show()
                        et_r_id.setBackgroundResource(R.drawable.red_edittext_rounded_corner_rectangle)
                        hid_check = true
                    }
                    for (data in dataSnapshot.children) {
                        hid = data.child("학번").getValue(String::class.java)

                        if (hid!! == et_r_id.text.toString()) {
                            hid_check = true
                            Toast.makeText(
                                applicationContext,
                                "이미 등록되어 있는 학번입니다",
                                Toast.LENGTH_SHORT
                            ).show()
                            et_r_id.setBackgroundResource(R.drawable.red_edittext_rounded_corner_rectangle)
                            et_r_id.setText("")
                        }
                    }
                    if(hid_check == false){
                        Toast.makeText(applicationContext, "중복확인 성공", Toast.LENGTH_SHORT).show()
                        et_r_id.setBackgroundResource(R.drawable.edittext_rounded_corner_rectangle)
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
        }

        //사진선택 버튼
        btn_face_up.setOnClickListener {
            TedPermission.with(this)
                .setPermissionListener(object : PermissionListener {
                    override fun onPermissionGranted() {
                        takeCapture()
                    }
                    override fun onPermissionDenied(deniedPermissions: ArrayList<String>?) {
                        for(i in deniedPermissions!!)
                            Log.d("Error", i)
                    }
                })
                .setDeniedMessage("사진을 등록하시려면 권한을 허가하셔야합니다.")
                .setPermissions(Manifest.permission.CAMERA)
                .check()

        }

        btn_back.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        //회원가입 버튼
        btn_register.setOnClickListener {
            if (et_r_name.text.toString().isEmpty() || et_r_id.text.toString().isEmpty() ||
                et_r_email.text.toString().isEmpty() || et_r_password.text.toString().isEmpty() ||
                subject.selectedItem == "전공을 선택해주세요" || et_r_tel.text.toString().isEmpty()
            ) Toast.makeText(this, "정보를 제대로 기입해주세요", Toast.LENGTH_SHORT).show()
            //회원가입
            else {
                auth.createUserWithEmailAndPassword(et_r_email.text.toString(), et_r_password.text.toString())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val registeUser = User(et_r_name.text.toString(), et_r_id.text.toString(), subject.selectedItem.toString(),
                                                    et_r_email.text.toString(), et_r_password.text.toString(), et_r_tel.text.toString(),
                                                    0, "", "")
                            myRef.child(user!!.uid).setValue(registeUser)
                            uploadImage(mbitmap)
                            Toast.makeText(applicationContext, "회원가입을 성공하셨습니다.", Toast.LENGTH_SHORT).show()

                            finish()
                        } else {
                            //비밀번호 글자 수 오류
                            if(et_r_password.length()<6) {
                                Toast.makeText(applicationContext, "비밀번호를 6글자 이상 입력해주세요", Toast.LENGTH_SHORT).show()
                                et_r_password.setBackgroundResource(R.drawable.red_edittext_rounded_corner_rectangle)
                            }
                            else{
                                Toast.makeText(applicationContext, "이미 존재하는 이메일 입니다.", Toast.LENGTH_SHORT).show()
                                et_r_email.setText("")
                                et_r_email.setBackgroundResource(R.drawable.red_edittext_rounded_corner_rectangle)
                            }
                        }
                    }
            }
        }
    }
    fun takeCapture() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA",true)
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.ljh.hasicpass_android.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    //이미지 파일 생성
    @SuppressLint("SimpleDateFormat")
    private fun createImageFile(): File {
        val timestamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timestamp}_", ".jpg", storageDir)
            .apply { curPhotoPath = absolutePath }
    }

    //파이어베이스 스토리지 업로드
    private fun uploadImage(bitmap: Bitmap) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val ref = storageReference?.child("uploads/" + et_r_id.text.toString() + "_face")
        ref?.putBytes(data)
            ?.addOnSuccessListener {
                //업로드 성공
            }?.addOnFailureListener { e ->
                Toast.makeText(this, "얼굴이미지 등록 실패" + e.message, Toast.LENGTH_SHORT).show()
            }
    }
}