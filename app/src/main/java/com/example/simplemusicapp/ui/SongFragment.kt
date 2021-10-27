package com.example.simplemusicapp.ui

import com.example.simplemusicapp.service.MusicState
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.simplemusicapp.R
import com.example.simplemusicapp.databinding.FragmentSongBinding
import com.example.simplemusicapp.service.MusicService

class SongFragment : Fragment() {
    private var _binding: FragmentSongBinding? = null
    private val binding get() = _binding!!

    private val boundServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder: MusicService.MusicBinder = service as MusicService.MusicBinder
            musicService = binder.getService()
            mainViewModel.isMusicServiceBound = true
            mainViewModel.musicService = musicService
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            musicService?.runAction(MusicState.STOP)
            musicService = null
            mainViewModel.isMusicServiceBound = false
        }
    }

    private val mainViewModel: MainViewModel by viewModels()

    private var musicService: MusicService? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSongBinding.inflate(inflater, container, false)

        musicService = mainViewModel.musicService
        setUI()
        if (!mainViewModel.isMusicServiceBound) {
            bindToMusicService()
        }

        mainViewModel.music.observe(viewLifecycleOwner) {
            musicService?.addSongs(it.music)
        }

        binding.buttonPlayAndPause.setOnClickListener {
            when (musicService?.getState()) {
                MusicState.PLAY -> {
                    sendCommandToBoundService(MusicState.STOP)
                    binding.buttonPlayAndPause.setImageDrawable(resources.getDrawable(R.drawable.ic_play, activity?.theme))

                }
                MusicState.STOP -> {
                    sendCommandToBoundService(MusicState.PLAY)
                    binding.buttonPlayAndPause.setImageDrawable(resources.getDrawable(R.drawable.ic_pause, activity?.theme))
                    setUI()
                }
            }
        }

        binding.buttonNextSong.setOnClickListener {
            sendCommandToBoundService(MusicState.NEXT_SONG)
            setUI()
        }

        binding.buttonPreviousSong.setOnClickListener {
            sendCommandToBoundService(MusicState.PREVIOUS_SONG)
            setUI()
        }

        return binding.root
    }


    private fun setUI() {
        musicService?.let { musicService ->
            when (musicService.getState()) {
                MusicState.PLAY -> {
                    binding.buttonPlayAndPause.setImageDrawable(resources.getDrawable(R.drawable.ic_pause, activity?.theme))
                }
                MusicState.STOP -> {
                    binding.buttonPlayAndPause.setImageDrawable(resources.getDrawable(R.drawable.ic_play, activity?.theme))
                }
            }
            Glide.with(binding.imageViewPreview.context)
                .load(
                    musicService.getCurrentSong()?.let {
                        it.image.toUri().buildUpon().scheme("https").build()
                    }
                )
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.ic_image)
                )
                .into(binding.imageViewPreview)
            binding.textViewTitle.text = musicService.getCurrentSong()?.title ?: ""
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        //unbindMusicService()

    }

    // Bound Service Methods

    private fun bindToMusicService() {
        context?.bindService(Intent(activity, MusicService::class.java), boundServiceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun unbindMusicService() {
        if (mainViewModel.isMusicServiceBound) {
            // stop the audio
            musicService?.runAction(MusicState.STOP)

            // disconnect the service and save state
            context?.unbindService(boundServiceConnection)
            mainViewModel.isMusicServiceBound = false
        }
    }



    private fun sendCommandToBoundService(state: MusicState) {
        if (mainViewModel.isMusicServiceBound) {
            musicService?.runAction(state)
            //informUser(state)
            //enableButtons(state)
        } else {
            //Toast.makeText(this, R.string.service_is_not_bound, Toast.LENGTH_SHORT).show()
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}