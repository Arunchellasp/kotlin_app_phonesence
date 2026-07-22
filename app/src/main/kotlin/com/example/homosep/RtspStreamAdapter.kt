package com.example.homosep

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.rtsp.RtspMediaSource
import androidx.recyclerview.widget.RecyclerView
import com.example.homosep.databinding.ItemRtspStreamBinding

class RtspStreamAdapter(
    private val context: Context,
    private val streams: MutableList<String>,
    private val onStreamClicked: (String) -> Unit,
    private val onStreamRemoved: (Int) -> Unit
) : RecyclerView.Adapter<RtspStreamAdapter.StreamViewHolder>() {

    private val activePlayers = mutableSetOf<ExoPlayer>()

    class StreamViewHolder(val binding: ItemRtspStreamBinding) : RecyclerView.ViewHolder(binding.root) {
        var player: ExoPlayer? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StreamViewHolder {
        val binding = ItemRtspStreamBinding.inflate(LayoutInflater.from(context), parent, false)
        return StreamViewHolder(binding)
    }

    override fun getItemCount(): Int = streams.size

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun onBindViewHolder(holder: StreamViewHolder, position: Int) {
        val url = streams[position]

        holder.player?.let { 
            activePlayers.remove(it)
            it.release() 
        }
        
        val player = ExoPlayer.Builder(context).build()
        holder.player = player
        activePlayers.add(player)
        
        holder.binding.playerView.player = player
        holder.binding.playerView.useController = false 

        val mediaSource = RtspMediaSource.Factory()
            .setForceUseRtpTcp(true)
            .createMediaSource(MediaItem.fromUri(Uri.parse(url)))
            
        player.setMediaSource(mediaSource)
        player.prepare()
        player.playWhenReady = true

        holder.binding.clickOverlay.setOnClickListener {
            onStreamClicked(url)
        }

        holder.binding.removeStreamButton.setOnClickListener {
            val currentPosition = holder.bindingAdapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                onStreamRemoved(currentPosition)
            }
        }
    }

    override fun onViewRecycled(holder: StreamViewHolder) {
        super.onViewRecycled(holder)
        holder.player?.let {
            activePlayers.remove(it)
            it.release()
        }
        holder.player = null
        holder.binding.playerView.player = null
    }

    fun releaseAll() {
        activePlayers.forEach { it.release() }
        activePlayers.clear()
    }
}
