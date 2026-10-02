package com.muttaqi.android.feature.prayer.platform

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.view.Display
import android.view.Surface
import com.muttaqi.shared.feature.prayer.domain.model.CompassHeading
import com.muttaqi.shared.feature.prayer.domain.model.Coordinates
import com.muttaqi.shared.feature.prayer.domain.platform.HeadingProvider
import com.muttaqi.shared.feature.prayer.domain.platform.HeadingUpdates
import kotlin.math.abs

class RotationVectorHeadingProvider(
    private val context: Context,
    private val lastKnownCoordinates: () -> Coordinates?,
) : HeadingProvider {
    private val sensors = context.getSystemService(SensorManager::class.java)
    private val rotationVector: Sensor? = sensors?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    override val isAvailable: Boolean get() = rotationVector != null

    override fun startUpdates(onHeading: (CompassHeading) -> Unit): HeadingUpdates {
        val sensor = rotationVector ?: return NoUpdates
        val declination = lastKnownCoordinates()?.let {
            GeomagneticField(it.latitude.toFloat(), it.longitude.toFloat(), 0f, System.currentTimeMillis()).declination
        } ?: 0f
        val display = context.getSystemService(DisplayManager::class.java)?.getDisplay(Display.DEFAULT_DISPLAY)
        val listener = HeadingListener(declination, { display?.rotation ?: Surface.ROTATION_0 }, onHeading)
        sensors.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        return object : HeadingUpdates {
            override fun stop() = sensors.unregisterListener(listener)
        }
    }

    private object NoUpdates : HeadingUpdates {
        override fun stop() = Unit
    }
}

private class HeadingListener(
    private val declination: Float,
    private val displayRotation: () -> Int,
    private val onHeading: (CompassHeading) -> Unit,
) : SensorEventListener {
    private val rotation = FloatArray(9)
    private val remapped = FloatArray(9)
    private val orientation = FloatArray(3)
    private var status = SensorManager.SENSOR_STATUS_ACCURACY_HIGH
    private var reported: CompassHeading? = null

    override fun onSensorChanged(event: SensorEvent) {
        SensorManager.getRotationMatrixFromVector(rotation, event.values)
        val (x, y) = when (displayRotation()) {
            Surface.ROTATION_90 -> SensorManager.AXIS_Y to SensorManager.AXIS_MINUS_X
            Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X to SensorManager.AXIS_MINUS_Y
            Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Y to SensorManager.AXIS_X
            else -> SensorManager.AXIS_X to SensorManager.AXIS_Y
        }
        SensorManager.remapCoordinateSystem(rotation, x, y, remapped)
        SensorManager.getOrientation(remapped, orientation)
        val degrees = (Math.toDegrees(orientation[0].toDouble()) + declination).mod(360.0)
        val heading = CompassHeading(degrees, accuracy(event))
        val last = reported
        if (last != null && abs(turn(last.degrees, degrees)) < 1 && (last.accuracy < 0) == (heading.accuracy < 0)) return
        reported = heading
        onHeading(heading)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        status = accuracy
    }

    private fun accuracy(event: SensorEvent): Double = when {
        status == SensorManager.SENSOR_STATUS_UNRELIABLE -> -1.0
        event.values.size > 4 && event.values[4] >= 0 -> Math.toDegrees(event.values[4].toDouble())
        status == SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> 5.0
        status == SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> 15.0
        else -> 30.0
    }

    private fun turn(from: Double, to: Double): Double {
        val delta = (to - from).mod(360.0)
        return if (delta > 180) delta - 360 else delta
    }
}
