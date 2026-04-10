package com.osakafam.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.offset
//import androidx.compose.ui.unit.IntOffset
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osakafam.app.network.ApiService
import com.osakafam.app.network.WeatherResult
import com.osakafam.app.ui.theme.C
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.floor

@Composable
fun HomeScreen(exchangeRate: Int, onExchangeRateChanged: (Int) -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var weather by remember { mutableStateOf<WeatherResult?>(null) }
    var isLiveRate by remember { mutableStateOf(false) }
    var jpyAmount by remember { mutableStateOf("") }
    var taxPrice by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        scope.launch { weather = ApiService.fetchWeather() }
        scope.launch {
            ApiService.fetchExchangeRate()?.let {
                onExchangeRateChanged(it)
                isLiveRate = true
            }
        }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── 날씨 카드 ──
        weather?.let { w ->
            val info = weatherInfo(w.maxTemp, w.minTemp, w.weatherCode)
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF4a90d9), Color(0xFF74c1e8))))
                    .clickable { openUrl(ctx, "https://search.naver.com/search.naver?query=오사카+날씨") }
                    .padding(18.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("오사카 현재 날씨 (code: ${w.weatherCode})", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = .8f))
                        Spacer(Modifier.height(5.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("${w.currentTemp.toInt()}°", fontSize = 42.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = (-2).sp)
                            Spacer(Modifier.width(10.dp))
                            Text("최고 ${w.maxTemp.toInt()}° / 최저 ${w.minTemp.toInt()}°", fontSize = 12.sp, color = Color.White.copy(alpha = .85f), fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 6.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                        Box(Modifier.clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = .18f)).padding(horizontal = 11.dp, vertical = 7.dp)) {
                            Text("${info.first}  ${info.second}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                    }
                    // ★ 날씨 애니메이션 ★
                    Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                        val infiniteTransition = rememberInfiniteTransition(label = "weather")
                        val offsetY by infiniteTransition.animateFloat(
                            initialValue = -6f, targetValue = 6f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(2000, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ), label = "bounce"
                        )
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 0.25f, targetValue = 0.5f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(3000),
                                repeatMode = RepeatMode.Reverse
                            ), label = "glow"
                        )
                        Text(
                            text = info.first,
                            fontSize = 64.sp,
                            modifier = Modifier.offset(y = offsetY.dp),
                            color = Color.White.copy(alpha = alpha)
                        )
                    }
                }
            }
        } ?: Box(
            Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(18.dp)).background(Color(0xFFe8f0fa)),
            contentAlignment = Alignment.Center
        ) { Text("날씨 불러오는 중...", color = C.TextMuted, fontWeight = FontWeight.Bold, fontSize = 13.sp) }

        // ── 긴급 시설 ──
        CardSection("내 주변 긴급 시설", badgeText = "GPS") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f).clip(RoundedCornerShape(13.dp)).background(C.SkyLight).clickable { searchNearby(ctx, "トイレ") }.padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) { Text("🚻", fontSize = 20.sp); Text("화장실", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = C.TextPrimary) }
                }
                Box(Modifier.weight(1f).clip(RoundedCornerShape(13.dp)).background(C.GreenLight).clickable { searchNearby(ctx, "薬局") }.padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) { Text("💊", fontSize = 20.sp); Text("약국", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = C.TextPrimary) }
                }
                Box(Modifier.weight(1f).clip(RoundedCornerShape(13.dp)).background(C.AmberLight).clickable { searchNearby(ctx, "近くの美味しいレストラン") }.padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) { Text("🍽️", fontSize = 20.sp); Text("음식점", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = C.TextPrimary) }
                }
            }
        }

        // ══════════════════════════════════════════
        // ── 환율 계산기 (환율 표시를 카드 본문에 크게) ──
        // ══════════════════════════════════════════
        CardSection("실시간 환율 계산기") {
            // ★★★ 환율 표시 - 큰 글자로 확실하게 ★★★
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFF8F0))
                    .border(1.5.dp, C.CoralMid, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "100¥ = ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = C.TextSecondary
                    )
                    Text(
                        text = exchangeRate.toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = C.Coral
                    )
                    Text(
                        text = " 원",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = C.TextSecondary
                    )
                    if (isLiveRate) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            Modifier.clip(RoundedCornerShape(8.dp))
                                .background(C.GreenLight)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("● 실시간", fontSize = 9.sp, color = C.Green, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // 엔화 입력
            OutlinedTextField(
                value = jpyAmount, onValueChange = { jpyAmount = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("일본 엔 금액 입력") },
                textStyle = LocalTextStyle.current.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = C.BorderSoft, focusedBorderColor = C.Coral, focusedContainerColor = C.WarmBg, unfocusedContainerColor = C.WarmBg)
            )

            // 환산 결과
            val jpy = jpyAmount.toLongOrNull() ?: 0
            if (jpy > 0) {
                val convKRW = floor(jpy.toDouble() * exchangeRate / 100.0).toLong()
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(C.SkyLight).padding(11.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("≈ 한국 돈", fontSize = 12.sp, color = C.TextSecondary, fontWeight = FontWeight.SemiBold)
                        Text("${NumberFormat.getNumberInstance(Locale.KOREA).format(convKRW)}원", fontSize = 20.sp, fontWeight = FontWeight.Black, color = C.Sky)
                    }
                }
            }
        }

        // ── 오사카 가이드 스크롤 ──
        Text("오사카 필수 가이드", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = C.TextPrimary)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            data class Guide(val emoji: String, val title: String, val sub: String, val url: String)
            listOf(
                Guide("🎢", "USJ 가족여행", "완벽 공략법", "https://www.youtube.com/results?search_query=오사카+USJ+가족여행+공략"),
                Guide("🚇", "교통패스", "완벽 총정리", "https://search.naver.com/search.naver?where=view&query=오사카+교통패스+총정리+비교"),
                Guide("♨️", "온천/료칸", "베스트 추천", "https://search.naver.com/search.naver?where=view&query=오사카+가족+온천+료칸+추천"),
                Guide("🍜", "현지인 찐맛집", "실패 없는 리스트", "https://www.youtube.com/results?search_query=오사카+현지인+찐맛집+가족"),
            ).forEach { g ->
                Box(
                    Modifier.width(128.dp).clip(RoundedCornerShape(14.dp)).border(1.5.dp, C.BorderSoft, RoundedCornerShape(14.dp))
                        .background(C.CardBg).clickable { openUrl(ctx, g.url) }.padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(g.emoji, fontSize = 22.sp)
                        Text(g.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = C.TextPrimary)
                        Text(g.sub, fontSize = 10.sp, color = C.TextMuted)
                    }
                }
            }
        }

        // ── 면세 확인기 ──
        CardSection("면세 확인기") {
            OutlinedTextField(
                value = taxPrice, onValueChange = { taxPrice = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("장바구니 총액 (엔)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = C.BorderSoft, focusedBorderColor = C.Coral, focusedContainerColor = C.WarmBg, unfocusedContainerColor = C.WarmBg)
            )
            val tax = taxPrice.toDoubleOrNull() ?: 0.0
            if (tax > 0) {
                val ok = tax >= 5500
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(if (ok) C.GreenLight else C.CoralLight).padding(11.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(if (ok) "✅" else "❌", fontSize = 20.sp)
                        Column {
                            if (ok) {
                                val refund = floor(tax - tax / 1.1).toLong()
                                Text("면세 가능!", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = C.Green)
                                Text("약 ${NumberFormat.getNumberInstance(Locale.KOREA).format(refund)}엔 환급", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = C.TextSecondary)
                            } else {
                                val remain = (5500 - tax).toLong()
                                Text("면세 불가", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = C.Coral)
                                Text("${NumberFormat.getNumberInstance(Locale.KOREA).format(remain)}엔 더 담으면 면세!", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = C.TextSecondary)
                            }
                        }
                    }
                }
            }
        }

        // ── 제작자 서명 ──
        Text(
            "Made with ♥ by S&B-성윤-for-Family-in-Osaka",
            Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 9.sp,
            color = C.TextMuted,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(60.dp))
    }
}

@Composable
fun CardSection(
    title: String, badgeText: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).border(1.5.dp, C.BorderSoft, RoundedCornerShape(16.dp))
            .background(C.CardBg).padding(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.width(3.dp).height(12.dp).clip(RoundedCornerShape(2.dp)).background(C.Coral))
                Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = C.TextMuted, letterSpacing = 0.08.sp)
                badgeText?.let {
                    Text(it, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = C.Coral,
                        modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(C.CoralLight).padding(horizontal = 8.dp, vertical = 2.dp))
                }
            }
            trailing?.invoke()
        }
        Spacer(Modifier.height(10.dp))
        content()
    }
}

private fun weatherInfo(max: Double, min: Double, weatherCode: Int = -1): Pair<String, String> {
    val gap = max - min
    val month = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1

    // 날씨 코드 기반 (비, 눈, 안개 등)
    var emoji = when (weatherCode) {
        in 61..67, in 80..82 -> "🌧️"  // 비
        in 71..77, in 85..86 -> "🌨️"  // 눈
        in 95..99 -> "⛈️"             // 뇌우
        in 45..48 -> "🌫️"             // 안개
        in 51..57 -> "🌦️"             // 이슬비
        3 -> "☁️"                      // 흐림
        2 -> "⛅"                       // 구름 많음
        1 -> "🌤️"                     // 약간 흐림
        0 -> "☀️"                      // 맑음
        else -> ""                     // 코드 없으면 온도 기반
    }

    var text = when (weatherCode) {
        in 61..67, in 80..82 -> "비가 와요. 우산 필수!"
        in 71..77, in 85..86 -> "눈이 와요. 미끄럼 주의!"
        in 95..99 -> "뇌우 주의! 외출 자제"
        in 45..48 -> "안개가 짙어요. 시야 주의"
        in 51..57 -> "이슬비가 내려요. 우산 챙기세요"
        3 -> "흐린 날씨. 겉옷 챙기세요"
        else -> ""
    }

    // 날씨 코드가 없거나 맑음/흐림이면 온도 기반 추가
    if (text.isEmpty()) {
        when {
            max >= 33 -> { text = "폭염 주의! 반팔+선크림 필수"; if (emoji.isEmpty()) emoji = "🌞" }
            max >= 28 -> { text = "한여름 더위. 반팔+얇은 겉옷"; if (emoji.isEmpty()) emoji = "☀️" }
            max >= 24 -> { text = "초여름 선선함. 반팔+가디건"; if (emoji.isEmpty()) emoji = "⛅" }
            max >= 19 -> { text = "봄/가을 날씨. 여러 겹으로"; if (emoji.isEmpty()) emoji = if (month in 3..5) "🌸" else "🍂" }
            max >= 15 -> { text = "쌀쌀해요. 니트/자켓 필수"; if (emoji.isEmpty()) emoji = if (month in 3..5) "🌷" else "🍁" }
            max >= 10 -> { text = "꽤 춥습니다. 두꺼운 외투 필수"; if (emoji.isEmpty()) emoji = "🧥" }
            max >= 5 -> { text = "매우 추움! 패딩+목도리 필수"; if (emoji.isEmpty()) emoji = "⛄" }
            else -> { text = "극한 추위! 완전 방한 필수"; if (emoji.isEmpty()) emoji = "❄️" }
        }
    }

    if (gap >= 10) text += " · 일교차 큼!"
    return Pair(emoji, text)
}

fun openUrl(ctx: Context, url: String) = ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
fun searchNearby(ctx: Context, keyword: String) {
    val url = "https://www.google.com/maps/search/?api=1&query=${Uri.encode(keyword)}"
    ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}