package com.example.logtextapplication

import android.os.Environment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date


class MyLogger {


    fun captureLogs(logLevel: String): StringBuilder {
        val stringBuilderLog = StringBuilder()

        val command = "logcat -d *:$logLevel"

        try {
            val process = Runtime.getRuntime().exec(command)
            val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))

            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                stringBuilderLog.append(line).append("\n")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return stringBuilderLog
    }

    fun saveLogsToFile(s: String) {
        val coroutineScope = CoroutineScope(Dispatchers.IO)

        coroutineScope.launch {
            try {
                // Ensure directory existence
                val logDirectory = File(Environment.getExternalStorageDirectory(), "MyLogs")
                if (!logDirectory.exists()) {
                    logDirectory.mkdirs()
                }

                // Create a file with a timestamp
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                val logFile = File(logDirectory, "log_$timeStamp.txt")
                logFile.createNewFile()

                // Append logs to the file
                logFile.appendText(captureLogs("E").toString()) // Example: capture error logs
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}







