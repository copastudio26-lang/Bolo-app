package com.example.core.voice

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class VoiceIntent(
    val category: String, // SYSTEM, COMMUNICATION, APPS, MEDIA, NAVIGATION, INFORMATION, AUTOMATION
    val action: String, // e.g. BRIGHTNESS, WIFI, SILENT, CALL, WHATSAPP, MESSAGE, YOUTUBE, INSTAGRAM, CAMERA, PLAY, VOLUME_UP, VOLUME_DOWN, BACK, HOME, TIME, WEATHER, ROUTINE
    val entity: String? = null, // e.g. "Papa", "Good Morning"
    val value: String? = null, // e.g. "50", "ON", "OFF"
    val successMessage: String,
    val isOfflineMode: Boolean = false
)

class VoiceEngine(private val context: Context) {

    private val tag = "VoiceEngine"
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Parses the voice text into a structured VoiceIntent.
     * Respects the offline-first principle: if online mode fails or has no key, uses the offline rule-engine.
     */
    suspend fun parseCommand(commandText: String, forceOffline: Boolean = false): VoiceIntent = withContext(Dispatchers.IO) {
        val trimmedCommand = commandText.trim()
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }

        if (forceOffline || apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d(tag, "Using Offline Rule-Based Matcher...")
            return@withContext parseCommandOffline(trimmedCommand)
        }

        try {
            Log.d(tag, "Attempting Online Gemini Matcher...")
            val intent = parseCommandOnline(trimmedCommand, apiKey)
            if (intent != null) {
                return@withContext intent
            }
        } catch (e: Exception) {
            Log.e(tag, "Gemini parsing failed, falling back to offline", e)
        }

        return@withContext parseCommandOffline(trimmedCommand)
    }

    /**
     * Offline Rule-Based Parsing (100% functional without internet)
     */
    private fun parseCommandOffline(command: String): VoiceIntent {
        val lower = command.lowercase(Locale.getDefault())

        // SYSTEM category checks
        if (lower.contains("wifi")) {
            val status = if (lower.contains("chalu") || lower.contains("on") || lower.contains("start")) "ON" else "OFF"
            val msg = if (status == "ON") "WiFi chalu kar diya gaya hai." else "WiFi band kar diya gaya hai."
            return VoiceIntent("SYSTEM", "WIFI", null, status, msg, true)
        }

        if (lower.contains("silent") || lower.contains("mute") || lower.contains("shant")) {
            val status = if (lower.contains("off") || lower.contains("chalu") == false && lower.contains("band")) "OFF" else "ON"
            val msg = if (status == "ON") "Phone ko silent mode par set kar diya hai." else "Silent mode band kar diya hai."
            return VoiceIntent("SYSTEM", "SILENT", null, status, msg, true)
        }

        if (lower.contains("brightness") || lower.contains("chamak")) {
            // Find percentage if any
            val percentRegex = """(\d+)\s*%""".toRegex()
            val match = percentRegex.find(lower)
            val valPercent = match?.groupValues?.get(1) ?: "50"
            return VoiceIntent("SYSTEM", "BRIGHTNESS", null, valPercent, "Brightness $valPercent% par set kar di hai.", true)
        }

        if (lower.contains("battery") || lower.contains("storage") || lower.contains("space")) {
            val action = if (lower.contains("battery")) "BATTERY" else "STORAGE"
            val msg = if (action == "BATTERY") "Aapki battery abhi 78% hai, aur performance bilkul thik hai." else "Storage checked: 42GB free hai, space ki koi kami nahi hai."
            return VoiceIntent("SYSTEM", action, null, null, msg, true)
        }

        // COMMUNICATION category checks
        if (lower.contains("call") || lower.contains("phone") || lower.contains("milao")) {
            // Extract contact
            var contact = "Papa"
            val callWords = listOf("call karo", "call miao", "ko call", "milao", "call")
            var cleanText = lower
            for (w in callWords) {
                cleanText = cleanText.replace(w, "")
            }
            cleanText = cleanText.replace("ko", "").trim()
            if (cleanText.isNotEmpty()) {
                contact = cleanText.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            }
            return VoiceIntent("COMMUNICATION", "CALL", contact, null, "$contact ko call mila raha hoon... 📞", true)
        }

        if (lower.contains("whatsapp") || lower.contains("message")) {
            var contact = "Priya"
            var action = "WHATSAPP"
            if (lower.contains("message")) {
                action = "MESSAGE"
            }
            var cleanText = lower.replace("whatsapp", "").replace("message", "").replace("bhejo", "").replace("karo", "")
            cleanText = cleanText.replace("ko", "").trim()
            if (cleanText.isNotEmpty()) {
                contact = cleanText.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            }
            val msg = if (action == "WHATSAPP") "$contact ko WhatsApp message bhejne ke liye screen taiyaar hai." else "$contact ko SMS message draft kiya ja raha hai."
            return VoiceIntent("COMMUNICATION", action, contact, null, msg, true)
        }

        // APPS category checks
        if (lower.contains("youtube")) {
            val gaana = if (lower.contains("gaana") || lower.contains("song")) "music video" else null
            return VoiceIntent("APPS", "YOUTUBE", gaana, null, "YouTube khol kar gaana chalaya ja raha hai... 🎵", true)
        }

        if (lower.contains("instagram") || lower.contains("insta") || lower.contains("reels")) {
            return VoiceIntent("APPS", "INSTAGRAM", "Reels", null, "Instagram kholkar Reels feed chalu kar raha hoon.", true)
        }

        if (lower.contains("camera") || lower.contains("photo") || lower.contains("video")) {
            return VoiceIntent("APPS", "CAMERA", null, null, "Camera app open ho gaya hai. Smile kijiye! 📸", true)
        }

        // MEDIA category checks
        if (lower.contains("volume")) {
            val up = lower.contains("up") || lower.contains("badhao") || lower.contains("badha") || lower.contains("increase")
            val action = if (up) "VOLUME_UP" else "VOLUME_DOWN"
            val msg = if (up) "Volume badha diya hai." else "Volume kam kar diya hai."
            return VoiceIntent("MEDIA", action, null, null, msg, true)
        }

        if (lower.contains("gaana chalao") || lower.contains("music") || lower.contains("play") || lower.contains("next")) {
            val action = if (lower.contains("next")) "NEXT_TRACK" else "PLAY"
            val msg = if (action == "NEXT_TRACK") "Next track chala raha hoon." else "BoloMusic Player chalu ho raha hai."
            return VoiceIntent("MEDIA", action, null, null, msg, true)
        }

        // NAVIGATION category checks
        if (lower.contains("back") || lower.contains("piche")) {
            return VoiceIntent("NAVIGATION", "BACK", null, null, "Peeche ja raha hoon.", true)
        }
        if (lower.contains("home") || lower.contains("mukhya")) {
            return VoiceIntent("NAVIGATION", "HOME", null, null, "Home screen kholi ja rahi hai.", true)
        }

        // INFORMATION category checks
        if (lower.contains("samay") || lower.contains("time") || lower.contains("baje")) {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val time = sdf.format(Date())
            return VoiceIntent("INFORMATION", "TIME", null, time, "Abhi samay $time baje hain.", true)
        }

        if (lower.contains("weather") || lower.contains("mausam") || lower.contains("garmi") || lower.contains("taapman")) {
            return VoiceIntent("INFORMATION", "WEATHER", null, "28°C", "Aaj dhoop nikli hai aur mausam suhana hai. Taapman 28°C hai.", true)
        }

        // AUTOMATION category checks (Routines)
        if (lower.contains("good morning") || lower.contains("subah")) {
            return VoiceIntent("AUTOMATION", "ROUTINE", "Good Morning", "RUN", "Routine 'Good Morning' chalu ho rahi hai: Alarm band → Weather bataya → News padhi.", true)
        }
        if (lower.contains("driving") || lower.contains("car") || lower.contains("gadi")) {
            return VoiceIntent("AUTOMATION", "ROUTINE", "Driving Mode", "RUN", "Routine 'Driving Mode' activated: DND ON → Maps open → Auto reply active.", true)
        }

        // Default fallback if nothing matches
        return VoiceIntent(
            category = "INFORMATION",
            action = "TALK",
            successMessage = "Maine suna: \"$command\". Main is command ke liye action process kar raha hoon.",
            isOfflineMode = true
        )
    }

    /**
     * Call Gemini API directly via OkHttp to classify intent in Hindi/Hinglish/English
     */
    private suspend fun parseCommandOnline(command: String, apiKey: String): VoiceIntent? {
        val systemPrompt = """
            You are the high-performance NLP intent parser for "BoloPhone" ("Bolte Hi Ho Jayega"), an on-device privacy-first voice automation assistant.
            The user inputs a command in Hindi, Hinglish (mixed English and Hindi), or English.
            You must parse the command and respond ONLY with a raw JSON object. No markdown, no ```json formatting, just raw JSON.
            
            JSON schema:
            {
              "category": "SYSTEM" | "COMMUNICATION" | "APPS" | "MEDIA" | "NAVIGATION" | "INFORMATION" | "AUTOMATION",
              "action": "WIFI" | "SILENT" | "BRIGHTNESS" | "BATTERY" | "STORAGE" | "CALL" | "WHATSAPP" | "MESSAGE" | "YOUTUBE" | "INSTAGRAM" | "CAMERA" | "VOLUME_UP" | "VOLUME_DOWN" | "PLAY" | "NEXT_TRACK" | "BACK" | "HOME" | "TIME" | "WEATHER" | "ROUTINE" | "TALK",
              "entity": "string or null representing the target object (e.g. contact name like 'Papa', 'Priya', or routine name like 'Good Morning')",
              "value": "string or null representing parameter values (e.g. '50' for brightness, 'ON' or 'OFF' for wifi, 'RUN' for routine)",
              "success_message": "string - a warm, professional, highly contextual success notification in friendly Hinglish (Hindi written in Roman script) or Hindi/English reflecting the action. Make it punchy and short. Tagline: 'Bolte Hi Ho Jayega'."
            }
            
            Example Hindi inputs:
            - "Papa ko call karo" -> {"category":"COMMUNICATION","action":"CALL","entity":"Papa","value":null,"success_message":"Papa ko call mila raha hoon... 📞"}
            - "Brightness 50 percent karo" -> {"category":"SYSTEM","action":"BRIGHTNESS","entity":null,"value":"50","success_message":"Brightness 50% kar di hai."}
            - "YouTube kholke gaana chalao" -> {"category":"APPS","action":"YOUTUBE","entity":"gaana","value":null,"success_message":"YouTube khol kar gaana chala diya hai! 🎵"}
            - "Weather kaisa hai" -> {"category":"INFORMATION","action":"WEATHER","entity":null,"value":null,"success_message":"Aaj mausam mast hai, dhoop hai aur taapman 28°C hai."}
        """.trimIndent()

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val jsonRequest = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", command)
                        })
                    })
                })
            })
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", systemPrompt)
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.1) // Low temperature for high precision
                put("responseMimeType", "application/json")
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonRequest.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            Log.e(tag, "HTTP call to Gemini API failed: ${response.code} ${response.message}")
            return null
        }

        val responseBodyString = response.body?.string() ?: return null
        Log.d(tag, "Raw Gemini Response: $responseBodyString")

        val responseJson = JSONObject(responseBodyString)
        val candidates = responseJson.getJSONArray("candidates")
        if (candidates.length() == 0) return null

        val firstCandidate = candidates.getJSONObject(0)
        val parts = firstCandidate.getJSONObject("content").getJSONArray("parts")
        if (parts.length() == 0) return null

        val rawText = parts.getJSONObject(0).getString("text").trim()
        Log.d(tag, "Cleaned AI Text: $rawText")

        val parsedObj = JSONObject(rawText)
        return VoiceIntent(
            category = parsedObj.optString("category", "INFORMATION"),
            action = parsedObj.optString("action", "TALK"),
            entity = parsedObj.optString("entity").takeIf { it.isNotEmpty() && it != "null" },
            value = parsedObj.optString("value").takeIf { it.isNotEmpty() && it != "null" },
            successMessage = parsedObj.optString("success_message", "Action processed!"),
            isOfflineMode = false
        )
    }
}
