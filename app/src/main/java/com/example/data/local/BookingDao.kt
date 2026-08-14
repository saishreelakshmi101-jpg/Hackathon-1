package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BookingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings ORDER BY bookingDate DESC, startMinutes ASC")
    fun getAllBookingsFlow(): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE bookingDate = :date AND status = 'CONFIRMED' ORDER BY startMinutes ASC")
    fun getConfirmedBookingsForDateFlow(date: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE resourceId = :resourceId AND bookingDate = :date AND status = 'CONFIRMED' ORDER BY startMinutes ASC")
    suspend fun getConfirmedBookingsForResourceAndDate(resourceId: String, date: String): List<BookingEntity>

    @Query("""
        SELECT * FROM bookings 
        WHERE resourceId = :resourceId 
        AND bookingDate = :date 
        AND status = 'CONFIRMED' 
        AND startMinutes < :reqEnd 
        AND endMinutes > :reqStart
        LIMIT 1
    """)
    suspend fun findConflict(resourceId: String, date: String, reqStart: Int, reqEnd: Int): BookingEntity?

    @Query("SELECT * FROM bookings WHERE id = :id LIMIT 1")
    suspend fun getBookingById(id: Long): BookingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bookings: List<BookingEntity>)

    @Update
    suspend fun updateBooking(booking: BookingEntity)

    @Query("UPDATE bookings SET status = 'CANCELLED' WHERE id = :id")
    suspend fun cancelBooking(id: Long)

    @Query("DELETE FROM bookings WHERE id = :id")
    suspend fun deleteBooking(id: Long)

    @Query("SELECT COUNT(*) FROM bookings")
    suspend fun getCount(): Int
}
