package com.vextrainer.android.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// AuthRepository uses @Singleton + @Inject constructor — Hilt provides it automatically.
// Add interface bindings here as repositories are introduced in later stages.
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule
