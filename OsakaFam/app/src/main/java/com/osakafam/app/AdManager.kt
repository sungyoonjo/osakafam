package com.osakafam.app

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdManager {
    private const val TAG = "AdManager"

    // ★ 실제 광고 Unit ID
    private const val REAL_AD_UNIT_ID = "ca-app-pub-3961794568580148/1221192956"
    // ★ 테스트 광고 Unit ID (Google 공식)
    private const val TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    // ★★★ true = 테스트광고, false = 실제광고 ★★★
    // Play Store 출시 전에 반드시 false로!
    private const val USE_TEST_AD = false

    private val adUnitId: String
        get() = if (USE_TEST_AD) TEST_AD_UNIT_ID else REAL_AD_UNIT_ID

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false

    fun loadAd(context: Context) {
        if (interstitialAd != null || isLoading) return
        isLoading = true
        Log.d(TAG, "🔄 광고 로드 시작... (unitId: $adUnitId)")

        InterstitialAd.load(
            context, adUnitId, AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "✅ 광고 로드 성공!")
                    interstitialAd = ad
                    isLoading = false
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "❌ 광고 로드 실패: code=${error.code}, msg=${error.message}")
                    interstitialAd = null
                    isLoading = false
                }
            }
        )
    }

    fun showAd(activity: Activity, onAdDismissed: () -> Unit = {}) {
        val ad = interstitialAd
        if (ad == null) {
            Log.d(TAG, "⚠️ 광고 준비 안 됨 → 바로 진행")
            Toast.makeText(activity, "광고 로딩 중...", Toast.LENGTH_SHORT).show()
            onAdDismissed()
            loadAd(activity)
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "✅ 광고 닫힘")
                interstitialAd = null
                onAdDismissed()
                loadAd(activity)
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.e(TAG, "❌ 광고 표시 실패: ${error.message}")
                interstitialAd = null
                onAdDismissed()
                loadAd(activity)
            }
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "📺 전면 광고 표시됨!")
            }
        }
        ad.show(activity)
    }

    fun isAdReady(): Boolean = interstitialAd != null
}
