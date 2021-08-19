package com.jankku.alphawall.ui.detail

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.paging.ExperimentalPagingApi
import com.google.android.material.snackbar.Snackbar
import com.jankku.alphawall.R
import com.jankku.alphawall.databinding.FragmentDetailBinding
import com.jankku.alphawall.ui.BaseFragment
import com.jankku.alphawall.util.Constants.DOWNLOAD_QUALITY
import com.jankku.alphawall.util.Constants.DOWNLOAD_RELATIVE_PATH
import com.jankku.alphawall.util.Constants.DOWNLOAD_RELATIVE_PATH_PRE_Q
import com.jankku.alphawall.util.GlideApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

@ExperimentalPagingApi
class WallpaperDetailFragment : BaseFragment() {

    override var bottomNavigationVisibility = View.GONE

    private lateinit var application: Application
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private val args: WallpaperDetailFragmentArgs by navArgs()
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    private val viewModel: WallpaperDetailViewModel by viewModels {
        WallpaperDetailViewModelFactory(args.wallpaper)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        application = requireActivity().application
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (!isGranted) {
                    Toast.makeText(
                        requireContext(),
                        R.string.permission_storage_enable,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
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
        binding.ivDetail.setImageBitmap(null)
        _binding = null
    }

    private fun setupObservers() {
        viewModel.networkError.observe(viewLifecycleOwner) { networkError ->
            if (!networkError) return@observe
            Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()
        }

        viewModel.downloadWallpaper.observe(viewLifecycleOwner) { downloadWallpaper ->
            if (!downloadWallpaper) return@observe
            val id = viewModel.wallpaper.value!!.id
            val url = viewModel.wallpaper.value!!.imageUrl
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                    downloadWallpaper(url, id)
                }
            } else {
                downloadWallpaper(url, id)
            }
        }

        viewModel.setWallpaper.observe(viewLifecycleOwner) { setWallpaper ->
            if (!setWallpaper) return@observe
            val id = viewModel.wallpaper.value!!.id
            val url = viewModel.wallpaper.value!!.imageUrl
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE) {
                    setWallpaper(url, id)
                }
            } else {
                setWallpaper(url, id)
            }
        }

        viewModel.openWallpaperPage.observe(viewLifecycleOwner) { openInWeb ->
            if (!openInWeb) return@observe
            val pageUrl = viewModel.wallpaper.value!!.pageUrl
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(pageUrl))
            startActivity(intent)
        }
    }

    private fun downloadWallpaper(url: String, id: String) {
        lifecycleScope.launch(Dispatchers.Default) {
            try {
                if (!imageExists(id)) {
                    val bitmap = downloadBitmap(url)
                    saveBitmapToPictures(bitmap, id)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            R.string.image_saved,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
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

    private fun setWallpaper(url: String, id: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (!imageExists(id)) {
                    val bitmap = downloadBitmap(url)
                    saveBitmapToPictures(bitmap, id)
                    bitmap.recycle()
                    val imageUri = getImageContentUri(id)
                    val intent = Intent(Intent.ACTION_ATTACH_DATA)
                        .setDataAndType(imageUri, "image/jpeg").apply {
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                    startActivity(Intent.createChooser(intent, getString(R.string.image_set_as)))
                } else {
                    val imageUri = getImageContentUri(id)
                    val intent = Intent(Intent.ACTION_ATTACH_DATA)
                        .setDataAndType(imageUri, "image/jpeg").apply {
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                    startActivity(Intent.createChooser(intent, getString(R.string.image_set_as)))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun requestPermission(permission: String, callback: () -> Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            when {
                application.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED -> {
                    callback()
                }
                shouldShowRequestPermissionRationale(permission) -> {
                    Snackbar.make(
                        binding.root,
                        R.string.permission_rationale_reason,
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction(R.string.permission_rationale_ok) {
                        requestPermissionLauncher.launch(permission)
                    }.show()
                }
                else -> {
                    requestPermissionLauncher.launch(permission)
                }
            }
        }
    }

    private suspend fun downloadBitmap(imageUrl: String): Bitmap {
        return withContext(Dispatchers.IO) {
            GlideApp.with(requireContext())
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
            val projection = arrayOf(MediaStore.Images.Media.DISPLAY_NAME)
            val selection = "${MediaStore.Images.Media.DISPLAY_NAME}=?"
            val selectionArgs = arrayOf("$id.jpg")

            val query = application.contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                null,
            )
            query?.use { cursor ->
                if (cursor.count > 0) exists = true
            }
        }
        return exists
    }

    @SuppressLint("Range")
    private suspend fun getImageContentUri(id: String): Uri {
        var contentUri: Uri = Uri.EMPTY
        withContext(Dispatchers.Default) {
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME
            )
            val selection = "${MediaStore.Images.Media.DISPLAY_NAME}=?"
            val selectionArgs = arrayOf("$id.jpg")

            val query = application.contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                null
            )
            query?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val imageId = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID))
                    contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        imageId
                    )
                }
            }
        }
        return contentUri
    }

    private fun saveBitmapToPictures(bitmap: Bitmap, name: String) {
        if (activity == null) return
        val outputStream: OutputStream?
        val imageUri: Uri?
        var image: File? = null
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
                    Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES + DOWNLOAD_RELATIVE_PATH_PRE_Q
                    )
                if (!imagesDir.exists()) {
                    imagesDir.mkdirs()
                }
                image = File(imagesDir, "$name.jpg")
                outputStream = FileOutputStream(image)
            }
            bitmap.compress(Bitmap.CompressFormat.JPEG, DOWNLOAD_QUALITY, outputStream)
            outputStream?.close()
            refreshGallery(image)
        } catch (e: Exception) {
            e.printStackTrace()
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), R.string.error_image_save, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun refreshGallery(file: File?) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            MediaScannerConnection.scanFile(application, arrayOf("$file"), null) { _, _ -> }
        }
    }
}
