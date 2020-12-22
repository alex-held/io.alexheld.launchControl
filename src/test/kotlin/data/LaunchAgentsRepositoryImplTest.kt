package data

import kotlin.io.path.*
import kotlin.test.*

class LaunchAgentsRepositoryImplTest {

	@Test
	fun Should_Get_All_UserAgents() {
		val repo = LaunchAgentsRepositoryImpl()
		repo.getUserAgents  {
			when(it){
				is Result.Success -> println(it.data.nodes)
				is Result.Error -> it.exception
			}
		}
	}
}
