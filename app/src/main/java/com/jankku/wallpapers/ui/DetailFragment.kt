package com.jankku.wallpapers.ui

import android.app.Application
import android.app.WallpaperManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.jankku.wallpapers.R
import com.jankku.wallpapers.databinding.FragmentDetailBinding
import com.jankku.wallpapers.viewmodel.DetailViewModel
import com.jankku.wallpapers.viewmodel.DetailViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_detail,
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
            requireActivity().window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            requireActivity().window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }*/

        detailViewModel.setWallpaper.observe(viewLifecycleOwner) { setWallpaper ->
            if (setWallpaper == true) {
                val url: String? = detailViewModel.wallpaper.value?.imageUrl

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val wallpaperManager = WallpaperManager.getInstance(application)
                        val bitmap = Glide.with(application).asBitmap()
                            .load(url)
                            .submit()
                            .get()

                        wallpaperManager.setBitmap(bitmap)
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
//        (context as AppCompatActivity).supportActionBar?.show()
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            requireActivity().window.setDecorFitsSystemWindows(true)
//            requireActivity().window.insetsController?.show(WindowInsets.Type.statusBars())
//        } else {
//            requireActivity().window.setFlags(
//                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN
//            )
//        }
    }
}