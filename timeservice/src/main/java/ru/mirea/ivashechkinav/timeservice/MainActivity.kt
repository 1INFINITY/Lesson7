package ru.mirea.ivashechkinav.timeservice

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.mirea.ivashechkinav.timeservice.databinding.ActivityMainBinding
import java.io.BufferedReader
import java.io.IOException
import java.net.Socket


class MainActivity : AppCompatActivity() {
    private val TAG = this::class.simpleName
    private lateinit var binding: ActivityMainBinding
    private val host = "time-a.nist.gov" // или time-a.nist.gov

    private val port = 13
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnStart.setOnClickListener {
            lifecycleScope.launch {
                textViewUpdate()
            }
        }
    }

    private suspend fun timeTaskLaunch() = withContext(Dispatchers.IO) {
        var timeResult = ""
        try {
            val socket = Socket(host, port)
            val reader: BufferedReader = SocketUtils.getReader(socket)
            reader.readLine() // игнорируем первую строку
            timeResult = reader.readLine() // считываем вторую строку
            Log.d(TAG, timeResult)
            socket.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return@withContext timeResult
    }
    private suspend fun textViewUpdate() {
        val result = timeTaskLaunch()
        binding.textView.text = result
    }
}