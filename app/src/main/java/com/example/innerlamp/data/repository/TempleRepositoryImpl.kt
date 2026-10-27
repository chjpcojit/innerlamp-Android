package com.example.innerlamp.data.repository

import com.example.innerlamp.core.network.NetworkClient
import com.example.innerlamp.data.mapper.toDomain
import com.example.innerlamp.data.remote.api.TempleApiService
import com.example.innerlamp.domain.model.Temple
import com.example.innerlamp.domain.model.TempleDetail
import com.example.innerlamp.domain.repository.TempleRepository

/**
 * [TempleRepository] over the public temple routes; online only, no cache (Temple spec: "every open is
 * one call"). /
 * Cài đặt [TempleRepository] qua các route chùa công khai; chỉ online, không cache.
 */
class TempleRepositoryImpl(
    private val api: TempleApiService,
    private val networkClient: NetworkClient,
) : TempleRepository {

    override suspend fun getFeatured(): Result<List<Temple>> =
        networkClient.loadContent(call = { api.getTop() }, map = { dto -> dto.items.map { it.toDomain() } })

    override suspend fun getAll(): Result<List<Temple>> =
        networkClient.loadAllPages(
            call = { page -> api.getList(page) },
            items = { it.items },
            totalPages = { it.totalPages },
        ).map { items -> items.map { it.toDomain() } }

    override suspend fun getDetail(id: String): Result<TempleDetail> =
        networkClient.loadContent(notFoundCode = TEMPLE_NOT_FOUND, call = { api.getDetail(id) }, map = { it.toDomain() })

    private companion object {
        const val TEMPLE_NOT_FOUND = "TEMPLE_NOT_FOUND"
    }
}
