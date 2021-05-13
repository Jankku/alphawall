package com.jankku.wallpapers.ui

import android.app.Application
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.jankku.wallpapers.R
import com.jankku.wallpapers.databinding.FragmentDetailBinding
import com.jankku.wallpapers.util.Constants.IMAGE_DOWNLOAD_QUALITY
import com.jankku.wallpapers.util.Constants.IMAGE_DOWNLOAD_RELATIVE_PATH
import com.jankku.wallpapers.viewmodel.DetailViewModel
import com.jankku.wallpapers.viewmodel.DetailViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.MalformedURLException
import java.net.URISyntaxException


class DetailFragment : BaseFragment() {

    override var bottomNavigationVisibility = View.GONE

    private lateinit var application: Application
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private val args: DetailFragmentArgs by navArgs()

    private val detailViewModel: DetailViewModel by viewModels {
        DetailViewModelFactory(args.wallpaper)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        application = requireActivity().application
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(
            inflater,
            container,
            false
        )
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = detailViewModel

        setupObservers()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupObservers() {
        detailViewModel.networkError.observe(viewLifecycleOwner) { networkError ->
            if (networkError) {
                Toast.makeText(application, "Network error", Toast.LENGTH_SHORT).show()
            }
        }

        detailViewModel.downloadWallpaper.observe(viewLifecycleOwner) { downloadWallpaper ->
            if (downloadWallpaper) {
                val id = detailViewModel.wallpaper.value!!.id
                val url = detailViewModel.wallpaper.value!!.imageUrl
                detailViewModel.isDownloadingWallpaper.value = true

                lifecycleScope.launch(Dispatchers.Default) {
                    try {
                        val bitmap = downloadImage(url)
                        saveImageToDownloads(bitmap, id)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        detailViewModel.setWallpaper.observe(viewLifecycleOwner) { setWallpaper ->
            if (setWallpaper) {
                try {
                    TODO("Save wallpaper to app specific storage, get content URI, call intent")
                    //val uri = Uri.parse(url)
                    //val intent = Intent(wallpaperManager.getCropAndSetWallpaperIntent(uri))
                    //startActivity(intent)
                } catch (e: MalformedURLException) {
                    e.printStackTrace()
                } catch (e: URISyntaxException) {
                    e.printStackTrace()
                }

            }
        }

        detailViewModel.openWallpaperPage.observe(viewLifecycleOwner) { openInWeb ->
            if (openInWeb) {
                val url = detailViewModel.wallpaper.value!!.pageUrl
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }
        }
    }

    private suspend fun downloadImage(imageUrl: String): Bitmap {
        return withContext(Dispatchers.IO) {
            Glide
                .with(application)
                .asBitmap()
                .load(imageUrl)
                .submit()
                .get()
        }
    }

    private fun saveImageToDownloads(bitmap: Bitmap, id: String) {
        if (activity == null) return

        var outputStream: OutputStream? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val resolver: ContentResolver = requireActivity().contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.DownloadColumns.DISPLAY_NAME, id)
                    put(MediaStore.DownloadColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.DownloadColumns.RELATIVE_PATH, IMAGE_DOWNLOAD_RELATIVE_PATH)
                }
                val imageUri = resolver
                    .insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                outputStream = imageUri?.let { resolver.openOutputStream(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            try {
                val imagesDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        .toString()
                val image = File(imagesDir, "$id.jpg")
                outputStream = FileOutputStream(image)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_DOWNLOAD_QUALITY, outputStream)
            outputStream?.close()
            requireActivity().runOnUiThread {
                Toast.makeText(application, R.string.image_saved, Toast.LENGTH_SHORT).show()
                detailViewModel.isDownloadingWallpaper.value = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            requireActivity().runOnUiThread {
                Toast.makeText(application, R.string.error_image_save, Toast.LENGTH_SHORT).show()
                detailViewModel.isDownloadingWallpaper.value = false
            }
        }
    }
}