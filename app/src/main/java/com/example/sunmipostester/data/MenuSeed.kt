package com.example.sunmipostester.data

/**
 * SAMPLE starter menu, inserted only when the products table is empty (first run).
 * These are placeholder items and illustrative prices — the shop should edit them
 * in "Quản lý món" or replace this seed entirely.
 */
object MenuSeed {

    fun starterProducts(): List<ProductEntity> = listOf(
        ProductEntity(name = "Cà phê đen", price = 20000, category = "Cà phê"),
        ProductEntity(name = "Cà phê sữa", price = 25000, category = "Cà phê"),
        ProductEntity(name = "Bạc xỉu", price = 30000, category = "Cà phê"),

        ProductEntity(name = "Trà đào", price = 35000, category = "Trà"),
        ProductEntity(name = "Trà vải", price = 35000, category = "Trà"),
        ProductEntity(name = "Trà sen vàng", price = 40000, category = "Trà"),

        ProductEntity(name = "Trà sữa trân châu", price = 40000, category = "Trà sữa"),
        ProductEntity(name = "Trà sữa matcha", price = 45000, category = "Trà sữa"),

        ProductEntity(name = "Nước ép cam", price = 40000, category = "Nước ép"),
        ProductEntity(name = "Nước ép dưa hấu", price = 40000, category = "Nước ép"),

        ProductEntity(name = "Bánh flan", price = 15000, category = "Đồ ăn nhẹ"),
        ProductEntity(name = "Hướng dương", price = 10000, category = "Đồ ăn nhẹ"),
    )
}
