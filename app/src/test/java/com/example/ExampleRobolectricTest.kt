package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.TimeSlot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("JIT Campus Bookings", appName)
  }

  @Test
  fun `verify time slot conflict calculation`() {
    // Booking A: 14:00 to 16:00 (840 to 960)
    // Booking B: 15:00 to 17:00 (900 to 1020) -> OVERLAPS
    assertTrue(TimeSlot.checkOverlap(840, 960, 900, 1020))

    // Booking C: 16:00 to 18:00 (960 to 1080) -> DOES NOT OVERLAP (Adjacent boundary)
    assertFalse(TimeSlot.checkOverlap(840, 960, 960, 1080))

    // Booking D: 10:00 to 12:00 (600 to 720) -> DOES NOT OVERLAP
    assertFalse(TimeSlot.checkOverlap(840, 960, 600, 720))
  }
}

