package com.example.myapplication.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.Player
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.databinding.FullPlayerSheetBinding
import com.example.myapplication.viewModels.TrackViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class FullPlayerSheet : BottomSheetDialogFragment() {

    private val viewModel: TrackViewModel by activityViewModels()
    private lateinit var binding: FullPlayerSheetBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FullPlayerSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentTrack.collect { track ->
                    track?.let {

                        binding.title.text = it.title
                        binding.title.setSelected(true)
                        binding.artist.text = it.artist

                        Glide.with(requireContext())
                            .load(it.coverUri)
                            .placeholder(R.drawable.default_album_art)
                            .into(binding.fullAlbumImage)

                    }

                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isPlaying.collect { playing ->
                    if (playing) {
                        binding.playButton.setImageResource(R.drawable.icon_pause)
                    } else {
                        binding.playButton.setImageResource(R.drawable.icon_play)
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentPosition.collect { position ->
                    binding.progressBar.progress = position.toInt()
                    binding.tvProgress.text =
                        position.milliseconds.toComponents { minutes, seconds, _ ->
                            String.format("%02d:%02d", minutes, seconds)
                        }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.duration.collect { duration ->
                    binding.progressBar.max = duration.toInt()
                }
            }
        }
        binding.progressBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                p0: SeekBar?,
                p1: Int,
                p2: Boolean
            ) {
                if (p2) {
                    viewModel.seekTo(p1.toLong())
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })
        binding.playButton.setOnClickListener {
            viewModel.togglePlayPause()
        }
        binding.previous.setOnClickListener {
            viewModel.seekToPreviousMediaItem()
        }
        binding.next.setOnClickListener {
            viewModel.seekToNextMediaItem()
        }

        binding.repeatButton.setOnClickListener {
            viewModel.setRepeatOnTrack()
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.repeatMode.collect { mode ->
                mode?.let {
                    updateRepeatIcon(mode)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? BottomSheetDialog
        val bottomSheet =
            dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as? FrameLayout

        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isFitToContents = true
            behavior.skipCollapsed = true
            it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
    }

    private fun updateRepeatIcon(mode: Int) {
        val iconRes = when (mode) {
            Player.REPEAT_MODE_ONE -> R.drawable.icon_repeat_1
            else -> R.drawable.icon_repeat
        }
        binding.repeatButton.setIconResource(iconRes)
    }
}