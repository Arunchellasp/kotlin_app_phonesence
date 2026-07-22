package com.example.homosep

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.rtsp.RtspMediaSource
import androidx.recyclerview.widget.GridLayoutManager
import com.example.homosep.databinding.FragmentRtspStreamsBinding

class RtspStreamsFragment : Fragment() {

    private var _binding: FragmentRtspStreamsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: RtspStreamAdapter
    private val streamsList = mutableListOf<String>()

    private var fullscreenPlayer: ExoPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRtspStreamsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.streamsRecyclerView.layoutManager = GridLayoutManager(context, 2)
        adapter = RtspStreamAdapter(
            requireContext(),
            streamsList,
            onStreamClicked = { url ->
                openFullscreen(url)
            },
            onStreamRemoved = { position ->
                if (position in 0 until streamsList.size) {
                    streamsList.removeAt(position)
                    adapter.notifyItemRemoved(position)
                    updateEmptyState()
                }
            }
        )
        binding.streamsRecyclerView.adapter = adapter
        updateEmptyState()

        binding.addStreamButton.setOnClickListener {
            val url = binding.rtspUrlInput.text.toString().trim()
            if (url.isNotEmpty()) {
                streamsList.add(url)
                adapter.notifyItemInserted(streamsList.size - 1)
                binding.rtspUrlInput.text?.clear()
                updateEmptyState()
            }
        }

        binding.backToGridButton.setOnClickListener {
            closeFullscreen()
        }
    }

    private fun updateEmptyState() {
        if (streamsList.isEmpty()) {
            binding.noStreamsText.visibility = View.VISIBLE
            binding.streamsRecyclerView.visibility = View.GONE
        } else {
            binding.noStreamsText.visibility = View.GONE
            binding.streamsRecyclerView.visibility = View.VISIBLE
        }
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun openFullscreen(url: String) {
        binding.gridContainer.visibility = View.GONE
        binding.fullscreenContainer.visibility = View.VISIBLE

        fullscreenPlayer = ExoPlayer.Builder(requireContext()).build()
        binding.fullscreenPlayerView.player = fullscreenPlayer
        binding.fullscreenPlayerView.useController = true // Enable controls for fullscreen

        val mediaSource = RtspMediaSource.Factory()
            .setForceUseRtpTcp(true)
            .createMediaSource(MediaItem.fromUri(Uri.parse(url)))
            
        fullscreenPlayer?.setMediaSource(mediaSource)
        fullscreenPlayer?.prepare()
        fullscreenPlayer?.playWhenReady = true
    }

    private fun closeFullscreen() {
        fullscreenPlayer?.release()
        fullscreenPlayer = null
        binding.fullscreenPlayerView.player = null

        binding.fullscreenContainer.visibility = View.GONE
        binding.gridContainer.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter.releaseAll()
        fullscreenPlayer?.release()
        _binding = null
    }
}
