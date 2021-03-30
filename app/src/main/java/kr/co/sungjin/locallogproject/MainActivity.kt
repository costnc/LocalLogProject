package kr.co.sungjin.locallogproject

import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import kr.co.sungjin.localloglib.LocalLog
import kr.co.sungjin.localloglib.LogFile
import java.io.File

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        LocalLog.d("lim.sung.jin", "testset1");
        LocalLog.d("lim.sung.jin", "testset2");
        LocalLog.d("lim.sung.jin", "testset3");

    }
}