package com.aerodot.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Background = Color(0xFF080D16)
private val Panel = Color(0xFF111B2A)
private val Cyan = Color(0xFF35D9FF)
private val Muted = Color(0xFF91A4B8)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                background = Background,
                surface = Panel,
                primary = Cyan,
                onBackground = Color.White,
                onSurface = Color.White
            )) {
                AeroDotHome()
            }
        }
    }
}

@Composable
private fun AeroDotHome() {
    Surface(modifier = Modifier.fillMaxSize(), color = Background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("AeroDot", color = Cyan, fontSize = 28.sp, fontWeight = FontWeight.Black)
                    Text("CROSSHAIR CONTROL CENTER", color = Muted, fontSize = 10.sp, letterSpacing = 2.sp)
                }
                Box(
                    modifier = Modifier.size(42.dp).border(1.dp, Cyan.copy(alpha = 0.55f), CircleShape),
                    contentAlignment = Alignment.Center
                ) { Text("AD", color = Cyan, fontWeight = FontWeight.Bold) }
            }

            Column(
                modifier = Modifier.fillMaxWidth()
                    .background(Panel, RoundedCornerShape(24.dp))
                    .border(1.dp, Cyan.copy(alpha = 0.18f), RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Text("CROSSHAIR PREVIEW", color = Muted, fontSize = 11.sp, letterSpacing = 1.5.sp)
                Box(modifier = Modifier.fillMaxWidth().height(190.dp), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(width = 34.dp, height = 3.dp).background(Cyan))
                    Box(Modifier.size(width = 3.dp, height = 34.dp).background(Cyan))
                    Box(Modifier.size(5.dp).background(Cyan, CircleShape))
                }
                Text("Default • Cyan", color = Color.White, fontWeight = FontWeight.SemiBold)
                Text("Your crosshair preview will appear here.", color = Muted, fontSize = 12.sp)
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Cyan),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("CUSTOMIZE CROSSHAIR", color = Background, fontWeight = FontWeight.Bold) }
            }

            Text("GAMING TOOLS", color = Muted, fontSize = 11.sp, letterSpacing = 1.5.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ToolCard("128+", "Styles", Modifier.weight(1f))
                ToolCard("AIM", "Trainer", Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ToolCard("REACTION", "Reflex test", Modifier.weight(1f))
                ToolCard("CPS", "Tap speed", Modifier.weight(1f))
            }
            Spacer(Modifier.weight(1f))
            Text(
                "AeroDot • Precision starts here",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = Muted,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun ToolCard(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.background(Panel, RoundedCornerShape(18.dp))
            .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Text(title, color = Cyan, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(5.dp))
        Text(subtitle, color = Muted, fontSize = 12.sp)
    }
}
