/**
 * AirDataBridgeActivity - Java Class for Android
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

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AirDataBridgeActivity extends AppCompatActivity {

    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

    private ViewPager mViewPager;
    private TextView TVStatus;

    private boolean prefKeepScreenOn = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_airdatabridge);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setOffscreenPageLimit(2);
        setupViewPager(mViewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.id_tablayout);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setupWithViewPager(mViewPager);

        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */

        TVStatus = (TextView) findViewById(R.id.id_textviewstatus);

        // Check for Location runtime Permissions (for Android 23+)
        if (!AirDataBridgeApplication.getInstance().isStoragePermissionChecked()) {
            CheckStoragePermission();
            AirDataBridgeApplication.getInstance().setStoragePermissionChecked(true);
        }
    }



    @Override
    public void onResume() {
        EventBus.getDefault().register(this);
        //Log.w("myApp", "[#] AirDataBridgeActivity.java - onResume()");
        EventBus.getDefault().post(EventBusMSG.APP_RESUME);
        LoadPreferences();
        Update();
        super.onResume();
    }



    @Override
    public void onPause() {
        EventBus.getDefault().post(EventBusMSG.APP_PAUSE);
        Log.w("myApp", "[#] AirDataBridgeActivity.java - onPause()");
        EventBus.getDefault().unregister(this);
        super.onPause();
    }



    @Override
    protected void onDestroy(){
        super.onDestroy();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        if (mViewPager.getCurrentItem() == 1) {                     // REMOTE tab active
            switch (item.getItemId()) {
                case R.id.action_new_logfile:
                    // Show FileName Dialog
                    FragmentManager fm = getSupportFragmentManager();
                    FragmentNewFileDialog_Remote filenameDialog = new FragmentNewFileDialog_Remote();
                    filenameDialog.show(fm, "");
                    return true;
                case R.id.action_sync:
                    // Force sync the remote
                    EventBus.getDefault().post(EventBusMSG.REMOTE_REQUEST_SYNC);
                    return true;
            }
        }
        if (mViewPager.getCurrentItem() == 2) {                     // DOWNLOADS tab active
            switch (item.getItemId()) {
                case R.id.action_new_logfile:
                    // Show FileName Dialog
                    FragmentManager fm = getSupportFragmentManager();
                    FragmentNewFileDialog_Local filenameDialog = new FragmentNewFileDialog_Local();
                    filenameDialog.show(fm, "");
                    return true;
                case R.id.action_sync:
                    // Force sync the remote
                    EventBus.getDefault().post(EventBusMSG.LOCAL_REQUEST_SYNC);
                    return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }



    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new FragmentRealtime(), getString(R.string.tab_adcstatus));
        adapter.addFragment(new FragmentLogList_Remote(), getString(R.string.tab_remote_loglist));
        adapter.addFragment(new FragmentLogList_Local(), getString(R.string.tab_local_loglist));
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);
    }



    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }



    public boolean CheckStoragePermission() {
        Log.w("myApp", "[#] AirDataBridgeActivity.java - Check Storage Permission...");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.w("myApp", "[#] AirDataBridgeActivity.java - Storage Permission granted");
            EventBus.getDefault().post(EventBusMSG.STORAGE_PERMISSION_GRANTED);
            return true;    // Permission Granted
        } else {
            Log.w("myApp", "[#] AirDataBridgeActivity.java - Storage Permission denied");
            AirDataBridgeApplication.getInstance().setStoragePermissionGranted(false);
            boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (showRationale || !AirDataBridgeApplication.getInstance().isStoragePermissionChecked()) {
                Log.w("myApp", "[#] AirDataBridgeActivity.java - Storage Permission denied, need new check");
                List<String> listPermissionsNeeded = new ArrayList<>();
                listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]) , REQUEST_ID_MULTIPLE_PERMISSIONS);
            }
            return false;
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<>();

                if (grantResults.length > 0) {
                    // Fill with actual results from user
                    for (int i = 0; i < permissions.length; i++) perms.put(permissions[i], grantResults[i]);
                    // Check for permissions

                    if (perms.containsKey(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                            Log.w("myApp", "[#] AirDataBridgeActivity.java - WRITE_EXTERNAL_STORAGE = PERMISSION_GRANTED");
                            EventBus.getDefault().post(EventBusMSG.STORAGE_PERMISSION_GRANTED);
                        }
                        //AirDataBridgeApplication.getInstance().setStoragePermissionChecked(true);
                    }
                }
                break;
            }
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }



    @Subscribe
    public void onEvent(Short msg) {
        switch (msg) {
            case  EventBusMSG.BLUETOOTH_NOT_PRESENT:
            case  EventBusMSG.BLUETOOTH_OFF:
            case  EventBusMSG.BLUETOOTH_DISCONNECTED:
            case  EventBusMSG.BLUETOOTH_CONNECTING:
            case  EventBusMSG.BLUETOOTH_CONNECTED:
            case  EventBusMSG.BLUETOOTH_HEARTBEAT_SYNC:
            case  EventBusMSG.BLUETOOTH_HEARTBEAT_OUTOFSYNC:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Update();
                    }
                });
                break;
        }
    }


    private void LoadPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefKeepScreenOn = preferences.getBoolean("prefKeepScreenOn", true);
        if (prefKeepScreenOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            //Log.w("myApp", "[#] AirDataBridgeActivity.java - addFlags FLAG_KEEP_SCREEN_ON");
        }
        else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            //Log.w("myApp", "[#] AirDataBridgeActivity.java - clearFlags FLAG_KEEP_SCREEN_ON");
        }
    }


    void Update() {
        switch (AirDataBridgeApplication.getInstance().getBluetoothConnectionStatus()) {
            case EventBusMSG.BLUETOOTH_NOT_PRESENT:
                TVStatus.setText(R.string.status_bt_not_present);
                //TVStatus.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorInactiveGray));
                break;
            case EventBusMSG.BLUETOOTH_OFF:
                TVStatus.setText(R.string.status_bt_off);
                //TVStatus.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorInactiveGray));
                break;
            case  EventBusMSG.BLUETOOTH_DISCONNECTED:
                TVStatus.setText(R.string.status_bt_disconnected);
                //TVStatus.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorInactiveGray));
                break;
            case  EventBusMSG.BLUETOOTH_CONNECTING:
                TVStatus.setText(R.string.status_bt_connecting);
                //TVStatus.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorInactiveGray));
                break;
            case  EventBusMSG.BLUETOOTH_CONNECTED:
            case  EventBusMSG.BLUETOOTH_HEARTBEAT_OUTOFSYNC:
                TVStatus.setText(R.string.status_bt_connected);
                //TVStatus.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorInactiveGray));
                break;
            case  EventBusMSG.BLUETOOTH_HEARTBEAT_SYNC:
                TVStatus.setText(getResources().getString(R.string.status_bt_connected_with,
                        AirDataBridgeApplication.getInstance().getADCName(), AirDataBridgeApplication.getInstance().getADCFirmwareVersion()));
                //TVStatus.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                break;
        }
    }
}
