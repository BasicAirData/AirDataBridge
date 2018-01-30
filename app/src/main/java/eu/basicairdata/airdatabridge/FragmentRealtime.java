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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.SimpleDateFormat;
import java.util.Locale;


public class FragmentRealtime extends Fragment {

    private RelativeLayout RLRealtimeContent;

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

    private ImageView IMGViewStatus;

    private final Bitmap[] bmpVisibility = {
            BitmapFactory.decodeResource(AirDataBridgeApplication.getInstance().getResources(), R.drawable.ic_visibility_off_black_24dp),
            BitmapFactory.decodeResource(AirDataBridgeApplication.getInstance().getResources(), R.drawable.ic_visibility_black_24dp)
    };

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

        RLRealtimeContent = (RelativeLayout) view.findViewById(R.id.id_realtime_content);
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

        IMGViewStatus = (ImageView) view.findViewById(R.id.id_imageview_status);

        IMGViewStatus.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {               // Toggle Real Time Data visibility
                if (isAdded()) {
                    ADBApplication.setStatusViewEnabled(!ADBApplication.isStatusViewEnabled());
                    if (ADBApplication.isStatusViewEnabled()) {
                        EventBus.getDefault().post(EventBusMSG.REQUEST_ENABLE_REALTIME_VIEW);
                    } else {
                        EventBus.getDefault().post(EventBusMSG.REQUEST_DISABLE_REALTIME_VIEW);
                    }
                }
            }
        });

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
                String[] CurrentDTA = ADBApplication.getCurrentDTA().split(",", -1);

                String stime = CurrentDTA[1];
                if (!stime.isEmpty()) {
                    long unixtime = Long.parseLong(stime);
                    String vv = new SimpleDateFormat("dd MMM yyyy - HH:mm:ss", Locale.ENGLISH).format(unixtime * 1000);
                    TVDatetime.setText(vv);
                }
                String sairspeed = CurrentDTA[13];
                if (!sairspeed.isEmpty()) {
                    float as = Float.parseFloat(sairspeed);
                    String vv = String.format(Locale.ENGLISH, "%.1f", (as * 3.6f));
                    TVAirspeed2.setText(vv);
                } else TVAirspeed2.setText("-");

                TVDifferentialPressure.setText(CurrentDTA[7].isEmpty() ? "-" : CurrentDTA[7] + "  " + getString(R.string.Pa));          // Differential Pressure
                TVAbsolutePressure.setText(CurrentDTA[8].isEmpty() ? "-" : CurrentDTA[8] + "  " + getString(R.string.Pa));              // Absolute Pressure
                TVTemperature.setText(CurrentDTA[9].isEmpty() ? "-" : CurrentDTA[9]);                   // External Temperature
                TVAirspeed.setText(CurrentDTA[13].isEmpty() ? "-" : CurrentDTA[13]);                     // TAS
                TVAltitude.setText(CurrentDTA[14].isEmpty() ? "-" : CurrentDTA[14]);                     // Altitude
                TVAirDensity.setText(CurrentDTA[21].isEmpty() ? "-" : CurrentDTA[21] + "  " + getString(R.string.kg_m3));                   // Air density
                TVAirViscosity.setText(CurrentDTA[22].isEmpty() ? "-" : CurrentDTA[22] + "  " + getString(R.string.Paxs));                 // Air viscosity

                if (ADBApplication.isStatusViewEnabled()) {
                    TVUpdates.setText(getResources().getString(R.string.realtime_update_at_hz,
                            String.valueOf(ADBApplication.getBluetoothDTAFrequency())));
                    IMGViewStatus.setImageBitmap(bmpVisibility[1]);
                    IMGViewStatus.setAlpha(255);
                } else {
                    TVUpdates.setText(R.string.realtime_disabled);
                    IMGViewStatus.setImageBitmap(bmpVisibility[0]);
                    IMGViewStatus.setAlpha(128);
                }
                TVRealtimeDisabled.setVisibility(View.INVISIBLE);
                RLRealtimeContent.setVisibility(View.VISIBLE);

            } else {
                TVRealtimeDisabled.setVisibility(View.VISIBLE);
                RLRealtimeContent.setVisibility(View.INVISIBLE);
            }
        }
    }
}