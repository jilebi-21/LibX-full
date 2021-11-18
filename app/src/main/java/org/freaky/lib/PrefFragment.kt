package org.freaky.lib

import android.os.Bundle
import com.libx.ui.preference.PreferenceFragmentCompat

class PrefFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.prefs)
    }
}
