package id.ideahousetech.prayertime_qibla.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ideahousetech.prayertime_qibla.ui.theme.*

// Pemisah tipis gold
@Composable
fun GoldDivider(modifier: Modifier = Modifier) {
    Divider(modifier = modifier, color = DividerLine, thickness = 0.5.dp)
}

// Label section uppercase
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text          = text.uppercase(),
        fontSize      = 10.sp,
        fontWeight    = FontWeight.Bold,
        color         = TextMuted,
        letterSpacing = 2.sp,
        modifier      = modifier
    )
}

// Top bar premium konsisten
@Composable
fun PremiumTopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Outlined.ArrowBack, "Kembali", tint = GoldPrimary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontFamily = CinzelFont,
            color = GoldPrimary
        )
    }
}

// Loading state
@Composable
fun LoadingOverlay() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = GoldPrimary, strokeWidth = 2.dp, modifier = Modifier.size(40.dp))
    }
}
