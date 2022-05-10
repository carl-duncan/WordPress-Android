package org.wordpress.android.models.bloggingprompts

data class BloggingPrompt(
    val id: Int,
    val text: String,
    val content: String,
    val respondents: List<BloggingPromptRespondent>
) {
    companion object {
        // TODO @RenanLukas get BloggingPrompt from Store when it's ready
        val Tmp = BloggingPrompt(
                id = 1234,
                text = "Cast the movie of your life.",
                content = "<!-- wp:pullquote -->\n" +
                        "<figure class=\"wp-block-pullquote\"><blockquote><p>" +
                        "You have 15 minutes to address the whole world live " +
                        "(on television or radio — choose your format). What would you say?" +
                        "</p><cite>(courtesy of plinky.com)</cite></blockquote></figure>\n" +
                        "<!-- /wp:pullquote -->",
                respondents = emptyList()
        )
    }
}
