package com.byarahchar.translate.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byarahchar.translate.ByarAhCharApp
import com.byarahchar.translate.data.TranslationEntity
import kotlinx.coroutines.launch

/**
 * Clean Material Design 3 Home Screen for OnePlus Nord CE 2 Lite 5G.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    hasOverlayPermission: Boolean,
    onRequestOverlayPermission: () -> Unit,
    onToggleFloatingBubble: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var burmeseInput by remember { mutableStateOf("") }
    var englishResult by remember { mutableStateOf("") }
    var tamilResult by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isBubbleRunning by remember { mutableStateOf(false) }

    val clipboardManager = remember {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Byar ah char Translate",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Burmese (မြန်မာ) → English & Tamil (தமிழ்)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Floating Bubble Service Quick Toggle
                    IconButton(onClick = {
                        isBubbleRunning = !isBubbleRunning
                        onToggleFloatingBubble(isBubbleRunning)
                    }) {
                        Icon(
                            imageVector = if (isBubbleRunning) Icons.Default.Layers else Icons.Default.LayersClear,
                            contentDescription = "Toggle Floating Bubble",
                            tint = if (isBubbleRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Permission Banner (if overlay not granted)
            if (!hasOverlayPermission) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Floating Bubble Permission Needed",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "To translate over WhatsApp, Chrome, or Facebook, allow 'Display over other apps' in OxygenOS Settings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onRequestOverlayPermission,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Grant Overlay Permission")
                        }
                    }
                }
            }

            // Floating Bubble Controller Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Floating Chat Bubble",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = if (isBubbleRunning) "Active over all apps. Tap bubble to translate." else "Tap switch to float over other apps",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Switch(
                        checked = isBubbleRunning,
                        onCheckedChange = { checked ->
                            isBubbleRunning = checked
                            onToggleFloatingBubble(checked)
                        }
                    )
                }
            }

            // Burmese Input Card
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Burmese (မြန်မာစာ)",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row {
                            // Paste button (Feature 2)
                            TextButton(
                                onClick = {
                                    val clip = clipboardManager.primaryClip
                                    if (clip != null && clip.itemCount > 0) {
                                        burmeseInput = clip.getItemAt(0).text.toString()
                                        Toast.makeText(context, "Pasted from clipboard", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Paste")
                            }
                            if (burmeseInput.isNotEmpty()) {
                                IconButton(onClick = { burmeseInput = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = burmeseInput,
                        onValueChange = { burmeseInput = it },
                        placeholder = { Text("Type or paste Burmese text here (e.g. မင်္ဂလာပါ, နေကောင်းလား)...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 110.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Translate Button
                    Button(
                        onClick = {
                            if (burmeseInput.isBlank()) {
                                Toast.makeText(context, "Please enter Burmese text", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isLoading = true
                            coroutineScope.launch {
                                val result = ByarAhCharApp.instance.repository.translateBurmese(burmeseInput.trim())
                                isLoading = false
                                if (result.isSuccess) {
                                    val (eng, tam) = result.getOrThrow()
                                    englishResult = eng
                                    tamilResult = tam

                                    // Save history locally (Feature 7)
                                    ByarAhCharApp.instance.repository.saveHistory(
                                        TranslationEntity(
                                            burmeseText = burmeseInput.trim(),
                                            englishTranslation = eng,
                                            tamilTranslation = tam,
                                            timestamp = System.currentTimeMillis()
                                        )
                                    )
                                } else {
                                    Toast.makeText(context, "Translation error. Check connection.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Translating...")
                        } else {
                            Icon(Icons.Default.Translate, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Translate to English & Tamil")
                        }
                    }
                }
            }

            // Results Section (Feature 5 & 6: English & Tamil Cards with Copy buttons)
            if (englishResult.isNotEmpty() || tamilResult.isNotEmpty()) {
                // English Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "English Translation",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(onClick = {
                                val clip = ClipData.newPlainText("English", englishResult)
                                clipboardManager.setPrimaryClip(clip)
                                Toast.makeText(context, "English copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy English")
                            }
                        }
                        Text(
                            text = englishResult,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Tamil Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tamil Translation (தமிழ்)",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            IconButton(onClick = {
                                val clip = ClipData.newPlainText("Tamil", tamilResult)
                                clipboardManager.setPrimaryClip(clip)
                                Toast.makeText(context, "Tamil copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Tamil")
                            }
                        }
                        Text(
                            text = tamilResult,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Quick Tips Card for OnePlus Nord CE 2 Lite 5G
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3 Ways to Translate on OnePlus Nord CE 2 Lite 5G:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. In-App: Paste Burmese above and tap Translate.\n2. Floating Bubble: Enable switch, drag bubble anywhere, tap to translate over WhatsApp/Facebook.\n3. Text Selection: Highlight Burmese text anywhere, tap ⋮ or 'Byar ah char Translate' in the popup menu.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
