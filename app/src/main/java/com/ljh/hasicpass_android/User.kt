package com.ljh.hasicpass_android

data class User(
    var 이름: String? = "",
    var 학번: String? = "",
    var 학과: String? = "",
    var 이메일: String? = "",
    var 비밀번호: String? = "",
    var 전화번호: String? = "",
    var 포인트: Int? = 0,
    var 바코드번호: String? = "",
    var 발급일자: String? = ""
)
