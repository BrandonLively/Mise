package com.patchfox.mise.data.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.persistentCacheSettings
import com.patchfox.mise.data.auth.AuthRepository
import com.patchfox.mise.data.auth.AuthRepositoryImpl
import com.patchfox.mise.data.cookcard.CookCardRepository
import com.patchfox.mise.data.cookcard.CookCardRepositoryImpl
import com.patchfox.mise.data.cookcard.CookCardSource
import com.patchfox.mise.data.cookcard.FirestoreCookCardSource
import com.patchfox.mise.data.local.CompletedRecipeDao
import com.patchfox.mise.data.local.CookCardDao
import com.patchfox.mise.data.local.CookStepIndexDao
import com.patchfox.mise.data.local.LaneVisibilityDao
import com.patchfox.mise.data.local.DaysDivisorDao
import com.patchfox.mise.data.local.MiseCheckDao
import com.patchfox.mise.data.local.MiseDatabase
import com.patchfox.mise.data.local.PendingAlarmDao
import com.patchfox.mise.data.local.PrepCheckDao
import com.patchfox.mise.data.local.StartedTimerDao
import com.patchfox.mise.data.local.StepCompletionDao
import com.patchfox.mise.data.local.SummaryInputDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindingsModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindCookCardSource(impl: FirestoreCookCardSource): CookCardSource

    @Binds
    @Singleton
    abstract fun bindCookCardRepository(impl: CookCardRepositoryImpl): CookCardRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DataProvidesModule {

    @Provides
    @Singleton
    fun provideMiseDatabase(@ApplicationContext context: Context): MiseDatabase =
        Room.databaseBuilder(context, MiseDatabase::class.java, "mise.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideCookCardDao(db: MiseDatabase): CookCardDao = db.cookCardDao()
    @Provides fun provideMiseCheckDao(db: MiseDatabase): MiseCheckDao = db.miseCheckDao()
    @Provides fun providePrepCheckDao(db: MiseDatabase): PrepCheckDao = db.prepCheckDao()
    @Provides fun provideStepCompletionDao(db: MiseDatabase): StepCompletionDao = db.stepCompletionDao()
    @Provides fun provideStartedTimerDao(db: MiseDatabase): StartedTimerDao = db.startedTimerDao()
    @Provides fun provideSummaryInputDao(db: MiseDatabase): SummaryInputDao = db.summaryInputDao()
    @Provides fun provideDaysDivisorDao(db: MiseDatabase): DaysDivisorDao = db.daysDivisorDao()
    @Provides fun providePendingAlarmDao(db: MiseDatabase): PendingAlarmDao = db.pendingAlarmDao()
    @Provides fun provideLaneVisibilityDao(db: MiseDatabase): LaneVisibilityDao = db.laneVisibilityDao()
    @Provides fun provideCookStepIndexDao(db: MiseDatabase): CookStepIndexDao = db.cookStepIndexDao()
    @Provides fun provideCompletedRecipeDao(db: MiseDatabase): CompletedRecipeDao = db.completedRecipeDao()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance().apply {
        firestoreSettings = firestoreSettings {
            setLocalCacheSettings(persistentCacheSettings { })
        }
    }
}
