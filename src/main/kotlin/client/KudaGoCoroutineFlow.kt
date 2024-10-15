package org.example.client

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import org.example.dto.News
import org.example.util.NewsPrinter
import java.io.File
import kotlin.math.ceil

class KudaGoCoroutineFlow(
    private val kudaGoClient: KudaGoClientImpl,
    private val totalNewsCount: Int,
    private val workerCount: Int
) {

    private val channel = Channel<News>(Channel.UNLIMITED)
    @OptIn(DelicateCoroutinesApi::class)
    private val threadPool = newFixedThreadPoolContext(workerCount, "WorkerPool")
    private val printer = NewsPrinter()

    suspend fun execute() = coroutineScope {
        val workerJobs = List(workerCount) { workerId ->
            launch(threadPool) {
                worker(workerId)
            }
        }

        val processorJob = launch { processor() }

        workerJobs.joinAll()
        channel.close()
        processorJob.join()
    }

    private suspend fun worker(id: Int) {
        val totalPages = ceil(totalNewsCount.toDouble() / KudaGoClientImpl.MAX_PAGE_SIZE).toInt()
        var currentPage = id

        while (currentPage < totalPages) {
            val news = kudaGoClient.getNewsPage(currentPage + 1, KudaGoClientImpl.MAX_PAGE_SIZE)
            news.forEach { channel.send(it) }
            currentPage += workerCount
        }
    }

    private suspend fun processor() {
        File("news_output.md").bufferedWriter().use { writer ->
            for (news in channel) {
                printer.formatNews(news)
                writer.write(printer.build())
                writer.newLine()
                printer.clear()
            }
        }
    }
}