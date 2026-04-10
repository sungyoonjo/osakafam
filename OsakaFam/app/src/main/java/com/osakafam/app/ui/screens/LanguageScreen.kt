package com.osakafam.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osakafam.app.categories
import com.osakafam.app.phrases
import com.osakafam.app.ui.theme.C
import java.util.Locale

@Composable
fun LanguageScreen() {
    val ctx = LocalContext.current
    var activeCat by remember { mutableStateOf("전체") }
    var searchKw by remember { mutableStateOf("") }
    var expandedIdx by remember { mutableStateOf<Int?>(null) }
    var customPhrase by remember { mutableStateOf("") }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(Unit) {
        val t = TextToSpeech(ctx) { status ->
            if (status == TextToSpeech.SUCCESS) { tts?.language = Locale.JAPANESE; tts?.setSpeechRate(0.85f) }
        }
        tts = t
        onDispose { t.shutdown() }
    }

    val filtered = phrases.filter { p ->
        val catOk = activeCat == "전체" || p.cat == activeCat
        val kwOk = searchKw.isBlank() || p.ko.contains(searchKw) || p.cat.contains(searchKw) || p.jp.contains(searchKw) || p.pron.contains(searchKw)
        catOk && kwOk
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("생존 & 학습 회화", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = C.TextPrimary, letterSpacing = (-0.5).sp)

        // ── 카테고리 필터 칩 ──
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            categories.forEach { cat ->
                val isActive = activeCat == cat
                Box(
                    Modifier.clip(RoundedCornerShape(20.dp))
                        .border(1.5.dp, if (isActive) C.Coral else C.BorderSoft, RoundedCornerShape(20.dp))
                        .background(if (isActive) C.Coral else C.CardBg)
                        .clickable { activeCat = cat; expandedIdx = null }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(cat, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isActive) C.CardBg else C.TextSecondary)
                }
            }
        }

        // ── 검색 ──
        OutlinedTextField(
            value = searchKw, onValueChange = { searchKw = it }, Modifier.fillMaxWidth(),
            placeholder = { Text("표현 검색 (예: 표, 메뉴, 알레르기)") }, singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, null, tint = C.TextMuted) },
            trailingIcon = { if (searchKw.isNotBlank()) IconButton(onClick = { searchKw = "" }) { Icon(Icons.Default.Clear, null, tint = C.TextMuted) } },
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = C.BorderSoft, focusedBorderColor = C.Coral, focusedContainerColor = C.WarmBg, unfocusedContainerColor = C.WarmBg)
        )

        // ── 회화 카드 목록 ──
        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔍", fontSize = 22.sp, modifier = Modifier.padding(bottom = 8.dp))
                    Text("검색 결과가 없습니다", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = C.TextMuted)
                }
            }
        }

        filtered.forEachIndexed { idx, p ->
            val isExpanded = expandedIdx == idx
            val badgeColor = when {
                p.cat.contains("위급") -> C.Coral to C.CoralLight
                p.cat.contains("가족") -> C.Amber to C.AmberLight
                p.cat.contains("교통") -> C.Sky to C.SkyLight
                p.cat.contains("쇼핑") -> C.Green to C.GreenLight
                p.cat.contains("인사") -> C.Purple to C.PurpleLight
                else -> C.TextSecondary to C.BadgeNeutralBg
            }

            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .border(1.5.dp, if (isExpanded) C.CoralMid else C.BorderSoft, RoundedCornerShape(14.dp))
                    .background(C.CardBg).clickable { expandedIdx = if (isExpanded) null else idx }
            ) {
                Row(Modifier.padding(13.dp), verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(p.cat, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = badgeColor.first,
                            modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(badgeColor.second).padding(horizontal = 8.dp, vertical = 3.dp))
                        Spacer(Modifier.height(5.dp))
                        Text(p.ko, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = C.TextPrimary, lineHeight = 20.sp)
                        Spacer(Modifier.height(3.dp))
                        Text(p.jp, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = C.TextSecondary, lineHeight = 18.sp)
                        Spacer(Modifier.height(2.dp))
                        Text(p.pron, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = C.Coral)
                    }
                    Spacer(Modifier.width(10.dp))
                    Box(
                        Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(C.WarmBg).border(1.5.dp, C.BorderSoft, RoundedCornerShape(10.dp))
                            .clickable { tts?.speak(p.jp, TextToSpeech.QUEUE_FLUSH, null, "p$idx") },
                        contentAlignment = Alignment.Center
                    ) { Text("🔊", fontSize = 15.sp) }
                }

                AnimatedVisibility(isExpanded, enter = expandVertically(), exit = shrinkVertically()) {
                    Column(Modifier.background(C.WarmBg).padding(13.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (p.example.isNotBlank()) {
                            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(C.CardBg).border(1.5.dp, C.BorderSoft, RoundedCornerShape(10.dp)).padding(10.dp)) {
                                Text(p.example, fontSize = 11.sp, color = C.TextSecondary, fontWeight = FontWeight.Medium, lineHeight = 18.sp)
                            }
                        }
                        if (p.videoQuery.isNotBlank()) {
                            Button(
                                onClick = {
                                    val url = "https://www.youtube.com/results?search_query=${Uri.encode(p.videoQuery)}"
                                    ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                },
                                Modifier.fillMaxWidth().height(40.dp), shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = C.TextPrimary)
                            ) {
                                Text("▶️ ${p.videoTitle}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // ── 원하는 표현 바로 검색 ──
        CardSection("원하는 표현 바로 검색") {
            Text("한국어 입력 → 일본어 번역 바로 확인", fontSize = 11.sp, color = C.TextMuted, modifier = Modifier.padding(bottom = 6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = customPhrase, onValueChange = { customPhrase = it }, Modifier.weight(1f),
                    placeholder = { Text("예: 짐 맡길 수 있나요?", fontSize = 14.sp) }, singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = C.BorderSoft, focusedBorderColor = C.Coral, focusedContainerColor = C.WarmBg, unfocusedContainerColor = C.WarmBg)
                )
                Button(
                    onClick = {
                        if (customPhrase.isNotBlank()) {
                            val url = "https://translate.google.com/?sl=ko&tl=ja&text=${Uri.encode(customPhrase.trim())}&op=translate"
                            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }
                    },
                    modifier = Modifier.height(56.dp), shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = C.Coral)
                ) { Text("🔤", fontSize = 18.sp) }
            }
            Spacer(Modifier.height(6.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                listOf("짐 맡길 수 있나요?", "와이파이 비번", "몇 시에 닫나요?", "포장해주세요").forEach { q ->
                    Box(
                        Modifier.clip(RoundedCornerShape(16.dp)).border(1.5.dp, C.BorderSoft, RoundedCornerShape(16.dp))
                            .background(C.WarmBg)
                            .clickable {
                                customPhrase = q
                                val url = "https://translate.google.com/?sl=ko&tl=ja&text=${Uri.encode(q)}&op=translate"
                                ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) { Text(q, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = C.TextSecondary) }
                }
            }
        }

        Spacer(Modifier.height(60.dp))
    }
}
