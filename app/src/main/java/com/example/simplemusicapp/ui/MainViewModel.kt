package com.example.simplemusicapp.ui

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplemusicapp.data.entities.Music
import com.example.simplemusicapp.data.entities.Song
import com.example.simplemusicapp.service.MusicService
import com.example.simplemusicapp.service.MusicState
import com.example.simplemusicapp.service.SongsApi
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {

    var musicService: MusicService? = null

    private val _music = MutableLiveData<Music>()
    val music: LiveData<Music>
        get() = _music

    var isMusicServiceBound: Boolean = false

    init {
        getSongs()
    }

    private fun getSongs() {
        viewModelScope.launch {
            try {
                _music.value = SongsApi.retrofitService.getSongs()
            } catch (e: Exception) {
                Log.e("CONNECTION_TO_SONGS_API", e.stackTraceToString())
            }
        }
    }

}