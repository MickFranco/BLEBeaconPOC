package com.mrzhevskyi.beaconpoc.di

import android.content.Context
import com.minew.beaconplus.sdk.MTCentralManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideMTCentralManager(@ApplicationContext context: Context) = MTCentralManager.getInstance(context)

}