package de.lorenzschl.shelfvr.playback

import android.media.MediaCodecList

/**
 * MIME-Typen, die ABS beim Start einer Session als direkt abspielbar übermittelt bekommt
 * (PROJECT_SPEC.md §6.3). Für alles andere liefert ABS einen HLS-Stream.
 */
object SupportedMimeTypes {

    fun forThisDevice(): List<String> {
        val decoderTypes = MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos
            .filterNot { it.isEncoder }
            .flatMap { it.supportedTypes.asList() }
            .map { it.lowercase() }
            .toSet()
        return absMimeTypesFor(decoderTypes)
    }

    /** Ordnet Decoder-Typen von MediaCodec den MIME-Typen zu, die ABS für Audiodateien vergibt. */
    fun absMimeTypesFor(decoderTypes: Set<String>): List<String> = buildList {
        if ("audio/mpeg" in decoderTypes) add("audio/mpeg")
        if ("audio/mp4a-latm" in decoderTypes) addAll(listOf("audio/mp4", "audio/aac", "audio/x-m4b"))
        if ("audio/flac" in decoderTypes) add("audio/flac")
        // ABS führt Opus und Vorbis gemeinsam als audio/ogg; ExoPlayer extrahiert Ogg selbst.
        if ("audio/opus" in decoderTypes && "audio/vorbis" in decoderTypes) add("audio/ogg")
    }
}
