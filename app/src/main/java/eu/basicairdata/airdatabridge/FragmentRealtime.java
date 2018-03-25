/**
 * FragmentRealtime - Java Class for Android
 * Created by G.Capelli (BasicAirData) on 14/5/2017
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
 *
 */

package eu.basicairdata.airdatabridge;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.SimpleDateFormat;
import java.util.Locale;


public class FragmentRealtime extends Fragment {

    private TextView TVAirspeed;
    private TextView TVAirspeed2;
    private TextView TVTemperature;
    private TextView TVAltitude;

    private TextView TVDatetime;
    private TextView TVTimesync;
    private TextView TVAbsolutePressure;
    private TextView TVDifferentialPressure;
    private TextView TVAirDensity;
    private TextView TVAirViscosity;
    private TextView TVUpdates;
    private TextView TVRealtimeDisabled;


    final AirDataBridgeApplication ADBApplication = AirDataBridgeApplication.getInstance();

    public FragmentRealtime() {
        // Required empty public constructor
    }

    @Subscribe
    public void onEvent(Short msg) {
        switch (msg) {
            case EventBusMSG.DISABLE_REALTIME_VIEW:
            case EventBusMSG.ENABLE_REALTIME_VIEW:
            case EventBusMSG.DTA_UPDATED:
            case EventBusMSG.BLUETOOTH_CONNECTED:
            case EventBusMSG.BLUETOOTH_OFF:
            case EventBusMSG.BLUETOOTH_CONNECTING:
            case EventBusMSG.BLUETOOTH_HEARTBEAT_SYNC:
                (getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Update();
                    }
                });
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_realtime, container, false);

        TVRealtimeDisabled = (TextView) view.findViewById(R.id.id_textView_realtimedisabled);
        TVAirspeed = (TextView) view.findViewById(R.id.id_textView_airspeed);
        TVAirspeed2 = (TextView) view.findViewById(R.id.id_textView_airspeed2);
        TVTemperature = (TextView) view.findViewById(R.id.id_textView_temperature);
        TVAltitude = (TextView) view.findViewById(R.id.id_textView_altitude);
        TVDatetime = (TextView) view.findViewById(R.id.id_textView_datetime);
        TVTimesync = (TextView) view.findViewById(R.id.id_textView_timesync);
        TVAbsolutePressure = (TextView) view.findViewById(R.id.id_textView_absolute_pressure);
        TVDifferentialPressure  = (TextView) view.findViewById(R.id.id_textView_differential_pressure);
        TVAirDensity = (TextView) view.findViewById(R.id.id_textView_air_density);
        TVAirViscosity = (TextView) view.findViewById(R.id.id_textView_dynamic_air_viscosity);

        TVUpdates = (TextView) view.findViewById(R.id.id_textView_status_hz);

        return view;
    }

    @Override
    public void onResume() {
        //Log.w("myApp", "[#] FragmentGPSFix: onResume() - " + FLatitude);
        EventBus.getDefault().register(this);
        Update();
        super.onResume();
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        //Log.w("myApp", "[#] FragmentGPSFix: onPause()");
        super.onPause();
    }

    public void Update() {
        if (isAdded()) {
            if (ADBApplication.getBluetoothConnectionStatus() == EventBusMSG.BLUETOOTH_HEARTBEAT_SYNC) {
                String[] CurrentDTA = ADBApplication.isStatusViewEnabled() ?
                        ADBApplication.getCurrentDTA().split(",", -1):
                        AirDataBridgeApplication.EMPTY_DTA_MESSAGE.split(",", -1);

                // Time
                String stime = CurrentDTA[1];
                if (!stime.isEmpty()) {
                    long localtime = System.currentTimeMillis() / 1000L;       // The Android time
                    long remotetime  = Long.parseLong(stime);                  // The DTA timestasmp
                    String vv = new SimpleDateFormat("dd MMM yyyy - HH:mm:ss", Locale.ENGLISH).format(remotetime * 1000);
                    TVDatetime.setText(vv);

                    // a simple synchronization (with one second of resolution) is implemented for now
                    if (Math.abs(localtime - remotetime) <= 1) {
                        TVTimesync.setText(getString(R.string.sync_android_time));
                    } else {
                        TVTimesync.setText(getString(R.string.sync_no_sync));
                    }
                } else {
                    TVDatetime.setText("-");
                    TVTimesync.setText("-");
                }

                String sairspeed = CurrentDTA[13];
                if (!sairspeed.isEmpty()) {
                    float as = Float.parseFloat(sairspeed);
                    String vv = String.format(Locale.ENGLISH, "%.1f", (as * 3.6f));
                    TVAirspeed2.setText(vv);
                } else TVAirspeed2.setText("-");

                // Differential Pressure
                TVDifferentialPressure.setText(CurrentDTA[7].isEmpty() ? "-" : CurrentDTA[7] + "  " + getString(R.string.Pa));

                // Absolute Pressure
                TVAbsolutePressure.setText(CurrentDTA[8].isEmpty() ? "-" : CurrentDTA[8] + "  " + getString(R.string.Pa));

                // External Temperature
                float temp;
                try {
                    float f = Float.parseFloat(CurrentDTA[9]);
                    temp = f;
                }
                catch(NumberFormatException nfe)
                {
                    temp = 0;
                }
                TVTemperature.setText(CurrentDTA[9].isEmpty() ? "-" : String.format(Locale.ENGLISH, "%.0f", (temp - 273.15)));

                // TAS
                TVAirspeed.setText(CurrentDTA[13].isEmpty() ? "-" : CurrentDTA[13]);

                // Altitude
                float alt;
                try {
                    float f = Float.parseFloat(CurrentDTA[14]);
                    alt = f;
                }
                catch(NumberFormatException nfe)
                {
                    alt = 0;
                }
                TVAltitude.setText(CurrentDTA[14].isEmpty() ? "-" : String.format(Locale.ENGLISH, "%.1f", (alt)));

                // Air density
                TVAirDensity.setText(CurrentDTA[21].isEmpty() ? "-" : CurrentDTA[21] + "  " + getString(R.string.kg_m3));

                // Air viscosity
                TVAirViscosity.setText(CurrentDTA[22].isEmpty() ? "-" : CurrentDTA[22] + "  " + getString(R.string.Paxs));


                if (ADBApplication.isStatusViewEnabled()) {
                    float BTFreq = ADBApplication.getBluetoothDTAFrequency();
                    if(BTFreq == (long) BTFreq)
                        TVUpdates.setText(getResources().getString(R.string.realtime_update_at_hz, String.valueOf((long)BTFreq)));
                    else
                        TVUpdates.setText(getResources().getString(R.string.realtime_update_at_hz, String.valueOf(BTFreq)));
                } else {
                    TVUpdates.setText(R.string.realtime_disabled);
                }
                TVRealtimeDisabled.setVisibility(View.INVISIBLE);

            } else {
                TVRealtimeDisabled.setVisibility(View.VISIBLE);
            }
        }
    }
}