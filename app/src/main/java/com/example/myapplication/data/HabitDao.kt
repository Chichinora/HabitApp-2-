package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

class HabitConverters {
    @TypeConverter
    fun fromFrequency(value: HabitFrequency): String = value.name

    @TypeConverter
    fun toFrequency(value: String): HabitFrequency = HabitFrequency.valueOf(value)
}

@Dao
@TypeConverters(HabitConverters::class)
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun observeHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habit_completions WHERE completedOn = :date")
    fun observeCompletionsForDate(date: String): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions")
    fun observeAllCompletions(): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY completedOn DESC")
    suspend fun getCompletionsForHabit(habitId: Long): List<HabitCompletionEntity>

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND completedOn = :date LIMIT 1")
    suspend fun findCompletion(habitId: Long, date: String): HabitCompletionEntity?

    @Insert
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCompletion(completion: HabitCompletionEntity): Long

    @Delete
    suspend fun deleteCompletion(completion: HabitCompletionEntity)
}
