package com.osakafam.app.network

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

data class ChatResponse(val choices: List<Choice>)
data class Choice(val message: Msg)
data class Msg(val content: String)
data class WeatherResult(val currentTemp: Double, val minTemp: Double, val maxTemp: Double, val weatherCode: Int)

object ApiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()

    /**
     * URI에서 이미지를 안전하게 읽어서 Base64로 변환
     * - BitmapFactory.Options.inSampleSize로 다운샘플링 (OOM 방지)
     * - 최대 1000px로 리사이즈
     * - JPEG 80% 품질로 압축
     */
    suspend fun uriToBase64(context: Context, uri: Uri, maxDim: Int = 1000): String? = withContext(Dispatchers.IO) {
        try {
            // 1단계: 이미지 크기만 먼저 확인 (메모리 사용 없음)
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            val origW = options.outWidth
            val origH = options.outHeight
            if (origW <= 0 || origH <= 0) return@withContext null

            // 2단계: inSampleSize 계산 (2의 거듭제곱으로 다운샘플링)
            var sampleSize = 1
            while (origW / sampleSize > maxDim * 2 || origH / sampleSize > maxDim * 2) {
                sampleSize *= 2
            }

            // 3단계: 다운샘플링해서 디코딩 (메모리 안전)
            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val bitmap = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, decodeOptions)
            } ?: return@withContext null

            // 4단계: 최대 크기로 리사이즈
            var w = bitmap.width
            var h = bitmap.height
            if (w > h) {
                if (w > maxDim) { h = (h.toFloat() * maxDim / w).toInt(); w = maxDim }
            } else {
                if (h > maxDim) { w = (w.toFloat() * maxDim / h).toInt(); h = maxDim }
            }

            val scaled = if (w != bitmap.width || h != bitmap.height) {
                Bitmap.createScaledBitmap(bitmap, w, h, true).also {
                    if (it != bitmap) bitmap.recycle()
                }
            } else bitmap

            // 5단계: JPEG로 압축 후 Base64 인코딩
            val out = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, 80, out)
            scaled.recycle()

            Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
        } catch (e: OutOfMemoryError) {
            null // OOM 발생 시 null 반환
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 비트맵을 안전하게 Base64로 변환 (카메라 썸네일용)
     */
    fun bitmapToBase64(bitmap: Bitmap, maxDim: Int = 1000, quality: Int = 80): String? {
        return try {
            var w = bitmap.width
            var h = bitmap.height
            if (w > h) {
                if (w > maxDim) { h = (h.toFloat() * maxDim / w).toInt(); w = maxDim }
            } else {
                if (h > maxDim) { w = (w.toFloat() * maxDim / h).toInt(); h = maxDim }
            }
            val scaled = Bitmap.createScaledBitmap(bitmap, w, h, true)
            val out = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
            if (scaled != bitmap) scaled.recycle()
            Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }

    // 이전 호환용 (사용하지 않아도 됨)
    fun compressToBase64(bytes: ByteArray, maxDim: Int = 1000, quality: Int = 80): String {
        val orig = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: return Base64.encodeToString(bytes, Base64.NO_WRAP)
        var w = orig.width; var h = orig.height
        if (w > h) { if (w > maxDim) { h = (h.toFloat() * maxDim / w).toInt(); w = maxDim } }
        else { if (h > maxDim) { w = (w.toFloat() * maxDim / h).toInt(); h = maxDim } }
        val scaled = Bitmap.createScaledBitmap(orig, w, h, true)
        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
        if (scaled != orig) scaled.recycle()
        orig.recycle()
        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    }

    suspend fun translateImage(apiKey: String, base64: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isBlank()) {
                return@withContext Result.failure(Exception("API 키가 설정되지 않았습니다.\n⚙️ 설정 탭에서 OpenAI API 키를 입력해주세요."))
            }
            val body = """
            {"model":"gpt-4o","messages":[{"role":"user","content":[
                {"type":"text","text":"이 사진에 있는 일본어 텍스트를 파악하고, 한국인 여행자가 이해하기 쉽게 한국어로 번역해서 핵심만 요약해 줘."},
                {"type":"image_url","image_url":{"url":"data:image/jpeg;base64,$base64"}}
            ]}],"max_tokens":400}""".trimIndent()
            val req = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $apiKey")
                .post(body.toRequestBody("application/json".toMediaType()))
                .build()
            val resp = client.newCall(req).execute()
            val respBody = resp.body?.string() ?: ""
            if (!resp.isSuccessful) return@withContext Result.failure(Exception("OpenAI 에러(${resp.code}): $respBody"))
            val parsed = gson.fromJson(respBody, ChatResponse::class.java)
            Result.success(parsed.choices.firstOrNull()?.message?.content ?: "결과 없음")
        } catch (e: Exception) { Result.failure(Exception("통신 오류: ${e.message}")) }
    }

    suspend fun fetchWeather(): WeatherResult? = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder()
                .url("https://api.open-meteo.com/v1/forecast?latitude=34.6937&longitude=135.5022&current_weather=true&daily=temperature_2m_max,temperature_2m_min&timezone=Asia%2FTokyo")
                .build()
            val resp = client.newCall(req).execute()
            val json = gson.fromJson(resp.body?.string(), JsonObject::class.java)
            val cur = json.getAsJsonObject("current_weather").get("temperature").asDouble
            val daily = json.getAsJsonObject("daily")
            val max = daily.getAsJsonArray("temperature_2m_max").get(0).asDouble
            val min = daily.getAsJsonArray("temperature_2m_min").get(0).asDouble
            val code = json.getAsJsonObject("current_weather").get("weathercode").asInt
            WeatherResult(cur, min, max, code)
        } catch (e: Exception) { null }
    }

    suspend fun fetchExchangeRate(): Int? = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder().url("https://open.er-api.com/v6/latest/JPY").build()
            val resp = client.newCall(req).execute()
            val json = gson.fromJson(resp.body?.string(), JsonObject::class.java)
            val krw = json.getAsJsonObject("rates").get("KRW").asDouble
            Math.round(krw * 100).toInt()
        } catch (e: Exception) { android.util.Log.e("WEATHER", "Error: ${e.message}"); null }
    }
}
