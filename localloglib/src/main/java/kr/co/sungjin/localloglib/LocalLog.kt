package kr.co.sungjin.localloglib

import android.util.Log

class LocalLog {

    companion object{

        fun d(tag : String, msg : String) {
            Log.d(tag, msg);
        }

    }

}