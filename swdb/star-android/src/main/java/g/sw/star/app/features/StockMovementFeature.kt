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

class StockMovementFeature : Feature {
    override val name = "Movements"
    override val icon = android.R.drawable.ic_menu_recent_history
    private lateinit var client: StarClient

    override fun createView(inflater: LayoutInflater, container: ViewGroup): View {
        client = StarClient(container.context)
        val root = LinearLayout(container.context).apply { orientation = LinearLayout.VERTICAL }
        val listView = ListView(container.context)
        root.addView(listView)

        fun load() {
            try {
                val json = client.call("StockMovement", "listAll")
                val items = parseItems(json)
                listView.adapter = ArrayAdapter(container.context, android.R.layout.simple_list_item_1, items)
            } catch (_: Exception) {
                listView.adapter = ArrayAdapter(container.context, android.R.layout.simple_list_item_1,
                    listOf("Error loading movements"))
            }
        }

        root.addView(Button(container.context).apply {
            text = "+ Record (productId,inventoryId,delta,type,userId,note)"
            setOnClickListener {
                val input = EditText(container.context).apply {
                    hint = "pid,iid,delta,type,uid,note"
                }
                android.app.AlertDialog.Builder(container.context)
                    .setTitle("Record Movement")
                    .setView(input)
                    .setPositiveButton("Add") { _, _ ->
                        val p = input.text.toString().split(",").map { it.trim() }
                        val args = mutableMapOf(
                            "productId" to p.getOrElse(0) { "0" },
                            "inventoryId" to p.getOrElse(1) { "0" },
                            "delta" to p.getOrElse(2) { "0" },
                            "type" to p.getOrElse(3) { "" },
                        )
                        if (p.size > 4) args["userId"] = p[4]
                        if (p.size > 5) args["note"] = p[5]
                        client.call("StockMovement", "add", args)
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
