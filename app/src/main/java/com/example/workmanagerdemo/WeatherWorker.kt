package com.example.workmanagerdemo
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class WeatherWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    override fun doWork(): Result {
        val city = inputData.getString("city") ?: return Result.failure()
        val apiKey = applicationContext.getString(R.string.openweather_api_key)
        val urlStr = "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey&units=metric"
        return try {
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            val stream = connection.inputStream
            val response = Scanner(stream).useDelimiter("\\A").next()
            val json = JSONObject(response)
            val cityName = json.getString("name")
            val main = json.getJSONObject("main")
            val weather = json.getJSONArray("weather").getJSONObject(0)
            val temp = main.getDouble("temp")
            val feelsLike = main.getDouble("feels_like")
            val condition = weather.getString("description")
            val resultText = """
                $cityName
                Температура: $temp°C (ощущается как $feelsLike°C)
                Состояние: $condition
            """.trimIndent()

            val previousResult = inputData.getString("result_so_far") ?: ""
            val updatedResult = previousResult + "\n\n" + resultText
            val output = androidx.work.Data.Builder()
                .putString("result", updatedResult)
                .putString("result_so_far", updatedResult)
                .build()
            Result.success(output)
        } catch (e: Exception) {
            Result.failure()
        }
    }
}