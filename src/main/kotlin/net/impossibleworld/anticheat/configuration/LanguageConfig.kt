package net.impossibleworld.anticheat.configuration

interface LanguageConfig {
    fun getMessage(path: String): String
    fun getMessageList(path: String): List<String>
    fun reloadConfig()
}