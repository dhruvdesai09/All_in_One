package com.habit.app.domain

enum class WidgetDisplayMode(val raw: String) {
    Pending("pending"),
    Heatmap("heatmap"),
    ;

    companion object {
        fun fromRaw(value: String?): WidgetDisplayMode =
            entries.firstOrNull { it.raw == value } ?: Pending
    }
}
