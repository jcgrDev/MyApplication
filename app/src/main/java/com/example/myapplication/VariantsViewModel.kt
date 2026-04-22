package com.example.myapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

data class Variant(
    val bandwidth: Int?,
    val resolution: String?,
    val url: String?,
)

class VariantsViewModel : ViewModel() {
    private val _variants = MutableStateFlow<List<Variant>>(emptyList())
    val variants: StateFlow<List<Variant>> = _variants.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            val dataStr = withContext(Dispatchers.IO) {
                URL("https://qaltr-vod.immergo.tv/qaltr/transcoded/05535679-c467-4f05-b863-0bd00dba88f5/hls/master.m3u8")
                    .readText()
            }
            _variants.value = parseData(dataStr)
        }
    }

    private fun parseData(dataStr: String): List<Variant> {
        val lines = dataStr.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val variants = mutableListOf<Variant>()

        for (i in lines.indices) {
            val line = lines[i]
            if (line.startsWith("#EXT-X-STREAM-INF:")) {
                val attrs = line.removePrefix("#EXT-X-STREAM-INF:")
                val url = lines.getOrNull(i + 1) ?: continue
                if (url.startsWith("#")) continue

                variants += Variant(
                    bandwidth = Regex("BANDWIDTH=(\\d+)").find(attrs)?.groupValues?.get(1)?.toIntOrNull(),
                    resolution = Regex("RESOLUTION=([\\dx]+)").find(attrs)?.groupValues?.get(1),
                    url = url
                )
        }
    }
        return variants
    }

}
