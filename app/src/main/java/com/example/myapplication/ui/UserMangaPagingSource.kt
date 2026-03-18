package com.example.myapplication.ui

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.myapplication.data.model.UserMangaData
import com.example.myapplication.data.repository.AnimeRepository

class UserMangaPagingSource(
    private val repository: AnimeRepository,
    private val username: String?,
    private val status: String?,
    private val sort: String
) : PagingSource<Int, UserMangaData>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, UserMangaData> {
        val offset = params.key ?: 0
        return try {
            val response = repository.getUserMangaList(
                username = username,
                status = if (status == "all") null else status,
                sort = sort,
                offset = offset,
                limit = params.loadSize
            )
            LoadResult.Page(
                data = response.data,
                prevKey = if (offset == 0) null else (offset - params.loadSize).coerceAtLeast(0),
                nextKey = response.paging.next?.extractOffset()
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, UserMangaData>): Int? {
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
