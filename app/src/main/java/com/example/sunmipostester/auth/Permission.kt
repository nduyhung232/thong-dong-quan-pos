package com.example.sunmipostester.auth

import com.example.sunmipostester.data.StaffRole

/**
 * Actions that are access-controlled. Keeping them in one enum means the policy
 * lives in exactly one place and is auditable.
 */
enum class Permission {
    SELL,               // ring up orders, print KOT, take payment
    MANAGE_SHIFT,       // open / close a shift
    VIEW_REPORTS,       // revenue reports
    CANCEL_ORDER,       // void a paid order
    MANAGE_PRODUCTS,    // add / edit / stop selling items
    MANAGE_STAFF,       // create staff, change roles, reset PINs
    EXPORT_DATA,        // backup / export the database
    DEVICE_TEST,        // drawer / printer diagnostics
    MANAGE_SYNC         // configure server sync + run sync
}

/**
 * Role -> permission policy (single source of truth).
 *
 * Principle of least privilege: a cashier gets only what the job needs. Anything
 * touching money after the fact (cancelling), pricing, staff, exported data, or
 * aggregate financials is manager-only.
 */
object AccessPolicy {

    private val cashierPermissions = setOf(
        Permission.SELL,
        Permission.MANAGE_SHIFT,
        Permission.DEVICE_TEST
    )

    /** Managers hold every permission. */
    private val managerPermissions = Permission.values().toSet()

    fun permissionsOf(role: StaffRole): Set<Permission> = when (role) {
        StaffRole.CASHIER -> cashierPermissions
        StaffRole.MANAGER -> managerPermissions
    }

    fun allows(role: StaffRole, permission: Permission): Boolean =
        permission in permissionsOf(role)
}
