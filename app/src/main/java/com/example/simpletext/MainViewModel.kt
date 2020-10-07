package com.example.simpletext

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mozilla.universalchardet.UniversalDetector
import java.io.BufferedReader
import java.io.InputStreamReader

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application

    val title = MutableLiveData(context.getString(R.string.app_name))
    val textList = MutableLiveData(emptyList<String>())
    val isLoading = MutableLiveData(false)
    val fontSize: MutableLiveData<Float>

    init {
        val density = context.resources.displayMetrics.scaledDensity
        val fontSizePx = context.resources.getDimension(R.dimen.default_font_size)
        fontSize = MutableLiveData(fontSizePx / density)
    }

    fun openTextFile(activity: Activity, requestCode: Int) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "text/plain"
        activity.startActivityForResult(intent, requestCode)
    }

    fun handleTextOpen(uri: Uri) {
        isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val fullPath = uri.lastPathSegment ?: ""
            val fullName = getFullName(fullPath)
            val total = ArrayList<String>()

            var charset = "UTF-8"
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val buf = ByteArray(4096)
                val detector = UniversalDetector()
                var length: Int
                while (stream.read(buf).also { length = it } > 0 && !detector.isDone) {
                    detector.handleData(buf, 0, length)
                }
                detector.dataEnd()
                charset = detector.detectedCharset
            }

            context.contentResolver.openInputStream(uri)?.use { stream ->
                BufferedReader(InputStreamReader(stream, charset)).use { reader ->
                    var line: String
                    while (reader.readLine().also { line = it ?: "" } != null) {
                        total.add(line)
                    }
                }
            }

            textList.postValue(total)
            title.postValue(fullName)
            isLoading.postValue(false)
        }
    }

    fun increaseFontSize() {
        fontSize.value = fontSize.value!! + 1
    }

    fun decreaseFontSize() {
        fontSize.value = fontSize.value!! - 1
    }

    private fun getFullName(fullPath: String): String {
        return fullPath.substringAfterLast("/")
    }

    private fun getFileName(fullPath: String): String {
        return getFullName(fullPath).substringBeforeLast(".")
    }
}
