package com.example.musicapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.musicapp.data.entities.Song

abstract class BaseSongAdapter<T, bind : ViewBinding>() :
    RecyclerView.Adapter<BaseSongAdapter<T, bind>.SongViewHolder>() {
    protected abstract fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup): bind
    protected abstract fun bind(binding: bind, song: Song)

    inner class SongViewHolder(val binding: bind) : RecyclerView.ViewHolder(binding.root)


    protected val diffCallback = object : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean =
            oldItem.songId == newItem.songId

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean =
            oldItem.hashCode() == newItem.hashCode()
    }

    protected abstract var differ: AsyncListDiffer<Song>

    var songs: List<Song>
        get() = differ.currentList
        set(value) = differ.submitList(value)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = inflateBinding(inflater, parent)
        return SongViewHolder(binding)
    }

    override fun getItemCount(): Int = songs.size

    protected abstract fun setItemClickListener(song: Song, position: Int)

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.itemView.apply {
            bind(holder.binding, songs[position])
            setOnClickListener { setItemClickListener(songs[position], position) }
        }
    }
}