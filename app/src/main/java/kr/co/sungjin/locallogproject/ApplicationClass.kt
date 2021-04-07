package kr.co.sungjin.locallogproject

import android.app.Application
import android.os.Build
import android.os.Environment
import kr.co.sungjin.localloglib.LocalLog
import kr.co.sungjin.localloglib.LogFile

class ApplicationClass : Application() {

    override fun onCreate() {
        super.onCreate()

        val builder : LogFile.Builder = LogFile.Builder(applicationContext);
        /* if want save log file : set file name*/
        builder.fileName("saveLogFile.txt")
        /* if want save log file : set path*/
        builder.path("sub/sub")
        /* if want trun date log file*/
        builder.asTrunDate()
        /* if want trun file size log file : byte */
        //builder.trunLogFileSize = 1000

        LocalLog
            .initialize(applicationContext)
            /* if >= Build.VERSION_CODES.Q) : download folder save
                cause -> Android 11 issue */
            .saveLogFile(builder.build())
            /* if  want release mode log */
            .enableReleaseLog(false)
            /* if want save log with release */
            .enableReleaseSaveFileLog(false)

    }

}