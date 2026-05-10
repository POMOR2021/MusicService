package com.example.myapplication.fragments

import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.adapters.FavoriteAdapter
import com.example.myapplication.adapters.TrackAdapter
import com.example.myapplication.databinding.FragmentFavoriteBinding
import com.example.myapplication.viewModels.TrackViewModel
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.coroutines.launch
import kotlin.getValue


class FavoriteFragment : Fragment() {

    private lateinit var adapter: FavoriteAdapter
    private lateinit var binding: FragmentFavoriteBinding
    private val viewModel: TrackViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = FavoriteAdapter(
            onFavoriteClick = { track ->
                viewModel.toggleFavorite(track)
            }
        )

        binding.musicRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.musicRecycler.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.favoriteTracks.collect { list ->
                    adapter.submitList(list)
                    val count = list.size
                    binding.emptyRecycler.isVisible = count == 0
                }
            }
        }



    }

}