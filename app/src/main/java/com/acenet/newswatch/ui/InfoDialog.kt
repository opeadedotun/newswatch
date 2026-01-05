package com.acenet.newswatch.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.acenet.newswatch.ui.theme.BackgroundDark
import com.acenet.newswatch.ui.theme.SurfaceDark
import com.acenet.newswatch.ui.theme.TextWhite
import com.acenet.newswatch.R

@Composable
fun InfoDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(200.dp)
                )
                Text(
                    text = "All the headlines. One place",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextWhite.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "NewsWatch is a modern Android application that aggregates the latest news from various sources in Nigeria and around the world. Stay updated with World, Tech, Entertainment, and Sports news in one place.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = TextWhite
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
                    color = TextWhite
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Phone: +2348161836558",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextWhite
                )
                Text(
                    text = "Email: ope_adedotun@live.com",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextWhite
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    }
}
