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
import androidx.paging.ExperimentalPagingApi
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
import java.io.InputStream
import java.io.OutputStream


@ExperimentalPagingApi
class DetailFragment : BaseFragment() {

    override var bottomNavigationVisibility = View.GONE

    private lateinit var application: Application
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private val args: DetailFragmentArgs by navArgs()

    private val viewModel: DetailViewModel by viewModels {
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
        binding.viewModel = viewModel

        setupObservers()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupObservers() {
        viewModel.networkError.observe(viewLifecycleOwner) { networkError ->
            if (networkError) {
                Toast.makeText(application, "Network error", Toast.LENGTH_SHORT).show()
            }
        }

        // TODO("Before downloading, check if image exists")
        viewModel.downloadWallpaper.observe(viewLifecycleOwner) { downloadWallpaper ->
            if (downloadWallpaper) {
                val id = viewModel.wallpaper.value!!.id
                val url = viewModel.wallpaper.value!!.imageUrl
                lifecycleScope.launch(Dispatchers.Main) {
                    try {
                        val bitmap = downloadImage(url)
                        saveImageToPictures(bitmap, id)
                        requireActivity().runOnUiThread {
                            Toast.makeText(application, R.string.image_saved, Toast.LENGTH_SHORT)
                                .show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        // TODO("Before downloading, check if image exists")
        viewModel.setWallpaper.observe(viewLifecycleOwner) { setWallpaper ->
            if (setWallpaper) {
                val id = viewModel.wallpaper.value!!.id
                val url = viewModel.wallpaper.value!!.imageUrl
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val bitmap = downloadImage(url)
                        val imageUri = saveImageToPictures(bitmap, id) ?: return@launch
                        val intent = Intent(Intent.ACTION_ATTACH_DATA)
                            .setDataAndType(imageUri, "image/jpeg").apply {
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                        startActivity(
                            Intent.createChooser(
                                intent,
                                getString(R.string.image_set)
                            )
                        )

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        viewModel.openWallpaperPage.observe(viewLifecycleOwner) { openInWeb ->
            if (openInWeb) {
                val pageUrl = viewModel.wallpaper.value!!.pageUrl
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(pageUrl))
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

    private fun imageExists(imageUri: Uri): Boolean {
        var boolean = false
        try {
            val inputStream: InputStream? =
                requireContext().contentResolver.openInputStream(imageUri)
            inputStream?.close()
            boolean = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return boolean
    }

    private fun saveImageToPictures(bitmap: Bitmap, name: String): Uri? {
        if (activity == null) return null
        val outputStream: OutputStream?
        var imageUri: Uri? = null
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver: ContentResolver = requireActivity().contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, name)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH, IMAGE_DOWNLOAD_RELATIVE_PATH)
                }
                imageUri =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                outputStream = imageUri?.let { resolver.openOutputStream(it) }
            } else {
                val imagesDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + File.pathSeparator + IMAGE_DOWNLOAD_RELATIVE_PATH)
                        .toString()
                val image = File(imagesDir, "$name.jpeg")
                outputStream = FileOutputStream(image)
            }
            bitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_DOWNLOAD_QUALITY, outputStream)
            outputStream?.close()
        } catch (e: Exception) {
            e.printStackTrace()
            requireActivity().runOnUiThread {
                Toast.makeText(application, R.string.error_image_save, Toast.LENGTH_SHORT).show()
            }
        }
        return imageUri
    }
}