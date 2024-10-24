package com.example.musicapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import com.example.musicapp.R
import com.example.musicapp.adapters.SwipeAdapter
import com.example.musicapp.data.entities.Song
import com.example.musicapp.databinding.ActivityMainBinding
import com.example.musicapp.exoPlayer.isPlaying
import com.example.musicapp.exoPlayer.toSong
import com.example.musicapp.other.Status.*
import com.example.musicapp.ui.viewModels.MainViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.Timer
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    fun getBinding(): ActivityMainBinding = binding
    private val mainViewModel by viewModels<MainViewModel>()

    @Inject
    lateinit var glide: RequestManager

    @Inject
    lateinit var swipeAdapter: SwipeAdapter

    private var currentPlayingSong: Song? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.vpSong.adapter = swipeAdapter
        setContentView(binding.root)
        setNavigation()
        viewPagerListener()
        togglePlayButton()
        subscribeToObservers()
    }

    private fun setNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        swipeAdapter.setItemClickListener {
            navController.navigate(R.id.globalActionToSongFragment)
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.songFragment -> hideBottomBar()
                R.id.homeFragment -> showBottomBar()
                else -> showBottomBar()
            }
        }
    }

    private fun togglePlayButton() {
        binding.ivPlayPause.setOnClickListener {
            currentPlayingSong?.let {
                mainViewModel.playOrToggleSong(it)
            }
        }
    }

    private fun hideBottomBar() {
        binding.vpSong.isVisible = false
        binding.ivPlayPause.isVisible = false
        binding.ivCurSongImage.isVisible = false
    }

    private fun showBottomBar() {
        binding.vpSong.isVisible = true
        binding.ivPlayPause.isVisible = true
        binding.ivCurSongImage.isVisible = true
    }

    private fun viewPagerListener() {
        binding.vpSong.setCurrentItem(1, false)
        binding.vpSong.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val newSong = swipeAdapter.songs[position]
                currentPlayingSong = newSong
                glide.load(newSong.imageUrl).into(binding.ivCurSongImage)
                mainViewModel.playOrToggleSong(currentPlayingSong ?: return)
                mainViewModel.init = false
            }
        })
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(this) {
            it?.let { result ->
                when (result.status) {
                    SUCCESS -> {
                        result.data?.let { songs ->
                            swipeAdapter.songs = songs
                            if (songs.isNotEmpty()) {
                                glide.load((currentPlayingSong ?: songs[0]).imageUrl)
                                    .into(binding.ivCurSongImage)
                            }
                        }
                    }

                    else -> Unit
                }
            }
        }
        mainViewModel.currentPlayingSong.observe(this) {
            if (it == null) return@observe
            currentPlayingSong = it.toSong()
            glide.load(currentPlayingSong?.imageUrl).into(binding.ivCurSongImage)
        }
        mainViewModel.playbackState.observe(this) {
            it?.let {
                binding.ivPlayPause.setImageResource(
                    if (it.isPlaying) R.drawable.ic_pause
                    else R.drawable.ic_play
                )

            }
        }
        mainViewModel.isConnected.observe(this) {
            it?.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    ERROR -> Snackbar.make(
                        binding.root,
                        result.message ?: "An unknown error occurred",
                        Snackbar.LENGTH_LONG
                    ).show()

                    else -> Unit
                }
            }
        }
        mainViewModel.networkError.observe(this) {
            it?.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    ERROR -> Snackbar.make(
                        binding.root,
                        result.message ?: "An unknown error occurred",
                        Snackbar.LENGTH_LONG
                    ).show()

                    else -> Unit
                }
            }
        }
    }
}