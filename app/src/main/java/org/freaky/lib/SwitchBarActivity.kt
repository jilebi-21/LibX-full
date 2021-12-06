package org.freaky.lib

import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.libx.ui.views.SwitchBar
import com.libx.ui.views.ToolbarLayout

class SwitchBarActivity : AppCompatActivity(), SwitchBar.OnSwitchChangeListener {

    val key: String = "zswitch_pref_nav"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_switchbar)

        val toolbarLayout = findViewById<ToolbarLayout>(R.id.toolbar_layout)
        toolbarLayout.switchBar.addOnSwitchChangeListener(this)
        toolbarLayout.switchBar.isChecked = defaultValue()
        toolbarLayout.switchBar.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return true
    }

    override fun onSwitchChanged(switchView: SwitchCompat?, isChecked: Boolean) {
        Settings.Secure.putInt(contentResolver, key, if (isChecked) 1 else 0)
    }

    fun defaultValue(): Boolean {
        return Settings.Secure.getInt(contentResolver, key, 0) != 0
    }
}