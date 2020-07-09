package org.wordpress.android.ui.reader.discover

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.reader_discover_fragment_layout.*
import org.wordpress.android.R
import org.wordpress.android.WordPress
import org.wordpress.android.ui.reader.ReaderActivityLauncher
import org.wordpress.android.ui.reader.discover.ReaderDiscoverViewModel.DiscoverUiState.ContentUiState
import org.wordpress.android.ui.reader.discover.ReaderNavigationEvents.OpenPost
import org.wordpress.android.ui.reader.discover.ReaderNavigationEvents.SharePost
import org.wordpress.android.ui.reader.discover.ReaderNavigationEvents.ShowReaderComments
import org.wordpress.android.ui.utils.UiHelpers
import org.wordpress.android.util.image.ImageManager
import javax.inject.Inject

class ReaderDiscoverFragment : Fragment(R.layout.reader_discover_fragment_layout) {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var uiHelpers: UiHelpers
    @Inject lateinit var imageManager: ImageManager
    private lateinit var viewModel: ReaderDiscoverViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as WordPress).component().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        initViewModel()
    }

    private fun setupViews() {
        recycler_view.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        recycler_view.adapter = ReaderDiscoverAdapter(uiHelpers, imageManager)
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ReaderDiscoverViewModel::class.java)
        viewModel.uiState.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ContentUiState -> (recycler_view.adapter as ReaderDiscoverAdapter).update(it.cards)
            }
            uiHelpers.updateVisibility(recycler_view, it.contentVisiblity)
            uiHelpers.updateVisibility(progress_bar, it.progressVisibility)
            uiHelpers.updateVisibility(progress_text, it.progressVisibility)
        })
        viewModel.navigationEvents.observe(viewLifecycleOwner, Observer {
            it.applyIfNotHandled {
                when (this) {
                    is SharePost -> ReaderActivityLauncher.sharePost(context, post)
                    is OpenPost -> ReaderActivityLauncher.openPost(context, post)
                    is ShowReaderComments -> ReaderActivityLauncher.showReaderComments(context, blogId, postId)
                }
            }
        })
        viewModel.start()
    }
}
