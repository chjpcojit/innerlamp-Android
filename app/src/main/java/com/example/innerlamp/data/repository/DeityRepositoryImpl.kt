package com.example.innerlamp.data.repository

import com.example.innerlamp.core.network.NetworkClient
import com.example.innerlamp.data.mapper.toDomain
import com.example.innerlamp.data.remote.api.DeityApiService
import com.example.innerlamp.domain.model.BuddhaProfile
import com.example.innerlamp.domain.model.DeitySummary
import com.example.innerlamp.domain.repository.DeityRepository

/** [DeityRepository] over the public deity routes; online only, no cache. / Chỉ online, không cache. */
class DeityRepositoryImpl(
    private val api: DeityApiService,
    private val networkClient: NetworkClient,
) : DeityRepository {

    override suspend fun getFeatured(): Result<List<DeitySummary>> =
        networkClient.loadContent(call = { api.getTop() }, map = { dto -> dto.items.map { it.toDomain() } })

    override suspend fun getAll(): Result<List<DeitySummary>> =
        networkClient.loadAllPages(
            call = { page -> api.getList(page) },
            items = { it.items },
            totalPages = { it.totalPages },
        ).map { items -> items.map { it.toDomain() } }

    override suspend fun getProfile(id: String): Result<BuddhaProfile> =
        networkClient.loadContent(notFoundCode = DEITY_NOT_FOUND, call = { api.getDetail(id) }, map = { it.toDomain() })

    private companion object {
        const val DEITY_NOT_FOUND = "DEITY_NOT_FOUND"
    }
}
