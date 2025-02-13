package org.unifiedpush.example

import android.content.Context
import android.content.Intent
import android.os.Looper
import android.widget.Toast
import org.unifiedpush.android.connector.MessagingReceiver
import org.unifiedpush.android.connector.MessagingReceiverHandler
import java.net.URLDecoder

class CustomMessagingHandler(
    context: Context,
    workerParams: androidx.work.WorkerParameters
) : MessagingReceiverHandler(context, workerParams) {
    override fun onMessage(context: Context?, message: String, instance: String) {
        val dict = URLDecoder.decode(message, "UTF-8").split("&")
        val params = dict.associate {
            try {
                it.split("=")[0] to it.split("=")[1]
            } catch (e: Exception) {
                "" to ""
            }
        }
        val text = params["message"] ?: "New notification"
        val priority = params["priority"]?.toInt() ?: 8
        val title = params["title"] ?: context!!.getString(R.string.app_name)
        Notifier(context!!).sendNotification(title, text, priority)
    }

    override fun onNewEndpoint(context: Context?, endpoint: String, instance: String) {
        val broadcastIntent = Intent()
        broadcastIntent.`package` = context!!.packageName
        broadcastIntent.action = UPDATE
        broadcastIntent.putExtra("endpoint", endpoint)
        broadcastIntent.putExtra("registered", "true")
        context.sendBroadcast(broadcastIntent)
    }

    override fun onRegistrationFailed(context: Context?, instance: String) {
        if(Looper.myLooper() == null) Looper.prepare()
        Toast.makeText(context, "Registration Failed", Toast.LENGTH_SHORT).show()
    }

    override fun onRegistrationRefused(context: Context?, instance: String) {
        if(Looper.myLooper() == null) Looper.prepare()
        Toast.makeText(context, "Registration is Refused", Toast.LENGTH_SHORT).show()
    }

    override fun onUnregistered(context: Context?, instance: String) {
        val broadcastIntent = Intent()
        broadcastIntent.`package` = context!!.packageName
        broadcastIntent.action = UPDATE
        broadcastIntent.putExtra("endpoint", "")
        broadcastIntent.putExtra("registered", "false")
        context.sendBroadcast(broadcastIntent)
        val appName = context.getString(R.string.app_name)
        if(Looper.myLooper() == null) Looper.prepare()
        Toast.makeText(context, "$appName is unregistered", Toast.LENGTH_SHORT).show()
    }
}

class CustomReceiver : MessagingReceiver(CustomMessagingHandler::class.java)
