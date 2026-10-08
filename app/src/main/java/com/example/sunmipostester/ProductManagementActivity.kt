package com.example.sunmipostester

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.example.sunmipostester.auth.Permission
import com.example.sunmipostester.auth.SecuredActivity
import com.example.sunmipostester.data.PosRepository
import com.example.sunmipostester.data.ProductEntity
import com.example.sunmipostester.data.MoneyInput
import com.example.sunmipostester.databinding.ActivityProductManagementBinding
import com.example.sunmipostester.databinding.DialogEditProductBinding
import com.example.sunmipostester.manage.ManageProductAdapter
import kotlinx.coroutines.launch

/**
 * Product (menu) management: add, edit, stop/resume selling.
 *
 * Deletion is intentionally a soft-delete ("Ngừng bán"): historical orders keep a
 * snapshot of what was sold, and hard-deleting a product would make old orders
 * harder to explain during reconciliation.
 */
class ProductManagementActivity : SecuredActivity() {

    override val requiredPermission = Permission.MANAGE_PRODUCTS

    private lateinit var binding: ActivityProductManagementBinding
    private lateinit var repo: PosRepository
    private lateinit var adapter: ManageProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.prod_title)

        repo = PosRepository(this)

        adapter = ManageProductAdapter(
            onEdit = { product -> showProductDialog(product) },
            onToggleActive = { product -> toggleActive(product) }
        )
        binding.productList.adapter = adapter

        binding.btnAddProduct.setOnClickListener { showProductDialog(null) }

        loadProducts()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadProducts() {
        lifecycleScope.launch {
            val products = repo.allProducts()
            adapter.submit(products)
            binding.productEmpty.visibility =
                if (products.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    /**
     * Add/edit dialog. Passing null [existing] means "create new".
     */
    private fun showProductDialog(existing: ProductEntity?) {
        val dialogBinding = DialogEditProductBinding.inflate(layoutInflater)
        MoneyInput.attach(dialogBinding.inputPrice)

        existing?.let {
            dialogBinding.inputName.setText(it.name)
            dialogBinding.inputPrice.setText(it.price.toString())
            dialogBinding.inputCategory.setText(it.category)
        }

        AlertDialog.Builder(this)
            .setTitle(if (existing == null) R.string.prod_dialog_add else R.string.prod_dialog_edit)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.prod_save) { _, _ ->
                val name = dialogBinding.inputName.text?.toString()?.trim().orEmpty()
                val price = MoneyInput.parse(dialogBinding.inputPrice.text)
                val category = dialogBinding.inputCategory.text?.toString()?.trim().orEmpty()

                if (name.isEmpty() || category.isEmpty() || price == null || price < 0) {
                    toast(getString(R.string.prod_invalid))
                    return@setPositiveButton
                }
                saveProduct(existing, name, price, category)
            }
            .setNegativeButton(R.string.prod_cancel, null)
            .show()
    }

    private fun saveProduct(
        existing: ProductEntity?,
        name: String,
        price: Int,
        category: String
    ) {
        lifecycleScope.launch {
            if (existing == null) {
                repo.addProduct(name, price, category)
            } else {
                repo.updateProduct(
                    existing.copy(name = name, price = price, category = category)
                )
            }
            toast(getString(R.string.prod_saved))
            loadProducts()
        }
    }

    private fun toggleActive(product: ProductEntity) {
        lifecycleScope.launch {
            if (product.active) repo.deactivateProduct(product.id)
            else repo.reactivateProduct(product.id)
            loadProducts()
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
