package com.muttaqi.shared.feature.prayer

import com.muttaqi.shared.feature.prayer.presentation.qibla.shortestTurn
import kotlin.test.Test
import kotlin.test.assertEquals

class QiblaDialTest {
    @Test
    fun theShortestTurnIsNeverMoreThanHalfWayRound() {
        assertEquals(20.0, shortestTurn(from = 350.0, to = 10.0))
        assertEquals(-20.0, shortestTurn(from = 10.0, to = 350.0))
        assertEquals(-82.0, shortestTurn(from = 350.0, to = 268.0))
        assertEquals(180.0, shortestTurn(from = 0.0, to = 180.0))
        assertEquals(0.0, shortestTurn(from = 360.0, to = 720.0))
    }
}
