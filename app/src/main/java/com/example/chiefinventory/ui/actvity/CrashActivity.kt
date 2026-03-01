package com.example.chiefinventory.ui.actvity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.chiefinventory.databinding.ActivityCrashBinding

class CrashActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCrashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val crashInfo = intent.getStringExtra(EXTRA_CRASH_INFO)
        binding.tvErrorDetails.text = crashInfo

        binding.btnRestart.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }
    }

    companion object {
        const val EXTRA_CRASH_INFO: String = "EXTRA_CRASH_INFO"
    }
}
