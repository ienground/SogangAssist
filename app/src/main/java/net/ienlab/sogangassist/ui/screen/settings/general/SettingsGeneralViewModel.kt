package net.ienlab.sogangassist.ui.screen.settings.general

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import net.ienlab.sogangassist.Dlog
import net.ienlab.sogangassist.MyApplication
import net.ienlab.sogangassist.TAG
import net.ienlab.sogangassist.data.lms.Lms
import net.ienlab.sogangassist.data.lms.LmsDatabase
import net.ienlab.sogangassist.data.lms.LmsRepository
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOError
import java.io.IOException
import java.io.InputStreamReader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SettingsGeneralViewModel(
    application: MyApplication,
    private val lmsRepository: LmsRepository
): AndroidViewModel(application) {
    private val context: Context
        get() = getApplication<MyApplication>().applicationContext

    private var restoreData: JSONArray = JSONArray()

    fun preBackup(): Intent {
        val saveFormat = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        return Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, "albatross_backup_${LocalDateTime.now().format(saveFormat)}.txt")
        }
    }

    fun preRestore(): Intent {
        return Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "text/plain"
        }
    }

    suspend fun backup(uri: Uri) {
        val result = JSONArray()
        val entities = lmsRepository.getAllStream().first()
        for (entity in entities) {
            val jObject = JSONObject()
            jObject.put("className", entity.className)
            jObject.put("timeStamp", entity.timestamp)
            jObject.put("type", entity.type)
            jObject.put("startTime", entity.startTime)
            jObject.put("endTime", entity.endTime)
            jObject.put("isRenewAllowed", entity.isRenewAllowed)
            jObject.put("isFinished", entity.isFinished)
            jObject.put("week", entity.week)
            jObject.put("lesson", entity.lesson)
            jObject.put("homework_name", entity.homework_name)

            result.put(jObject)
        }

        withContext(Dispatchers.IO) {
            val outputStream = context.contentResolver.openOutputStream(uri)
            outputStream?.write(result.toString().toByteArray())
            outputStream?.close()
        }
    }

    suspend fun restoreData(uri: Uri) {
        val reader: BufferedReader
        val stringBuilder = StringBuilder()

        withContext(Dispatchers.IO) {
            try {
                reader = BufferedReader(InputStreamReader(context.contentResolver.openInputStream(uri)))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    stringBuilder.append(line)
                }
                reader.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        restoreData = JSONArray(stringBuilder.toString())
    }

    suspend fun restoreApply() {
        withContext(Dispatchers.IO) {
            LmsDatabase.getDatabase(context).clearAllTables()
        }
        for (i in 0 until restoreData.length()) {
            val jObject = restoreData.getJSONObject(i)
            val entity = Lms(
                className = jObject.getString("className"),
                timestamp = jObject.getLong("timeStamp"),
                type = jObject.getInt("type"),
                startTime = jObject.getLong("startTime"),
                endTime = jObject.getLong("endTime"),
                isRenewAllowed = jObject.getBoolean("isRenewAllowed"),
                isFinished = jObject.getBoolean("isFinished"),
                week = jObject.getInt("week"),
                lesson = jObject.getInt("lesson"),
                homework_name = jObject.getString("homework_name")
            )
            lmsRepository.upsert(entity)
        }
    }
}