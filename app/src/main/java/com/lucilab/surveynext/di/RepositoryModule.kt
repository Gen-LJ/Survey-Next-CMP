package com.lucilab.surveynext.di


import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.lucilab.surveynext.data.repository.AuthRepository
import com.lucilab.surveynext.data.repository.AuthRepositoryImpl
import com.lucilab.surveynext.data.repository.InterviewerRepository
import com.lucilab.surveynext.data.repository.InterviewerRepositoryImpl
import com.lucilab.surveynext.data.repository.RespondentRepository
import com.lucilab.surveynext.data.repository.RespondentRepositoryImpl
import dagger.Binds

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindInterviewerRepository(
        impl: InterviewerRepositoryImpl
    ): InterviewerRepository

    @Binds
    @Singleton
    abstract fun bindRespondentRepository(
        impl: RespondentRepositoryImpl
    ): RespondentRepository
}
