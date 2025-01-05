package com.luis.appweather

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import com.luis.appweather.databinding.ActivityMainBinding
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val apiKey = BuildConfig.apiKey

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        installSplashScreen()
        setContentView(binding.root)
        fetchWeatherData("Madrid")
        acciones()

    }

    private fun acciones() {
        binding.fetchWeatherButton.setOnClickListener {
            var cityName:String = binding.cityNameInput.text.toString()
            if(cityName.isNotEmpty())fetchWeatherData(cityName)
            else Snackbar.make(binding.root, "Please enter a city name", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun fetchWeatherData(cityName: String) {
        val idiomaDispositivo = Locale.getDefault().language
        var url: String = "https://api.openweathermap.org/data/2.5/weather?q=" + cityName + "&appid=" + apiKey + "&units=metric&lang="+idiomaDispositivo
        var executorService:ExecutorService = Executors.newSingleThreadExecutor()
        executorService.execute{
            var client = OkHttpClient()
            var request = Request.Builder().url(url).build()
            try {
                var response:Response = client.newCall(request).execute()
                var result = response.body?.string()
                runOnUiThread{
                    updateUI(result)
                }
            }catch (e:IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun updateUI(result: String?) {
        if(result!=null){
            try {
                var jsonObject = JSONObject(result)
                var main = jsonObject.getJSONObject("main")
                var temperature:Double = main.getDouble("temp")
                var humidity:Double = main.getDouble("humidity")
                var windSpeed:Double = jsonObject.getJSONObject("wind").getDouble("speed")
                var description:String = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description")
                var iconCode = jsonObject.getJSONArray("weather").getJSONObject(0).getString("icon")
                var resourceName = "ic"+iconCode
                var resId = resources.getIdentifier(resourceName, "drawable", packageName)
                binding.weatherIcon.setImageResource(resId)
                binding.textCityName.setText(jsonObject.getString("name"))
                binding.textTemperature.setText(String.format("%.0fº", temperature))
                binding.humidityText.setText(String.format("%.0f%%", humidity))
                binding.windText.setText(String.format("%.0f", windSpeed))
                binding.descriptionText.setText(description)

            }catch (e: JSONException){
                e.printStackTrace()
            }
        }
    }
}