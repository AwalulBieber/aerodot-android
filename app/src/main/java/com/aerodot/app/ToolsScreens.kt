package com.aerodot.app

import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.random.Random

private val TP = Color(0xFF111B2A)
private val TC = Color(0xFF35D9FF)
private val TM = Color(0xFF91A4B8)

@Composable
fun AeroDotTools(prefs: SharedPreferences, onOverlayUpdate: () -> Unit) {
    var page by remember { mutableStateOf("Tools") }
    Column(Modifier.fillMaxSize()) {
        if (page != "Tools") {
            Text("‹ TOOLS", color = TC, modifier = Modifier.clickable { page = "Tools" })
            Spacer(Modifier.height(12.dp))
        }
        when (page) {
            "Aim Trainer" -> AimTool()
            "Reaction Test" -> ReactionTool()
            "CPS Test" -> CpsTool()
            "Ping Checker" -> PingTool()
            "Profiles" -> ProfileTool(prefs, onOverlayUpdate)
            else -> {
                Text("GAMING TOOLS", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Practice, measure and improve.", color = TM, fontSize = 12.sp)
                Spacer(Modifier.height(14.dp))
                listOf("Aim Trainer", "Reaction Test", "CPS Test", "Ping Checker", "Profiles").forEach { item ->
                    Column(
                        Modifier.fillMaxWidth().padding(bottom = 10.dp)
                            .background(TP, RoundedCornerShape(16.dp))
                            .clickable { page = item }.padding(16.dp)
                    ) {
                        Text(item.uppercase(), color = TC, fontWeight = FontWeight.Bold)
                        Text(
                            when (item) {
                                "Aim Trainer" -> "30-second target drill • 3 difficulty levels"
                                "Reaction Test" -> "5 rounds • reaction time in milliseconds"
                                "CPS Test" -> "10-second tap speed test"
                                "Ping Checker" -> "Measure connection latency"
                                else -> "Save and load crosshair configurations"
                            }, color = TM, fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AimTool() {
    var difficulty by remember { mutableStateOf("Normal") }
    var running by remember { mutableStateOf(false) }
    var time by remember { mutableIntStateOf(30) }
    var hits by remember { mutableIntStateOf(0) }
    var x by remember { mutableFloatStateOf(90f) }
    var y by remember { mutableFloatStateOf(90f) }
    LaunchedEffect(running) {
        if (running) {
            while (time > 0) { delay(1000); time-- }
            running = false
        }
    }
    Text("AIM TRAINER", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        listOf("Easy", "Normal", "Hard").forEach {
            FilterChip(selected = difficulty == it, onClick = { if (!running) difficulty = it }, label = { Text(it) })
        }
    }
    Text("TIME: " + time + "s     HITS: " + hits, color = TC, fontWeight = FontWeight.Bold)
    BoxWithConstraints(Modifier.fillMaxWidth().weight(1f).background(TP, RoundedCornerShape(18.dp))) {
        val d = if (difficulty == "Easy") 52.dp else if (difficulty == "Hard") 28.dp else 40.dp
        Box(
            Modifier.offset(x.dp, y.dp).size(d).background(TC, CircleShape)
                .clickable(enabled = running) {
                    hits++
                    x = Random.nextFloat() * (maxWidth - d).value.coerceAtLeast(0f)
                    y = Random.nextFloat() * (maxHeight - d).value.coerceAtLeast(0f)
                },
            contentAlignment = Alignment.Center
        ) { Box(Modifier.size(d / 3).background(TP, CircleShape)) }
        if (!running) Text(if (time == 0) "FINISHED" else "PRESS START", color = TM, modifier = Modifier.align(Alignment.Center))
    }
    Button(onClick = { if (!running) { hits = 0; time = 30; running = true } }, modifier = Modifier.fillMaxWidth()) {
        Text(if (running) "RUNNING..." else "START 30 SECOND DRILL")
    }
}

@Composable
private fun ReactionTool() {
    var phase by remember { mutableStateOf("IDLE") }
    var round by remember { mutableIntStateOf(0) }
    var start by remember { mutableLongStateOf(0L) }
    var ms by remember { mutableIntStateOf(0) }
    var best by remember { mutableIntStateOf(Int.MAX_VALUE) }
    var early by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize()) {
        Text("REACTION TEST • ROUND " + (round + 1).coerceAtMost(5) + "/5", color = TM)
        Box(
            Modifier.fillMaxWidth().weight(1f).padding(vertical = 12.dp)
                .background(if (phase == "GO") Color(0xFF00B85C) else if (phase == "WAIT") Color(0xFFB83232) else TP, RoundedCornerShape(20.dp))
                .clickable {
                    when (phase) {
                        "IDLE", "RESULT" -> {
                            if (round >= 5) round = 0
                            early = false
                            phase = "WAIT"
                            scope.launch {
                                delay(Random.nextLong(1500, 4000))
                                if (phase == "WAIT") { start = System.nanoTime(); phase = "GO" }
                            }
                        }
                        "WAIT" -> { early = true; phase = "RESULT" }
                        "GO" -> {
                            ms = ((System.nanoTime() - start) / 1_000_000).toInt()
                            if (ms < best) best = ms
                            round++
                            phase = "RESULT"
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (phase == "WAIT") "WAIT FOR GREEN" else if (phase == "GO") "TAP NOW!" else if (phase == "RESULT") if (early) "TOO EARLY!" else ms.toString() + " ms" else "TAP TO START",
                color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Black
            )
        }
        Text("BEST: " + if (best == Int.MAX_VALUE) "--" else best.toString() + " ms", color = TC)
        if (round >= 5) Text("5 ROUNDS COMPLETED", color = TC)
    }
}

@Composable
private fun CpsTool() {
    var active by remember { mutableStateOf(false) }
    var seconds by remember { mutableIntStateOf(10) }
    var taps by remember { mutableIntStateOf(0) }
    LaunchedEffect(active) {
        if (active) {
            while (seconds > 0) { delay(1000); seconds-- }
            active = false
        }
    }
    Column(Modifier.fillMaxSize()) {
        Text("TAP SPEED / CPS", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Box(
            Modifier.fillMaxWidth().weight(1f).padding(vertical = 14.dp)
                .background(TP, CircleShape).clickable(enabled = active) { taps++ },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(taps.toString(), color = TC, fontSize = 58.sp, fontWeight = FontWeight.Black)
                Text(if (active) "TAP AS FAST AS YOU CAN" else "TOTAL TAPS", color = TM)
            }
        }
        Text("TIME LEFT: " + seconds + "s", color = TM)
        Button(onClick = { if (!active) { taps = 0; seconds = 10; active = true } }, modifier = Modifier.fillMaxWidth()) {
            Text(if (active) "TEST RUNNING" else "START 10 SECOND TEST")
        }
    }
}

@Composable
private fun PingTool() {
    var busy by remember { mutableStateOf(false) }
    var ping by remember { mutableStateOf("--") }
    var message by remember { mutableStateOf("Ready to test") }
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize()) {
        Text("PING CHECKER", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Column(Modifier.fillMaxWidth().weight(1f).background(TP, RoundedCornerShape(20.dp)), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(ping, color = TC, fontSize = 58.sp, fontWeight = FontWeight.Black)
            Text("milliseconds", color = TM)
            Text(message, color = Color.White)
            Text("Target: 1.1.1.1:443", color = TM, fontSize = 11.sp)
        }
        Button(onClick = {
            if (!busy) scope.launch {
                busy = true
                message = "Checking..."
                val value = withContext(Dispatchers.IO) {
                    try {
                        val t = System.nanoTime()
                        Socket().use { it.connect(InetSocketAddress("1.1.1.1", 443), 2500) }
                        ((System.nanoTime() - t) / 1_000_000).toInt()
                    } catch (_: Exception) { -1 }
                }
                ping = if (value < 0) "--" else value.toString()
                message = if (value < 0) "Connection failed" else "Connection successful"
                busy = false
            }
        }, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text(if (busy) "TESTING..." else "CHECK PING") }
    }
}

@Composable
private fun ProfileTool(prefs: SharedPreferences, update: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var revision by remember { mutableIntStateOf(0) }
    val profiles = remember(revision) { prefs.all.keys.filter { it.startsWith("profile_") }.sorted() }
    Column(Modifier.fillMaxSize().verticalScroll(androidx.compose.foundation.rememberScrollState())) {
        Text("PROFILES", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(name, { name = it }, label = { Text("Profile name") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            if (name.isNotBlank()) {
                val data = listOf(
                    prefs.getString("shape", "Cross"),
                    prefs.getInt("color", android.graphics.Color.CYAN).toString(),
                    prefs.getFloat("size", 22f).toString(),
                    prefs.getFloat("thickness", 2.5f).toString(),
                    prefs.getFloat("gap", 5f).toString(),
                    prefs.getFloat("opacity", 1f).toString(),
                    prefs.getFloat("offset_x", 0f).toString(),
                    prefs.getFloat("offset_y", 0f).toString(),
                    prefs.getBoolean("center_dot", false).toString()
                ).joinToString("|")
                prefs.edit().putString("profile_" + name.trim(), data).apply()
                name = ""
                revision++
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("SAVE CURRENT PROFILE") }
        profiles.forEach { key ->
            Row(Modifier.fillMaxWidth().padding(vertical = 5.dp).background(TP, RoundedCornerShape(12.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(key.removePrefix("profile_"), color = Color.White, modifier = Modifier.weight(1f))
                Text("LOAD", color = TC, modifier = Modifier.clickable {
                    val p = (prefs.getString(key, "") ?: "").split("|")
                    if (p.size == 9) {
                        prefs.edit().putString("shape", p[0])
                            .putInt("color", p[1].toIntOrNull() ?: android.graphics.Color.CYAN)
                            .putFloat("size", p[2].toFloatOrNull() ?: 22f)
                            .putFloat("thickness", p[3].toFloatOrNull() ?: 2.5f)
                            .putFloat("gap", p[4].toFloatOrNull() ?: 5f)
                            .putFloat("opacity", p[5].toFloatOrNull() ?: 1f)
                            .putFloat("offset_x", p[6].toFloatOrNull() ?: 0f)
                            .putFloat("offset_y", p[7].toFloatOrNull() ?: 0f)
                            .putBoolean("center_dot", p[8].toBoolean()).apply()
                        update()
                    }
                })
                Spacer(Modifier.width(12.dp))
                Text("✕", color = Color.Red, modifier = Modifier.clickable { prefs.edit().remove(key).apply(); revision++ })
            }
        }
        if (profiles.isEmpty()) Text("No profiles saved yet.", color = TM)
    }
}
