import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.artflow.AccelerometerSensorListener
import com.example.artflow.GyroscopeSensorListener
import com.example.artflow.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        if (accelerometer != null) {
            val accelerometerSensorListener = AccelerometerSensorListener()
            accelerometerSensorListener.setSensorManager(sensorManager)
            sensorManager.registerListener(accelerometerSensorListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        }

        if (gyroscope != null) {
            val gyroscopeSensorListener = GyroscopeSensorListener()
            gyroscopeSensorListener.setSensorManager(sensorManager)
            sensorManager.registerListener(gyroscopeSensorListener, gyroscope, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }
}