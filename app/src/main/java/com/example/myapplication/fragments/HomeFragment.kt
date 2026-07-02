package com.example.myapplication.fragments

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.adapters.TrackAdapter
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.db.TrackDatabase
import com.example.myapplication.models.Track
import com.example.myapplication.player.PlayerProvider
import com.example.myapplication.viewModels.TrackViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: TrackViewModel by activityViewModels()
    private lateinit var adapter: TrackAdapter
    private val pickAudio =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let { safeUri ->
                val filePath = safeUri.toString()
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                requireContext().contentResolver.takePersistableUriPermission(safeUri, takeFlags)
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    val exists = TrackDatabase.getDatabase(requireContext()).trackDao()
                        .isSongAlreadyAdded(filePath)
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                        if (exists) {
                            Toast.makeText(
                                requireContext(),
                                "Трек уже был добавлен",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            processAudio(safeUri)
                        }

                    }
                }
            }
        }

    val swipeHandler =
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                p0: RecyclerView,
                p1: RecyclerView.ViewHolder,
                p2: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(
                p0: RecyclerView.ViewHolder,
                p1: Int
            ) {
                val position = p0.adapterPosition
                val track = adapter.currentList[position]

                viewModel.deleteTracks(track)
            }

        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        binding.FAB.setOnClickListener {
            pickAudio.launch(arrayOf("audio/*"))
        }

        adapter = TrackAdapter(
            onClick = { track ->
                viewModel.playTrack(track)
            },
            onFavoriteClick = { track ->
                viewModel.toggleFavorite(track)
            }
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isPlaying.collect { playing ->
                    if (playing) {
                        binding.playButton.setIconResource(R.drawable.icon_pause)
                    } else {
                        binding.playButton.setIconResource(R.drawable.icon_play)
                    }
                }
            }
        }

        binding.musicRecycler.layoutManager = LinearLayoutManager(requireContext())
        itemTouchHelper.attachToRecyclerView(binding.musicRecycler)
        binding.musicRecycler.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tracks.collect { list ->
                    adapter.submitList(list)
                    val count = list.size
                    binding.emptyRecycler.isVisible = count == 0
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentTrack.collect { track ->
                    binding.miniPlayer.visibility = if (track != null) View.VISIBLE else View.GONE
                    track?.let {

                        binding.title.text = it.title
                        binding.title.isSelected = true
                        binding.artist.text = it.artist

                        Glide.with(requireContext())
                            .load(it.coverUri)
                            .placeholder(R.drawable.default_album_art)
                            .into(binding.albumImage)
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentPosition.collect { position ->
                    val totalDuration = viewModel.currentTrack.value?.durationMs ?: 0L
                    updateProgress(position, totalDuration)
                }
            }
        }

        binding.playButton.setOnClickListener {
            viewModel.togglePlayPause()
        }
        viewModel.initMediaController(requireContext())
        binding.miniPlayer.setOnClickListener {
            val fullPlayer = FullPlayerSheet()
            fullPlayer.show(parentFragmentManager, "player")
        }


        binding.searchClickButton.setOnClickListener {
            binding.titleMusic.visibility = View.GONE
            binding.searchClickButton.visibility = View.GONE
            binding.moreButton.visibility = View.GONE

            binding.searchView.visibility = View.VISIBLE
            binding.searchView.isIconified = false
            binding.searchView.requestFocus()
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.searchTracks(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchTracks(newText.orEmpty())
                return true
            }
        })

        binding.searchView.setOnCloseListener {
            binding.searchView.visibility = View.GONE

            binding.titleMusic.visibility = View.VISIBLE
            binding.searchClickButton.visibility = View.VISIBLE
            binding.moreButton.visibility = View.VISIBLE

            viewModel.searchTracks("")
            false
        }
    }

    private fun processAudio(uri: Uri) {

        val retriever = MediaMetadataRetriever()

        try {
            retriever.setDataSource(requireContext(), uri)

            val title =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                    ?: getFileName(uri)
                    ?: "Unknown"

            val artist =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                    ?: "Unknown Artist"

            val durationStr =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationMs = durationStr?.toLong() ?: 0L
            val embeddedPicture = retriever.embeddedPicture

            val coverUri = embeddedPicture?.let {
                saveCoverToInternalStorage(it)
            }

            val track = Track(
                title = title,
                artist = artist,
                filePath = uri.toString(),
                coverUri = coverUri,
                isFavorite = false,
                durationMs = durationMs
            )

            viewModel.addNewTrack(track)

            PlayerProvider.getInstance(requireContext()).addTrackToPlaylist(track)


        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            retriever.release()
        }
    }

    fun updateProgress(currentMs: Long, durationMs: Long) {
        if (durationMs > 0) {
            val progress = (currentMs * 100 / durationMs).toInt()

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                binding.backgroundProgress.setProgress(progress, true)
            } else {
                binding.backgroundProgress.progress = progress
            }

        }
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (index != -1) result = it.getString(index)
                }
            }
        }
        return result ?: uri.path?.substringAfterLast('/')
    }

    private fun saveCoverToInternalStorage(bytes: ByteArray): String {

        val fileName = "cover_${System.currentTimeMillis()}.jpg"

        val file = requireContext().openFileOutput(fileName, Context.MODE_PRIVATE)

        file.write(bytes)
        file.close()

        return requireContext().filesDir.absolutePath + "/" + fileName
    }
}