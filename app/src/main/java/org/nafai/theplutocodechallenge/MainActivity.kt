package org.nafai.theplutocodechallenge

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.nafai.theplutocodechallenge.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onResume() {
        super.onResume()
        MyApplication.dataCollector.setOnStatusChangeListener(::dataCollectorStatusChanged)
    }

    override fun onPause() {
        super.onPause()
        MyApplication.dataCollector.clearOnStatusChangeListener()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.dataCollectionButton.let { button ->
            button.setOnClickListener {
                if (MyApplication.dataCollector.isRunning) {
                    MyApplication.dataCollector.stop()
                }
                else {
                    MyApplication.dataCollector.start()
                }
            }
        }
    }

    private fun dataCollectorStatusChanged() {
        binding.dataCollectionButton.setText(if (MyApplication.dataCollector.isRunning) R.string.stop_data_collection else R.string.start_data_collection )
    }
}