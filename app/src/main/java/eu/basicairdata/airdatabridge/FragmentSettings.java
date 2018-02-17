/**
 * FragmentSettings - Java Class for Android
 * Created by G.Capelli (BasicAirData) on 3/2/2018
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.basicairdata.airdatabridge;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import android.view.View;
import android.widget.ListView;

import org.greenrobot.eventbus.EventBus;


public class FragmentSettings extends PreferenceFragment {

    SharedPreferences.OnSharedPreferenceChangeListener prefListener;

    private SharedPreferences prefs;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.app_preferences);

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals("prefBTDataFrequency")) {
                    float BTFreq = 2;
                    try {
                        float f = Float.parseFloat(sharedPreferences.getString("prefBTDataFrequency", "2"));
                        BTFreq = f;
                        if (BTFreq <= 0) BTFreq = 1;
                        if (BTFreq > 4) BTFreq = 4;
                    }
                    catch(NumberFormatException nfe)
                    {
                        BTFreq = 2;
                    }

                    SharedPreferences.Editor editor = prefs.edit();
                    if(BTFreq == (long) BTFreq) editor.putString("prefBTDataFrequency", String.valueOf((long)BTFreq));
                    else editor.putString("prefBTDataFrequency", String.valueOf(BTFreq));
                    editor.commit();
                }

                if (key.equals("prefSDRecordingFrequency")) {
                    float SDFreq = 50;
                    try {
                        float f = Float.parseFloat(sharedPreferences.getString("prefSDRecordingFrequency", "50"));
                        SDFreq = f;
                        if (SDFreq <= 0) SDFreq = 1;
                        if (SDFreq > 50) SDFreq = 50;
                    }
                    catch(NumberFormatException nfe)
                    {
                        SDFreq = 50;
                    }

                    SharedPreferences.Editor editor = prefs.edit();
                    if(SDFreq == (long) SDFreq) editor.putString("prefSDRecordingFrequency", String.valueOf((long)SDFreq));
                    else editor.putString("prefSDRecordingFrequency", String.valueOf(SDFreq));
                    editor.commit();
                }

                SetupPreferences();
            }
        };
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // remove dividers
        View rootView = getView();
        ListView list = (ListView) rootView.findViewById(android.R.id.list);
        list.setDivider(new ColorDrawable(Color.TRANSPARENT));
        list.setDividerHeight(0);

    }

    @Override
    public void onResume() {
        prefs.registerOnSharedPreferenceChangeListener(prefListener);
        //Log.w("myApp", "[#] FragmentSettings.java - onResume");
        //setDivider(new ColorDrawable(Color.TRANSPARENT));
        //setDividerHeight(0);
        SetupPreferences();
        super.onResume();
    }

    @Override
    public void onPause() {
        prefs.unregisterOnSharedPreferenceChangeListener(prefListener);
        //Log.w("myApp", "[#] FragmentSettings.java - onPause");
        EventBus.getDefault().post(EventBusMSG.UPDATE_SETTINGS);
        super.onPause();
    }

    public void SetupPreferences() {
        ListPreference prefSyncDatetime = (ListPreference) findPreference("prefSyncDatetime");
        prefSyncDatetime.setSummary(prefSyncDatetime.getEntry());

        EditTextPreference prefDeviceName = (EditTextPreference) findPreference("prefDeviceName");
        prefDeviceName.setSummary(prefs.getString("prefDeviceName", "HC-05"));

        EditTextPreference prefBTDataFrequency = (EditTextPreference) findPreference("prefBTDataFrequency");
        prefBTDataFrequency.setSummary(prefs.getString("prefBTDataFrequency", "2") + " " + getString(R.string.hz));

        EditTextPreference prefSDRecordingFrequency = (EditTextPreference) findPreference("prefSDRecordingFrequency");
        prefSDRecordingFrequency.setSummary(prefs.getString("prefSDRecordingFrequency", "50") + " " + getString(R.string.hz));
    }
}