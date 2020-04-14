package org.wordpress.android.imageeditor.preview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.wordpress.android.imageeditor.preview.PreviewImageViewModel.ImageLoadToFileState.ImageLoadToFileFailedState
import org.wordpress.android.imageeditor.preview.PreviewImageViewModel.ImageLoadToFileState.ImageLoadToFileIdleState
import org.wordpress.android.imageeditor.preview.PreviewImageViewModel.ImageLoadToFileState.ImageLoadToFileSuccessState
import org.wordpress.android.imageeditor.preview.PreviewImageViewModel.ImageLoadToFileState.ImageStartLoadingToFileState
import org.wordpress.android.imageeditor.preview.PreviewImageViewModel.ImageUiState.ImageDataStartLoadingUiState
import org.wordpress.android.imageeditor.preview.PreviewImageViewModel.ImageUiState.ImageInHighResLoadFailedUiState
import org.wordpress.android.imageeditor.preview.PreviewImageViewModel.ImageUiState.ImageInHighResLoadSuccessUiState
import org.wordpress.android.imageeditor.preview.PreviewImageViewModel.ImageUiState.ImageInLowResLoadFailedUiState
import org.wordpress.android.imageeditor.preview.PreviewImageViewModel.ImageUiState.ImageInLowResLoadSuccessUiState

class PreviewImageViewModel : ViewModel() {
    private val _uiState: MutableLiveData<UiState> = MutableLiveData()
    val uiState: LiveData<UiState> = _uiState

    private val _loadIntoFile = MutableLiveData<ImageLoadToFileState>(ImageLoadToFileIdleState)
    val loadIntoFile: LiveData<ImageLoadToFileState> = _loadIntoFile

    private val _navigateToCropScreenWithFileInfo = MutableLiveData<Pair<String, String?>>()
    val navigateToCropScreenWithFileInfo: LiveData<Pair<String, String?>> = _navigateToCropScreenWithFileInfo

    fun onCreateView(imageDataList: List<ImageData>) {
        if (uiState.value == null) {
            val newImageUiStates = createViewPagerItemsInitialUiStates(imageDataList)
            val currentUiState = uiState.value?.copy(viewPagerItemsStates = newImageUiStates) ?: UiState(
                newImageUiStates
            )
            updateUiState(currentUiState)
        }
    }

    fun onLoadIntoImageViewSuccess(currentUrl: String, currentPosition: Int) {
        val newImageUiStates = updateViewPagerItemsUiStates(
            currentUrl = currentUrl,
            currentPosition = currentPosition,
            loadSuccess = true
        )
        val newImageState = newImageUiStates[currentPosition]

        val currentUiState = uiState.value as UiState
        val currentImageState = currentUiState.viewPagerItemsStates[currentPosition]

        if (isSingleImageInUiState(currentUiState) && canLoadToFile(newImageState)) {
            updateLoadIntoFileState(
                ImageStartLoadingToFileState(
                    imageUrl = currentImageState.data.highResImageUrl,
                    position = currentPosition
                )
            )
        }
        updateUiState(currentUiState.copy(
                viewPagerItemsStates = newImageUiStates,
                editActionsEnabled = shouldEnableEditActionsForImageState(newImageState)
            )
        )
    }

    fun onLoadIntoImageViewFailed(currentUrl: String, currentPosition: Int) {
        val newImageUiStates = updateViewPagerItemsUiStates(
            currentUrl = currentUrl,
            currentPosition = currentPosition,
            loadSuccess = false
        )
        val currentUiState = uiState.value as UiState
        updateUiState(currentUiState.copy(viewPagerItemsStates = newImageUiStates))
    }

    fun onLoadIntoFileSuccess(inputFilePath: String, currentPosition: Int) {
        val currentImageState = (uiState.value as UiState).viewPagerItemsStates[currentPosition]
        val outputFileExtension = currentImageState.data.outputFileExtension

        updateLoadIntoFileState(ImageLoadToFileSuccessState(inputFilePath, currentPosition))
        _navigateToCropScreenWithFileInfo.value = Pair(inputFilePath, outputFileExtension)
    }

    fun onLoadIntoFileFailed() {
        // TODO: Do we need to display any error message to the user?
        updateLoadIntoFileState(ImageLoadToFileFailedState)
    }

    fun onCropMenuClicked(currentPosition: Int) {
        val currentImageState = (uiState.value as UiState).viewPagerItemsStates[currentPosition]
        updateLoadIntoFileState(
            ImageStartLoadingToFileState(imageUrl = currentImageState.data.highResImageUrl, position = currentPosition)
        )
    }

    private fun onLoadIntoImageViewRetry(currentUrl: String, currentPosition: Int) {
        val newImageUiStates = updateViewPagerItemsUiStates(
            currentUrl = currentUrl,
            currentPosition = currentPosition,
            loadSuccess = false,
            retry = true
        )
        val currentUiState = uiState.value as UiState
        updateUiState(currentUiState.copy(viewPagerItemsStates = newImageUiStates))
    }

    private fun createViewPagerItemsInitialUiStates(
        data: List<ImageData>
    ): List<ImageUiState> = data.map { createImageLoadStartUiState(it) }

    private fun updateViewPagerItemsUiStates(
        currentUrl: String,
        currentPosition: Int,
        loadSuccess: Boolean,
        retry: Boolean = false
    ): List<ImageUiState> {
        val currentUiState = uiState.value as UiState
        val currentImageState = currentUiState.viewPagerItemsStates[currentPosition]
        val currentImageData = currentImageState.data

        val items = currentUiState.viewPagerItemsStates.toMutableList()
        items[currentPosition] = when {
            loadSuccess -> createImageLoadSuccessUiState(currentUrl, currentImageData, currentImageState)
            retry -> createImageLoadStartUiState(currentImageData)
            else -> createImageLoadFailedUiState(currentUrl, currentImageData, currentPosition)
        }
        return items
    }

    private fun createImageLoadStartUiState(
        currentImageData: ImageData
    ): ImageUiState {
        return ImageDataStartLoadingUiState(currentImageData)
    }

    private fun createImageLoadSuccessUiState(
        currentUrl: String,
        currentImageData: ImageData,
        currentImageState: ImageUiState
    ): ImageUiState {
        return if (currentUrl == currentImageData.lowResImageUrl) {
            val isHighResImageAlreadyLoaded = currentImageState is ImageInHighResLoadSuccessUiState
            if (!isHighResImageAlreadyLoaded) {
                val isRetryShown = currentImageState is ImageInHighResLoadFailedUiState
                ImageInLowResLoadSuccessUiState(currentImageData, isRetryShown)
            } else {
                currentImageState
            }
        } else {
            ImageInHighResLoadSuccessUiState(currentImageData)
        }
    }

    private fun createImageLoadFailedUiState(
        currentUrl: String,
        currentImageData: ImageData,
        currentPosition: Int
    ): ImageUiState {
        val imageUiState = if (currentUrl == currentImageData.lowResImageUrl) {
            ImageInLowResLoadFailedUiState(currentImageData)
        } else {
            ImageInHighResLoadFailedUiState(currentImageData)
        }

        imageUiState.onItemTapped = {
            onLoadIntoImageViewRetry(currentUrl, currentPosition)
        }

        return imageUiState
    }

    private fun updateUiState(state: UiState) {
        _uiState.value = state
    }

    private fun updateLoadIntoFileState(fileState: ImageLoadToFileState) {
        _loadIntoFile.value = fileState
    }

    private fun shouldEnableEditActionsForImageState(currentImageState: ImageUiState) = canLoadToFile(currentImageState)

    private fun canLoadToFile(currentImageState: ImageUiState) = currentImageState is ImageInHighResLoadSuccessUiState

    private fun isSingleImageInUiState(currentUiState: UiState) = currentUiState.viewPagerItemsStates.size == 1

    data class ImageData(val lowResImageUrl: String, val highResImageUrl: String, val outputFileExtension: String)

    data class UiState(val viewPagerItemsStates: List<ImageUiState>, val editActionsEnabled: Boolean = false)

    sealed class ImageUiState(
        val data: ImageData,
        val progressBarVisible: Boolean = false,
        val retryLayoutVisible: Boolean
    ) {
        var onItemTapped: (() -> Unit)? = null
        data class ImageDataStartLoadingUiState(val imageData: ImageData) : ImageUiState(
            data = imageData,
            progressBarVisible = true,
            retryLayoutVisible = false
        )
        // Continue displaying progress bar on low res image load success
        data class ImageInLowResLoadSuccessUiState(val imageData: ImageData, val isRetryShown: Boolean = false)
            : ImageUiState(
            data = imageData,
            progressBarVisible = !isRetryShown,
            retryLayoutVisible = isRetryShown
        )
        data class ImageInLowResLoadFailedUiState(val imageData: ImageData) : ImageUiState(
            data = imageData,
            progressBarVisible = true,
            retryLayoutVisible = false
        )
        data class ImageInHighResLoadSuccessUiState(val imageData: ImageData) : ImageUiState(
            data = imageData,
            progressBarVisible = false,
            retryLayoutVisible = false
        )
        // Display retry only when high res image load failed
        data class ImageInHighResLoadFailedUiState(val imageData: ImageData) : ImageUiState(
            data = imageData,
            progressBarVisible = false,
            retryLayoutVisible = true
        )
    }

    sealed class ImageLoadToFileState {
        object ImageLoadToFileIdleState : ImageLoadToFileState()
        data class ImageStartLoadingToFileState(val imageUrl: String, val position: Int) : ImageLoadToFileState()
        data class ImageLoadToFileSuccessState(val filePath: String, val position: Int) : ImageLoadToFileState()
        object ImageLoadToFileFailedState : ImageLoadToFileState()
    }
}
