package ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.selection.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.font.FontStyle.*
import androidx.compose.ui.unit.*
import data.*
import data.LaunchServiceKind.*
import java.io.*


val Repository = ambientOf<LaunchAgentsRepository>()

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
	val currentLaunchAgent: MutableState<LaunchService?> = remember { mutableStateOf(null) }

	TwoColumnsLayout(currentLaunchAgent)
}


@Composable
fun TwoColumnsLayout(currentLaunchAgent: MutableState<LaunchService?>) {
	Row(Modifier.fillMaxSize()) {
		Box(modifier = Modifier.fillMaxWidth(0.4f), contentAlignment = Alignment.Center) {
			LaunchAgentsList(currentLaunchAgent)
		}
		CurrentLaunchAgent(currentLaunchAgent.value)
	}
}


@Composable
fun CurrentLaunchAgent(launchService: LaunchService?) {
	when (launchService) {
		null -> CurrentLaunchServiceStatus { Text("Select issue") }
		else -> {
			val repo = Repository.current
			val issueBody = uiStateFrom(null) { clb: (Result<LaunchServices>) -> Unit ->
				repo.getByKind(launchService.kind, clb)
			}.value
			when (issueBody) {
				is UiState.Loading -> CurrentLaunchServiceStatus { Loader() }
				is UiState.Error -> CurrentLaunchServiceStatus { Error("Issue loading error") }
				is UiState.Success -> CurrentLaunchServiceActive(launchService, issueBody.data)
			}
		}
	}
}

@Composable
fun CurrentLaunchServiceActive(launchService: LaunchService, data: LaunchServices) {
	ScrollableColumn(modifier = Modifier.padding(15.dp).fillMaxSize()) {
		SelectionContainer {
			Text(
				text = launchService.name,
				style = MaterialTheme.typography.h5
			)
		}

		Spacer(Modifier.height(8.dp))

		SelectionContainer {
			Text(
				text = File(launchService.path).readText(),
				modifier = Modifier.padding(4.dp),
				style = MaterialTheme.typography.body1
			)
		}
	}
}


@Composable
fun LaunchAgentsList(currentLaunchService: MutableState<LaunchService?>) {
	val scroll = rememberScrollState(0f)
	val launchServiceKind = remember { mutableStateOf(All) }
	Column {
		Scaffold(
			topBar = {
				TopAppBar(
					title = { Text(text = "LaunchControl") },
				)
			},
			bodyContent = {
				Column {
					FilterTabs(launchServiceKind, scroll)
					ListBody(
						scroll,
						currentLaunchService = currentLaunchService,
						launchServiceKind.value
					)
				}
			}
		)
	}
}

@Composable
fun CurrentLaunchServiceStatus(content: @Composable () -> Unit) {
	Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
		content()
	}
}

@Composable
fun FilterTabs(kind: MutableState<LaunchServiceKind>, scroll: ScrollState) {
	TabRow(selectedTabIndex = LaunchServiceKind.values().toList().indexOf(kind.value)) {
		LaunchServiceKind.values().forEach {
			Tab(
				text = { Text(it.name) },
				selected = kind.value == it,
				onClick = {
					kind.value = it
					scroll.scrollTo(0F)
				}
			)
		}
	}
}


@Composable
fun ListBody(
	scroll: ScrollState,
	currentLaunchService: MutableState<LaunchService?>,
	serviceKind: LaunchServiceKind
) {
	val repo = Repository.current
	val launchAgents = uiStateFrom(null) { clb: (Result<LaunchServices>) -> Unit ->
		repo.getByKind(serviceKind, clb)
	}

	ScrollableColumn(scrollState = scroll) {
		launchAgents.value.let {
			when (it) {
				is UiState.Success -> {
					for (iss in it.data.nodes) {
						Box(modifier = Modifier.clickable {
							currentLaunchService.value = iss
						}, contentAlignment = Alignment.CenterStart) {
							ListItem(iss)
						}
					}
					//MoreButton(launchAgents)
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
fun ListItem(x: LaunchService) {
	Card(modifier = Modifier.padding(10.dp).fillMaxWidth()) {
		LaunchAgentCardBody(x)
	}
}


@Composable
fun LaunchAgentCardBody(x: LaunchService) {
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
fun FilePath(x: LaunchService) {
	Text(text = x.path, fontStyle = Italic, color = darkColors().secondary)
}


@Composable
fun Title(x: LaunchService) {
	Text(text = x.name, fontWeight = FontWeight(600))
}
