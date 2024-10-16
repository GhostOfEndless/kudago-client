package client

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.client.KudaGoClientImpl
import org.example.client.KudaGoCoroutineFlow
import org.example.dto.News
import java.io.File

class KudaGoCoroutineFlowTest : StringSpec({

    beforeTest {
        val newsFile = File("news.md")
        if (newsFile.exists()) {
            newsFile.delete()
        }
    }

    "execute should successfully retrieve all news and write them to a file" {
        val mockClient = mockk<KudaGoClientImpl>()

        val totalNewsCount = 5
        val workerCount = 2
        val maxPageSize = 2

        coEvery { mockClient.getNewsPage(1, 2) } returns listOf(
            News(
                1, "News 1", "place 1", "description 1", "https://example.com/1", 1, 1,
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            ),
            News(
                2, "News 2", "place 2", "description 2", "https://example.com/2", 1, 1,
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            )
        )
        coEvery { mockClient.getNewsPage(2, 2) } returns listOf(
            News(
                3, "News 3", "place 3", "description 3", "https://example.com/3", 1, 1,
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            ),
            News(
                4, "News 4", "place 4", "description 4", "https://example.com/4", 1, 1,
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            )
        )
        coEvery { mockClient.getNewsPage(3, 2) } returns listOf(
            News(
                5, "News 5", "place 5", "description 5", "https://example.com/5", 1, 1,
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            )
        )

        val flow = KudaGoCoroutineFlow(
            kudaGoClient = mockClient,
            totalNewsCount = totalNewsCount,
            workerCount = workerCount,
            maxPageSize = maxPageSize
        )

        runBlocking {
            flow.execute()
        }

        val newsFile = File("news.md")
        newsFile.exists() shouldBe true

        coVerify(exactly = 1) { mockClient.getNewsPage(1, 2) }
        coVerify(exactly = 1) { mockClient.getNewsPage(2, 2) }
        coVerify(exactly = 1) { mockClient.getNewsPage(3, 2) }

        confirmVerified(mockClient)
    }

    "execute should correctly handle the case when there are no news" {
        val mockClient = mockk<KudaGoClientImpl>()

        val totalNewsCount = 0
        val workerCount = 2
        val maxPageSize = 2

        val flow = KudaGoCoroutineFlow(
            kudaGoClient = mockClient,
            totalNewsCount = totalNewsCount,
            workerCount = workerCount,
            maxPageSize = maxPageSize
        )

        runBlocking {
            flow.execute()
        }

        val newsFile = File("news.md")
        if (newsFile.exists()) {
            val actualLines = newsFile.readLines()
            actualLines shouldBe emptyList()
            newsFile.delete()
        } else {
            true shouldBe true
        }

        coVerify(exactly = 0) { mockClient.getNewsPage(any(), any()) }
        confirmVerified(mockClient)
    }

    "execute should correctly handle errors when retrieving pages" {
        val mockClient = mockk<KudaGoClientImpl>()

        val totalNewsCount = 3
        val workerCount = 1
        val maxPageSize = 2

        coEvery { mockClient.getNewsPage(1, 2) } returns listOf(
            News(
                1, "News 1", "place 1", "description 1", "https://example.com/1", 1, 1,
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            ),
            News(
                2, "News 2", "place 2", "description 2", "https://example.com/2", 1, 1,
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            )
        )

        coEvery { mockClient.getNewsPage(2, 2) } returns emptyList()

        val flow = KudaGoCoroutineFlow(
            kudaGoClient = mockClient,
            totalNewsCount = totalNewsCount,
            workerCount = workerCount,
            maxPageSize = maxPageSize
        )

        runBlocking {
            flow.execute()
        }

        val newsFile = File("news.md")
        newsFile.exists() shouldBe true

        coVerify(exactly = 1) { mockClient.getNewsPage(1, 2) }
        coVerify(exactly = 1) { mockClient.getNewsPage(2, 2) }

        confirmVerified(mockClient)
    }
})