package com.example.sunmipostester

import android.app.Activity
import android.content.Intent
import android.os.Bundle

/** Routes the launcher to the pending crash report before entering the POS. */
class EntryActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val destination = if (CrashLogStore.read(this).isNullOrBlank()) {
            SaleActivity::class.java
        } else {
            CrashLogActivity::class.java
        }
        startActivity(Intent(this, destination))
        finish()
    }
}
