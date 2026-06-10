package com.lvsmsmch.deckbuilder.data.hsjson

import com.lvsmsmch.deckbuilder.data.update.CardDataProgress
import com.lvsmsmch.deckbuilder.data.update.UpdateNotifier
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import okio.ForwardingSource
import okio.buffer

class CardDataProgressInterceptor(
    private val notifier: UpdateNotifier,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val body = response.body ?: return response
        val path = chain.request().url.encodedPath
        if (!path.endsWith("/cards.json")) return response
        return response.newBuilder()
            .body(ProgressBody(body, notifier))
            .build()
    }
}

private class ProgressBody(
    private val delegate: ResponseBody,
    private val notifier: UpdateNotifier,
) : ResponseBody() {
    override fun contentType() = delegate.contentType()

    override fun contentLength() = delegate.contentLength()

    override fun source() = object : ForwardingSource(delegate.source()) {
        private var downloaded = 0L
        private val total = contentLength().takeIf { it > 0L }
        private var lastEmitAt = 0L

        override fun read(sink: Buffer, byteCount: Long): Long {
            val read = super.read(sink, byteCount)
            if (read > 0L) {
                downloaded += read
                val now = System.currentTimeMillis()
                if (now - lastEmitAt >= 120L || downloaded == total) {
                    lastEmitAt = now
                    notifier.setCardDataProgress(
                        CardDataProgress(
                            stage = CardDataProgress.Stage.DOWNLOADING,
                            downloadedBytes = downloaded,
                            totalBytes = total,
                        ),
                    )
                }
            }
            return read
        }
    }.buffer()
}
