package org.wordpress.android.ui.comments.unified

import android.os.Bundle
import org.wordpress.android.R
import org.wordpress.android.WordPress
import org.wordpress.android.databinding.UnifiedCommentsEditActivityBinding
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.ui.LocaleAwareActivity

class UnifiedCommentsEditActivity : LocaleAwareActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(UnifiedCommentsEditActivityBinding.inflate(layoutInflater)) {
            setContentView(root)
        }

        val site = intent.getSerializableExtra(WordPress.SITE) as SiteModel
        val commentIdentifier: CommentIdentifier? = intent.getParcelableExtra(KEY_COMMENT_IDENTIFIER)

        val fm = supportFragmentManager
        var editCommentFragment = fm.findFragmentByTag(
                TAG_UNIFIED_EDIT_COMMENT_FRAGMENT
        ) as? UnifiedCommentsEditFragment

        if (editCommentFragment == null) {
            editCommentFragment = UnifiedCommentsEditFragment.newInstance(site, commentIdentifier)
            fm.beginTransaction()
                    .add(R.id.fragment_container, editCommentFragment, TAG_UNIFIED_EDIT_COMMENT_FRAGMENT)
                    .commit()
        }
    }

    companion object {
        const val KEY_COMMENT_IDENTIFIER = "key_comment_identifier"

        private const val TAG_UNIFIED_EDIT_COMMENT_FRAGMENT = "tag_unified_edit_comment_fragment"
    }
}
