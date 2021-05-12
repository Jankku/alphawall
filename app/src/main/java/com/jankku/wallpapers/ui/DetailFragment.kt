package com.jankku.wallpapers.ui

import android.app.Application
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.jankku.wallpapers.R
import com.jankku.wallpapers.databinding.FragmentDetailBinding
import com.jankku.wallpapers.util.Constants.IMAGE_DOWNLOAD_QUALITY
import com.jankku.wallpapers.util.Constants.IMAGE_DOWNLOAD_RELATIVE_PATH
import com.jankku.wallpapers.viewmodel.DetailViewModel
import com.jankku.wallpapers.viewmodel.DetailViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.MalformedURLException
import java.net.URISyntaxException


class DetailFragment : Fragment() {

    private lateinit var application: Application
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private val args: DetailFragmentArgs by navArgs()

    private val detailViewModel: DetailViewModel by viewModels {
        DetailViewModelFactory()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        application = requireActivity().application
        //(context as AppCompatActivity).supportActionBar?.hide()
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

        detailViewModel._wallpaper.value = args.wallpaper

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = detailViewModel

        detailViewModel.networkError.observe(viewLifecycleOwner) { networkError ->
            if (networkError) {
                Toast.makeText(application, "Network error", Toast.LENGTH_LONG).show()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // https://stackoverflow.com/questions/62577645/android-view-view-systemuivisibility-deprecated-what-is-the-replacement
/*        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireActivity().window.setDecorFitsSystemWindows(false)
            requireActivity().window.insetsController?.hide(WindowInsets.Type.systemBars())
        } else {
            requireActivity().window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }*/

        detailViewModel.downloadWallpaper.observe(viewLifecycleOwner) { downloadWallpaper ->
            if (downloadWallpaper == true) {
                val url = detailViewModel.wallpaper.value!!.imageUrl
                downloadImage(url)
            }
        }

        detailViewModel.setWallpaper.observe(viewLifecycleOwner) { setWallpaper ->
            if (setWallpaper == true) {
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        //(context as AppCompatActivity).supportActionBar?.show()

/*        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireActivity().window.setDecorFitsSystemWindows(true)
            requireActivity().window.insetsController?.show(WindowInsets.Type.systemBars())
        } else {
            requireActivity().window.setFlags(
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN
            )
        }*/
    }

    private fun downloadImage(imageUrl: String) {
        val id = detailViewModel.wallpaper.value!!.id
        val fileType = detailViewModel.wallpaper.value!!.fileType

        detailViewModel._isDownloadingWallpaper.value = true

        lifecycleScope.launch(Dispatchers.IO) {
            Glide
                .with(application)
                .asBitmap()
                .load(imageUrl)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        saveImageToPictures(resource, id, fileType)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        super.onLoadFailed(errorDrawable)
                        detailViewModel._isDownloadingWallpaper.value = false
                        lifecycleScope.launch(Dispatchers.Main) {
                            Toast.makeText(
                                application,
                                R.string.error_image_download,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                })
        }
    }

    fun saveImageToPictures(bitmap: Bitmap, id: String, fileType: String) {
        if (activity == null) return

        var outputStream: OutputStream? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val resolver: ContentResolver = requireActivity().contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.DownloadColumns.DISPLAY_NAME, "$id.$fileType")
                    put(MediaStore.DownloadColumns.MIME_TYPE, "image/$fileType")
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
                val image = File(imagesDir, "$id.$fileType")
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
                detailViewModel._isDownloadingWallpaper.value = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            requireActivity().runOnUiThread {
                Toast.makeText(application, R.string.error_image_save, Toast.LENGTH_SHORT).show()
                detailViewModel._isDownloadingWallpaper.value = false
            }
        }
    }
}