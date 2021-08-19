package com.jankku.alphawall.ui.search

import androidx.lifecycle.*
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.jankku.alphawall.database.model.Wallpaper
import com.jankku.alphawall.repository.WallpaperRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@ExperimentalPagingApi
class SearchViewModel(private val repository: WallpaperRepository) : ViewModel() {
    sealed class Event {
        data class SearchGuide(val hideSearchGuide: Boolean) : Event()
    }

    private val _wallpapers: MutableLiveData<PagingData<Wallpaper>> = MutableLiveData()
    val wallpapers: LiveData<PagingData<Wallpaper>> get() = _wallpapers

    private val seachGuideChannel = Channel<Event>(Channel.BUFFERED)
    val searchGuideFlow = seachGuideChannel.receiveAsFlow()

    private val _searchDone = MutableLiveData(false)
    val searchDone get() = _searchDone

    fun hideSearchGuide() = viewModelScope.launch {
        seachGuideChannel.send(Event.SearchGuide(true))
    }

    fun setSearchDoneValue(value: Boolean) {
        _searchDone.postValue(value)
    }

    fun search(term: String) {
        viewModelScope.launch {
            repository
                .search(term)
                .distinctUntilChanged()
                .cachedIn(viewModelScope)
                .collect { pagingData ->
                    _wallpapers.value = pagingData
                }
        }
    }
}

@ExperimentalPagingApi
class SearchViewModelFactory(private val repository: WallpaperRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
