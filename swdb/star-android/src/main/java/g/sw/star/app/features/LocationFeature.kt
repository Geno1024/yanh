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

class LocationFeature : Feature {
    override val name = "Locations"
    override val icon = android.R.drawable.ic_menu_myplaces
    private lateinit var client: StarClient

    override fun createView(inflater: LayoutInflater, container: ViewGroup): View {
        client = StarClient(container.context)
        val root = LinearLayout(container.context).apply { orientation = LinearLayout.VERTICAL }
        val listView = ListView(container.context)
        root.addView(listView)

        fun load(userId: Int = 1) {
            try {
                val json = client.call("Location", "list", mapOf("userId" to userId.toString()))
                val items = parseItems(json)
                listView.adapter = ArrayAdapter(container.context, android.R.layout.simple_list_item_1, items)
            } catch (_: Exception) {
                listView.adapter = ArrayAdapter(container.context, android.R.layout.simple_list_item_1,
                    listOf("Error"))
            }
        }

        root.addView(Button(container.context).apply {
            text = "+ Add (name,parentId,level,type)"
            setOnClickListener {
                val input = EditText(container.context).apply { hint = "name,pId,level,type" }
                android.app.AlertDialog.Builder(container.context)
                    .setTitle("Add Location")
                    .setView(input)
                    .setPositiveButton("Add") { _, _ ->
                        val p = input.text.toString().split(",").map { it.trim() }
                        client.call("Location", "add", mapOf(
                            "userId" to "1",
                            "name" to p.getOrElse(0) { "" },
                            "parentId" to p.getOrElse(1) { "0" },
                            "level" to p.getOrElse(2) { "0" },
                            "type" to p.getOrElse(3) { "" },
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
