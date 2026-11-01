package com.gridlab.app.core.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gridlab.app.core.data.database.AppDatabase
import com.gridlab.app.core.data.database.ProjectDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE documents ADD COLUMN gridJson TEXT NOT NULL DEFAULT '{}'",
            )
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "gridlab.db",
        )
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun provideProjectDao(database: AppDatabase): ProjectDao =
        database.projectDao()
}
