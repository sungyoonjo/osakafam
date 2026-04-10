package com.osakafam.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osakafam.app.network.ApiService
import com.osakafam.app.ui.theme.C
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun TranslateScreen(apiKey: String) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var isTranslating by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("") }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    // ★ 공통: URI → 안전하게 번역 ★
    val doTranslate: (Uri) -> Unit = { uri ->
        scope.launch {
            isTranslating = true
            result = ""
            try {
                val base64: String? = withContext(Dispatchers.IO) {
                      ApiService.uriToBase64(ctx, uri)
                }
                if (base64 == null) {
                    result = "⚠️ 이미지를 처리할 수 없습니다.\n다른 사진으로 시도해주세요."
                } else {
                    ApiService.translateImage(apiKey, base64).fold(
                        onSuccess = { result = it },
                        onFailure = { result = "⚠️ 번역 실패\n\n${it.message}" }
                    )
                }
            } catch (_: OutOfMemoryError) {
                result = "⚠️ 이미지가 너무 큽니다.\n다른 사진으로 시도해주세요."
            } catch (e: Exception) {
                result = "⚠️ 오류 발생\n\n${e.message}"
            }
            isTranslating = false
        }
    }

    // ── 갤러리: Photo Picker ──
    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) doTranslate(uri)
    }

    // ── 갤러리: fallback 파일 선택기 ──
    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) doTranslate(uri)
    }

    // ── 카메라: TakePicture (FileProvider로 파일 저장) ──
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && cameraUri != null) {
            doTranslate(cameraUri!!)
        }
    }

    // ── 카메라 권한 요청 ──
    val cameraPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        if (granted) {
            try {
                val file = File.createTempFile("osaka_cam_", ".jpg", ctx.cacheDir)
                val uri = androidx.core.content.FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)
                cameraUri = uri
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                result = "⚠️ 카메라 실행 실패: ${e.message}"
            }
        } else {
            Toast.makeText(ctx, "카메라 권한이 필요합니다", Toast.LENGTH_SHORT).show()
        }
    }

    // 카메라 실행 함수
    fun launchCamera() {
        val hasPerm = androidx.core.content.ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (hasPerm) {
            try {
                val file = File.createTempFile("osaka_cam_", ".jpg", ctx.cacheDir)
                val uri = androidx.core.content.FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)
                cameraUri = uri
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                result = "⚠️ 카메라 실행 실패: ${e.message}"
            }
        } else {
            cameraPermLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // ══════════════════════════════
    // UI
    // ══════════════════════════════
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("실전 번역 도구", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = C.TextPrimary, letterSpacing = (-0.5).sp)

        // API 키 미설정 경고
        if (apiKey.isBlank()) {
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(C.AmberLight).border(1.5.dp, C.Amber, RoundedCornerShape(12.dp)).padding(12.dp)) {
                Text("⚠️ AI 사진번역을 사용하려면 ⚙️설정 탭에서 OpenAI API 키를 입력해주세요.", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = C.Amber)
            }
        }

        // ── 카메라 촬영 버튼 ──
        Button(
            onClick = { launchCamera() },
            enabled = !isTranslating,
            modifier = Modifier.fillMaxWidth().height(100.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = C.CardBg),
            border = ButtonDefaults.outlinedButtonBorder
        ) {
            if (isTranslating) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularProgressIndicator(color = C.Coral, modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
                    Text("AI 분석 중...", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = C.TextSecondary)
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("📷", fontSize = 28.sp)
                    Text("메뉴판 · 간판 촬영", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = C.TextPrimary)
                    Text("사진을 찍으면 AI가 번역", fontSize = 11.sp, color = C.TextMuted)
                }
            }
        }

        // ── 갤러리에서 사진 선택 ──
        OutlinedButton(
            onClick = {
                try {
                    photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                } catch (_: Exception) {
                    try { filePicker.launch(arrayOf("image/*")) } catch (_: Exception) { result = "⚠️ 사진 선택기를 열 수 없습니다." }
                }
            },
            enabled = !isTranslating,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("🖼️ 갤러리에서 사진 선택", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = C.TextPrimary)
        }

        // ── 번역 결과 ──
        AnimatedVisibility(visible = result.isNotBlank()) {
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(C.CoralLight, C.WarmBg)))
                    .border(1.5.dp, C.CoralMid, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text("번역 결과", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = C.Coral,
                        modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(C.CoralLight).padding(horizontal = 8.dp, vertical = 3.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(result, fontSize = 13.sp, lineHeight = 22.sp, color = C.TextPrimary, fontWeight = FontWeight.Medium)
                }
            }
        }

        // ── 외부 번역 앱 ──
        CardSection("외부 번역 앱") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { openUrl(ctx, "https://papago.naver.com/") }, modifier = Modifier.weight(1f).height(44.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(containerColor = C.GreenLight)) {
                    Text("🦜 파파고", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = C.Green)
                }
                OutlinedButton(onClick = { openUrl(ctx, "https://translate.google.com/?hl=ko&sl=ja&tl=ko&op=images") }, modifier = Modifier.weight(1f).height(44.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(containerColor = C.SkyLight)) {
                    Text("🌐 구글 번역", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = C.Sky)
                }
            }
        }

        Spacer(Modifier.height(60.dp))
    }
}
