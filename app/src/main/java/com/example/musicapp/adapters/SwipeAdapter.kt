package com.example.musicapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import com.example.musicapp.data.entities.Song
import com.example.musicapp.databinding.SwipeItemBinding
import javax.inject.Inject

class SwipeAdapter @Inject constructor() : BaseSongAdapter<Song, SwipeItemBinding>() {

    override fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup): SwipeItemBinding {
        return SwipeItemBinding.inflate(inflater, parent, false)
    }

    override var differ = AsyncListDiffer(this, diffCallback)

    override fun bind(binding: SwipeItemBinding, song: Song) {
        binding.tvPrimary.text = "${song.title} - ${song.subtitle}"
        binding.tvPrimary.isSelected = true
    }

    override fun setItemClickListener(song: Song, position: Int) {
        onItemClickListener?.let { click -> click(song) }
    }

    private var onItemClickListener: ((Song) -> Unit)? = null
    fun setItemClickListener(listener: (Song) -> Unit) {
        onItemClickListener = listener
    }
}