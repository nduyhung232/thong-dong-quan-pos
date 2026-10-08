package com.example.sunmipostester.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sunmipostester.LoginActivity
import com.example.sunmipostester.R

/**
 * Base class for screens that require a signed-in user with a specific permission.
 *
 * Enforcement happens here rather than only by hiding menu buttons, so a screen
 * reached any other way (task restore, intent, restart after process death) still
 * fails closed instead of exposing data.
 */
abstract class SecuredActivity : AppCompatActivity() {

    /** The permission this screen requires. */
    abstract val requiredPermission: Permission

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!enforceAccess()) return
    }

    override fun onResume() {
        super.onResume()
        // Re-check on resume: the session may have been cleared while backgrounded.
        enforceAccess()
    }

    /**
     * @return true when access is granted. When denied, the user is redirected and
     *         callers should stop initialising the screen.
     */
    private fun enforceAccess(): Boolean {
        if (!Session.isSignedIn) {
            redirectToLogin()
            return false
        }
        if (!Session.can(requiredPermission)) {
            Toast.makeText(this, R.string.perm_denied, Toast.LENGTH_SHORT).show()
            finish()
            return false
        }
        return true
    }

    private fun redirectToLogin() {
        startActivity(
            Intent(this, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
        finish()
    }
}
