package com.example.workmanagerdemo

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkContinuation
import androidx.work.WorkManager
import androidx.work.workDataOf

class MainActivity : AppCompatActivity() {
    private lateinit var resultTextView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        resultTextView = findViewById(R.id.resultTextView)
        startWeatherChain()
    }
    private fun startWeatherChain() {
        val cities = arrayOf("Irkutsk", "Moscow", "Omsk")
        val workManager = WorkManager.getInstance(this)
        val weatherRequests = mutableListOf<OneTimeWorkRequest>()
        for (city in cities) {
            val inputData = workDataOf("city" to city)
            val weatherRequest = OneTimeWorkRequest.Builder(WeatherWorker::class.java)
                .setInputData(inputData)
                .build()
            weatherRequests.add(weatherRequest)
        }
        var continuation = workManager.beginWith(weatherRequests[0])
        for (i in 1 until weatherRequests.size) {
            continuation = continuation.then(weatherRequests[i])
        }
        continuation.enqueue()
        workManager.getWorkInfoByIdLiveData(weatherRequests.last().id).observe(this) { workInfo ->
            if (workInfo != null && workInfo.state.isFinished) {
                val result = workInfo.outputData.getString("result") ?: "Нет данных"
                resultTextView.text = result
            }
        }
    }
}