package id.ideahousetech.prayertime_qibla.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ideahousetech.prayertime_qibla.AppScreen
import id.ideahousetech.prayertime_qibla.ui.theme.*

data class NavigationTabItem(
    val screen: AppScreen,
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

/**
 * Modern Floating Bottom Navigation Bar (72dp height, 16dp pad bottom).
 * Designed for ultimate luxury, single-hand ergonomics, and smooth transitions.
 */
@Composable
fun FloatingBottomBar(
    currentScreen: AppScreen,
    onTabSelected: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = remember {
        listOf(
            NavigationTabItem(AppScreen.SHOLAT, "Beranda", Icons.Default.Home, "tab_home"),
            NavigationTabItem(AppScreen.QURAN, "Quran", Icons.Default.MenuBook, "tab_quran"),
            NavigationTabItem(AppScreen.TRACKER, "Aktivitas", Icons.Default.CheckCircle, "tab_activity"),
            NavigationTabItem(AppScreen.EXPLORE, "Eksplor", Icons.Default.Explore, "tab_explore"),
            NavigationTabItem(AppScreen.PROFILE, "Pengaturan", Icons.Default.Settings, "tab_settings")
        )
    }

    // Determine current active main tab (collapsing nested subpages back to their root parents)
    val activeTab = when (currentScreen) {
        AppScreen.SHOLAT -> AppScreen.SHOLAT
        AppScreen.QURAN -> AppScreen.QURAN
        AppScreen.TRACKER -> AppScreen.TRACKER
        AppScreen.EXPLORE -> AppScreen.EXPLORE
        AppScreen.PROFILE -> AppScreen.PROFILE
        // sub pages belong to different tabs (e.g., KIBLAT, TASBIH belong to EXPLORE tab)
        AppScreen.KIBLAT, AppScreen.TASBIH, AppScreen.DOA, AppScreen.KALENDER, AppScreen.JADWAL, AppScreen.JADWAL_HARIAN -> AppScreen.EXPLORE
        else -> AppScreen.SHOLAT
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars) // Menangani gesture bar / safe area sistem
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(72.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(28.dp),
                clip = false,
                ambientColor = GoldPrimary.copy(alpha = 0.25f),
                spotColor = Color.Black
            )
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        CardSurface.copy(alpha = 0.94f),
                        MidnightLayer.copy(alpha = 0.98f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        GlassBorder.copy(alpha = 0.35f),
                        GoldGlow.copy(alpha = 0.2f),
                        GlassBorder.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
    ) {
        // Subtle decorative specular glare inside floating container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(GlassGaze.copy(alpha = 0.08f), Color.Transparent)
                    )
                )
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            items.forEach { item ->
                val isSelected = activeTab == item.screen
                
                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) GoldPrimary else TextMuted,
                    animationSpec = spring(stiffness = 300f),
                    label = "tab_icon_color"
                )

                val weightScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.15f else 0.95f,
                    animationSpec = spring(stiffness = 300f),
                    label = "tab_scale"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .testTag(item.testTag)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null, // Disable full button default gray ripple rectangle inside custom layout
                            onClick = { onTabSelected(item.screen) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Icon + active indicators
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(32.dp)
                        ) {
                            if (isSelected) {
                                // Dynamic active indicator soft glow backdrop
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(GoldGlow.copy(alpha = 0.15f))
                                )
                            }
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = iconColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(Modifier.height(2.dp))

                        Text(
                            text = item.title,
                            fontFamily = NunitoFont,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                            color = iconColor,
                            letterSpacing = 0.2.sp
                        )
                    }
                }
            }
        }
    }
}
