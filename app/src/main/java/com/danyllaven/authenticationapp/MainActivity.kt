package com.danyllaven.authenticationapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import com.danyllaven.authenticationapp.data.RetrofitClient
import com.danyllaven.authenticationapp.ui.theme.AuthenticationAppTheme
import kotlinx.coroutines.launch


const val CLIENT_ID = "Ov23lik4GCPGDKKkXIbP"
const val CLIENT_SECRET = "5868da4c96caf419b70d44e88ee865c6c18cc835"
const val REDIRECT_URI = "myapp://callback" // URI de redirección
const val AUTHORIZE_URL = "https://github.com/login/oauth/authorize"
const val SCOPE = "user"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var accessToken by remember { mutableStateOf<String?>(null) }
            var showGitHubLogin by remember { mutableStateOf(false) }

            // Mostrar la pantalla principal si el token está disponible, o la pantalla de login si no
            if (accessToken != null) {
                HomeScreen() // Pantalla principal después de autenticación
            } else if (showGitHubLogin) {
                GitHubLoginScreen { code ->
                    // Solicitar el token usando el código recibido
                    lifecycleScope.launch {
                        val tokenResponse = RetrofitClient.instance.getAccessToken(
                            CLIENT_ID, CLIENT_SECRET, code, REDIRECT_URI
                        )
                        println(tokenResponse.body())
                        accessToken = tokenResponse.body()?.access_token
                        showGitHubLogin = false // Ocultar la pantalla de login
                    }
                }
            } else {
                LoginScreen {
                    showGitHubLogin = true // Mostrar la pantalla de GitHub login
                }
            }
        }
    }
}


@Composable
fun LoginScreen(onLoginClick: () -> Unit) {
    Scaffold { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Iniciar sesión")
            Spacer(modifier = Modifier.height(50.dp))
            TextField(value = "Correo electrónico", onValueChange = {})
            Spacer(modifier = Modifier.height(15.dp))
            TextField(value = "Contraseña", onValueChange = {})
            Spacer(modifier = Modifier.height(25.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HorizontalDivider()
                Text("Ó")
                HorizontalDivider()
            }

            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            ) {
                Text(text = "Iniciar sesión con GitHub")
            }
        }

    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun GitHubLoginScreen(onTokenReceived: (String) -> Unit) {
    val context = LocalContext.current
    val authUrl = "$AUTHORIZE_URL?client_id=$CLIENT_ID&scope=$SCOPE&redirect_uri=$REDIRECT_URI"

    // Usamos WebView para cargar la URL de autorización de GitHub
    AndroidView(
        factory = {
            WebView(context).apply {
                settings.javaScriptEnabled = true
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest
                    ): Boolean {
                        val url = request.url.toString()
                        if (url.startsWith(REDIRECT_URI)) {
                            val code = request.url.getQueryParameter("code")
                            if (code != null) {
                                onTokenReceived(code) // Recibimos el código y lo usamos para obtener el token
                            }
                            return true
                        }
                        return false
                    }
                }
                loadUrl(authUrl)
            }
        }
    )
}


@Composable
fun HomeScreen() {
    Scaffold { padding ->
        Text(
            text = "Bienvenido al HOME de tu aplicación",
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
                .padding(padding)
        )
    }
}
