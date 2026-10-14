package com.example.innerlamp.di

import com.example.innerlamp.core.network.NetworkClient
import com.example.innerlamp.data.remote.api.AdministrativeUnitApiService
import com.example.innerlamp.data.repository.AdministrativeUnitRepositoryImpl
import com.example.innerlamp.domain.repository.AdministrativeUnitRepository

/**
 * Wiring thủ công cho metadata đơn vị hành chính (không thêm thư viện DI).
 *
 * Repository giữ cache trong bộ nhớ nên cần dùng chung một instance: giữ kết quả của
 * [provideAdministrativeUnitRepository] ở nơi gọi (vd `Application`) thay vì gọi lại nhiều lần.
 */
object AdministrativeUnitModule {

    fun provideApiService(
        networkClient: NetworkClient = NetworkModule.provideNetworkClient(),
    ): AdministrativeUnitApiService =
        networkClient.createService(AdministrativeUnitApiService::class.java)

    fun provideAdministrativeUnitRepository(
        networkClient: NetworkClient = NetworkModule.provideNetworkClient(),
    ): AdministrativeUnitRepository = AdministrativeUnitRepositoryImpl(
        api = provideApiService(networkClient),
        networkClient = networkClient,
    )
}
