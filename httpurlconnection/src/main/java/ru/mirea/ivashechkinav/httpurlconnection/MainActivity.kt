package ru.mirea.ivashechkinav.httpurlconnection

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException

import org.json.JSONObject
import ru.mirea.ivashechkinav.httpurlconnection.databinding.ActivityMainBinding
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bthSendRequest.setOnClickListener(this::onClick)
    }

    fun onClick(view: View) {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var networkinfo: NetworkInfo? = null
        if (connectivityManager != null) {
            networkinfo = connectivityManager.activeNetworkInfo
        }
        if (networkinfo != null && networkinfo.isConnected) {
            lifecycleScope.launch {
                val result = downloadPageTask("https://ipinfo.io/json") ?: return@launch
                downloadWeatherTask("https://api.open-meteo.com/v1/forecast?latitude=${result.latitude}&longitude=${result.longitude}&current_weather=true")
            }
        } else {
            Toast.makeText(this, "Нет интернета", Toast.LENGTH_SHORT).show()
        }
    }

    private class Coordinates(
        val latitude: String,
        val longitude: String
    )
    private suspend fun downloadPageTask(url: String): Coordinates? {
        //onPreExecute
        binding.tvLoadState.text = "Загружаем..."
        //doInBackground
        val result = doInBackground(url)
        //onPostExecute
        result?.let {
            val responseJson = onPostExecute(it)
            val ip = responseJson.getString("ip")
            binding.tvCity.text = responseJson.getString("city")
            binding.tvRegion.text = responseJson.getString("region")
            binding.tvTimeZone.text = responseJson.getString("timezone")

            Log.d(
                MainActivity::class.java.simpleName,
                "IP: $ip"
            )
            val latlng = responseJson.getString("loc").split(",")

            return Coordinates(
                latlng[0],
                latlng[1]
            )
        }
        return null
    }
    private suspend fun downloadWeatherTask(url: String) {
        //onPreExecute
        binding.tvWeather.text = "Загружаем погоду..."
        //doInBackground
        val result = doInBackground(url)
        //onPostExecute
        result?.let {
            val responseJson = onPostExecute(it)
            binding.tvWeather.text = responseJson.getJSONObject("current_weather").toString()
            binding.tvLoadState.text = "Done"
        }
        binding.tvLoadState.text = "Error"

    }

    private suspend fun doInBackground(url: String) = withContext(Dispatchers.IO) {
        return@withContext try {
            downloadIpInfo(url)
        } catch (e: IOException) {
            e.printStackTrace()
            "error"
        }
    }
    private suspend fun onPostExecute(result: String) = withContext(Dispatchers.Main) {
        Log.d(MainActivity::class.java.simpleName, result)
        try {
            val responseJson = JSONObject(result)
            Log.d(
                MainActivity::class.java.simpleName,
                "Response: $responseJson"
            )
            return@withContext responseJson
        } catch (e: JSONException) {
            e.printStackTrace()
            JSONObject()
        }
    }

    @Throws(IOException::class)
    private fun downloadIpInfo(address: String): String? {
        var inputStream: InputStream? = null
        var data = ""
        try {
            val url = URL(address)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.setReadTimeout(1000000)
            connection.setConnectTimeout(1000000)
            connection.setRequestMethod("GET")
            connection.setInstanceFollowRedirects(true)
            connection.setUseCaches(false)
            connection.setDoInput(true)
            val responseCode: Int = connection.getResponseCode()
            if (responseCode == HttpURLConnection.HTTP_OK) { // 200 OK
                inputStream = connection.getInputStream()
                val bos = ByteArrayOutputStream()
                var read = 0
                while (inputStream.read().also { read = it } != -1) {
                    bos.write(read)
                }
                bos.close()
                data = bos.toString()
            } else {
                data = connection.getResponseMessage() + ". Error Code: " + responseCode
            }
            connection.disconnect()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (inputStream != null) {
                inputStream.close()
            }
        }
        return data
    }
}