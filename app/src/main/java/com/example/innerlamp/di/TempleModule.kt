package com.example.innerlamp.di

import com.example.innerlamp.core.network.NetworkClient
import com.example.innerlamp.data.remote.api.TempleApiService
import com.example.innerlamp.data.repository.TempleOptionRepositoryImpl
import com.example.innerlamp.domain.repository.TempleOptionRepository

/** Wiring thủ công cho data layer của chùa (không thêm thư viện DI). */
object TempleModule {

    fun provideApiService(
        networkClient: NetworkClient = NetworkModule.provideNetworkClient(),
    ): TempleApiService = networkClient.createService(TempleApiService::class.java)

    fun provideTempleOptionRepository(
        networkClient: NetworkClient = NetworkModule.provideNetworkClient(),
    ): TempleOptionRepository = TempleOptionRepositoryImpl(
        api = provideApiService(networkClient),
        networkClient = networkClient,
    )
}
