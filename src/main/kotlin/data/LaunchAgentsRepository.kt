package data

import data.LaunchAgentState.*
import data.LaunchServiceKind.*
import data.LaunchServiceKind.UserAgent
import java.io.*
import kotlin.io.path.*


interface LaunchAgentsRepository {

	 fun getByKind(kind: LaunchServiceKind, callback: (Result<LaunchServices>) -> Unit,)
	 fun getUserAgents(callback: (Result<LaunchServices>) -> Unit)
	 fun getGlobalAgents(callback: (Result<LaunchServices>) -> Unit)
}

class LaunchAgentsRepositoryImpl : LaunchAgentsRepository {

	private fun loadGlobalDaemons() = load("/Library/LaunchDaemons") {
		LaunchService(nameWithoutExtension, path, Running, GlobalDaemon)
	}

	private fun loadGlobalAgents() = load("/Library/LaunchAgents") {
		LaunchService(nameWithoutExtension, path, Running, GlobalAgent)
	}

	private fun loadUserAgents() = load("/Users/dev/Library/LaunchAgents") {
		LaunchService(nameWithoutExtension, path, Running, UserAgent)
	}

	private fun load(dir: String, c: File.() -> LaunchService): List<LaunchService> = File(dir).walk()
		.filter { it.isFile && it.extension == "plist" }
		.map(c)
		.onEach { println(it) }
		.toList()

	override fun getByKind(kind: LaunchServiceKind, callback: (Result<LaunchServices>) -> Unit) {
		val results = all().filter { it.isOfKind(kind) }
		val result = Result.Success(LaunchServices(results, ""))
		callback(result)
	}

	private fun all() : MutableList<LaunchService>{
		val results = mutableListOf<LaunchService>()
		results.addAll(loadUserAgents())
		results.addAll(loadGlobalAgents())
		results.addAll(loadGlobalDaemons())
		return results
	}

	override fun getUserAgents(callback: (Result<LaunchServices>) -> Unit) {
		val results = all()

		val result = Result.Success(LaunchServices(results, ""))
		callback(result)
	}

	override fun getGlobalAgents(callback: (Result<LaunchServices>) -> Unit) {
		val agents = LaunchServices(mutableListOf(
			LaunchService("test1", "/path/1", Stopped, GlobalAgent),
			LaunchService("test2", "/path/2", Running, GlobalDaemon),
			LaunchService("test3", "/path/3", Stopped, UserAgent)
		), null)
		val result = Result.Success(agents)
		callback(result)
	}
}

