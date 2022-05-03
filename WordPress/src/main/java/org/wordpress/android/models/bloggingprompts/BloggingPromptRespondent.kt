package org.wordpress.android.models.bloggingprompts

data class BloggingPromptRespondent(
    val userId: Long,
    val avatarUrl: String
) {
    companion object {
        private val Tmp = BloggingPromptRespondent(
                userId = 54279365,
                avatarUrl = "https://0.gravatar.com/avatar/cec64efa352617" +
                        "c35743d8ed233ab410?s=96&d=identicon&r=G"
        )
        val TmpList = listOf(Tmp, Tmp, Tmp, Tmp, Tmp)
    }
}
