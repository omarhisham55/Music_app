package com.example.musicapp.ui.viewModels

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_TITLE
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.musicapp.data.entities.Song
import com.example.musicapp.exoPlayer.MusicServiceConnection
import com.example.musicapp.exoPlayer.isPlayEnabled
import com.example.musicapp.exoPlayer.isPlaying
import com.example.musicapp.exoPlayer.isPrepared
import com.example.musicapp.other.Constants.MEDIA_ROOT_ID
import com.example.musicapp.other.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val musicServiceConnection: MusicServiceConnection
) : ViewModel() {
    private val _mediaItems = MutableLiveData<Resource<List<Song>>>()
    val mediaItems: LiveData<Resource<List<Song>>> = _mediaItems

    val isConnected = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.networkError
    val currentPlayingSong = musicServiceConnection.currentPlayingSong
    val playbackState = musicServiceConnection.playbackState

    var init = true

    init {
        _mediaItems.postValue(Resource.loading(null))
        musicServiceConnection.subscribe(
            MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {
                override fun onChildrenLoaded(
                    parentId: String,
                    children: MutableList<MediaBrowserCompat.MediaItem>,
                ) {
                    super.onChildrenLoaded(parentId, children)
                    val items = children.map {
                        Song(
                            it.mediaId!!,
                            it.description.title.toString(),
                            it.description.subtitle.toString(),
                            it.description.mediaUri.toString(),
                            it.description.iconUri.toString()
                        )
                    }
                    _mediaItems.postValue(Resource.success(items))
                }
            })
    }

    fun skipToNextSong() = musicServiceConnection.transportControls?.skipToNext()
    fun skipToPrevSong() = musicServiceConnection.transportControls?.skipToPrevious()
    fun seekTo(pos: Long) = musicServiceConnection.transportControls?.seekTo(pos)

    fun playOrToggleSong(mediaItem: Song) {
        if (init) return
        val isPrepared = playbackState.value?.isPrepared ?: false
        if (isPrepared && mediaItem.songId == currentPlayingSong.value?.getString(
                METADATA_KEY_MEDIA_ID
            )
        ) {
            playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> musicServiceConnection.transportControls?.pause()
                    playbackState.isPlayEnabled -> musicServiceConnection.transportControls?.play()
                    else -> Unit
                }
            }
        } else {
            musicServiceConnection.transportControls?.playFromMediaId(mediaItem.songId, null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unsubscribe(MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {})
    }

}