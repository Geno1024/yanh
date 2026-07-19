package g.sw.star.app.features

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import g.sw.star.app.plugin.Feature
import g.sw.star.app.network.StarClient

class ProductFeature : Feature {
    override val name = "Products"
    override val icon = android.R.drawable.ic_menu_gallery
    private lateinit var client: StarClient

    override fun createView(inflater: LayoutInflater, container: ViewGroup): View {
        client = StarClient(container.context)
        val root = LinearLayout(container.context).apply { orientation = LinearLayout.VERTICAL }

        val searchRow = LinearLayout(container.context).apply {
            addView(EditText(container.context).apply {
                id = View.generateViewId()
                hint = "Search keyword"
                layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
            })
            addView(Button(container.context).apply {
                text = "Search"
                setOnClickListener {
                    val kw = (parent as? LinearLayout)?.findViewById<EditText>(id)?.text.toString()
                    loadList(findViewById(android.R.id.list) ?: return@setOnClickListener, kw)
                }
            })
        }
        root.addView(searchRow)

        val listView = ListView(container.context).also { it.id = android.R.id.list }
        root.addView(listView)

        root.addView(Button(container.context).apply {
            text = "+ Add Product"
            setOnClickListener { showAddDialog(container) }
        })

        loadList(listView, "")
        return root
    }

    private fun loadList(listView: ListView, keyword: String) {
        try {
            val handler = if (keyword.isBlank()) "Product/list" else "Product/search"
            val args = if (keyword.isNotBlank()) mapOf("keyword" to keyword) else emptyMap()
            val json = client.call("Product", if (keyword.isBlank()) "list" else "search",
                if (keyword.isNotBlank()) mapOf("keyword" to keyword) else emptyMap())
            val items = parseItems(json)
            listView.adapter = ArrayAdapter(listView.context, android.R.layout.simple_list_item_1, items)
            listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                showDetailDialog(listView.context, items[position])
            }
        } catch (_: Exception) {
            listView.adapter = ArrayAdapter(listView.context, android.R.layout.simple_list_item_1,
                listOf("Error loading products"))
        }
    }

    private fun parseItems(json: String): List<String> {
        if (json.startsWith("[")) {
            return json.removeSurrounding("[", "]")
                .split("), ")
                .filter { it.isNotBlank() }
                .map { it.trimEnd(')') + ")" }
        }
        return listOf(json)
    }

    private fun showAddDialog(container: ViewGroup) {
        val context = container.context
        val dialog = android.app.AlertDialog.Builder(context)
        val input = EditText(context).apply { hint = "name,type,barcode,unit,expiryDays" }
        dialog.setTitle("Add Product")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val parts = input.text.toString().split(",").map { it.trim() }
                try {
                    client.call("Product", "add", mapOf(
                        "name" to parts.getOrElse(0) { "" },
                        "type" to parts.getOrElse(1) { "" },
                        "barcode" to parts.getOrElse(2) { "" },
                        "unit" to parts.getOrElse(3) { "" },
                        "expiryDays" to parts.getOrElse(4) { "0" },
                    ))
                } catch (_: Exception) {}
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDetailDialog(context: android.content.Context, item: String) {
        android.app.AlertDialog.Builder(context)
            .setTitle("Product Detail")
            .setMessage(item)
            .setPositiveButton("OK", null)
            .show()
    }
}
