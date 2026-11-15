package com.dailyintegral.dailyintegral

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewStateWithHTMLData

@Composable
fun LaTeXView(latex: String, modifier: Modifier = Modifier) {
    val html = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport"
                  content="width=device-width, initial-scale=1.0">

            <script>10pt
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
            <div style="white-space: pre-wrap; font-size: 70%;">
                \[\displaylines{$latex}\]
            </div>
        </body>
        </html>
    """.trimIndent()

    val state = rememberWebViewStateWithHTMLData(data = html)
    val navigator = rememberWebViewNavigator()

    val textColour = "#" + (MaterialTheme.colorScheme.onBackground.toArgb() and 0xFFFFFF)
        .toString(16)
        .uppercase()
        .padStart(6, '0')

    LaunchedEffect(state.loadingState, textColour) {
        if (state.loadingState == LoadingState.Finished) {
            navigator.evaluateJavaScript(
                """
                document.documentElement.style.color = '$textColour';
                document.body.style.color = '$textColour';
                """.trimIndent()
            )
        }
    }

    DisposableEffect(state) {
        state.webSettings.isJavaScriptEnabled = true
        onDispose { }
    }

    WebView(
        state = state,
        navigator = navigator,
        modifier = modifier,
    )
}