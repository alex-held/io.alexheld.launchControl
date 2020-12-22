package ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.selection.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.font.FontStyle.*
import androidx.compose.ui.unit.*


sealed class Result<out R> {
	data class Success<out T>(val data: T) : Result<T>()
	data class Error(val exception: Exception) : Result<Nothing>()
}


val Repository = ambientOf<LaunchAgentsRepository>()

data class LaunchAgent(val name: String, val path: String)

interface LaunchAgentsRepository {
	fun getLaunchAgents(callback: (Result<LaunchAgents>) -> Unit)
}

class LaunchAgentsRepositoryImpl : LaunchAgentsRepository {

	override fun getLaunchAgents(callback: (Result<LaunchAgents>) -> Unit) {
		val agents = LaunchAgents(mutableListOf(
			LaunchAgent("test1", "/path/1"),
			LaunchAgent("test2", "/path/2"),
			LaunchAgent("test3", "/path/3")
		), null)
		val result = Result.Success(agents)
		callback(result)
	}

}

@Composable
fun LaunchControlView() {
	MaterialTheme(
		colors = darkColors()
	) {
		DisableSelection {
			Main()
		}
	}
}


@Composable
fun Main() {
	val currentLaunchAgent: MutableState<LaunchAgent?> = remember { mutableStateOf(null) }

	TwoColumnsLayout(currentLaunchAgent)
}


@Composable
fun TwoColumnsLayout(currentLaunchAgent: MutableState<LaunchAgent?>) {
	Row(Modifier.fillMaxSize()) {
		Box(modifier = Modifier.fillMaxWidth(0.4f), contentAlignment = Alignment.Center) {
			LaunchAgentsList(currentLaunchAgent)
		}
		CurrentLaunchAgent(currentLaunchAgent.value)
	}
}


@Composable
fun CurrentLaunchAgent(launchAgent: LaunchAgent?) {
	when (launchAgent) {
		null -> {
			Text("Select issue")
		}
		else -> {
			val repo = Repository.current
			val issueBody = uiStateFrom(null) { clb: (Result<LaunchAgents>) -> Unit ->
				repo.getLaunchAgents(clb)
			}.value
			when (issueBody) {
				is UiState.Loading -> Loader()
				is UiState.Error -> Error("Issue loading error")
				is UiState.Success -> Loader()
			}
		}
	}
}



data class LaunchAgents(
	val nodes: List<LaunchAgent>,
	val cursor: String?,
)


@Composable
fun LaunchAgentsList(currentLaunchAgent: MutableState<LaunchAgent?>) {
	val scroll = rememberScrollState(0f)

	Column {
		Scaffold(
			topBar = {
				TopAppBar(
					title = { Text(text = "LaunchControl") },
					actions = {
						Button(onClick = { scroll.scrollTo(0F) }) {
							Text("Scroll -> Top")
						}
						Button(onClick = { scroll.scrollTo(scroll.maxValue) }) {
							Text("Scroll -> Bottom")
						}
					}
				)
			},
			bodyContent = {
				Column {
					// FilterTabs(issuesState, scroll)
					ListBody(
						scroll,
						currentLaunchAgent = currentLaunchAgent
					)
				}
			}
		)
	}
}

@Composable
fun ListBody(
	scroll: ScrollState,
	currentLaunchAgent: MutableState<LaunchAgent?>,
) {
	val repo = Repository.current
	val launchAgents = uiStateFrom(null) { clb: (Result<LaunchAgents>) -> Unit ->
		repo.getLaunchAgents(callback = clb)
	}

	ScrollableColumn(scrollState = scroll) {
		launchAgents.value.let {
			when (it) {
				is UiState.Success -> {
					for (iss in it.data.nodes) {
						Box(modifier = Modifier.clickable {
							currentLaunchAgent.value = iss
						}, contentAlignment = Alignment.CenterStart) {
							ListItem(iss)
						}
					}
					// MoreButton(launchAgents)
				}

				is UiState.Loading -> Loader()
				is UiState.Error -> Error("Issues loading error")
			}
		}
	}
}

@Composable
fun Loader() {
	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier.fillMaxWidth().padding(20.dp)
	) {
		CircularProgressIndicator()
	}
}


@Composable
fun ListItem(x: LaunchAgent) {
	Card(modifier = Modifier.padding(10.dp).fillMaxWidth()) {
		LaunchAgentCardBody(x)
	}
}


@Composable
fun LaunchAgentCardBody(x: LaunchAgent) {
	Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
		Column {
			Row {
				// CreatedAt(x)
				Spacer(Modifier.width(10.dp))
				//Number(x)
			}

			Title(x)
			FilePath(x)
			// Labels(x.labels)
		}
	}
}

@Composable
fun FilePath(x: LaunchAgent) {
	Text(text = x.path, fontStyle = Italic, color = darkColors().secondary)
}


@Composable
fun Title(x: LaunchAgent) {
	Text(text = x.name, fontWeight = FontWeight(600))
}
