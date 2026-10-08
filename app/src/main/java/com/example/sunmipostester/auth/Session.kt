package com.example.sunmipostester.auth

import com.example.sunmipostester.data.StaffEntity
import com.example.sunmipostester.data.StaffRole

/**
 * The signed-in staff member for this app process.
 *
 * Deliberately in-memory only: closing the app forces a fresh sign-in, so an
 * unattended terminal cannot be reused by whoever picks it up next. No credential
 * material is kept here — only identity and role.
 */
object Session {

    /** Minimal identity of the signed-in user; never holds PIN data. */
    data class User(
        val id: Long,
        val name: String,
        val role: StaffRole
    )

    @Volatile
    var current: User? = null
        private set

    val isSignedIn: Boolean get() = current != null

    fun signIn(staff: StaffEntity) {
        current = User(id = staff.id, name = staff.name, role = staff.role)
    }

    fun signOut() {
        current = null
    }

    /** True when the signed-in user holds [permission]. False when signed out. */
    fun can(permission: Permission): Boolean {
        val role = current?.role ?: return false
        return AccessPolicy.allows(role, permission)
    }

    fun requireUser(): User =
        current ?: error("No signed-in user; the screen should have redirected to login")
}
