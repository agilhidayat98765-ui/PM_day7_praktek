package com.example.pm_day7_praktek

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Color(0xFF0D1117), surface = Color(0xFF161B22),
                    primary = Color(0xFF00FF00), error = Color(0xFFFF3333)
                )
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    SocAppNavigation()
                }
            }
        }
    }
}

// =========================================================================
// SISTEM NAVIGASI & SHARED STATE (STATE HOISTING)
// =========================================================================
@Composable
fun SocAppNavigation() {
    val navController = rememberNavController()

    // Tempat penampungan data sementara agar bisa dikirim dari Layar 2 ke Layar 3
    var savedIp by rememberSaveable { mutableStateOf("") }
    var savedThreat by rememberSaveable { mutableStateOf("") }
    var savedDesc by rememberSaveable { mutableStateOf("") }

    NavHost(navController = navController, startDestination = "layar_login") {
        composable("layar_login") { LoginScreen(navController) }

        composable("layar_dashboard") {
            SocAdaptiveDashboard(navController) { ip, threat, desc ->
                // Saat tombol View Database ditekan, data dari form disimpan ke pusat
                savedIp = ip
                savedThreat = threat
                savedDesc = desc
            }
        }

        composable("layar_database") {
            // Data yang sudah disimpan di pusat dikirimkan ke Layar Database
            DatabaseScreen(navController, savedIp, savedThreat, savedDesc)
        }
    }
}

// =========================================================================
// LAYAR 1: LOGIN
// =========================================================================
@Composable
fun LoginScreen(navController: NavController) {
    var agentId by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isError by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.Security, contentDescription = null, tint = Color(0xFF00FF00), modifier = Modifier.size(80.dp))
        Spacer(Modifier.height(16.dp))
        Text("SOC AGENT PORTAL", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Autentikasi Terenkripsi 256-bit", color = Color.Gray, fontSize = 14.sp)

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = agentId, onValueChange = { agentId = it; isError = false },
            label = { Text("Agent ID / Username") }, leadingIcon = { Icon(Icons.Filled.Person, null) },
            modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = password, onValueChange = { password = it; isError = false },
            label = { Text("Password") }, leadingIcon = { Icon(Icons.Filled.Lock, null) },
            visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        if (isError) {
            Text("Akses Ditolak! ID atau Password tidak boleh kosong.", color = Color(0xFFFF3333), modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(Modifier.height(32.dp))
        Button(
            onClick = {
                if (agentId.isNotEmpty() && password.isNotEmpty()) {
                    navController.navigate("layar_dashboard")
                } else {
                    isError = true
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF00), contentColor = Color.Black)
        ) {
            Text("LOGIN TO SERVER", fontWeight = FontWeight.Bold)
        }
    }
}

// =========================================================================
// LAYAR 2: DASHBOARD FORM
// =========================================================================
@Composable
fun SocAdaptiveDashboard(navController: NavController, onSaveData: (String, String, String) -> Unit) {
    var ipAddress by rememberSaveable { mutableStateOf("") }
    var threatLevel by rememberSaveable { mutableStateOf("LOW") }
    var description by rememberSaveable { mutableStateOf("") }
    var sudahSubmit by rememberSaveable { mutableStateOf(false) }

    val errorIp = sudahSubmit && (ipAddress.trim().isEmpty() || !ipAddress.contains("."))
    val errorDesc = sudahSubmit && description.trim().length < 15
    val formValid = !errorIp && !errorDesc && ipAddress.isNotEmpty() && description.isNotEmpty()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        if (maxWidth < 520.dp) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                HeaderSection()
                FormSection(ipAddress, { ipAddress = it; sudahSubmit = false }, errorIp, threatLevel, { threatLevel = it; sudahSubmit = false }, description, { description = it; sudahSubmit = false }, errorDesc, sudahSubmit, formValid) { sudahSubmit = true }
                LiveLogSection(ipAddress, threatLevel, description, sudahSubmit, formValid, navController, onSaveData)
            }
        } else {
            Row(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    HeaderSection()
                    Spacer(Modifier.height(16.dp))
                    FormSection(ipAddress, { ipAddress = it; sudahSubmit = false }, errorIp, threatLevel, { threatLevel = it; sudahSubmit = false }, description, { description = it; sudahSubmit = false }, errorDesc, sudahSubmit, formValid) { sudahSubmit = true }
                }
                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    LiveLogSection(ipAddress, threatLevel, description, sudahSubmit, formValid, navController, onSaveData)
                }
            }
        }
    }
}

// =========================================================================
// LAYAR 3: DATABASE ARSIP (Kini Menerima Data Asli)
// =========================================================================
@Composable
fun DatabaseScreen(navController: NavController, latestIp: String, latestThreat: String, latestDesc: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Storage, null, tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("SERVER DATABASE", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Text("Daftar arsip log ancaman yang telah diverifikasi.", color = Color.Gray, fontSize = 14.sp)

        // Memunculkan data asli yang baru saja diketik oleh pengguna
        if (latestIp.isNotEmpty()) {
            Text(">> LAPORAN TERBARU ANDA:", color = Color(0xFF00FF00), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            DatabaseCard(latestIp, latestThreat, latestDesc)

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color.DarkGray)
            Spacer(Modifier.height(16.dp))
        }

        Text(">> ARSIP TERDAHULU:", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        DatabaseCard("10.0.0.14", "CRITICAL", "SQL Injection pada port 3306")
        DatabaseCard("192.168.1.5", "LOW", "Akses login gagal 3 kali.")

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { navController.navigate("layar_login") { popUpTo(0) } },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3333), contentColor = Color.White)
        ) {
            Text("LOGOUT SESSION", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DatabaseCard(ip: String, threat: String, desc: String) {
    val color = if (threat == "CRITICAL") Color(0xFFFF3333) else if (threat == "MEDIUM") Color(0xFFFFA500) else Color(0xFF00FF00)
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("IP: $ip", color = Color.White, fontWeight = FontWeight.Bold)
            Text("Level: $threat", color = color)
            Text("Desc: $desc", color = Color.Gray, fontSize = 12.sp)
        }
    }
}

// =========================================================================
// KOMPONEN PENDUKUNG (TIDAK ADA PERUBAHAN VISUAL)
// =========================================================================
@Composable
fun HeaderSection() {
    Column {
        Text(">> SYSTEM COMPROMISED", color = Color(0xFFFF3333), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text("SOC Incident Report", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun FormSection(ipAddress: String, onIpChange: (String) -> Unit, errorIp: Boolean, threatLevel: String, onThreatChange: (String) -> Unit, description: String, onDescChange: (String) -> Unit, errorDesc: Boolean, sudahSubmit: Boolean, formValid: Boolean, onSubmitClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = ipAddress, onValueChange = onIpChange, label = { Text("Target IP Address") }, leadingIcon = { Icon(Icons.Filled.Dns, null) }, isError = errorIp, supportingText = { if (errorIp) Text("IP wajib diisi dan format harus benar (mengandung titik)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
            Text("Tingkat Ancaman (Threat Level):", color = Color.White, fontSize = 14.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThreatButton("LOW", threatLevel == "LOW", Color(0xFF00FF00), { onThreatChange("LOW") }, Modifier.weight(1f))
                ThreatButton("MEDIUM", threatLevel == "MEDIUM", Color(0xFFFFA500), { onThreatChange("MEDIUM") }, Modifier.weight(1f))
                ThreatButton("TINGGGI", threatLevel == "CRITICAL", Color(0xFFFF3333), { onThreatChange("CRITICAL") }, Modifier.weight(1f))
            }
            OutlinedTextField(value = description, onValueChange = onDescChange, label = { Text("Deskripsi Log") }, leadingIcon = { Icon(Icons.Filled.BugReport, null) }, isError = errorDesc, supportingText = { if (errorDesc) Text("Minimal 15 karakter.") }, modifier = Modifier.fillMaxWidth().height(100.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
            Button(onClick = onSubmitClick, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(
                0xFF9C27B0
            ), contentColor = Color.Black)) { Text("ENCRYPT & SEND REPORT", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
fun ThreatButton(label: String, isSelected: Boolean, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick = onClick, modifier = modifier, colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) color else Color.Transparent, contentColor = if (isSelected) Color.Black else color), shape = RoundedCornerShape(8.dp), border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, color) else null) { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
}

@Composable
fun LiveLogSection(ipAddress: String, threatLevel: String, description: String, sudahSubmit: Boolean, formValid: Boolean, navController: NavController, onSaveData: (String, String, String) -> Unit) {
    val borderColor = when (threatLevel) { "LOW" -> Color(0xFF00FF00); "MEDIUM" -> Color(0xFFFFA500); "CRITICAL" -> Color(0xFFFF3333); else -> Color.Gray }
    Box(modifier = Modifier.fillMaxWidth().border(1.dp, borderColor, RoundedCornerShape(8.dp)).background(Color(0xFF0D1117)).padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.CrisisAlert, null, tint = borderColor); Spacer(Modifier.width(8.dp)); Text("LIVE SERVER MONITORING", color = borderColor, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
            Text("IP Tracked : ${if (ipAddress.isEmpty()) "[EMPTY]" else ipAddress}", color = Color.White, fontSize = 13.sp)
            Text("Threat     : [$threatLevel]", color = borderColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text("Log Desc   : ${if (description.isEmpty()) "Waiting..." else description}", color = Color.Gray, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(16.dp))
            if (sudahSubmit) {
                if (formValid) {
                    Text("STATUS: SECURED ✅", color = Color(0xFF00FF00), fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            onSaveData(ipAddress, threatLevel, description) // Pemicu pengiriman data!
                            navController.navigate("layar_database")
                        },
                        modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF00), contentColor = Color.Black)
                    ) {
                        Text("VIEW DATABASE", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text("STATUS: VALIDATION FAILED ❌", color = Color(0xFFFF3333), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}