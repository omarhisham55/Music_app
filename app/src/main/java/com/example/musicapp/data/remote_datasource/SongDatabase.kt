package com.example.musicapp.data.remote_datasource

import android.util.Log
import com.example.musicapp.data.entities.Song
import com.example.musicapp.other.Constants.SONG_COLLECTION
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class SongDatabase {
    private val _firestore = FirebaseFirestore.getInstance()

    suspend fun getAllSongs(): List<Song> {
        val songsRef = _firestore.collection(SONG_COLLECTION)
        return try {
            val response = songsRef.get().await().toObjects(Song::class.java)
            response
        } catch (e: Exception) {
            Timber.tag("Firebase exception:").d(e.message.toString())
            emptyList()
        }
    }
}