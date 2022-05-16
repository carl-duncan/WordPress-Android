package org.wordpress.android.ui.bloggingprompts.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.wordpress.android.fluxc.store.SiteStore
import org.wordpress.android.models.bloggingprompts.BloggingPrompt
import org.wordpress.android.ui.bloggingprompts.onboarding.BloggingPromptsOnboardingAction.DismissDialog
import org.wordpress.android.ui.bloggingprompts.onboarding.BloggingPromptsOnboardingAction.OpenEditor
import org.wordpress.android.ui.bloggingprompts.onboarding.BloggingPromptsOnboardingAction.OpenRemindersIntro
import org.wordpress.android.ui.bloggingprompts.onboarding.BloggingPromptsOnboardingAction.OpenSitePicker
import org.wordpress.android.ui.bloggingprompts.onboarding.BloggingPromptsOnboardingDialogFragment.DialogType
import org.wordpress.android.ui.bloggingprompts.onboarding.BloggingPromptsOnboardingDialogFragment.DialogType.INFORMATION
import org.wordpress.android.ui.bloggingprompts.onboarding.BloggingPromptsOnboardingDialogFragment.DialogType.ONBOARDING
import org.wordpress.android.ui.mysite.SelectedSiteRepository
import javax.inject.Inject

class BloggingPromptsOnboardingViewModel @Inject constructor(
    private val siteStore: SiteStore,
    private val uiStateMapper: BloggingPromptsOnboardingUiStateMapper,
    private val selectedSiteRepository: SelectedSiteRepository,
    private val analyticsTracker: BloggingPromptsOnboardingAnalyticsTracker
) : ViewModel() {
    private val _uiState = MutableLiveData<BloggingPromptsOnboardingUiState>()
    val uiState: LiveData<BloggingPromptsOnboardingUiState> = _uiState

    private val _action = MutableLiveData<BloggingPromptsOnboardingAction>()
    val action: LiveData<BloggingPromptsOnboardingAction> = _action

    private lateinit var dialogType: DialogType
    private lateinit var bloggingPrompt: BloggingPrompt
    private var hasTrackedScreenShown = false

    fun start(type: DialogType) {
        if (!hasTrackedScreenShown) {
            hasTrackedScreenShown = true
            analyticsTracker.trackScreenShown()
        }
        dialogType = type
        // TODO @RenanLukas get BloggingPrompt from Store when it's ready
        bloggingPrompt = BloggingPrompt.Tmp
        _uiState.value = uiStateMapper.mapReady(dialogType, ::onPrimaryButtonClick, ::onSecondaryButtonClick)
    }

    private fun onPrimaryButtonClick() {
        val action = when (dialogType) {
            ONBOARDING -> {
                analyticsTracker.trackTryItNowClicked()
                OpenEditor(bloggingPrompt.id)
            }
            INFORMATION -> {
                analyticsTracker.trackGotItClicked()
                DismissDialog
            }
        }
        _action.value = action
    }

    private fun onSecondaryButtonClick() {
        analyticsTracker.trackRemindMeClicked()
        if (siteStore.sitesCount > 1) {
            _action.value = OpenSitePicker(selectedSiteRepository.getSelectedSite())
        } else {
            siteStore.sites.firstOrNull()?.let {
                _action.value = OpenRemindersIntro(it.id)
            }
        }
    }

    fun onSiteSelected(selectedSiteLocalId: Int) {
        _action.value = OpenRemindersIntro(selectedSiteLocalId)
    }
}
