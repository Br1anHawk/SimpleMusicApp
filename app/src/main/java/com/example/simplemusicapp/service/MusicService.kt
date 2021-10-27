package com.example.simplemusicapp.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import com.example.simplemusicapp.data.entities.Song
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer

@RequiresApi(Build.VERSION_CODES.O)
class MusicService: Service() {

    private var musicState = MusicState.STOP
    private var musicMediaPlayer: ExoPlayer? = null

    private val songs: MutableList<Song> = mutableListOf()
    private var currentWindow = 0
    private var playbackPosition = 0L

    private val binder by lazy { MusicBinder() }

    private val notification by lazy { Notification(this) }

    override fun onBind(intent: Intent?): IBinder = binder

    fun getState(): MusicState {
        return musicState
    }

    fun runAction(state: MusicState) {
        //musicState = state
        when (state) {
            MusicState.PLAY -> {
                startMusic()
                musicState = MusicState.PLAY
            }
            MusicState.PAUSE -> {
                pauseMusic()
            }
            MusicState.STOP -> {
                stopMusic()
                musicState = MusicState.STOP
            }
            MusicState.NEXT_SONG -> nextSong()
            MusicState.PREVIOUS_SONG -> previousSong()
        }
        notification.updateNotification(getCurrentSong())
    }

    fun addSongs(songs: List<Song>) {
        this.songs.addAll(songs)
    }

    fun getCurrentSong(): Song? {
        if (songs.isEmpty()) return null
        return songs[currentWindow]
    }

    private fun initializeMediaPlayer() {
        musicMediaPlayer = SimpleExoPlayer.Builder(this)
            .build()
            .also { exoPlayer ->
                //viewBinding.videoView.player = exoPlayer
                //viewBinding.videoView.defaultArtwork = resources.getDrawable(R.drawable.google_logo)
                //val mediaItem = MediaItem.fromUri(getString(R.string.media_url_mp4))
                //exoPlayer.setMediaItem(mediaItem)
                //val secondMediaItem = MediaItem.fromUri(getString(R.string.media_url_mp3))
                //exoPlayer.addMediaItem(secondMediaItem)

                songs.forEach {
                    exoPlayer.addMediaItem(MediaItem.fromUri(it.source))
                }

                exoPlayer.playWhenReady = true
                exoPlayer.seekTo(currentWindow, playbackPosition)
                exoPlayer.prepare()
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startMusic() {
        initializeMediaPlayer()
        musicMediaPlayer?.play()
        startForeground(Notification.NOTIFICATION_ID, notification.getNotification())
    }

    private fun pauseMusic() {
        musicMediaPlayer?.pause()
    }

    private fun nextSong() {
        musicMediaPlayer?.seekToNext()
        if (currentWindow < songs.size - 1) currentWindow++
        playbackPosition = 0L
    }

    private fun previousSong() {
        musicMediaPlayer?.seekToPrevious()
        if (currentWindow > 0) currentWindow--
        playbackPosition = 0L
    }

    private fun stopMusic() {
        musicMediaPlayer?.run {
            playbackPosition = this.currentPosition
            currentWindow = this.currentWindowIndex
            stop()
            release()
        }
        stopForeground(true)
    }

    inner class MusicBinder : Binder() {

        fun getService(): MusicService = this@MusicService
    }
}