package data

import data.LaunchAgentState.*
import data.LaunchServiceKind.*
import data.LaunchServiceKind.UserAgent
import java.io.*
import kotlin.io.path.*


interface LaunchAgentsRepository {
	fun getUserAgents(callback: (Result<LaunchServices>) -> Unit)
	fun getLaunchAgents(callback: (Result<LaunchServices>) -> Unit)
}

class LaunchAgentsRepositoryImpl : LaunchAgentsRepository {

	@ExperimentalPathApi
	override fun getUserAgents(callback: (Result<LaunchServices>) -> Unit) {

		File(Paths.UserAgentsPath).walk().forEach {
			println(it.toString())
		}

		val results = File(Paths.UserAgentsPath).walk()
			.filter {  it.extension == "plist"}
			.map { LaunchService(it.nameWithoutExtension, it.toString(), Running, UserAgent) }
			.toList()

		val result = Result.Success(LaunchServices(results, ""))
		callback(result)
	}

	override fun getLaunchAgents(callback: (Result<LaunchServices>) -> Unit) {
		val agents = LaunchServices(mutableListOf(
			LaunchService("test1", "/path/1", Stopped, GlobalAgent),
			LaunchService("test2", "/path/2", Running, GlobalDaemon),
			LaunchService("test3", "/path/3", Stopped, UserAgent)
		), null)
		val result = Result.Success(agents)
		callback(result)
	}
}

