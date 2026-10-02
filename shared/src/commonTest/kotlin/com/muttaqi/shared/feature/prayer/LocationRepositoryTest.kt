package com.muttaqi.shared.feature.prayer

import com.muttaqi.shared.feature.prayer.PrayerTestData.karachi
import com.muttaqi.shared.feature.prayer.PrayerTestData.lahore
import com.muttaqi.shared.feature.prayer.data.location.DeviceLocationRepository
import com.muttaqi.shared.feature.prayer.domain.model.Coordinates
import com.muttaqi.shared.feature.prayer.domain.model.LocationAccess
import com.muttaqi.shared.feature.prayer.domain.usecase.LocateReader
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

class LocationRepositoryTest {
    private val settings = MapSettings()
    private val legacy = mutableMapOf<String, String>()
    private val saved = savedCoordinates(settings, legacy)

    private val swiftJson = """{"longitude":67.0011,"latitude":24.8607}"""

    @Test
    fun readsTheCoordinatesTheSwiftAppStoredAsData() {
        legacy["last_known_coordinates"] = swiftJson
        assertEquals(karachi, saved.load())
    }

    @Test
    fun readsTheSwiftFormatAsText() {
        settings.putString("last_known_coordinates", swiftJson)
        assertEquals(karachi, saved.load())
    }

    @Test
    fun savesTheSwiftFormatAsTextOverTheOldData() {
        legacy["last_known_coordinates"] = swiftJson
        saved.save(lahore)
        assertEquals("""{"latitude":31.5204,"longitude":74.3587}""", settings.getStringOrNull("last_known_coordinates"))
        assertEquals(lahore, saved.load())
    }

    @Test
    fun missingOrUnreadableCoordinatesAreNone() {
        assertNull(saved.load())
        settings.putString("last_known_coordinates", "not json")
        assertNull(saved.load())
        settings.putString("last_known_coordinates", """{"latitude":24.8607}""")
        assertNull(saved.load())
    }

    @Test
    fun aFreshFixIsSavedForNextTime() = runTest {
        val repository = DeviceLocationRepository(FakeLocationProvider(LocationAccess.Granted, fix = lahore), saved)
        assertEquals(lahore, repository.refreshCoordinates())
        assertEquals(lahore, repository.lastKnownCoordinates())
    }

    @Test
    fun withoutAccessTheDeviceIsntAsked() = runTest {
        val provider = FakeLocationProvider(LocationAccess.Denied)
        assertNull(DeviceLocationRepository(provider, saved).refreshCoordinates())
        assertEquals(0, provider.fixRequests)
    }

    @Test
    fun aFixThatDoesntArriveInTimeIsNoneAndStopsTheSearch() = runTest {
        val provider = FakeLocationProvider(LocationAccess.Granted).apply { holdFix = true }
        val repository = DeviceLocationRepository(provider, saved, fixTimeout = 10.seconds)
        assertNull(repository.refreshCoordinates())
        assertEquals(1, provider.cancelledRequests)
        assertNull(repository.lastKnownCoordinates())
    }

    @Test
    fun requestingAccessGivesTheAnswer() = runTest {
        val provider = FakeLocationProvider(answer = LocationAccess.Denied)
        val repository = DeviceLocationRepository(provider, saved)
        assertEquals(LocationAccess.Denied, repository.requestAccess())
        assertEquals(LocationAccess.Denied, repository.access)
    }

    @Test
    fun theReaderIsFoundWhereTheyWereThenWhereTheyAre() = runTest {
        saved.save(karachi)
        val repository = DeviceLocationRepository(FakeLocationProvider(LocationAccess.Granted, fix = lahore), saved)
        assertEquals(listOf(karachi, lahore), LocateReader(repository)().toList())
    }

    @Test
    fun withoutAccessOnlyTheSavedLocationIsUsed() = runTest {
        saved.save(karachi)
        val provider = FakeLocationProvider(LocationAccess.Denied)
        assertEquals(listOf(karachi), LocateReader(DeviceLocationRepository(provider, saved))().toList())
        assertEquals(emptyList<Coordinates>(), LocateReader(DeviceLocationRepository(provider, savedCoordinates()))().toList())
    }
}
