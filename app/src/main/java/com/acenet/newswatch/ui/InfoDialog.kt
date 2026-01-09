package com.acenet.newswatch.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.acenet.newswatch.R

enum class InfoDialogType {
    ABOUT, PRIVACY_POLICY, LICENSES
}

@Composable
fun InfoDialog(
    type: InfoDialogType = InfoDialogType.ABOUT,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (type) {
                    InfoDialogType.ABOUT -> AboutContent()
                    InfoDialogType.PRIVACY_POLICY -> PrivacyPolicyContent()
                    InfoDialogType.LICENSES -> LicensesContent()
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Close", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun AboutContent() {
    Image(
        painter = painterResource(id = R.drawable.ic_launcher),
        contentDescription = "App Logo",
        modifier = Modifier.size(120.dp)
    )
    Text(
        text = "All the headlines. One place",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        fontSize = 10.sp,
        modifier = Modifier.padding(bottom = 16.dp)
    )
    Text(
        text = "NewsWatch is a modern Android application that aggregates the latest news from various sources in Nigeria and around the world. Stay updated with World, Tech, Entertainment, and Sports news in one place.",
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "App Version 1.0.1",
        style = MaterialTheme.typography.bodyMedium,
        color = Color.Gray
    )
    Spacer(modifier = Modifier.height(24.dp))
    Text(
        text = "Powered by:",
        style = MaterialTheme.typography.labelSmall,
        color = Color.Gray
    )
    Text(
        text = "Acenet Technology",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Phone: +2348161836558",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
    Text(
        text = "Email: ope_adedotun@live.com",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun PrivacyPolicyContent() {
    Text(
        text = "Privacy Policy",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 16.dp)
    )
    Text(
        text = """
            Your privacy is important to us. NewsWatch does not collect or share any personal information. 
            
            1. Information Collection: We do not collect any personally identifiable information.
            2. Data Usage: Any data stored locally (like bookmarks) remains on your device.
            3. Third-party Links: The app contains links to external news websites. We are not responsible for the privacy practices of those sites.
            4. Changes: We may update our Privacy Policy from time to time.
            
            By using NewsWatch, you agree to this policy.
        """.trimIndent(),
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Start,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun LicensesContent() {
    Text(
        text = "Open Source Licenses",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 16.dp)
    )
    Text(
        text = "NewsWatch is built using the following open source libraries:",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp),
        color = MaterialTheme.colorScheme.onSurface
    )
    val libraries = listOf(
        "Jetpack Compose",
        "Material Components for Android",
        "Retrofit & OkHttp",
        "Coil (Image Loading)",
        "Gson",
        "SimpleXML Converter",
        "Kotlin Coroutines"
    )
    libraries.forEach { library ->
        Text(
            text = "• $library",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "All libraries are licensed under the Apache License, Version 2.0 or MIT License.",
        style = MaterialTheme.typography.labelSmall,
        color = Color.Gray,
        textAlign = TextAlign.Center
    )
}
