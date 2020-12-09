//페이스 로그인 페이지
package com.ljh.hasicpass_android

import android.annotation.SuppressLint
import android.app.Activity

import android.content.Intent
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class CameraActivity : AppCompatActivity() {
    private val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var curPhotoPath: String
    private lateinit var firebaseStore: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var requestRef: DatabaseReference
    private lateinit var resultRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var mbitmap : Bitmap

    private lateinit var customProgressDialog : ProgressDialog

    var result = ""
    var email = ""
    var passwd = ""
    var end = ""

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {

            val matrix = Matrix()
            matrix.setScale(-1f, 1f)
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
            upload(mbitmap)
        } else {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            Toast.makeText(applicationContext, "페이스로그인 취소", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        customProgressDialog = ProgressDialog(this)
        customProgressDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        customProgressDialog.setCancelable(false)

        firebaseStore = FirebaseStorage.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        auth = FirebaseAuth.getInstance()
        val database = Firebase.database
        requestRef = database.getReference("requests")
        resultRef = database.getReference("results")

        //대조결과 가져오기
        resultRef.addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val face = dataSnapshot.getValue<CompareResult>()
                result = face!!.Check.toString()
                email = face.email.toString()
                passwd = face.password.toString()
                end = face.End.toString()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
//        resultRef?.child("checked")?.addValueEventListener(object :
//            ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                val r = dataSnapshot.getValue<String>()
//                result = r!!
//            }
//
//            override fun onCancelled(error: DatabaseError) {}
//        })
//        //얼굴과 일치하는 이메일 가져오기
//        resultRef?.child("이메일")?.addValueEventListener(object :
//            ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                val e = dataSnapshot.getValue<String>()
//                email = e!!
//            }
//            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
//            }
//        })
//        //얼굴과 일치하는 비밀번호 가져오기
//        resultRef?.child("비밀번호")?.addValueEventListener(object :
//            ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                val p = dataSnapshot.getValue<String>()
//                passwd = p!!
//            }
//
//            override fun onCancelled(error: DatabaseError) {}
//        })
//        //대조 완료시 end값 가져오기
//        resultRef?.child("end")?.addValueEventListener(object :
//            ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                val en = dataSnapshot.getValue<String>()
//                end = en!!
//            }
//
//            override fun onCancelled(error: DatabaseError) {}
//        })
        takeCapture()
    }

    //카메라
    private fun takeCapture() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
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

    //이미지 업로드및 요청값,결과값 초기화
    private fun upload(bitmap: Bitmap) {
        customProgressDialog.show()

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = storageReference.child("faces").child("face.jpg").putBytes(data)
        uploadTask.addOnFailureListener {
            Toast.makeText(this, "업로드 오류", Toast.LENGTH_SHORT).show()
        }.addOnSuccessListener {
            //            Toast.makeText(this, "업로드 성공 ", Toast.LENGTH_SHORT).show()
            requestRef.child("checked").setValue("true")
            resultRef.child("checked").setValue("true")
            resultRef.child("end").setValue("false")
            resultRef.child("이메일").setValue("null")
            resultRef.child("비밀번호").setValue("null")

            val mHandler = Handler()
            //대조가 완료될때까지 dalay
            //delay중에 end값이 true가 된다면 로그인시도
            mHandler.postDelayed({
                if (end == "true")  results()
                else { mHandler.postDelayed({
                    if (end == "true") results()
                    else { mHandler.postDelayed({
                        if (end == "true") results()
                        else { mHandler.postDelayed({
                            if (end == "true") results()
                            else { mHandler.postDelayed({
                                if (end == "true") results() }, 5000) } }, 5000) } }, 5000) } }, 5000) } }, 5000) }
    }

    //요청값,결과값에 따라 로그인 성공 or 실패
    private fun results() {
        if (result == "true" && end == "true") {
            auth.signInWithEmailAndPassword(email, passwd)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        customProgressDialog.dismiss()
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        resultRef.child("checked").setValue("false")
                        resultRef.child("end").setValue("false")
                    }
                }
        } else if (result == "false" && end == "true") {
            customProgressDialog.dismiss()
            Toast.makeText(this, "등록되지 않은 얼굴 입니다. ", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}

