package com.habit.app.presentation.navigation

object Routes {
    // Welcome
    const val Welcome = "welcome"

    // Habits
    const val Home = "home"
    const val Add = "add"
    const val Edit = "edit/{habitId}"
    fun edit(id: Long) = "edit/$id"
    const val Detail = "detail/{habitId}"
    fun detail(id: Long) = "detail/$id"
    const val Settings = "settings"
    const val All = "all"

    // Password vault
    const val PinEntry = "pin_entry"
    const val PasswordList = "password_list"
    const val AddPassword = "add_password"
    const val EditPassword = "edit_password/{passwordId}"
    fun editPassword(id: Long) = "edit_password/$id"
    const val PasswordDetail = "password_detail/{passwordId}"
    fun passwordDetail(id: Long) = "password_detail/$id"
    const val VaultBackup = "vault_backup"

    // Budget Tracker
    const val BudgetOverview = "budget_overview"
    const val AddBudget = "add_budget"
    const val EditBudget = "edit_budget/{budgetId}"
    fun editBudget(id: Long) = "edit_budget/$id"
    const val AddTransaction = "add_transaction"
    const val TransactionHistory = "transaction_history"
    const val BudgetInsights = "budget_insights"
}
