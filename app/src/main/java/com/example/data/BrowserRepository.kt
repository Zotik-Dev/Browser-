package com.example.data

import kotlinx.coroutines.flow.Flow

class BrowserRepository(private val dao: BrowserDao) {

    val allBookmarks: Flow<List<BookmarkEntity>> = dao.getAllBookmarks()
    val allHistory: Flow<List<HistoryEntity>> = dao.getAllHistory()
    val allDownloads: Flow<List<DownloadEntity>> = dao.getAllDownloads()

    fun isBookmarked(url: String): Flow<Boolean> = dao.isBookmarked(url)

    suspend fun addBookmark(title: String, url: String) {
        if (url.isBlank() || url.startsWith("apex://")) return
        dao.insertBookmark(BookmarkEntity(title = title.ifBlank { url }, url = url))
    }

    suspend fun removeBookmarkByUrl(url: String) {
        dao.deleteBookmarkByUrl(url)
    }

    suspend fun deleteBookmark(bookmark: BookmarkEntity) {
        dao.deleteBookmark(bookmark)
    }

    suspend fun addHistory(title: String, url: String) {
        if (url.isBlank() || url.startsWith("apex://") || url == "about:blank") return
        dao.insertHistory(
            HistoryEntity(
                title = title.ifBlank { url },
                url = url,
                visitedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteHistory(history: HistoryEntity) {
        dao.deleteHistory(history)
    }

    suspend fun clearAllHistory() {
        dao.clearAllHistory()
    }

    suspend fun addDownload(fileName: String, url: String, fileSize: Long, mimeType: String) {
        dao.insertDownload(
            DownloadEntity(
                fileName = fileName,
                url = url,
                fileSize = fileSize,
                mimeType = mimeType,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteDownload(download: DownloadEntity) {
        dao.deleteDownload(download)
    }

    suspend fun clearAllDownloads() {
        dao.clearAllDownloads()
    }
}
