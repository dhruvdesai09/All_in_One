package com.habit.app.presentation.navigation

object Routes {
    const val Home = "home"
    const val Add = "add"
    const val Edit = "edit/{habitId}"
    fun edit(id: Long) = "edit/$id"
    const val Detail = "detail/{habitId}"
    fun detail(id: Long) = "detail/$id"
    const val Settings = "settings"
    const val All = "all"
}
