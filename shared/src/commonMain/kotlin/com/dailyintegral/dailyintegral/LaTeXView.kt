package com.dailyintegral.dailyintegral

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.adman.kmp.webview.KWebView

@Composable
fun LaTeXView(latex: String, modifier: Modifier = Modifier) {
    val latex = latex
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\\", "&#92;")
        .replace("\n", "\\\\")
    println("new latex:\n$latex")

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

    KWebView(
        modifier = modifier,
        htmlContent = html,
        enableJavaScript = true
    )
}