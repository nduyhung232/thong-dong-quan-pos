package com.example.sunmipostester.data

/**
 * Pre-seeded default staff accounts matching pos-admin server:
 * - MinhQuan (PIN: 66668888)
 * - Dinh (PIN: 66668888)
 * - Loi (PIN: 66668888)
 */
object StaffSeed {

    fun defaultStaff(): List<StaffEntity> = listOf(
        StaffEntity(
            syncId = "00000000-0000-0000-0000-000000000001",
            name = "MinhQuan",
            role = StaffRole.MANAGER,
            pinHash = "uez3nFNbUfVDGHY3pZAtbblyFyBW7sarEoPytMc9Uto=",
            pinSalt = "sCUjGRuCsCg8aUcMVZGwYA=="
        ),
        StaffEntity(
            syncId = "00000000-0000-0000-0000-000000000002",
            name = "Dinh",
            role = StaffRole.MANAGER,
            pinHash = "Ad2TC2EwcWpjlcILLQj4P8VdiSwbHeRsdP02OihWQXU=",
            pinSalt = "TJPHYQnH46ubIGZYPmez4g=="
        ),
        StaffEntity(
            syncId = "00000000-0000-0000-0000-000000000003",
            name = "Loi",
            role = StaffRole.MANAGER,
            pinHash = "RBXYfYFNcbNdcX8H6bDGgTxhKcykHQUSENF473p/x60=",
            pinSalt = "TP0kx1KcnKxSpcOuRC+m2Q=="
        )
    )
}
