# LocalLogProject


support
=============
* save local log
  - support >= Build.VERSION_CODES.Q and < Build.VERSION_CODES.Q
  
* default is disable release log
  - you can enable release log

* use io.reactivex.rxjava3:rxandroid:3.0.0
  - background thread > save log file in Order

* targetSdkVersion 30
* See the sample code -> app/src

How to use
=============

How to
To get a Git project into your build:

Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories
``` gradle
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```
Step 2. Add the dependency
``` gradle
dependencies {
        implementation 'com.github.costnc:LocalLogProject:1.0.0'
}
```
Step 3. add application class
``` xml
<application
        android:name=".ApplicationClass"
        ...
/>
```
Step 4. init LocalLog in application calss
``` Kotlin
class ApplicationClass : Application() {

    override fun onCreate() {
        super.onCreate()

        val builder : LogFile.Builder = LogFile.Builder();
        /* if want save log file : set file name*/
        builder.fileName("saveLogFile.txt")
        /* if want save log file : set path*/
        builder.path("sub/sub")

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
```
Step 5. use Log
``` kotlin
LocalLog.d("lim.sung.jin", "testset1");
LocalLog.d("lim.sung.jin", "testset2");
LocalLog.d("lim.sung.jin", "testset3");
```
