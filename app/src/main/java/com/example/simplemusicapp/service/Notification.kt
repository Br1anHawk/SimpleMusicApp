package com.example.simplemusicapp.service

import android.app.Notification
import android.app.Notification.EXTRA_NOTIFICATION_ID
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.session.MediaSession
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.simplemusicapp.*
import com.example.simplemusicapp.data.entities.Song

@RequiresApi(Build.VERSION_CODES.O)
class Notification(private val context: Context) {

    private val mediaSession = MediaSession(context, "tag")

    val nextSongIntent = Intent(context, MainActivity::class.java).apply {
        action = "next"
        putExtra(EXTRA_NOTIFICATION_ID, 0)
    }
    val nextSongPendingIntent: PendingIntent =
        PendingIntent.getBroadcast(context, 0, nextSongIntent, 0)


    private val notificationBuilder: Notification.Builder by lazy {
        Notification.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentIntent(contentIntent)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setStyle(Notification.MediaStyle().setMediaSession(mediaSession.sessionToken))
            .addAction(R.drawable.ic_skip, "skip", nextSongPendingIntent)
    }

    private val notificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private val contentIntent by lazy {
        PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun getNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(createChannel())
        }

        return notificationBuilder.build()
    }

    fun updateNotification(song: Song?) {
        song?.let {
            notificationBuilder.setContentText(it.title)
            Glide.with(context)
                .asBitmap()
                .load( it.image.toUri().buildUpon().scheme("https").build())
                .into(object : CustomTarget<Bitmap>(){
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        notificationBuilder.setLargeIcon(resource)
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {
                        // this is called when imageView is cleared on lifecycle call or for
                        // some other reason.
                        // if you are referencing the bitmap somewhere else too other than this imageView
                        // clear it here as you can no longer have the bitmap
                    }
                })

        }
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun createChannel() =
        NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = CHANNEL_DESCRIPTION
            setSound(null, null)
        }

    companion object {
        const val NOTIFICATION_ID = 99
    }
}