package org.wordpress.android.ui.sitecreation.verticals

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.wordpress.android.modules.BG_THREAD
import org.wordpress.android.ui.sitecreation.misc.SiteCreationTracker
import org.wordpress.android.viewmodel.SingleLiveEvent
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

class SiteCreationIntentsViewModel @Inject constructor(
    private val analyticsTracker: SiteCreationTracker,
    @Named(BG_THREAD) private val bgDispatcher: CoroutineDispatcher
) : ViewModel(), CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = bgDispatcher + job

    private var isStarted = false

    private val _onSkipButtonPressed = SingleLiveEvent<Unit>()
    val onSkipButtonPressed: LiveData<Unit> = _onSkipButtonPressed

    private val _onBackButtonPressed = SingleLiveEvent<Unit>()
    val onBackButtonPressed: LiveData<Unit> = _onBackButtonPressed

    fun start() {
        if (isStarted) return
        isStarted = true
        analyticsTracker.trackSiteIntentQuestionViewed()
    }

    fun onSkipPressed() {
        analyticsTracker.trackSiteIntentQuestionSkipped()
        _onSkipButtonPressed.call()
    }

    fun onBackPressed() {
        analyticsTracker.trackSiteIntentQuestionCanceled()
        _onBackButtonPressed.call()
    }
}
