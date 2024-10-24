package com.example.musicapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.example.musicapp.R
import com.example.musicapp.data.entities.Song
import com.example.musicapp.databinding.FragmentSongBinding
import com.example.musicapp.exoPlayer.isPlaying
import com.example.musicapp.exoPlayer.toSong
import com.example.musicapp.other.Status
import com.example.musicapp.other.Status.SUCCESS
import com.example.musicapp.ui.viewModels.MainViewModel
import com.example.musicapp.ui.viewModels.SongViewModel
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class SongFragment : Fragment() {
    @Inject
    lateinit var glide: RequestManager
    private lateinit var binding: FragmentSongBinding
    private lateinit var mainViewModel: MainViewModel
    private val songViewModel = viewModels<SongViewModel>()

    private var currentPlayingSong: Song? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSongBinding.inflate(inflater)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()
        handleSeekBar()
        onButtonsClick()
    }

    private fun updateTitleAndImage(song: Song) {
        binding.tvSongName.text = song.title
        glide.load(song.imageUrl).into(binding.ivSongImage)
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner) {
            it?.let { result ->
                when (result.status) {
                    SUCCESS -> {
                        result.data?.let { songs ->
                            if (currentPlayingSong == null && songs.isNotEmpty()) {
                                currentPlayingSong = songs[0]
                                updateTitleAndImage(songs[0])
                            }
                        }
                    }

                    else -> Unit
                }
            }
        }
        mainViewModel.currentPlayingSong.observe(viewLifecycleOwner) {
            it?.let {
                currentPlayingSong = it.toSong()
                updateTitleAndImage(currentPlayingSong!!)
            }
        }
        mainViewModel.playbackState.observe(viewLifecycleOwner) {
            it?.let {
                binding.ivPlayPauseDetail.setImageResource(
                    if (it.isPlaying) R.drawable.ic_pause
                    else R.drawable.ic_play
                )
                binding.seekBar.progress = it.position.toInt()
            }
        }
    }

    private var shouldUpdateSeekBar = true
    private fun handleSeekBar() {
        songViewModel.value.currentSongDuration.observe(viewLifecycleOwner) {
            binding.seekBar.max = it.toInt()
            setTimeToTextView(it, binding.tvSongDuration)
        }
        songViewModel.value.currentPlayerPosition.observe(viewLifecycleOwner) {
            if (shouldUpdateSeekBar) {
                binding.seekBar.progress = it.toInt()
                setTimeToTextView(it, binding.tvCurTime)
            }
        }
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?, progress: Int, fromUser: Boolean
            ) {
                if (fromUser) {
                    setTimeToTextView(progress.toLong(), binding.tvCurTime)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                shouldUpdateSeekBar = false
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    mainViewModel.seekTo(it.progress.toLong())
                    shouldUpdateSeekBar = true
                }
            }
        })
    }

    private fun setTimeToTextView(ms: Long, setText: MaterialTextView) {
        val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        setText.text = dateFormat.format(ms)
    }

    private fun onButtonsClick() {
        binding.ivPlayPauseDetail.setOnClickListener {
            mainViewModel.playOrToggleSong(currentPlayingSong ?: return@setOnClickListener)
        }
        binding.ivSkip.setOnClickListener {
            mainViewModel.skipToNextSong()
            mainViewModel.playOrToggleSong(currentPlayingSong ?: return@setOnClickListener)
        }
        binding.ivSkipPrevious.setOnClickListener {
            mainViewModel.skipToPrevSong()
        }
    }
}