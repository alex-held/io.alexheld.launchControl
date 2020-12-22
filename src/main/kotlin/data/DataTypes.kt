package data

import data.LaunchServiceKind.*
import ui.*


sealed class Result<out R> {
	data class Success<out T>(val data: T) : Result<T>()
	data class Error(val exception: Exception) : Result<Nothing>()
}


enum class LaunchAgentState(active: Boolean){
	Running(true),
	Stopped(false)
}

enum class LaunchServiceKind(name: String, short: Short){
	UserAgent("UserAgent", 0),
	GlobalAgent("GlobalAgent", 1),
	GlobalDaemon("GlobalDaemon", 2),
}

object Paths {

	public fun pathFromServiceKind(kind: LaunchServiceKind) : String{
		return when(kind) {
			UserAgent -> UserAgentsPath
			GlobalAgent -> GlobalAgentsPath
			GlobalDaemon -> GlobalDaemonsPath
		}
	}
	public const val UserAgentsPath = "/Users/dev/Library/LaunchAgents"
	public const val GlobalAgentsPath = "/Library/LaunchAgents"
	public const val GlobalDaemonsPath = "/Library/LaunchDaemons"
}



data class LaunchServices(val nodes: List<LaunchService>, val cursor: String?)
data class LaunchService(val name: String, val path: String, var state: LaunchAgentState, val kind: LaunchServiceKind)
