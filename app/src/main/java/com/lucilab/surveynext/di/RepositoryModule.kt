package com.lucilab.surveynext.di


import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.lucilab.surveynext.data.repository.AuthRepository
import com.lucilab.surveynext.data.repository.AuthRepositoryImpl
import dagger.Binds

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository
}




