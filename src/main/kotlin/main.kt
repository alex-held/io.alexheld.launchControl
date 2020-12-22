import androidx.compose.desktop.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.*
import data.*
import ui.*


val repo = LaunchAgentsRepositoryImpl()


fun main() = Window(
	title = "launchControl",
	size = IntSize(1500, 1440)) {

	Providers(Repository provides repo) {
		LaunchControlView()
	}
}
