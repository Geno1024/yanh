package g.sw.star.app

import android.app.Application
import g.sw.star.app.features.InventoryFeature
import g.sw.star.app.features.LocationFeature
import g.sw.star.app.features.ProductFeature
import g.sw.star.app.features.StockMovementFeature
import g.sw.star.app.plugin.FeatureManager

class StarApp : Application() {
    lateinit var featureManager: FeatureManager
        private set
    var serverUrl: String = "http://10.0.2.2:8081"

    override fun onCreate() {
        super.onCreate()
        featureManager = FeatureManager(this)
        featureManager.registerBuiltin(ProductFeature())
        featureManager.registerBuiltin(LocationFeature())
        featureManager.registerBuiltin(InventoryFeature())
        featureManager.registerBuiltin(StockMovementFeature())
        featureManager.loadPlugins()
    }
}
