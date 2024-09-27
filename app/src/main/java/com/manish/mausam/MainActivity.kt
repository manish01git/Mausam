package com.manish.mausam

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.manish.mausam.databinding.ActivityMainBinding
import com.manish.mausam.modals.WeatherApp
import com.manish.mausam.util.ConnectionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        binding.progreLayout.visibility = View.VISIBLE
        if (ConnectionManager().checkConnectivity(this)) {
            fetchWeatherData("Varanasi".trim())
            searchCity()
        } else {
            showNoConnectionDialog()
        }
    }

    private fun showNoConnectionDialog() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Error")
        dialog.setMessage("Internet Connection Not Found")
        dialog.setPositiveButton("Open Settings") { _, _ ->
            startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
            finish()
        }
        dialog.setNegativeButton("Exit") { _, _ ->
            ActivityCompat.finishAffinity(this)
        }
        dialog.show()
    }

    private fun searchCity() {
        binding.searchView.setQueryHint("Search For A City")
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    fetchWeatherData(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    private fun fetchWeatherData(cityName: String) {
        val city = cityName.trim()
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build()

        val apiInterface = retrofit.create(ApiInterface::class.java)

        val call = apiInterface.getWeatherData(city, "f23a451f2698d6e63dd4caa4207dfc64", "metric")

        call.enqueue(object : Callback<WeatherApp> {
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        binding.progreLayout.visibility = View.GONE
                        updateUIWithWeatherData(responseBody, cityName)
                    } else {
                        Toast.makeText(this@MainActivity, "No data available", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Log.e(
                        "MainActivity",
                        "Response not successful: ${response.code()} ${response.message()}"
                    )
                    Toast.makeText(
                        this@MainActivity,
                        "Response not successful: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error fetching data", Toast.LENGTH_SHORT).show()
                Log.e("MainActivity", "Error fetching data", t)
            }
        })
    }

    private fun updateUIWithWeatherData(responseBody: WeatherApp, cityName: String) {

        val temp = responseBody.main.temp.toString()
        val humidity = responseBody.main.humidity
        val sunrise = responseBody.sys.sunrise.toLong()
        val sunset = responseBody.sys.sunset.toLong()
        val windSpeed = responseBody.wind.speed
        val seaLevel = responseBody.main.sea_level
        val max_Temp = responseBody.main.temp_max
        val min_Temp = responseBody.main.temp_min
        val condition = responseBody.weather.firstOrNull()?.main ?: "unknown"

        binding.temprecture.text = "$temp °C"
        binding.weather.text = condition
        binding.humidity.text = "$humidity %"
        binding.sunrise.text = formatTimestamp(sunrise)
        binding.sunset.text = formatTimestamp(sunset)
        binding.location.text = cityName
        binding.maxTemp.text = "Max_Temp : $max_Temp °C"
        binding.minTemp.text = "Min_Temp : $min_Temp °C"
        binding.sealevel.text = "$seaLevel hPa"
        binding.windspeed.text = "$windSpeed m/s"
        binding.weatherconditon.text = condition
        changeBackgroundAndTextColor(condition)
    }

    private fun changeBackgroundAndTextColor(conditions: String) {
        val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)

        when (conditions) {
            "Clear Sky", "Sunny", "Clear" -> {
                if (currentHour in 6..18) {
                    binding.root.setBackgroundResource(R.drawable.sunny_background)
                    binding.lottieAnimationView.setAnimation(R.raw.suncloud)
                    setTextColor(false) // Daytime
                } else {
                    binding.root.setBackgroundResource(R.drawable.night) // Night background
                    binding.lottieAnimationView.setAnimation(R.raw.night)
                    setTextColor(true) // Nighttime
                }
            }

            "Partly Cloud", "Clouds", "Overcast", "Mist", "Haze", "Foggy" -> {
                binding.root.setBackgroundResource(R.drawable.colud_background)
                binding.lottieAnimationView.setAnimation(R.raw.cloudcloud)
                setTextColor(false)
            }

            "Light Rain", "Drizzle", "Moderate Rain", "Showers", "Rain", "Heavy Rain" -> {
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.raincloud)
                setTextColor(false)
            }

            "Light Snow", "Moderate Snow", "Heavy Snow", "Blizzard" -> {
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottieAnimationView.setAnimation(R.raw.snowcloud)
                setTextColor(false)
            }

            else -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.suncloud)
                setTextColor(false)
            }
        }

        binding.lottieAnimationView.playAnimation()
    }

    private fun setTextColor(isNight: Boolean) {
        val textColor = if (isNight) android.graphics.Color.WHITE else android.graphics.Color.BLACK
        binding.temprecture.setTextColor(textColor)
        binding.weather.setTextColor(textColor)
        binding.today.setTextColor(textColor)
        binding.location.setTextColor(textColor)
        binding.maxTemp.setTextColor(textColor)
        binding.minTemp.setTextColor(textColor)
        binding.date.setTextColor(textColor)
        binding.day.setTextColor(textColor)
    }

    private fun formatTimestamp(timestamp: Long): String {
        val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return formatter.format(Date(timestamp * 1000)) // Convert from seconds to milliseconds
    }

    private fun date(): String {
        val format = SimpleDateFormat("dd MMMM YYYY", Locale.getDefault())
        return format.format(Date())
    }

    private fun dayName(timestamp: Long): String {
        val format = SimpleDateFormat("EEEE", Locale.getDefault())
        return format.format(Date())
    }
}
