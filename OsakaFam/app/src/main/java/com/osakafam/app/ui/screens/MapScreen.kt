package com.osakafam.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osakafam.app.popularSpots
import com.osakafam.app.stations
import com.osakafam.app.ui.theme.C
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.ui.graphics.Color

@Composable
fun MapScreen(exchangeRate: Int) {
    val ctx = LocalContext.current
    var hotelAddr by remember { mutableStateOf("") }
    var icBalance by remember { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("교통 & 길찾기", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = C.TextPrimary, letterSpacing = (-0.5).sp)

        // ── 인기 관광지 ──
        CardSection("인기 관광지 바로가기") {
            Text("터치하면 현위치에서 경로 안내", fontSize = 11.sp, color = C.TextMuted, modifier = Modifier.padding(bottom = 8.dp))
            val gridCols = 4
            val rows = popularSpots.chunked(gridCols)
            rows.forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    row.forEach { spot ->
                        Column(
                            Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(C.WarmBg)
                                .clickable { openNavTo(ctx, spot.dest) }.padding(12.dp, 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(spot.emoji, fontSize = 20.sp)
                            Text(spot.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = C.TextPrimary, maxLines = 1)
                        }
                    }
                    repeat(gridCols - row.size) { Spacer(Modifier.weight(1f)) }
                }
                Spacer(Modifier.height(4.dp))
            }
        }

        // ── 공식 노선도 ──
        CardSection("공식 노선도") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(13.dp)).background(C.SkyLight)
                        .clickable { openUrl(ctx, "https://subway.osakametro.co.jp/ko/guide/routemap.php") }.padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("🚇", fontSize = 18.sp)
                        Text("시내 지하철", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = C.TextPrimary)
                        Text("오사카 메트로", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = C.TextMuted)
                    }
                }
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(13.dp)).background(C.CoralLight)
                        .clickable { openUrl(ctx, "https://www.westjr.co.jp/global/kr/timetable/pdf/map_kansai.pdf") }.padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("🚆", fontSize = 18.sp)
                        Text("JR 간사이", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = C.TextPrimary)
                        Text("JR West PDF", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = C.TextMuted)
                    }
                }
            }
        }

        // ── 주요역 빠른 경로 ──
        CardSection("주요역 빠른 경로") {
            stations.forEach { st ->
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).border(1.5.dp, C.BorderSoft, RoundedCornerShape(12.dp))
                        .clickable { openNavTo(ctx, st.dest) }.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(Modifier.size(10.dp).clip(CircleShape).background(Color(st.colorHex)))
                    Text(st.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = C.TextPrimary, modifier = Modifier.weight(1f))
                    Text("➤", fontSize = 13.sp, color = C.TextMuted)
                }
                Spacer(Modifier.height(6.dp))
            }
        }

        // ── 숙소 & 경로 ──
        CardSection("숙소 & 경로 찾기") {
            OutlinedTextField(
                value = hotelAddr, onValueChange = { hotelAddr = it }, Modifier.fillMaxWidth(),
                placeholder = { Text("숙소 이름 또는 역 (예: 난바역)") }, singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = C.BorderSoft, focusedBorderColor = C.Coral, focusedContainerColor = C.WarmBg, unfocusedContainerColor = C.WarmBg)
            )
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { openMap(ctx, "Kansai International Airport", hotelAddr.ifBlank { "Namba Station" }) },
                    Modifier.weight(1f).height(44.dp), shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder,
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = C.WarmBg)
                ) { Text("✈️ 공항→숙소", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = C.TextPrimary) }
                Button(
                    onClick = { openMap(ctx, "", hotelAddr.ifBlank { "Namba Station" }) },
                    Modifier.weight(1f).height(44.dp), shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = C.Coral)
                ) { Text("🏨 현위치→숙소", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            }
            Spacer(Modifier.height(6.dp))
            OutlinedButton(
                onClick = { openNavTo(ctx, "Universal Studios Japan") },
                Modifier.fillMaxWidth().height(44.dp), shape = RoundedCornerShape(12.dp)
            ) { Text("🎢 현위치 → USJ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = C.TextPrimary) }
        }

        // ── IC카드 잔액 ──
        CardSection("IC카드 잔액 체크") {
            Text("ICOCA/Suica 잔액으로 이용 가능 횟수", fontSize = 11.sp, color = C.TextMuted, modifier = Modifier.padding(bottom = 6.dp))
            OutlinedTextField(
                value = icBalance, onValueChange = { icBalance = it }, Modifier.fillMaxWidth(),
                placeholder = { Text("IC카드 잔액 (엔)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = C.BorderSoft, focusedBorderColor = C.Coral, focusedContainerColor = C.WarmBg, unfocusedContainerColor = C.WarmBg)
            )
            val bal = icBalance.toLongOrNull() ?: 0
            if (bal > 0) {
                Spacer(Modifier.height(8.dp))

                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(C.SkyLight).padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("🚇 지하철 (~190¥)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = C.TextSecondary)
                        Text("${bal / 190}회", fontSize = 14.sp, fontWeight = FontWeight.Black, color = C.Sky)
                    }
                }
                Spacer(Modifier.height(6.dp))

                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(C.GreenLight).padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("🚌 버스 (~230¥)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = C.TextSecondary)
                        Text("${bal / 230}회", fontSize = 14.sp, fontWeight = FontWeight.Black, color = C.Green)
                    }
                }
                Spacer(Modifier.height(6.dp))

                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(C.AmberLight).padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("≈ 한국 돈", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = C.TextSecondary)
                        Text("${NumberFormat.getNumberInstance(Locale.KOREA).format(bal * exchangeRate / 100)}원", fontSize = 14.sp, fontWeight = FontWeight.Black, color = C.Amber)
                    }
                }
                Spacer(Modifier.height(6.dp))

                if (bal < 500) {
                    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(C.CoralLight).padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Text("⚠️ 잔액 부족! 역 충전기/편의점에서 충전하세요", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = C.Coral)
                    }
                }
            }
        }

        Spacer(Modifier.height(60.dp))
    }
}

private fun openNavTo(ctx: Context, dest: String) {
    val url = "https://www.google.com/maps/dir/?api=1&destination=${Uri.encode(dest)}&travelmode=transit"
    ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}
private fun openMap(ctx: Context, origin: String, dest: String) {
    val url = buildString {
        append("https://www.google.com/maps/dir/?api=1")
        if (origin.isNotBlank()) append("&origin=${Uri.encode(origin)}")
        append("&destination=${Uri.encode(dest)}&travelmode=transit")
    }
    ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}
