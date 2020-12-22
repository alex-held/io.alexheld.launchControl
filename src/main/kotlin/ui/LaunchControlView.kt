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
import data.*



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
		null -> {
			Text("Select issue")
		}
		else -> {
			val repo = Repository.current
			val issueBody = uiStateFrom(null) { clb: (Result<LaunchServices>) -> Unit ->
				repo.getUserAgents { clb }
			}.value
			when (issueBody) {
				is UiState.Loading -> Loader()
				is UiState.Error -> Error("Issue loading error")
				is UiState.Success -> Loader()
			}
		}
	}
}




@Composable
fun LaunchAgentsList(currentLaunchService: MutableState<LaunchService?>) {
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
						currentLaunchService = currentLaunchService
					)
				}
			}
		)
	}
}

@Composable
fun ListBody(
	scroll: ScrollState,
	currentLaunchService: MutableState<LaunchService?>,
) {
	val repo = Repository.current
	val launchAgents = uiStateFrom(null) { clb: (Result<LaunchServices>) -> Unit ->
		repo.getUserAgents(clb)
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
