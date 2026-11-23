package com.example.lifeclock

import android.app.DatePickerDialog
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LifeClockScreen() }
    }
}

@Composable
private fun LifeClockScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("lifeclock", 0) }
    var dob by remember {
        mutableStateOf(prefs.getString("dob", null)?.let(LocalDate::parse))
    }
    var error by remember { mutableStateOf<String?>(null) }
    val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")

    val stats = dob?.let {
        runCatching { LifeMath.calculate(it) }.getOrNull()
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Color(0xFF080808),
            surface = Color(0xFF171717),
            primary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().background(Color(0xFF080808)).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("LIFECLOCK", color = Color.LightGray, fontSize = 13.sp, letterSpacing = 4.sp)
            Spacer(Modifier.height(32.dp))
            Text("Your time. Your life.", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            Text("A daily reminder to make your time count.", color = Color.Gray, fontSize = 14.sp)
            Spacer(Modifier.height(36.dp))

            OutlinedButton(
                onClick = {
                    val initial = dob ?: LocalDate.of(2000, 1, 1)
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            val selected = LocalDate.of(year, month + 1, day)
                            if (selected.isAfter(LocalDate.now())) {
                                error = "Date of birth cannot be in the future."
                            } else {
                                dob = selected
                                prefs.edit().putString("dob", selected.toString()).apply()
                                error = null
                            }
                        },
                        initial.year, initial.monthValue - 1, initial.dayOfMonth
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(dob?.format(formatter) ?: "Select your date of birth")
            }

            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = Color(0xFFFF7777), fontSize = 12.sp)
            }

            stats?.let { s ->
                Spacer(Modifier.height(28.dp))
                Text("${s.remainingDays}", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                Text("DAYS REMAINING", color = Color.Gray, fontSize = 12.sp, letterSpacing = 2.sp)
                Spacer(Modifier.height(18.dp))
                Text("${s.percentage}% of your 60-year reference lifespan completed", color = Color.LightGray, fontSize = 13.sp)
                Text("${s.livedDays} days lived  •  ${s.remainingWeeks} weeks remaining", color = Color.Gray, fontSize = 12.sp)
            }

            Spacer(Modifier.height(36.dp))
            Button(
                onClick = {
                    val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                        putExtra(
                            WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                            ComponentName(context, LifeClockWallpaperService::class.java)
                        )
                    }
                    context.startActivity(intent)
                },
                enabled = dob != null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Set LifeClock wallpaper", modifier = Modifier.padding(vertical = 6.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text("Your date of birth stays on this device.", color = Color.DarkGray, fontSize = 11.sp)
        }
    }
}
