package com.aerodot.app

import android.Manifest
import android.content.Intent
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF080D16)
private val Panel = Color(0xFF111B2A)
private val Cyan = Color(0xFF35D9FF)
private val Muted = Color(0xFF91A4B8)
private val presets = listOf(
    "Cross", "X-Cross", "T-Cross", "Circle", "Dual Ring", "Chevron", "Brackets", "Corners",
    "Sniper", "Dot", "Plus", "Small Cross", "Open Cross", "Square", "Diamond", "Star"
)
private val colors = listOf(
    Color(0xFF35D9FF), Color(0xFF00FF66), Color(0xFFFF3B3B), Color(0xFFFFFF00),
    Color(0xFFFF55FF), Color(0xFFFFFFFF), Color(0xFFFF8C32), Color(0xFF7C4DFF)
)

class MainActivity : ComponentActivity() {
    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                background = Bg, surface = Panel, primary = Cyan,
                onBackground = Color.White, onSurface = Color.White
            )) {
                AeroDotApp(
                    onStartOverlay = { startOverlay() },
                    onStopOverlay = { sendOverlay(OverlayService.ACTION_STOP) },
                    onShowOverlay = { sendOverlay(OverlayService.ACTION_SHOW) },
                    onHideOverlay = { sendOverlay(OverlayService.ACTION_HIDE) },
                    onRequestOverlayPermission = { requestOverlayPermission() },
                    onUpdateOverlay = { sendOverlay(OverlayService.ACTION_UPDATE) }
                )
            }
        }
    }

    private fun startOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission()
            return
        }
        val intent = Intent(this, OverlayService::class.java).setAction(OverlayService.ACTION_START)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent)
        else startService(intent)
    }

    private fun sendOverlay(action: String) {
        val intent = Intent(this, OverlayService::class.java).setAction(action)
        if (action == OverlayService.ACTION_STOP) startService(intent)
        else startService(intent)
    }

    private fun requestOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        }
    }
}

@Composable
private fun AeroDotApp(
    onStartOverlay: () -> Unit,
    onStopOverlay: () -> Unit,
    onShowOverlay: () -> Unit,
    onHideOverlay: () -> Unit,
    onRequestOverlayPermission: () -> Unit,
    onUpdateOverlay: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("aerodot_settings", Context.MODE_PRIVATE) }
    var selectedShape by remember { mutableStateOf(prefs.getString("shape", "Cross") ?: "Cross") }
    var selectedColor by remember { mutableIntStateOf(prefs.getInt("color", android.graphics.Color.rgb(53,217,255))) }
    var size by remember { mutableFloatStateOf(prefs.getFloat("size", 22f)) }
    var thickness by remember { mutableFloatStateOf(prefs.getFloat("thickness", 2.5f)) }
    var gap by remember { mutableFloatStateOf(prefs.getFloat("gap", 5f)) }
    var opacity by remember { mutableFloatStateOf(prefs.getFloat("opacity", 1f)) }
    var offsetX by remember { mutableFloatStateOf(prefs.getFloat("offset_x", 0f)) }
    var offsetY by remember { mutableFloatStateOf(prefs.getFloat("offset_y", 0f)) }
    var centerDot by remember { mutableStateOf(prefs.getBoolean("center_dot", false)) }
    var page by remember { mutableStateOf("Home") }

    fun saveAndRefresh() {
        prefs.edit()
            .putString("shape", selectedShape)
            .putInt("color", selectedColor)
            .putFloat("size", size)
            .putFloat("thickness", thickness)
            .putFloat("gap", gap)
            .putFloat("opacity", opacity)
            .putFloat("offset_x", offsetX)
            .putFloat("offset_y", offsetY)
            .putBoolean("center_dot", centerDot)
            .apply()
        onUpdateOverlay()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Bg) {
        Column(Modifier.fillMaxSize()) {
            Column(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 18.dp, vertical = 22.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("AeroDot", color = Cyan, fontSize = 28.sp, fontWeight = FontWeight.Black)
                        Text("CROSSHAIR PRO • GAMING TOOLS", color = Muted, fontSize = 10.sp, letterSpacing = 1.5.sp)
                    }
                    Text("● LIVE", color = Color(0xFF00E676), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(18.dp))
                when (page) {
                    "Home" -> HomePage(onStartOverlay, onShowOverlay, onHideOverlay, onStopOverlay, onRequestOverlayPermission)
                    "Crosshair" -> CrosshairPage(
                        selectedShape, { selectedShape = it; saveAndRefresh() },
                        selectedColor, { selectedColor = it; saveAndRefresh() },
                        size, { size = it; saveAndRefresh() },
                        thickness, { thickness = it; saveAndRefresh() },
                        gap, { gap = it; saveAndRefresh() },
                        opacity, { opacity = it; saveAndRefresh() },
                        offsetX, { offsetX = it; saveAndRefresh() },
                        offsetY, { offsetY = it; saveAndRefresh() },
                        centerDot, { centerDot = it; saveAndRefresh() }
                    )
                    "Tools" -> ToolsPage()
                    else -> HomePage(onStartOverlay, onShowOverlay, onHideOverlay, onStopOverlay, onRequestOverlayPermission)
                }
            }
            NavigationBar(containerColor = Panel) {
                listOf("Home", "Crosshair", "Tools").forEach { item ->
                    NavigationBarItem(
                        selected = page == item,
                        onClick = { page = item },
                        icon = { Text(when(item) { "Home" -> "⌂"; "Crosshair" -> "⊕"; else -> "⚡" }, fontSize = 20.sp) },
                        label = { Text(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomePage(
    start: () -> Unit, show: () -> Unit, hide: () -> Unit, stop: () -> Unit, permission: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(22.dp)).padding(18.dp)) {
            Text("CROSSHAIR PREVIEW", color = Muted, fontSize = 11.sp, letterSpacing = 1.5.sp)
            Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.width(38.dp).height(3.dp).background(Cyan))
                Box(Modifier.width(3.dp).height(38.dp).background(Cyan))
                Box(Modifier.size(5.dp).background(Cyan, CircleShape))
            }
            Text("Your aim. Your style.", color = Color.White, fontWeight = FontWeight.Bold)
            Text("Floating crosshair overlay for your games", color = Muted, fontSize = 12.sp)
        }
        Text("OVERLAY CONTROL", color = Muted, fontSize = 11.sp, letterSpacing = 1.5.sp)
        Button(onClick = start, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Cyan)) {
            Text("START CROSSHAIR", color = Bg, fontWeight = FontWeight.Bold)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = show, modifier = Modifier.weight(1f)) { Text("SHOW") }
            OutlinedButton(onClick = hide, modifier = Modifier.weight(1f)) { Text("HIDE") }
            OutlinedButton(onClick = stop, modifier = Modifier.weight(1f)) { Text("STOP") }
        }
        OutlinedButton(onClick = permission, modifier = Modifier.fillMaxWidth()) {
            Text("OVERLAY PERMISSION")
        }
        Text("Quick controls are also available from the persistent notification while the overlay service runs.", color = Muted, fontSize = 11.sp)
    }
}

@Composable
private fun CrosshairPage(
    shape: String, setShape: (String) -> Unit,
    color: Int, setColor: (Int) -> Unit,
    size: Float, setSize: (Float) -> Unit,
    thickness: Float, setThickness: (Float) -> Unit,
    gap: Float, setGap: (Float) -> Unit,
    opacity: Float, setOpacity: (Float) -> Unit,
    offsetX: Float, setOffsetX: (Float) -> Unit,
    offsetY: Float, setOffsetY: (Float) -> Unit,
    centerDot: Boolean, setCenterDot: (Boolean) -> Unit
) {
    Column(Modifier.fillMaxWidth().fillMaxHeight().padding(bottom = 8.dp)) {
        Text("CROSSHAIR EDITOR", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Choose a style and fine-tune every detail.", color = Muted, fontSize = 12.sp)
        Spacer(Modifier.height(12.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            presets.forEach { name ->
                FilterChip(selected = shape == name, onClick = { setShape(name) }, label = { Text(name) })
            }
        }
        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(9.dp), verticalAlignment = Alignment.CenterVertically) {
            colors.forEach { swatch ->
                Box(
                    Modifier.size(29.dp).background(swatch, CircleShape)
                        .border(if (color == swatch.toArgb()) 3.dp else 1.dp, Color.White.copy(alpha = .7f), CircleShape)
                        .clickable { setColor(swatch.value.toInt()) }
                )
            }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            SettingSlider("Size", size, 6f..60f, setSize)
            SettingSlider("Thickness", thickness, 1f..8f, setThickness)
            SettingSlider("Gap", gap, 0f..24f, setGap)
            SettingSlider("Opacity", opacity, 0.1f..1f, setOpacity)
            SettingSlider("Horizontal position", offsetX, -200f..200f, setOffsetX)
            SettingSlider("Vertical position", offsetY, -300f..300f, setOffsetY)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Center dot", color = Color.White)
                Switch(checked = centerDot, onCheckedChange = setCenterDot)
            }
        }
    }
}

@Composable
private fun SettingSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Color.White, fontSize = 12.sp)
            Text(String.format("%.1f", value), color = Cyan, fontSize = 12.sp)
        }
        Slider(value = value, onValueChange = onChange, valueRange = range, colors = SliderDefaults.colors(thumbColor = Cyan, activeTrackColor = Cyan))
    }
}

@Composable
private fun ToolsPage() {
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("GAMING TOOLS", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Practice, measure and improve.", color = Muted, fontSize = 12.sp)
        ToolPanel("AIM TRAINER", "30-second target practice • 3 difficulty levels")
        ToolPanel("REACTION TEST", "5 rounds • measure your reaction time in milliseconds")
        ToolPanel("TAP SPEED / CPS", "10-second tapping speed test")
        ToolPanel("PING CHECKER", "Check network latency")
        ToolPanel("PROFILES", "Save and load your favorite crosshair configurations")
    }
}

@Composable
private fun ToolPanel(title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(16.dp)).padding(16.dp)) {
        Text(title, color = Cyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, color = Muted, fontSize = 12.sp)
    }
}
