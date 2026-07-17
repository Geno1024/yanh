package g.sw.star.app

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import g.sw.star.app.plugin.FeatureManager

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val featureManager = FeatureManager(this)
        featureManager.loadPlugins()

        val listView = findViewById<ListView>(R.id.feature_list)
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
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
