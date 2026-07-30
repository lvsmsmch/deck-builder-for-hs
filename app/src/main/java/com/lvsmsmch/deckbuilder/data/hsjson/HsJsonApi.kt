package com.lvsmsmch.deckbuilder.data.hsjson

import com.lvsmsmch.deckbuilder.data.hsjson.dto.HsJsonCardDto
import com.lvsmsmch.deckbuilder.data.update.CardDataProgress
import com.lvsmsmch.deckbuilder.data.update.UpdateNotifier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.get
import kotlinx.datetime.Clock

/**
 * HearthstoneJSON CDN. Base URL: https://api.hearthstonejson.com/v1/
 *
 * Build numbers: `latest` is a redirect to the current numeric build directory
 * (e.g. /v1/229145/enUS/cards.json). [BuildChecker] resolves the
 * latest build number; this api fetches a snapshot for a known build + locale.
 *
 * Download progress is published straight to [UpdateNotifier] (throttled), the
 * same contract the old OkHttp interceptor provided.
 */
class HsJsonApi(
    private val client: HttpClient,
    private val notifier: UpdateNotifier,
    private val baseUrl: String = "https://api.hearthstonejson.com/v1/",
) {

    /** Cards JSON for a specific build + locale (HearthstoneJSON locale form: `enUS`, `ruRU`). */
    suspend fun cardsForBuild(build: String, locale: String): List<HsJsonCardDto> {
        var lastEmitAt = 0L
        return client.get("$baseUrl$build/$locale/cards.json") {
            onDownload { downloaded, total ->
                val now = Clock.System.now().toEpochMilliseconds()
                if (now - lastEmitAt >= 120L || (total != null && downloaded == total)) {
                    lastEmitAt = now
                    notifier.setCardDataProgress(
                        CardDataProgress(
                            stage = CardDataProgress.Stage.DOWNLOADING,
                            downloadedBytes = downloaded,
                            totalBytes = total?.takeIf { it > 0L },
                        ),
                    )
                }
            }
        }.body()
    }
}
