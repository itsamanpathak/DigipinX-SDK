package com.amanpathak.digipinx.sdk.demo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amanpathak.digipinx.sdk.DigipinXS
import com.amanpathak.digipinx.sdk.demo.ui.theme.DigipinXTheme
import com.amanpathak.digipinx.sdk.utils.DigipinXResult
import kotlin.let
import kotlin.text.all
import kotlin.text.contains
import kotlin.text.isNotBlank
import kotlin.text.replace
import kotlin.text.substring
import kotlin.text.toDouble
import kotlin.text.uppercase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DigipinXTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainDemoScreen()
                }
            }
        }
    }
}

fun copyToClipboard(context: Context, text: String, label: String = "DigipinX") {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
}

fun isValidLatitude(lat: String): Boolean {
    return try {
        val value = lat.toDouble()
        value >= -90.0 && value <= 90.0
    } catch (e: NumberFormatException) {
        false
    }
}

fun isValidLongitude(lon: String): Boolean {
    return try {
        val value = lon.toDouble()
        value >= -180.0 && value <= 180.0
    } catch (e: NumberFormatException) {
        false
    }
}

fun isValidDigipinX(DigipinX: String): Boolean {
    val allowedChars = "FC98J327K456LMPT"
    val clean = DigipinX.replace("-", "")
    return clean.length == 10 && clean.all { it in allowedChars }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDemoScreen() {
    val sdk = remember {
        DigipinXS.init()
    }
    val context = LocalContext.current
    var DigipinXInput by remember { mutableStateOf("") }
    var latInput by remember { mutableStateOf("") }
    var lonInput by remember { mutableStateOf("") }
    var decodedLatLon by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var encodedDigipinX by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "DigipinX Demo",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            "Easily convert between Digipin and Latitude/Longitude",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // DigipinX to Lat/Lon
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Digipin → Lat/Lon", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                
                OutlinedTextField(
                    value = DigipinXInput,
                    onValueChange = { DigipinXInput = it.uppercase() },
                    label = { Text("Enter Digipin (e.g., 39J438P582)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = DigipinXInput.isNotBlank() && !isValidDigipinX(DigipinXInput),
                    supportingText = if (DigipinXInput.isNotBlank() && !isValidDigipinX(DigipinXInput)) {
                        { Text("Invalid DigipinX format", color = MaterialTheme.colorScheme.error) }
                    } else null
                )
                
                Button(
                    onClick = {
                        val result = sdk.generateLatLon(DigipinXInput)
                        when (result) {
                            is DigipinXResult.Success -> {
                                val decoded = result.data
                                decodedLatLon = Pair(
                                    decoded.centerCoordinate.latitude,
                                    decoded.centerCoordinate.longitude
                                )
                            }
                            is DigipinXResult.Error -> {
                                decodedLatLon = null
                                Toast.makeText(context, "Decode Error: ${result.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    enabled = DigipinXInput.isNotBlank() && isValidDigipinX(DigipinXInput),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Convert to Lat/Lon")
                }
                
                decodedLatLon?.let { (lat, lon) ->
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Latitude: $lat", fontWeight = FontWeight.Medium)
                        Text("Longitude: $lon", fontWeight = FontWeight.Medium)
                        OutlinedButton(
                            onClick = {
                                val result = sdk.generateLatLon(DigipinXInput)
                                if (result is DigipinXResult.Success) {
                                    val urlResult = sdk.createGoogleMapsUrl(result.data)
                                    when (urlResult) {
                                        is DigipinXResult.Success -> {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlResult.data))
                                            context.startActivity(intent)
                                        }
                                        is DigipinXResult.Error -> {
                                            Toast.makeText(context, "URL Error: ${urlResult.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "Decode Error: ${(result as DigipinXResult.Error).message}", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("View on Map")
                        }
                    }
                }
            }
        }
        
        // Lat/Lon to DigipinX
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Lat/Lon → Digipin", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                
                OutlinedTextField(
                    value = latInput,
                    onValueChange = { latInput = it },
                    label = { Text("Latitude (-90 to 90)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = latInput.isNotBlank() && !isValidLatitude(latInput),
                    supportingText = if (latInput.isNotBlank() && !isValidLatitude(latInput)) {
                        { Text("Invalid latitude (-90 to 90)", color = MaterialTheme.colorScheme.error) }
                    } else null
                )
                
                OutlinedTextField(
                    value = lonInput,
                    onValueChange = { lonInput = it },
                    label = { Text("Longitude (-180 to 180)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = lonInput.isNotBlank() && !isValidLongitude(lonInput),
                    supportingText = if (lonInput.isNotBlank() && !isValidLongitude(lonInput)) {
                        { Text("Invalid longitude (-180 to 180)", color = MaterialTheme.colorScheme.error) }
                    } else null
                )
                
                Button(
                    onClick = {
                        val lat = latInput.toDouble()
                        val lon = lonInput.toDouble()
                        val result = sdk.generateDigipin(lat, lon)
                        when (result) {
                            is DigipinXResult.Success -> {
                                encodedDigipinX = result.data.digipin
                            }
                            is DigipinXResult.Error -> {
                                encodedDigipinX = null
                                Toast.makeText(context, "Encode Error: ${result.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    enabled = latInput.isNotBlank() && lonInput.isNotBlank() && 
                             isValidLatitude(latInput) && isValidLongitude(lonInput),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.SwapHoriz, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Convert to Digipin")
                }
                
                encodedDigipinX?.let { code ->
                    val formattedCode = try {
                        val result = sdk.generateLatLon(code)
                        when (result) {
                            is DigipinXResult.Success -> result.data.getFormattedCode()
                            is DigipinXResult.Error -> "${code.substring(0, 3)}-${code.substring(3, 6)}-${code.substring(6, 10)}"
                        }
                    } catch (e: Exception) {
                        "${code.substring(0, 3)}-${code.substring(3, 6)}-${code.substring(6, 10)}"
                    }
                    
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Digipin: $formattedCode", fontWeight = FontWeight.Medium, fontSize = 20.sp)
                        
                        Row(
                            modifier = Modifier.padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    copyToClipboard(context, formattedCode)
                                }
                            ) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Copy")
                            }
                            
                            OutlinedButton(
                                onClick = {
                                    val result = sdk.generateLatLon(code)
                                    if (result is DigipinXResult.Success) {
                                        val urlResult = sdk.createGoogleMapsUrl(result.data)
                                        when (urlResult) {
                                            is DigipinXResult.Success -> {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlResult.data))
                                                context.startActivity(intent)
                                            }
                                            is DigipinXResult.Error -> {
                                                Toast.makeText(context, "URL Error: ${urlResult.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(context, "Decode Error: ${(result as DigipinXResult.Error).message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            ) {
                                Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("View on Map")
                            }
                        }
                    }
                }
            }
        }
    }
}