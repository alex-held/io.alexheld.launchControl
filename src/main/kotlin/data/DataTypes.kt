package data

import data.LaunchServiceKind.*


sealed class Result<out R> {
	data class Success<out T>(val data: T) : Result<T>()
	data class Error(val exception: Exception) : Result<Nothing>()
}


enum class LaunchAgentState(active: Boolean){
	Running(true),
	Stopped(false)
}

enum class LaunchServiceKind(name: String){
	All("All"),
	UserAgent("UserAgent"),
	GlobalAgent("GlobalAgent"),
	GlobalDaemon("GlobalDaemon"),
}


data class LaunchServices(val nodes: List<LaunchService>, val cursor: String?)
data class LaunchService(val name: String, val path: String, var state: LaunchAgentState, val kind: LaunchServiceKind)

fun LaunchService.isOfKind(kind: LaunchServiceKind): Boolean {
	if (kind == All)
		return true
	return this.kind == kind
}
