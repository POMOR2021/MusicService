package com.example.myapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.currentPosition.collect{ position ->
                    val totalProgress = viewModel.currentTrack.value?.durationMs ?: 0L
                    updateProgress(position, totalProgress)

                    binding.tvProgress.text = position.milliseconds.toComponents { minutes, seconds, _ ->
                        String.format("%02d:%02d", minutes, seconds)
                    }
                }
            }
        }
        binding.playButton.setOnClickListener {
            viewModel.togglePlayPause()
        }
        binding.previous.setOnClickListener {
            viewModel.seekToPreviousMediaItem()
        }
        binding.next.setOnClickListener {
            viewModel.seekToNextMediaItem()
        }

    }
    override fun onStart() {
        super.onStart()
        val dialog = dialog as? BottomSheetDialog

        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as? FrameLayout

        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isFitToContents = true
            behavior.skipCollapsed = true
            it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
    }

    fun updateProgress(currentMs: Long, durationMs: Long) {
        if (durationMs > 0) {
            val progress = (currentMs * 100 / durationMs).toInt()

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                binding.progressBar.setProgress(progress, true)
            } else {
                binding.progressBar.progress = progress
            }

        }
    }
}