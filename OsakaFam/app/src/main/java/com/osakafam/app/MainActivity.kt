package com.osakafam.app

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.ads.MobileAds
import com.osakafam.app.ui.screens.*
import com.osakafam.app.ui.theme.C
import com.osakafam.app.ui.theme.OsakaFamTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ★ AdMob SDK 초기화 ★
        MobileAds.initialize(this) { status ->
            Log.d("AdMob", "SDK 초기화 완료")
            AdManager.loadAd(this)
        }

        setContent { OsakaFamTheme { OsakaFamApp() } }
    }
}

enum class Tab(val label: String, val icon: ImageVector) {
    HOME("홈", Icons.Default.Explore),
    MAP("길찾기", Icons.Default.Map),
    TRANSLATE("번역", Icons.Default.CameraAlt),
    LANGUAGE("회화", Icons.Default.ChatBubble),
    SETTINGS("설정", Icons.Default.Settings),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OsakaFamApp() {
    val ctx = LocalContext.current
    val activity = ctx as? Activity
    var tab by remember { mutableStateOf(Tab.HOME) }
    var exchangeRate by remember { mutableIntStateOf(900) }

    // 앱 시작 후 첫 광고 표시 여부
    var startupAdShown by remember { mutableStateOf(false) }

    val prefs = remember { ctx.getSharedPreferences("osakafam_prefs", Context.MODE_PRIVATE) }
    var apiKey by remember { mutableStateOf(prefs.getString("openai_api_key", "") ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.size(30.dp).clip(RoundedCornerShape(8.dp)).background(C.Coral), contentAlignment = Alignment.Center) { Text("⛩️", fontSize = 13.sp) }
                            Text("OsakaFam", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = C.TextPrimary, letterSpacing = (-0.3).sp)
                        }
                        Box(Modifier.clip(RoundedCornerShape(20.dp)).border(1.5.dp, C.BorderSoft, RoundedCornerShape(20.dp)).background(C.WarmBg).padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text("🇯🇵 가족여행", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = C.TextMuted)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = C.WarmBg.copy(alpha = .92f))
            )
        },
        bottomBar = {
            NavigationBar(containerColor = C.CardBg, tonalElevation = 0.dp) {
                Tab.entries.forEach { t ->
                    val sel = tab == t
                    val color = if (sel) C.Coral else C.TextMuted
                    NavigationBarItem(
                        selected = sel,
                        onClick = {
                            if (tab != t) {
                                val showAd = when {
                                    // ★ 앱 시작 후 첫 탭 전환 시 1번 ★
                                    !startupAdShown && t != Tab.TRANSLATE -> {
                                        startupAdShown = true
                                        true
                                    }
                                    // ★ 설정 탭으로 이동할 때마다 ★
                                    t == Tab.SETTINGS -> true
                                    else -> false
                                }

                                if (showAd && activity != null) {
                                    AdManager.showAd(activity) { tab = t }
                                } else {
                                    tab = t
                                }
                            }
                        },
                        icon = { Icon(t.icon, t.label, tint = color, modifier = Modifier.size(if (t == Tab.SETTINGS) 20.dp else 24.dp)) },
                        label = { Text(t.label, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = color, textAlign = TextAlign.Center) },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = C.CoralLight)
                    )
                }
            }
        }
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(pad).background(C.WarmBg)) {
            when (tab) {
                Tab.HOME -> HomeScreen(exchangeRate = exchangeRate, onExchangeRateChanged = { exchangeRate = it })
                Tab.MAP -> MapScreen(exchangeRate)
                Tab.TRANSLATE -> TranslateScreen(apiKey)
                Tab.LANGUAGE -> LanguageScreen()
                Tab.SETTINGS -> SettingsScreen(apiKey = apiKey, onApiKeyChanged = { newKey -> apiKey = newKey; prefs.edit().putString("openai_api_key", newKey).apply() })
            }
        }
    }
}

// ═══════════════════════════════════
// 설정 화면
// ═══════════════════════════════════
@Composable
fun SettingsScreen(apiKey: String, onApiKeyChanged: (String) -> Unit) {
    var keyInput by remember { mutableStateOf(apiKey) }
    var showKey by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("⚙️ 설정", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = C.TextPrimary, letterSpacing = (-0.5).sp)

        CardSection("OpenAI API 키") {
            Text("AI 사진번역 기능을 사용하려면 본인의 OpenAI API 키가 필요합니다.", fontSize = 12.sp, color = C.TextSecondary, lineHeight = 18.sp)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = keyInput, onValueChange = { keyInput = it },
                modifier = Modifier.fillMaxWidth(), placeholder = { Text("sk-proj-...") }, singleLine = true,
                visualTransformation = if (showKey) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                trailingIcon = { IconButton(onClick = { showKey = !showKey }) { Icon(if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = C.TextMuted) } },
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = C.BorderSoft, focusedBorderColor = C.Coral, focusedContainerColor = C.WarmBg, unfocusedContainerColor = C.WarmBg)
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = { onApiKeyChanged(keyInput.trim()) }, modifier = Modifier.fillMaxWidth().height(44.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = C.Coral)) {
                Text("💾 API 키 저장", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            if (apiKey.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(C.GreenLight).padding(10.dp)) {
                    Text("✅ API 키가 저장되어 있습니다.", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = C.Green)
                }
            }
        }

        CardSection("API 키 발급 방법") {
            listOf("1️⃣ platform.openai.com 에 접속", "2️⃣ 회원가입 또는 로그인", "3️⃣ 좌측 메뉴 → API Keys", "4️⃣ 'Create new secret key' 클릭", "5️⃣ 생성된 키(sk-proj-...)를 복사", "6️⃣ 위 입력칸에 붙여넣기 후 저장").forEach { Text(it, fontSize = 12.sp, color = C.TextSecondary, fontWeight = FontWeight.Medium, lineHeight = 22.sp) }
            Spacer(Modifier.height(8.dp))
            Text("💡 OpenAI 크레딧을 미리 충전해야 사진번역이 작동합니다. 사진 1장당 약 \$0.01~0.03 정도 소요됩니다.", fontSize = 11.sp, color = C.TextMuted, lineHeight = 18.sp)
        }

        CardSection("앱 정보") {
            Text("OsakaFam v1.0", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = C.TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text("오사카 가족여행을 위한 올인원 가이드 앱", fontSize = 12.sp, color = C.TextSecondary)
            Spacer(Modifier.height(8.dp))
            Text("Made with ♥ by S&B-성윤-for-Family-in-Osaka", fontSize = 10.sp, color = C.TextMuted, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(60.dp))
    }
}
