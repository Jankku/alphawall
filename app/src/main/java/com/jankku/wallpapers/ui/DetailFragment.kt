package com.jankku.wallpapers.ui

import android.app.Application
import android.content.*
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.*
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
import com.jankku.wallpapers.util.Constants.IMAGE_DOWNLOAD_RELATIVE_PATH_SUB_Q
import com.jankku.wallpapers.viewmodel.DetailViewModel
import com.jankku.wallpapers.viewmodel.DetailViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


@ExperimentalPagingApi
class DetailFragment : BaseFragment() {

    override var bottomNavigationVisibility = View.GONE

    private lateinit var application: Application
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private val args: DetailFragmentArgs by navArgs()
    private lateinit var picturesFolderImages: List<Uri>

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

        viewModel.downloadWallpaper.observe(viewLifecycleOwner) { downloadWallpaper ->
            if (downloadWallpaper) {
                val id = viewModel.wallpaper.value!!.id
                val url = viewModel.wallpaper.value!!.imageUrl
                lifecycleScope.launch(Dispatchers.Default) {
                    try {
                        if (!imageExists(id)) {
                            val bitmap = downloadImage(url)
                            saveImageToPictures(bitmap, id)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    application,
                                    R.string.image_saved,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    application,
                                    R.string.image_already_saved,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        viewModel.setWallpaper.observe(viewLifecycleOwner) { setWallpaper ->
            if (setWallpaper) {
                val id = viewModel.wallpaper.value!!.id
                val url = viewModel.wallpaper.value!!.imageUrl
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        if (!imageExists(id)) {
                            val bitmap = downloadImage(url)
                            val savedImageUri = saveImageToPictures(bitmap, id) ?: return@launch
                            setWallpaperIntent(savedImageUri)
                        } else {
                            val savedImageUri = getImageUri(id)
                            setWallpaperIntent(savedImageUri)
                        }
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

    private fun setWallpaperIntent(savedImageUri: Uri) {
        val intent =
            Intent(Intent.ACTION_ATTACH_DATA).setDataAndType(savedImageUri, "image/jpeg").apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        startActivity(
            Intent.createChooser(intent, getString(R.string.image_set))
        )
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

    private suspend fun imageExists(id: String): Boolean {
        var exists = false
        withContext(Dispatchers.Default) {
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
            val selection = "${MediaStore.Images.Media.DISPLAY_NAME} == ?"
            val selectionArgs = arrayOf("$id.jpg")
            val projection = arrayOf(MediaStore.Images.Media.DISPLAY_NAME)

            val query = application.contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                null
            )
            query?.use { cursor ->
                if (cursor.count > 0) exists = true
            }
        }
        return exists
    }

    private suspend fun getImageUri(id: String): Uri {
        var contentUri: Uri = Uri.EMPTY
        withContext(Dispatchers.Default) {
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
            val selection = "${MediaStore.Images.Media.DISPLAY_NAME} == ?"
            val selectionArgs = arrayOf("$id.jpg")
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME
            )

            val query = application.contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                null
            )
            query?.use { cursor ->
                val idColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                cursor.moveToFirst()
                val imageId = cursor.getLong(idColumn)
                contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    imageId
                )
            }
        }
        return contentUri
    }

    // TODO("Fix image saving on devices below Android Q")
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
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + IMAGE_DOWNLOAD_RELATIVE_PATH_SUB_Q)
                        .toString()
                val image = File(imagesDir, "$name.jpg")
                outputStream = FileOutputStream(image)
            }
            bitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_DOWNLOAD_QUALITY, outputStream)
            outputStream?.close()
        } catch (e: Exception) {
            e.printStackTrace()
            requireActivity().runOnUiThread {
                Toast.makeText(application, R.string.error_image_save, Toast.LENGTH_SHORT)
                    .show()
            }
        }
        return imageUri
    }
}