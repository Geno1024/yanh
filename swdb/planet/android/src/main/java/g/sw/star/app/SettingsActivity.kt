package g.sw.star.app

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import g.sw.star.app.plugin.FeatureManager

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        val toolbar = Toolbar(this)
        toolbar.title = "Settings"
        toolbar.setBackgroundColor(resources.getColor(android.R.color.darker_gray, null))
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        root.addView(toolbar)

        val serverRow = LinearLayout(this).apply {
            val edit = EditText(this@SettingsActivity).apply {
                id = View.generateViewId()
                setText((application as StarApp).serverUrl)
                layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
            }
            addView(edit)
            addView(Button(this@SettingsActivity).apply {
                text = "Save"
                setOnClickListener {
                    (application as StarApp).serverUrl = edit.text.toString()
                }
            })
        }
        root.addView(serverRow)

        val listView = ListView(this)
        root.addView(listView)

        val featureManager = FeatureManager(this)
        featureManager.loadPlugins()
        val features = featureManager.allFeatures()

        listView.adapter = object : ArrayAdapter<Any>(this, 0, features) {
            override fun getView(position: Int, convert: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = convert ?: layoutInflater.inflate(R.layout.item_feature_toggle, parent, false)
                val feature = features[position]
                view.findViewById<TextView>(android.R.id.text1).text = feature.name
                val toggle = view.findViewById<Switch>(R.id.feature_toggle)
                toggle.isChecked = featureManager.isEnabled(feature.name)
                toggle.setOnCheckedChangeListener { _, checked ->
                    featureManager.setEnabled(feature.name, checked)
                }
                return view
            }
        }

        setContentView(root)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
