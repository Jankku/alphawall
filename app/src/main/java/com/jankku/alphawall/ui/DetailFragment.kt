package com.jankku.alphawall.ui

import android.Manifest
import android.app.Application
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.paging.ExperimentalPagingApi
import com.bumptech.glide.Glide
import com.jankku.alphawall.R
import com.jankku.alphawall.databinding.FragmentDetailBinding
import com.jankku.alphawall.util.Constants.DOWNLOAD_QUALITY
import com.jankku.alphawall.util.Constants.DOWNLOAD_RELATIVE_PATH
import com.jankku.alphawall.util.Constants.DOWNLOAD_RELATIVE_PATH_PRE_Q
import com.jankku.alphawall.viewmodel.DetailViewModel
import com.jankku.alphawall.viewmodel.DetailViewModelFactory
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
                checkPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                val id = viewModel.wallpaper.value!!.id
                val url = viewModel.wallpaper.value!!.imageUrl
                lifecycleScope.launch(Dispatchers.Default) {
                    try {
                        if (!imageExists(id)) {
                            val bitmap = downloadBitmap(url)
                            saveBitmapToPictures(bitmap, id)
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
                            val bitmap = downloadBitmap(url)
                            val savedImageUri = saveBitmapToPictures(bitmap, id) ?: return@launch
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

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(
                    application,
                    R.string.permission_storage_enable,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    private fun checkPermissions(permission: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val hasPermission = application.checkSelfPermission(permission)
            if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                activityResultLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    // TODO("Fix set wallpaper on pre Q devices")
    private fun setWallpaperIntent(savedImageUri: Uri) {
        val intent =
            Intent(Intent.ACTION_ATTACH_DATA).setDataAndType(savedImageUri, "image/jpeg").apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        startActivity(
            Intent.createChooser(intent, getString(R.string.image_set))
        )
    }

    private suspend fun downloadBitmap(imageUrl: String): Bitmap {
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

    private fun saveBitmapToPictures(bitmap: Bitmap, name: String): Uri? {
        if (activity == null) return null
        val outputStream: OutputStream?
        var imageUri: Uri? = null
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver: ContentResolver = application.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, name)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH, DOWNLOAD_RELATIVE_PATH)
                }
                imageUri =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                outputStream = imageUri?.let { resolver.openOutputStream(it) }
            } else {
                val imagesDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + DOWNLOAD_RELATIVE_PATH_PRE_Q)
                if (!imagesDir.exists()) {
                    imagesDir.mkdirs()
                }
                val image = File(imagesDir, "$name.jpg")
                outputStream = FileOutputStream(image)
            }
            bitmap.compress(Bitmap.CompressFormat.JPEG, DOWNLOAD_QUALITY, outputStream)
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