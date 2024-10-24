package com.example.musicapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import com.bumptech.glide.RequestManager
import com.example.musicapp.data.entities.Song
import com.example.musicapp.databinding.ListItemBinding
import javax.inject.Inject

class SongAdapter @Inject constructor(
    private val glide: RequestManager,
) : BaseSongAdapter<Song, ListItemBinding>() {

    override fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup): ListItemBinding {
        return ListItemBinding.inflate(inflater, parent, false)
    }

    override var differ = AsyncListDiffer<Song>(this, diffCallback)

    override fun bind(binding: ListItemBinding, song: Song) {
        binding.tvPrimary.text = song.title
        binding.tvSecondary.text = song.subtitle
        glide.load(song.imageUrl).into(binding.ivItemImage)
    }

    override fun setItemClickListener(song: Song, position: Int) {
        onItemClickListener?.let { click -> click(song, position) }
    }

    private var onItemClickListener: ((Song, position: Int) -> Unit)? = null
    fun setItemClickListener(listener: (Song, position: Int) -> Unit) {
        onItemClickListener = listener
    }
}