package com.example.weather_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import kotlinx.coroutines.*
import org.json.JSONObject
import org.w3c.dom.Text
import java.lang.Exception
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    val apiKey = "4291fdd25f08dde60263dbabdf3caf8a"

    private lateinit var search: LinearLayout
    private lateinit var enterZipCode: EditText
    private lateinit var searchButton: Button

    private lateinit var mainLayout: LinearLayout
    private lateinit var tvCityCountry: TextView
    private lateinit var tvUpdateTime: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvTemperature: TextView
    private lateinit var tvLow: TextView
    private lateinit var tvHigh: TextView
    private lateinit var tvHumidityLevel: TextView
    private lateinit var tvPressureLevel: TextView
    private lateinit var tvWindLevel: TextView
    private lateinit var tvSunrise: TextView
    private lateinit var tvSunset: TextView
    private lateinit var llReset: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        search = findViewById(R.id.llSearch)
        enterZipCode = findViewById(R.id.etEnterZipCode)
        searchButton = findViewById(R.id.btnSearch)

        mainLayout = findViewById(R.id.mainLayout)
        tvCityCountry = findViewById(R.id.tvCityCountry)
        tvUpdateTime = findViewById(R.id.tvUpdatedTime)
        tvStatus = findViewById(R.id.tvStatus)
        tvTemperature = findViewById(R.id.tvTemperature)
        tvLow = findViewById(R.id.tvLow)
        tvHigh = findViewById(R.id.tvHigh)
        tvHumidityLevel = findViewById(R.id.tvHumidityLevel)
        tvPressureLevel = findViewById(R.id.tvPressureLevel)
        tvWindLevel = findViewById(R.id.tvWindLevel)
        tvSunrise = findViewById(R.id.tvSunriseTime)
        tvSunset = findViewById(R.id.tvSunsetTime)
        llReset = findViewById(R.id.llReset)

        tvCityCountry.setOnClickListener{ getWeather() }
        searchButton.setOnClickListener{ getWeather() }
    }

    private fun getWeather() {
//        search.isVisible = true
//        mainLayout.isVisible = false

        CoroutineScope(Dispatchers.IO).launch {
            val data = async { fetchData() }.await()
            if (data.isNotEmpty()){
                weatherData(data)
            } else {
                Log.d("DATA", "Unable to get data")
            }
        }
    }

    private fun fetchData(): String {
        var response = ""
        try {
            response = URL("api.openweathermap.org/data/2.5/weather?zip=${enterZipCode.text}&appid=${apiKey}").readText()
        } catch (e: Exception) {
            Log.d("RESPONSE", "ISSUE: $e")
        }
        return response
    }

    private suspend fun weatherData(data: String) {
        // Fetched from Almin's solution
        withContext(Dispatchers.Main) {
            val jsonObj = JSONObject(data)
            val main = jsonObj.getJSONObject("main")
            val sys = jsonObj.getJSONObject("sys")
            val wind = jsonObj.getJSONObject("wind")
            val weather = jsonObj.getJSONArray("weather").getJSONObject(0)

            val lastUpdate:Long = jsonObj.getLong("dt")
            val lastUpdateText = "Updated at: " + SimpleDateFormat(
                "dd/MM/yyyy hh:mm a",
                Locale.ENGLISH).format(Date(lastUpdate*1000))
            val currentTemperature = main.getString("temp")
            val temp = try{
                currentTemperature.substring(0, currentTemperature.indexOf(".")) + "°C"
            }catch(e: Exception){
                currentTemperature + "°C"
            }
            val minTemperature = main.getString("temp_min")
            val tempMin = "Low: " + minTemperature.substring(0, minTemperature.indexOf("."))+"°C"
            val maxTemperature = main.getString("temp_max")
            val tempMax = "High: " + maxTemperature.substring(0, maxTemperature.indexOf("."))+"°C"
            val pressure = main.getString("pressure")
            val humidity = main.getString("humidity")

            val sunrise:Long = sys.getLong("sunrise")
            val sunset:Long = sys.getLong("sunset")
            val windSpeed = wind.getString("speed")
            val weatherDescription = weather.getString("description")

            val address = jsonObj.getString("name")+", "+sys.getString("country")

            tvCityCountry.text = address
            tvUpdateTime.text = lastUpdateText
            tvStatus.text = weatherDescription
            tvTemperature.text = temp
            tvLow.text = tempMin
            tvHigh.text = tempMax
            tvHumidityLevel.text = humidity
            tvPressureLevel.text = pressure
            tvWindLevel.text = windSpeed
            tvSunrise.text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise*1000))
            tvSunset.text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset*1000))
            llReset.setOnClickListener {getWeather()}
        }
    }
}