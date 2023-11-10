package com.example.logtextapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val WRITE_EXTERNAL_STORAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check and request runtime permissions if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    WRITE_EXTERNAL_STORAGE_REQUEST
                )
            }
        }

        // Call the function to save logs to a text file
        saveLogsToTxtFile()
    }

    // Handle the result of the runtime permissions request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, call the function to save logs to a text file
                saveLogsToTxtFile()
            } else {
                // Permission denied, handle accordingly (e.g., show a message)
            }
        }
    }

    private fun saveLog(): StringBuilder {
        val stringBuilderLog = StringBuilder()

        // "logcat -d *:E" -> Error logs
        val command = "logcat -d *:E"
        val process = Runtime.getRuntime().exec(command)
        val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
        var line: String?

        while (bufferedReader.readLine().also { line = it } != null) {
            stringBuilderLog.append(line).append("\n")
        }

        // Close the BufferedReader to avoid resource leaks
        bufferedReader.close()

        return stringBuilderLog
    }


    private fun saveLogsToTxtFile() {
        val coroutineCallLogger = CoroutineScope(Dispatchers.IO)
        coroutineCallLogger.launch {
            async {
                // External storage directory reference
                val externalStorageDirectory = Environment.getExternalStorageDirectory()

                // Check if external storage is mounted
                if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
                    // Creating file with the timestamped name in the Downloads directory
                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val filePath = File(downloadsDirectory, "TEXT_LOGGER_$timeStamp.txt")

                    // This run block is coroutine usage purposes.
                    runCatching {
                        filePath.createNewFile()
                        filePath.appendText(saveLog().toString())
                        Log.d("FileSaved", "Log file saved to: ${filePath.absolutePath}")
                    }.onFailure {
                        // Handle the error, e.g., log it or show a toast
                        it.printStackTrace()
                        Log.e("FileSaveError", "Error saving log file: ${it.message}")
                    }
                } else {
                    Log.e("ExternalStorage", "External storage is not mounted.")
                    // Handle the case where external storage is not available
                }
            }
        }
    }
}


