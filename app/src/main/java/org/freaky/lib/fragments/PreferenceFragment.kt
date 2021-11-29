package org.freaky.lib.fragments

import android.os.Bundle
import com.libx.ui.preference.PreferenceFragmentCompat
import org.freaky.lib.R

class PreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.prefs)
    }
}
