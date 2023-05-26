package ru.mirea.ivashechkinav.timeservice

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

object SocketUtils {
    /**
     * BufferedReader для получения входящих данных
     */
    @Throws(IOException::class)
    fun getReader(s: Socket): BufferedReader {
        return BufferedReader(InputStreamReader(s.getInputStream()))
    }

    /**
     * Makes a PrintWriter to send outgoing data. This PrintWriter will
     * automatically flush stream when println is called.
     * В примере не используется
     */
    @Throws(IOException::class)
    fun getWriter(s: Socket): PrintWriter {
    // Second argument of true means autoflush.
        return PrintWriter(s.getOutputStream(), true)
    }
}