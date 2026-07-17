package g.sw.star.app

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import g.sw.star.app.plugin.Feature
import g.sw.star.app.plugin.FeatureManager

class MainActivity : AppCompatActivity() {
    private lateinit var drawer: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var content: FrameLayout
    private lateinit var featureManager: FeatureManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawer = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        content = findViewById(R.id.content_frame)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        ActionBarDrawerToggle(this, drawer, toolbar, R.string.open_drawer, R.string.close_drawer)
            .also { drawer.addDrawerListener(it); it.syncState() }

        featureManager = FeatureManager(this)
        featureManager.loadPlugins()
        rebuildMenu()

        navigationView.setNavigationItemSelectedListener { item ->
            selectFeature(item)
            drawer.closeDrawers()
            true
        }
    }

    override fun onResume() {
        super.onResume()
        rebuildMenu()
    }

    private fun rebuildMenu() {
        val menu = navigationView.menu
        menu.clear()
        var groupId = 0
        for (feature in featureManager.getEnabledFeatures()) {
            menu.add(++groupId, groupId, Menu.NONE, feature.name)
                .setIcon(feature.icon)
            if (groupId == 1) {
                selectFeature(menu.getItem(0))
            }
        }
        menu.add(++groupId, groupId, Menu.NONE, "Settings")
            .setIcon(android.R.drawable.ic_menu_manage)
    }

    private fun selectFeature(item: MenuItem) {
        val features = featureManager.getEnabledFeatures()
        val index = item.itemId - 1
        if (index in features.indices) {
            showFeature(features[index])
        } else if (index == features.size) {
            startActivity(android.content.Intent(this, SettingsActivity::class.java))
        }
    }

    private fun showFeature(feature: Feature) {
        content.removeAllViews()
        val view = feature.createView(layoutInflater, content)
        content.addView(view)
        supportActionBar?.title = feature.name
    }
}
