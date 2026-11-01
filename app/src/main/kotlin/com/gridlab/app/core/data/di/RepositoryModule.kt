package com.gridlab.app.core.data.di

import com.gridlab.app.core.data.repository.ProjectRepository
import com.gridlab.app.core.data.repository.ProjectRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProjectRepository(
        impl: ProjectRepositoryImpl,
    ): ProjectRepository
}
