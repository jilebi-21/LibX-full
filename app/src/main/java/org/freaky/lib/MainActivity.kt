package org.freaky.lib

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.libx.ui.views.ToolbarLayout

class MainActivity : AppCompatActivity() {

    private val tabTitles = arrayOf("Sample UI Elements", "Preferences")
    private val tabSubTitles = arrayOf("All the modified UI elements", "All available Preferences")
    private var toolbarLayout: ToolbarLayout? = null
    private var bottomNav: BottomNavigationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbarLayout = findViewById(R.id.toolbar_layout)
        val toolbar = toolbarLayout?.toolbar
        setSupportActionBar(toolbar)

        bottomNav = findViewById(R.id.bottom_nav)

        val navController = findNavController(R.id.frag_container_view)
        bottomNav?.setupWithNavController(navController)

        //Just to override navigation component reselection
        bottomNav?.setOnItemReselectedListener { }

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.tab_views -> {
                    toolbarLayout?.title = tabTitles[0]
                    toolbarLayout?.subtitle = tabSubTitles[0]
                }
                R.id.tab_preferences -> {
                    toolbarLayout?.title = tabTitles[1]
                    toolbarLayout?.subtitle = tabSubTitles[1]
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
}