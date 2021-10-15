package com.joyy.nomral

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        findViewById<TextView>(R.id.tv).apply {
            append(getString(R.string.batch_live_uncoming))
            append(getString(R.string.NNN_name))
            append("\n")
            append(getString(R.string.test_string))
        }

        val hashMap = HashMap<String, String>()
        val keys: MutableSet<String> = hashMap.keys
        val values: MutableCollection<String> = hashMap.values
    }
}