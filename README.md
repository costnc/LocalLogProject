# LocalLogProject


support
=============
* Android Library
* save local log
  - support >= Build.VERSION_CODES.JELLY_BEAN(16)
  - support >= Build.VERSION_CODES.Q(29)
  
* log files by date  
* log files by size(byte)
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
------------

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
------------
``` gradle
dependencies {
        implementation 'com.github.costnc:LocalLogProject:1.2.0'
}
```
Step 3. add application class
------------
``` xml
<application
        android:name=".ApplicationClass"
        ...
/>
```
Step 4. init LocalLog in application calss
------------

kotlin
``` Kotlin
class ApplicationClass : Application() {

    override fun onCreate() {
        super.onCreate()

        val builder : LogFile.Builder = LogFile.Builder(applicationContext);
        /* if want save log file : set file name */
        builder.fileName("saveLogFile")
        /* if want save log file : set path */
        builder.path("sub/sub")
        /* if want log files by date */
        builder.asTrunDate()
        /* if want log files by size(byte) */
        builder.trunLogFileSize = 100

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


java
``` java
public class MainAppliction extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        
        LogFile.Builder builder = new LogFile.Builder(getApplicationContext());
        /* if want save log file : set file name*/
        builder.fileName("saveLogFile");
        /* if want log files by size(byte) */
        builder.asTrunLogFileSize(100);
        /* if want save log file : set path, default is root folder*/
        builder.path("sub/sub");
        /* if want log files by date */
        builder.asTrunDate();

        LocalLog
                .Companion.initialize(MainAppliction.this)
                /* if >= Build.VERSION_CODES.Q) : download folder save
                cause -> Android 11 issue */
                .saveLogFile(builder.build())
                /* if  want release mode log */
                .enableReleaseLog(false)
                /* if want save log with release */
                .enableReleaseSaveFileLog(false);
                
    }
}
```
Step 5. use Log
------------

kotlin
``` kotlin
LocalLog.d("lim.sung.jin", "testset1");
LocalLog.d("lim.sung.jin", "testset2");
LocalLog.d("lim.sung.jin", "testset3");
```


java
``` java
LocalLog.Companion.d("lim.sung.jin", "testset1");
LocalLog.Companion.d("lim.sung.jin", "testset2");
LocalLog.Companion.d("lim.sung.jin", "testset3");
```
