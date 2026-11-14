package com.dailyintegral.dailyintegral

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewStateWithHTMLData

@Composable
fun LaTeXView(latex: String, modifier: Modifier = Modifier) {
    val latex = latex
        .split("\n\n")
        .component1()
        // is this even necessary?
//        .replace("&", "&amp;")
//        .replace("<", "&lt;")
//        .replace(">", "&gt;")
//        .replace("\\", "&#92;")
        .replace("\n", "\\\\")

    val html = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport"
                  content="width=device-width, initial-scale=1.0">

            <script>
                window.MathJax = {
                    tex: {
                        inlineMath: [['\\(', '\\)']],
                        displayMath: [['\\[', '\\]']]
                    }
                };
            </script>

            <script async
                src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js">
            </script>
        </head>

        <body>
            <div style="white-space: pre-wrap;">
                \[\displaylines{$latex}\]
            </div>
        </body>
        </html>
    """.trimIndent()

    val state = rememberWebViewStateWithHTMLData(
        data = html
    )

    DisposableEffect(state) {
        state.webSettings.isJavaScriptEnabled = true
        onDispose { }
    }

    WebView(
        state = state,
        modifier = modifier
    )
}