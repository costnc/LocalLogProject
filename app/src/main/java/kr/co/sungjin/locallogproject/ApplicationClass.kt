package kr.co.sungjin.locallogproject

import android.app.Application
import android.os.Build
import android.os.Environment
import kr.co.sungjin.localloglib.LocalLog
import kr.co.sungjin.localloglib.LogFile

class ApplicationClass : Application() {

    override fun onCreate() {
        super.onCreate()

        val builder : LogFile.Builder = LogFile.Builder();
        builder.fileName("saveLogFile.txt")
        builder.path("sub/sub")

        LocalLog
            .initialize(applicationContext)
            /* if >= Build.VERSION_CODES.Q) : download folder save
                cause -> Android 11 issue */
            .saveLogFile(builder.build())

    }

}