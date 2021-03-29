package kr.co.sungjin.locallogproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kr.co.sungjin.localloglib.LocalLog

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        LocalLog.init(applicationContext);
        LocalLog.d("sjlim","testset");

    }
}