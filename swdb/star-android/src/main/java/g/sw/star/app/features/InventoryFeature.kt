package g.sw.star.app.features

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import g.sw.star.app.network.StarClient
import g.sw.star.app.plugin.Feature

class InventoryFeature : Feature {
    override val name = "Inventory"
    override val icon = android.R.drawable.ic_menu_sort_by_size
    private lateinit var client: StarClient

    override fun createView(inflater: LayoutInflater, container: ViewGroup): View {
        client = StarClient(container.context)
        val root = LinearLayout(container.context).apply { orientation = LinearLayout.VERTICAL }
        val listView = ListView(container.context)
        root.addView(listView)

        fun load() {
            try {
                val json = client.call("Inventory", "list")
                val items = parseItems(json)
                listView.adapter = ArrayAdapter(container.context, android.R.layout.simple_list_item_1, items)
                listView.setOnItemClickListener { _, _, _, _ ->
                    android.app.AlertDialog.Builder(container.context)
                        .setMessage(items.joinToString("\n"))
                        .setPositiveButton("OK", null)
                        .show()
                }
            } catch (_: Exception) {
                listView.adapter = ArrayAdapter(container.context, android.R.layout.simple_list_item_1,
                    listOf("Error loading inventory"))
            }
        }

        root.addView(Button(container.context).apply {
            text = "+ Add (productId,locationId,qty)"
            setOnClickListener {
                val input = EditText(container.context).apply { hint = "productId,locationId,quantity" }
                android.app.AlertDialog.Builder(container.context)
                    .setTitle("Add Inventory")
                    .setView(input)
                    .setPositiveButton("Add") { _, _ ->
                        val p = input.text.toString().split(",").map { it.trim() }
                        client.call("Inventory", "add", mapOf(
                            "productId" to p.getOrElse(0) { "0" },
                            "locationId" to p.getOrElse(1) { "0" },
                            "quantity" to p.getOrElse(2) { "0" },
                        ))
                        load()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        })

        root.addView(Button(container.context).apply {
            text = "Adjust (id,delta)"
            setOnClickListener {
                val input = EditText(container.context).apply { hint = "id,delta" }
                android.app.AlertDialog.Builder(container.context)
                    .setTitle("Adjust Quantity")
                    .setView(input)
                    .setPositiveButton("Adjust") { _, _ ->
                        val p = input.text.toString().split(",").map { it.trim() }
                        client.call("Inventory", "adjust", mapOf(
                            "id" to p[0],
                            "delta" to p.getOrElse(1) { "0" },
                        ))
                        load()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        })

        load()
        return root
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
}
