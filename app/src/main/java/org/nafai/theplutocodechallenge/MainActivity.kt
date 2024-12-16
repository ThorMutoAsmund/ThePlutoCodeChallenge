package org.nafai.theplutocodechallenge

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.nafai.theplutocodechallenge.databinding.ActivityMainBinding
import org.nafai.theplutocodechallenge.data.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onResume() {
        super.onResume()
        DataCollector.setOnStatusChangeListener(::dataCollectorStatusChanged)
    }

    override fun onPause() {
        super.onPause()
        DataCollector.clearOnStatusChangeListener()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.dataCollectionButton.let { button ->
            button.setOnClickListener {
                if (DataCollector.isRunning) {
                    DataCollector.stop()
                }
                else {
                    DataCollector.start(this.applicationContext)
                }
            }
        }
    }

    private fun dataCollectorStatusChanged() {
        binding.dataCollectionButton.setText(if (DataCollector.isRunning) R.string.stop_data_collection else R.string.start_data_collection )
    }
}