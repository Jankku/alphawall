package com.jankku.alphawall.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.jankku.alphawall.BuildConfig
import com.jankku.alphawall.R

class SettingsFragment : PreferenceFragmentCompat() {

    private var versionPref: Preference? = null
    private var githubPref: Preference? = null
    private var apiPref: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        versionPref = findPreference(getString(R.string.app_key))
        versionPref?.summary = BuildConfig.VERSION_NAME

        githubPref = findPreference(getString(R.string.github_key))
        githubPref?.onPreferenceClickListener = githubListener

        apiPref = findPreference(getString(R.string.api_key))
        apiPref?.onPreferenceClickListener = apiListener
    }

    private val githubListener = Preference.OnPreferenceClickListener {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubPref?.summary as String?))
        startActivity(intent)
        true
    }

    private val apiListener = Preference.OnPreferenceClickListener {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(apiPref?.summary as String?))
        startActivity(intent)
        true
    }
}