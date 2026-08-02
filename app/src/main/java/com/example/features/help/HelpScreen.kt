package com.example.features.help

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.features.dashboard.BoloViewModel
import com.example.ui.theme.*

data class SampleCommand(
    val englishText: String,
    val hindiText: String,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    viewModel: BoloViewModel,
    onNavigateToDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("SYSTEM") }

    val categories = listOf("SYSTEM", "COMMUNICATION", "APPS", "MEDIA", "NAVIGATION", "INFORMATION", "AUTOMATION")

    val commandsMap = remember {
        mapOf(
            "SYSTEM" to listOf(
                SampleCommand("WiFi band karo", "वाईफाई बंद करो", "Turns off simulated device WiFi network"),
                SampleCommand("WiFi chalu karo", "वाईफाई चालू करो", "Turns on simulated device WiFi network"),
                SampleCommand("Silent mode lagao", "साइलेंट मोड लगाओ", "Mutes system alerts and switches silent state"),
                SampleCommand("Brightness 80% set karo", "ब्राइटनेस ८०% सेट करो", "Sets display brightness percent parameter"),
                SampleCommand("Battery status batao", "बैटरी स्टेटस बताओ", "Inspects current battery health percentage"),
                SampleCommand("Storage space check karo", "स्टोरेज चेक करो", "Analyses storage space usage")
            ),
            "COMMUNICATION" to listOf(
                SampleCommand("Papa ko call karo", "पापा को कॉल करो", "Triggers hands-free calling simulator dials Papa"),
                SampleCommand("Priya ko WhatsApp karo", "प्रिया को व्हाट्सएप करो", "Drafts message overlay template to Priya"),
                SampleCommand("Message bhejo: Main aa raha hoon", "मैसेज भेजो: मैं आ रहा हूँ", "Drafts GSM SMS message box with specified body")
            ),
            "APPS" to listOf(
                SampleCommand("YouTube par gaana chalao", "यूट्यूब पर गाना चलाओ", "Launches customized internal YouTube player screen"),
                SampleCommand("Instagram kholke Reels dikhao", "इंस्टाग्राम खोलके रील्स दिखाओ", "Launches mock Instagram Reels auto-scroller feed"),
                SampleCommand("Camera kholo", "कैमरा खोलो", "Launches Camera capture viewfinder overlay screen")
            ),
            "MEDIA" to listOf(
                SampleCommand("Music play karo", "म्यूजिक प्ले करो", "Launches simulated internal background player"),
                SampleCommand("Volume badhao", "वॉल्यूम बढ़ाओ", "Increments media volume slider by 15%"),
                SampleCommand("Volume kam karo", "वॉल्यूम कम करो", "Decrements media volume slider by 15%"),
                SampleCommand("Next track chalao", "नेक्स्ट ट्रैक चलाओ", "Skips to the next mock song")
            ),
            "NAVIGATION" to listOf(
                SampleCommand("Home screen jao", "होम स्क्रीन जाओ", "Resets and minimizes all running overlays"),
                SampleCommand("Back jao", "बैक जाओ", "Performs backward hierarchical screen navigation")
            ),
            "INFORMATION" to listOf(
                SampleCommand("Mausam kaisa hai?", "मौसम कैसा है?", "Retrieves local weather parameters"),
                SampleCommand("Time kya hua?", "टाइम क्या हुआ?", "Announces current localized system clock time")
            ),
            "AUTOMATION" to listOf(
                SampleCommand("Routine: Good Morning chalao", "रूटीन: गुड मॉर्निंग चलाओ", "Triggers step-by-step 'Good Morning' chaining runner"),
                SampleCommand("Routine: Driving Mode chalao", "रूटीन: ड्राइविंग मोड चलाओ", "Triggers 'Driving Mode' auto-configuration pipeline")
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MidnightSlate, DeepCoal)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 16.dp, bottom = 12.dp)
            ) {
                Text(
                    text = "Bolo Commands Guide",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = SoftCyan
                )
                Text(
                    text = "Bilingual (Hinglish/Hindi) voice command catalog",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedSlate
                )
            }

            // Interactive Category Scrollable Row
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory),
                containerColor = Color.Transparent,
                contentColor = SunsetAmber,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[categories.indexOf(selectedCategory)]),
                        color = SunsetAmber
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                categories.forEach { cat ->
                    val selected = selectedCategory == cat
                    Tab(
                        selected = selected,
                        onClick = { selectedCategory = cat },
                        text = {
                            Text(
                                text = cat,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp,
                                color = if (selected) SunsetAmber else MutedSlate
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable list of commands
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val currentList = commandsMap[selectedCategory] ?: emptyList()

                items(currentList) { cmd ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DeepCoal),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, DarkSteel, RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "\"${cmd.englishText}\"",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = SoftCyan
                            )
                            Text(
                                text = "\"${cmd.hindiText}\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SunsetAmber,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )

                            Divider(color = DarkSteel, modifier = Modifier.padding(vertical = 4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = cmd.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedSlate,
                                    modifier = Modifier.weight(1f)
                                )

                                Button(
                                    onClick = {
                                        // Trigger Speak Simulation
                                        onNavigateToDashboard()
                                        viewModel.startListening()
                                        viewModel.stopListeningAndProcess(cmd.englishText)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SunsetAmber.copy(alpha = 0.15f)),
                                    border = BorderStroke(1.dp, SunsetAmber),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("try_command_${cmd.englishText.replace(" ", "_").lowercase()}")
                                ) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = "Try", tint = SunsetAmber, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("TRY NOW", color = SunsetAmber, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
