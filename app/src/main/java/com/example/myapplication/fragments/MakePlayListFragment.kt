package com.example.myapplication.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapters.MusicChoosePlaylistAdapter
import com.example.myapplication.databinding.FragmentMakePlayListBinding
import com.example.myapplication.viewModels.TrackViewModel
import kotlinx.coroutines.launch
import kotlin.getValue

class MakePlayListFragment : Fragment() {

    private lateinit var binding: FragmentMakePlayListBinding
    private lateinit var adapter: MusicChoosePlaylistAdapter
    private val viewModel: TrackViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMakePlayListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = MusicChoosePlaylistAdapter()

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.playlistRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.playlistRecycler.adapter = adapter
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tracks.collect { list ->
                    adapter.submitList(list)
                }
            }
        }
    }
}