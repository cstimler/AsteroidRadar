package com.udacity.asteroidradar.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.databinding.AsteroidViewItemBinding

class AsteroidAdapter(val onClickListener: OnClickListener) : ListAdapter<Asteroid, AsteroidAdapter.AsteroidViewHolder>(DiffCallback)  {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AsteroidAdapter.AsteroidViewHolder {
        return AsteroidViewHolder(AsteroidViewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: AsteroidAdapter.AsteroidViewHolder, position: Int) {
        val asteroid = getItem(position)
        holder.bind(asteroid)
        holder.itemView.setOnClickListener {
            onClickListener.onClick(asteroid)
        }
    }

    class AsteroidViewHolder(private var binding: AsteroidViewItemBinding) :
            RecyclerView.ViewHolder(binding.root) {
                fun bind(asteroid: Asteroid) {
                    binding.asteroid = asteroid
                    binding.executePendingBindings()
                }
            }

    class OnClickListener(val clickListener: (asteroid: Asteroid) -> Unit) {
        fun onClick(asteroid: Asteroid) = clickListener(asteroid)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Asteroid>() {
        override fun areItemsTheSame(oldItem: Asteroid, newItem: Asteroid): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Asteroid, newItem: Asteroid): Boolean {
            return oldItem.id == newItem.id
        }
    }



}