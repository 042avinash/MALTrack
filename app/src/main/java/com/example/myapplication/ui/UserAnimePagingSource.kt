package com.example.myapplication.ui

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.myapplication.data.model.UserAnimeData
import com.example.myapplication.data.repository.AnimeRepository

class UserAnimePagingSource(
    private val repository: AnimeRepository,
    private val username: String?,
    private val status: String?,
    private val sort: String,
    private val onPageLoaded: suspend (List<UserAnimeData>) -> Unit
) : PagingSource<Int, UserAnimeData>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, UserAnimeData> {
        val offset = params.key ?: 0
        return try {
            val response = repository.getUserAnimeList(
                username = username,
                status = if (status == "all") null else status,
                sort = sort,
                offset = offset,
                limit = params.loadSize
            )
            onPageLoaded(response.data)
            LoadResult.Page(
                data = response.data,
                prevKey = if (offset == 0) null else (offset - params.loadSize).coerceAtLeast(0),
                nextKey = response.paging.next?.extractOffset()
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, UserAnimeData>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}

private fun String.extractOffset(): Int? {
    return substringAfter("offset=", missingDelimiterValue = "")
        .substringBefore("&")
        .toIntOrNull()
}
