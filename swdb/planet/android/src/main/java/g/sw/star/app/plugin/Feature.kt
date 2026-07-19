package g.sw.star.app.plugin

import android.view.LayoutInflater
import android.view.ViewGroup

interface Feature {
    val name: String
    val icon: Int
    fun createView(inflater: LayoutInflater, container: ViewGroup): android.view.View
}
