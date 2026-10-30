package de.lorenzschl.shelfvr.core.model

/** Einstellungen (FR-12, FR-32, FR-35, FR-60, FR-61) mit den Defaults aus der Spec. */
data class AppSettings(
    val activeLibraryId: String? = null,
    val sortKey: SortKey = SortKey.TITLE,
    val sortDesc: Boolean = false,
    val skipBackSeconds: Int = 15,
    val skipForwardSeconds: Int = 30,
    val defaultSpeed: Float = 1.0f,
    val syncIntervalSeconds: Int = 15,
    val pauseOnHeadsetRemoved: Boolean = false,
)
