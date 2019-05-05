/**
 * AirDataBridgeApplication - Java Class for Android
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

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import eu.basicairdata.bluetoothhelper.BluetoothHelper;


public class AirDataBridgeApplication extends Application {

    public static final short SD_STATUS_NOT_PRESENT = 0;
    public static final short SD_STATUS_EMPTY       = 1;
    public static final short SD_STATUS_PRESENT     = 2;
    public static final int   SYNC_NOSYNC           = 0;
    public static final int   SYNC_ANDROID_TIME     = 1;

    public static final short NOT_AVAILABLE         = -1;

    public static final String EMPTY_DTA_MESSAGE = "$DTA,,,,,,,,,,,,,,,,,,,,,,,,";

    private short SD_Status = SD_STATUS_NOT_PRESENT;

    Ringtone ringtone;
    File DLFile;
    PrintWriter KMLfw = null;
    BufferedWriter KMLbw = null;
    final String newLine = "\r\n";

    boolean StoragePermissionChecked = false;
    boolean StoragePermissionGranted = false;
    boolean BluetoothAutoReconnect = true;                                  // Auto reconnect if disconnected

    boolean StatusViewEnabled = false;                                      // If true, the status updates are enabled
    boolean ForceRemoteLST = true;                                          // If true, ask $FMQ,LST also if recording
    boolean DownloadDialogVisible = false;
    boolean DumpMode = false;
    private List<LogFile> LogfileList_Remote = Collections.synchronizedList(new ArrayList<LogFile>());
    private List<LogFile> LogfileList_Local = Collections.synchronizedList(new ArrayList<LogFile>());

    private String CurrentDTA = EMPTY_DTA_MESSAGE;                          // The last DTA received

    short BluetoothConnectionStatus = EventBusMSG.BLUETOOTH_NOT_PRESENT;    // The status of Bluetooth connection
    String BluetoothDeviceName = "HC-05";                                   // The name of BT device to connect
    String ADCName = "";                                                    // The name of the remote device
    String ADCFirmwareVersion = "";                                         // The firmware version of remote device
    LogFile CurrentRemoteLogFile = new LogFile();
    LogFile CurrentRemoteDownload = new LogFile();
    float SerialDTAFrequency    = NOT_AVAILABLE;                            // The frequency of Serial $DTA messages
    float BluetoothDTAFrequency = NOT_AVAILABLE;                            // The frequency of Bluetooth $DTA messages
    float SDCardDTAFrequency    = NOT_AVAILABLE;                            // The frequency of SDCard $DTA messages
    long DownloadedSize = 0L;

    private int     prefSyncDateTime = SYNC_ANDROID_TIME;
    private boolean prefNotifyDownloadFinished = false;                     // Audio notification at download end
    private boolean prefDeleteRemoteFileWhenDownloadFinished = false;       // Delete the remote file at download end
    private float   prefBTDataFrequency = 2.0f;                             // The Bluetooth Data Frequency
    private float   prefSDRecordingFrequency = 50.0f;                       // The Remote SD recording Frequency


    // GETTERS AND SETTERS -----------------------------------------------------------------


    public short getSD_Status() {
        return SD_Status;
    }
    public boolean isStoragePermissionChecked() {
        return StoragePermissionChecked;
    }
    public void setStoragePermissionChecked(boolean storagePermissionChecked) {
        StoragePermissionChecked = storagePermissionChecked;
    }

    public boolean isStoragePermissionGranted() {
        return StoragePermissionGranted;
    }

    public void setStoragePermissionGranted(boolean storagePermissionGranted) {
        StoragePermissionGranted = storagePermissionGranted;
    }

    public boolean isBluetoothAutoReconnect() {
        return BluetoothAutoReconnect;
    }

    public void setBluetoothAutoReconnect(boolean bluetoothAutoReconnect) {
        BluetoothAutoReconnect = bluetoothAutoReconnect;
    }

    public boolean isStatusViewEnabled() {
        return StatusViewEnabled;
    }
    public void setStatusViewEnabled(boolean statusViewEnabled) {
        StatusViewEnabled = statusViewEnabled;
    }
    public boolean isDownloadDialogVisible() {
        return DownloadDialogVisible;
    }
    public void setDownloadDialogVisible(boolean downloadDialogVisible) {
        DownloadDialogVisible = downloadDialogVisible;
    }
    public final List<LogFile> getLogfileList_Remote() {
        return LogfileList_Remote;
    }
    public final List<LogFile> getLogfileList_Local() {
        return LogfileList_Local;
    }
    public String getCurrentDTA() {
        return CurrentDTA;
    }
    public short getBluetoothConnectionStatus() {
        return BluetoothConnectionStatus;
    }
    public String getADCName() {
        return ADCName;
    }
    public String getADCFirmwareVersion() {
        return ADCFirmwareVersion;
    }
    public LogFile getCurrentRemoteDownload() {
        return CurrentRemoteDownload;
    }
    public void setCurrentRemoteDownload(LogFile currentRemoteDownload) {
        CurrentRemoteDownload = currentRemoteDownload;
    }
    public float getSDCardDTAFrequency() {
        return SDCardDTAFrequency;
    }
    public float getBluetoothDTAFrequency() {
        return BluetoothDTAFrequency;
    }
    public long getDownloadedSize() {
        return DownloadedSize;
    }
    public void setPrefNotifyDownloadFinished(boolean prefNotifyDownloadFinished) {
        this.prefNotifyDownloadFinished = prefNotifyDownloadFinished;
    }
    public void setPrefDeleteRemoteFileWhenDownloadFinished(boolean prefDeleteRemoteFileWhenDownloadFinished) {
        this.prefDeleteRemoteFileWhenDownloadFinished = prefDeleteRemoteFileWhenDownloadFinished;
    }
    public float getPrefSDRecordingFrequency() {
        return prefSDRecordingFrequency;
    }
    public float getPrefBTDataFrequency() {
        return prefBTDataFrequency;
    }
    // -------------------------------------------------------------------------------------

    public static final int COMMTIMEOUTHANDLERTIME = 1000;                  // Timer of 1 second
    final Handler CommTimeoutHandler = new Handler();                       // It runs when a communication timeout occurs
    Runnable CommTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            ADCName = "";
            ADCFirmwareVersion = "";
            isCommTimeoutHandler = false;
            mBluetooth.Disconnect();
            if (DumpMode) {
                DumpMode = false;
                DownloadDialogVisible = false;
                DownloadedSize = 0L;
                EventBus.getDefault().post(EventBusMSG.END_DOWNLOAD);
                DLFile.delete();
                updateLogFileList_Local();
            }
            BluetoothConnectionStatus = EventBusMSG.BLUETOOTH_DISCONNECTED;
            EventBus.getDefault().post(EventBusMSG.BLUETOOTH_DISCONNECTED);
        }
    };

    boolean isCommTimeoutHandler = false;                                   // True if the CommTimeoutHandler is active

    BluetoothHelper mBluetooth = new BluetoothHelper();
    BluetoothAdapter mBluetoothAdapter = null;

    // It receive the system notification about Bluetooth state changes
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        BluetoothConnectionStatus = EventBusMSG.BLUETOOTH_OFF;
                        EventBus.getDefault().post(EventBusMSG.BLUETOOTH_OFF);
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        BluetoothConnectionStatus = EventBusMSG.BLUETOOTH_OFF;
                        EventBus.getDefault().post(EventBusMSG.BLUETOOTH_OFF);
                        mBluetooth.Disconnect();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        BluetoothConnectionStatus = EventBusMSG.BLUETOOTH_CONNECTING;
                        EventBus.getDefault().post(EventBusMSG.BLUETOOTH_CONNECTING);
                        mBluetooth.Connect(BluetoothDeviceName);
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            }
        }
    };

    // Singleton instance
    private static AirDataBridgeApplication singleton;
    public static AirDataBridgeApplication getInstance(){
        return singleton;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;

        // work around the android.os.FileUriExposedException
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        File sd = new File(Environment.getExternalStorageDirectory() + "/AirDataBridge");   // Create the Directory if not exist
        if (!sd.exists()) {
            sd.mkdir();
            Log.w("myApp", "[#] GPSApplication.java - Folder created: " + sd.getAbsolutePath());
        }

        EventBus.getDefault().register(this);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();               // Check for BT adapter
        if (mBluetoothAdapter != null) {
            BluetoothConnectionStatus = EventBusMSG.BLUETOOTH_DISCONNECTED;
            // Device does not support Bluetooth
            // a BT adapter is found

            // Register the Broadcast Receiver for Bluetooth state changes
            final IntentFilter filter1 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mReceiver, filter1);

            // Setup listener for Bluetooth helper;
            mBluetooth.setBluetoothHelperListener(new BluetoothHelper.BluetoothHelperListener() {
                @Override
                public void onBluetoothHelperMessageReceived(BluetoothHelper bluetoothhelper,
                                                             final String message) {
                    Log.w("myApp", "[#] AirDataBridgeApplication.java - Message received: " + message);

                    if (BluetoothConnectionStatus == EventBusMSG.BLUETOOTH_HEARTBEAT_SYNC) {
                        // Process the normal messages

                        if (message.startsWith("$DTA,")) {   // ----------------------------------------------- $DTA
                            if (!DumpMode) {
                                String[] msgsplit = CurrentDTA.split(",", -1);
                                if (msgsplit.length == 25) {
                                    //Log.w("myApp", "[#] AirDataBridgeApplication.java - DTA UPDATED");
                                    stopCommTimeout();
                                    CurrentDTA = message;
                                    EventBus.getDefault().post(EventBusMSG.DTA_UPDATED);
                                }
                            } else {
                                String[] msgsplit = CurrentDTA.split(",", -1);
                                if (msgsplit.length == 25) {
                                    stopCommTimeout();
                                    long ds = DownloadedSize + message.length() + 2;
                                    if (Math.floor(ds) != Math.floor(DownloadedSize))
                                        EventBus.getDefault().post(EventBusMSG.UPDATE_DOWNLOAD_PROGRESS);
                                    DownloadedSize += message.length() + 2;
                                    try {
                                        KMLbw.write(message + newLine);
                                    } catch (IOException e) {

                                    }
                                    startCommTimeout();
                                }
                            }
                            return;
                        }

                        if (message.startsWith("$DFA,") && !DownloadDialogVisible) {   // --------------------- $DFA
                            StringTokenizer tokens = new StringTokenizer(message, ",");
                            if (tokens.countTokens() == 4) {
                                stopCommTimeout();
                                tokens.nextToken();                                         // Command $DFA
                                SerialDTAFrequency = Float.parseFloat(tokens.nextToken());  // Serial frequency
                                float newBTvalue = BluetoothDTAFrequency;
                                float newSDvalue = SDCardDTAFrequency;
                                String BTfreqvalue = tokens.nextToken();                      // Bluetooth Frequency
                                if (!BTfreqvalue.equals("="))
                                    newBTvalue = Float.parseFloat(BTfreqvalue);
                                BTfreqvalue = tokens.nextToken();                             // SDCard Frequency
                                if (!BTfreqvalue.equals("="))
                                    newSDvalue = Float.parseFloat(BTfreqvalue);


                                SDCardDTAFrequency = newSDvalue;
                                //Log.w("myApp", "[#] AirDataBridgeApplication.java - New SDCardDTAFrequency = " + SDCardDTAFrequency);
                                if ((SDCardDTAFrequency == 0) || (ForceRemoteLST)) {
                                    mBluetooth.SendMessage("$FMQ,LST");
                                    startCommTimeout();
                                    ForceRemoteLST = false;
                                } else {
                                    mBluetooth.SendMessage("$LCQ");
                                    startCommTimeout();
                                }


                                if (BluetoothDTAFrequency != newBTvalue) {
                                    BluetoothDTAFrequency = newBTvalue;
                                    if (BluetoothDTAFrequency == 0) {
                                        StatusViewEnabled = false;
                                        EventBus.getDefault().post(EventBusMSG.DISABLE_REALTIME_VIEW);
                                    } else {
                                        StatusViewEnabled = true;
                                        EventBus.getDefault().post(EventBusMSG.ENABLE_REALTIME_VIEW);
                                    }
                                }
                            }
                            return;
                        }

                        if (message.startsWith("$FMA,LST")) {   // ----------------------------------------------- $FMA,LST
                            stopCommTimeout();
                            if (message.equals("$FMA,LST")) {   // NO SD INSERTED
                                SD_Status = SD_STATUS_NOT_PRESENT;
                                synchronized (LogfileList_Remote) {
                                    LogfileList_Remote.clear();
                                }
                                EventBus.getDefault().post(EventBusMSG.REMOTE_UPDATE_LOGLIST);
                                Log.w("myApp", "[#] AirDataBridgeApplication.java -------------------------- ");
                                return;
                            }
                            StringTokenizer tokens = new StringTokenizer(message, ",");
                            tokens.nextToken();                                         // Command $FMA
                            tokens.nextToken();                                         // LST
                            int numberoffiles = Integer.parseInt(tokens.nextToken());   // Number of files
                            SD_Status = SD_STATUS_EMPTY;
                            if (numberoffiles == tokens.countTokens() / 3) {
                                LogFile lgf;
                                synchronized (LogfileList_Remote) {
                                    LogfileList_Remote.clear();
                                    for (int i = 0; i < numberoffiles; i++) {
                                        lgf = new LogFile(tokens.nextToken(), tokens.nextToken(), tokens.nextToken());
                                        //Log.w("myApp", "[#] AirDataBridgeApplication.java - Filename = " + lgf.Name + " - Filesize = " + lgf.lsize);
                                        if (lgf.Extension.equals("CSV"))
                                            LogfileList_Remote.add(lgf);
                                    }
                                    if (!LogfileList_Remote.isEmpty()) Collections.sort(LogfileList_Remote);
                                }
                                //Log.w("myApp", "[#] AirDataBridgeApplication.java - EventBusMSG.REMOTE_UPDATE_LOGLIST");
                                ForceRemoteLST = false;
                            }
                            mBluetooth.SendMessage("$LCQ");
                            startCommTimeout();
                            return;
                        }

                        if (message.startsWith("$FMA,NEW")) {   // ----------------------------------------------- $FMA,NEW
                            stopCommTimeout();
                            if (message.equals("$FMA,NEW")) {   // ERROR IN ADDING
                                EventBus.getDefault().post(EventBusMSG.ERROR_FILE_ALREADY_EXISTS);
                                mBluetooth.SendMessage("$DFQ");
                                startCommTimeout();
                                return;
                            }
                            StringTokenizer tokens = new StringTokenizer(message, ",");
                            if (tokens.countTokens() == 3) {
                                tokens.nextToken();                                         // Command $FMA
                                tokens.nextToken();                                         // NEW
                                String filenameext = tokens.nextToken();                    // File name.extension
                                LogFile lgf = new LogFile(filenameext, "0", "0");
                                synchronized (LogfileList_Remote) {
                                    LogfileList_Remote.add(0, lgf);
                                    //Log.w("myApp", "[#] AirDataBridgeApplication.java - File ADDED");
                                    if (SDCardDTAFrequency == 0)
                                        mBluetooth.SendMessage("$LCS," + filenameext);
                                    mBluetooth.SendMessage("$FMQ,PRP," + filenameext);
                                    startCommTimeout();
                                }
                            }
                            return;
                        }

                        if (message.startsWith("$FMA,DEL")) {   // ----------------------------------------------- $FMA,DEL
                            stopCommTimeout();
                            if (message.equals("$FMA,DEL")) {   // ERROR IN DELETING
                                mBluetooth.SendMessage("$DFQ");
                                startCommTimeout();
                                return;
                            }
                            StringTokenizer tokens = new StringTokenizer(message, ",");
                            if ((tokens.countTokens() == 3) && (!LogfileList_Remote.isEmpty())) {
                                tokens.nextToken();                                         // Command $FMA
                                tokens.nextToken();                                         // DEL
                                String filenameext = tokens.nextToken();                    // File name.extension
                                synchronized (LogfileList_Remote) {
                                    for (int i = 0; i <= LogfileList_Remote.size(); ) {
                                        //Log.w("myApp", "[#] AirDataBridgeApplication.java - Filename = "
                                        //        + LogfileList_Remote.get(i).Name + "." + LogfileList_Remote.get(i).Extension);
                                        if (filenameext.equals(LogfileList_Remote.get(i).Name + "." + LogfileList_Remote.get(i).Extension)) {
                                            LogfileList_Remote.remove(i);
                                            //Log.w("myApp", "[#] AirDataBridgeApplication.java - File REMOVED");
                                            break;
                                        } else i++;
                                    }
                                }
                                EventBus.getDefault().post(EventBusMSG.REMOTE_UPDATE_LOGLIST);
                            }
                            return;
                        }

                        if (message.startsWith("$FMA,PRP")) {   // ----------------------------------------------- $FMA,PRP
                            stopCommTimeout();
                            if (message.equals("$FMA,PRP")) {   // ERROR IN PROPERTIES
                                mBluetooth.SendMessage("$DFQ");
                                startCommTimeout();
                                return;
                            }
                            StringTokenizer tokens = new StringTokenizer(message, ",");
                            if ((tokens.countTokens() == 5) && (!LogfileList_Remote.isEmpty())) {
                                tokens.nextToken();                                         // Command $FMA
                                tokens.nextToken();                                         // PRP
                                String filenameext = tokens.nextToken();                    // File name.extension
                                String filesize = tokens.nextToken();                       // File size
                                String filetimestamp = tokens.nextToken();                  // File timestamp

                                synchronized (LogfileList_Remote) {
                                    for (LogFile lgf : LogfileList_Remote) {
                                        if (filenameext.equals(lgf.Name + "." + lgf.Extension)) {
                                            lgf.setSize(filesize);
                                            lgf.setDatetime(filetimestamp);
                                            break;
                                        }
                                    }
                                }
                                EventBus.getDefault().post(EventBusMSG.REMOTE_UPDATE_LOGLIST);
                                Log.w("myApp", "[#] AirDataBridgeApplication.java -------------------------- ");
                            }
                            return;
                        }

                        if (message.startsWith("$LCA,")) {   // ----------------------------------------------- $LCA
                            stopCommTimeout();
                            StringTokenizer tokens = new StringTokenizer(message, ",");
                            if ((tokens.countTokens() == 2) && (!LogfileList_Remote.isEmpty())) {
                                tokens.nextToken();                                         // Command $LCA
                                String filenameext = tokens.nextToken();                    // File name.extension
                                synchronized (LogfileList_Remote) {
                                    for (LogFile lgf : LogfileList_Remote) {
                                        if (filenameext.equals(lgf.Name + "." + lgf.Extension)) {
                                            lgf.Current = true;
                                            CurrentRemoteLogFile = lgf;
                                        } else lgf.Current = false;
                                    }
                                }
                                Log.w("myApp", "[#] AirDataBridgeApplication.java -------------------------- ");
                            }

                            // Time sync req
                            if ((SDCardDTAFrequency == 0) && (prefSyncDateTime != SYNC_NOSYNC)) {
                                mBluetooth.SendMessage("$TMQ");
                                startCommTimeout();
                            }

                            EventBus.getDefault().post(EventBusMSG.REMOTE_UPDATE_LOGLIST);
                            return;
                        }


                        if (message.startsWith("$TMA,")) {   // -------------------------------------------------- $TMA
                            StringTokenizer tokens = new StringTokenizer(message, ",");
                            if (tokens.countTokens() == 2) {
                                stopCommTimeout();
                                tokens.nextToken();                                         // Command $TMA
                                long localtime  = System.currentTimeMillis() / 1000L;       // The Android time
                                long remotetime = Long.parseLong(tokens.nextToken());       // The remote time

                                // a simple synchronization (with one second of resolution) is implemented for now
                                if (remotetime != localtime) {
                                    mBluetooth.SendMessage("$TMS," + localtime);
                                    startCommTimeout();
                                }
                            }
                            return;
                        }


                        if (message.startsWith("$FMA,DMP")) {   // ----------------------------------------------- $FMA,DMP
                            stopCommTimeout();
                            if (message.equals("$FMA,DMP")) {   // ERROR IN DUMP
                                mBluetooth.SendMessage("$DFQ");
                                startCommTimeout();
                                return;
                            }
                            DumpMode = true;
                            Log.w("myApp", "[#] AirDataBridgeApplication.java - START_DOWNLOAD: " + CurrentRemoteDownload.LocalName);

                            File sdCardRoot = Environment.getExternalStorageDirectory();
                            File yourDir = new File(sdCardRoot, "AirDataBridge");
                            if (!yourDir.exists()) yourDir.mkdir();
                            if (yourDir.exists()) {
                                DLFile = new File(yourDir, (CurrentRemoteDownload.LocalName + "." + CurrentRemoteDownload.Extension));
                                try {
                                    DLFile.createNewFile();
                                    KMLfw = new PrintWriter(DLFile);
                                    KMLbw = new BufferedWriter(KMLfw);
                                } catch (IOException e) {

                                }
                            }
                            EventBus.getDefault().post(EventBusMSG.START_DOWNLOAD);
                            return;
                        }


                        if (message.equals("$EOF")) {   // ----------------------------------------------- $EOF
                            //Log.w("myApp", "[#] AirDataBridgeApplication.java - DOWNLOAD ENDED: " + DownloadedSize + " / " + CurrentRemoteDownload.lsize);
                            stopCommTimeout();
                            try {
                                KMLbw.close();
                                KMLfw.close();
                            } catch (IOException e) {

                            }
                            if (DownloadedSize != CurrentRemoteDownload.lsize) {
                                DLFile.delete();
                            } else {
                                if (prefNotifyDownloadFinished) {
                                    // Download finished, play audio notification
                                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                    ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
                                    ringtone.play();
                                }
                                if (prefDeleteRemoteFileWhenDownloadFinished) {
                                    // Download finished, Delete the remote file
                                    mBluetooth.SendMessage("$FMQ,DEL," + CurrentRemoteDownload.Name + "." + CurrentRemoteDownload.Extension);
                                }
                            }

                            DumpMode = false;
                            DownloadDialogVisible = false;
                            DownloadedSize = 0L;

                            // Set the previous frequencies
                            mBluetooth.SendMessage("$DFS," + SerialDTAFrequency + "," + BluetoothDTAFrequency + "," + SDCardDTAFrequency);
                            startCommTimeout();

                            EventBus.getDefault().post(EventBusMSG.END_DOWNLOAD);
                            updateLogFileList_Local();
                            return;
                        }


                    }

                    if (message.equals("$HBA,ASGARD,0.5")) {
                        stopCommTimeout();
                        ADCName = "ASGARD";
                        DumpMode = false;
                        ADCFirmwareVersion = "0.5";
                        if (BluetoothConnectionStatus != EventBusMSG.BLUETOOTH_HEARTBEAT_SYNC) {
                            Log.w("myApp", "[#] AirDataBridgeApplication.java - BLUETOOTH_HEARTBEAT_SYNC");
                            BluetoothConnectionStatus = EventBusMSG.BLUETOOTH_HEARTBEAT_SYNC;
                            EventBus.getDefault().post(EventBusMSG.BLUETOOTH_HEARTBEAT_SYNC);
                        }
                    }
                }


                @Override
                public void onBluetoothHelperConnectionStateChanged(BluetoothHelper bluetoothhelper,
                                                                    boolean isConnected) {
                    if (isConnected) {
                        BluetoothConnectionStatus = EventBusMSG.BLUETOOTH_CONNECTED;
                        EventBus.getDefault().post(EventBusMSG.BLUETOOTH_CONNECTED);
                        mBluetooth.SendMessage("$HBQ,AirDataBridge," + BuildConfig.VERSION_NAME);
                        mBluetooth.SendMessage("$HBQ,AirDataBridge," + BuildConfig.VERSION_NAME);
                        startCommTimeout();
                        Log.w("myApp", "[#] AirDataBridgeApplication.java - BLUETOOTH_CONNECTED");
                        // Do something
                    } else {
                        //synchronized(LogfileList_Remote) {
                        //    LogfileList_Remote.clear();
                        //    EventBus.getDefault().post(EventBusMSG.REMOTE_UPDATE_LOGLIST);
                        //}
                        //Log.w("myApp", "[#] AirDataBridgeApplication.java - EventBusMSG.REMOTE_UPDATE_LOGLIST");
                        // Auto reconnect
                        if ((BluetoothConnectionStatus != EventBusMSG.BLUETOOTH_OFF) && (BluetoothAutoReconnect)) {
                            BluetoothConnectionStatus = EventBusMSG.BLUETOOTH_CONNECTING;
                            EventBus.getDefault().post(EventBusMSG.BLUETOOTH_CONNECTING);
                            mBluetooth.Connect(BluetoothDeviceName);
                        }
                        EventBus.getDefault().post(EventBusMSG.REMOTE_UPDATE_LOGLIST);
                    }
                }
            });

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            BluetoothDeviceName = preferences.getString("prefDeviceName", "HC-05");

            if (mBluetoothAdapter.isEnabled()) {
                BluetoothConnectionStatus = EventBusMSG.BLUETOOTH_CONNECTING;
                EventBus.getDefault().post(EventBusMSG.BLUETOOTH_CONNECTING);
                mBluetooth.Connect(BluetoothDeviceName);
            } else {
                BluetoothConnectionStatus = EventBusMSG.BLUETOOTH_OFF;
                EventBus.getDefault().post(EventBusMSG.BLUETOOTH_OFF);
            }
        } else {
            // Bluetooth adapter not present
            Log.w("myApp", "[#] AirDataBridgeApplication.java - BLUETOOTH NOT PRESENT");
        }
        LoadPreferences();
    }


    @Override
    public void onTerminate() {
        Log.w("myApp", "[#] AirDataBridgeApplication.java - onTerminate");
        mBluetooth.Disconnect();
        EventBus.getDefault().unregister(this);
        unregisterReceiver(mReceiver);                          // Unregister broadcast listeners
        super.onTerminate();
    }


    @Subscribe
    public void onEvent(Short msg) {
        switch (msg) {
            case EventBusMSG.START_APP:
                if (!BluetoothAutoReconnect) {
                    BluetoothAutoReconnect = true;
                    if (mBluetoothAdapter.isEnabled()) {
                        BluetoothConnectionStatus = EventBusMSG.BLUETOOTH_CONNECTING;
                        EventBus.getDefault().post(EventBusMSG.BLUETOOTH_CONNECTING);
                        mBluetooth.Connect(BluetoothDeviceName);
                    } else {
                        BluetoothConnectionStatus = EventBusMSG.BLUETOOTH_OFF;
                        EventBus.getDefault().post(EventBusMSG.BLUETOOTH_OFF);
                    }
                }
                break;
            case EventBusMSG.EXIT_APP:
                BluetoothAutoReconnect = false;
                mBluetooth.Disconnect();
                break;
            case EventBusMSG.STORAGE_PERMISSION_GRANTED:
                StoragePermissionGranted = true;
                // Create folder if not exists
                File sd = new File(Environment.getExternalStorageDirectory() + "/AirDataBridge");
                if (!sd.exists()) {
                    sd.mkdir();
                }
                AirDataBridgeApplication.getInstance().setStoragePermissionGranted(true);
                updateLogFileList_Local();
                break;
            case EventBusMSG.BLUETOOTH_HEARTBEAT_SYNC:
                mBluetooth.SendMessage("$DFQ");
                startCommTimeout();
                break;
            case EventBusMSG.BLUETOOTH_CONNECTING:
                EventBus.getDefault().post(EventBusMSG.REMOTE_UPDATE_LOGLIST);
                break;
            case EventBusMSG.REMOTE_REQUEST_SYNC:
                ForceRemoteLST = true;
                mBluetooth.SendMessage("$DFQ");
                startCommTimeout();
                break;
            case EventBusMSG.REMOTE_REQUEST_STOP_RECORDING:
                mBluetooth.SendMessage("$DFS,=,=,0");
                startCommTimeout();
                break;
            case EventBusMSG.LOCAL_REQUEST_SYNC:
                updateLogFileList_Local();
                break;
            case EventBusMSG.REQUEST_ENABLE_REALTIME_VIEW:
                //Log.w("myApp", "[#] AirDataBridgeApplication.java - REQUEST_ENABLE_REALTIME_VIEW");
                CurrentDTA = EMPTY_DTA_MESSAGE;
                mBluetooth.SendMessage("$DFS,=," + prefBTDataFrequency + ",=");
                startCommTimeout();
                break;
            case EventBusMSG.REQUEST_DISABLE_REALTIME_VIEW:
                //Log.w("myApp", "[#] AirDataBridgeApplication.java - REQUEST_DISABLE_REALTIME_VIEW");
                mBluetooth.SendMessage("$DFS,=,0,=");
                startCommTimeout();
                break;
            case EventBusMSG.REQUEST_START_DOWNLOAD:
                Log.w("myApp", "[#] AirDataBridgeApplication.java - REQUEST_START_DOWNLOAD");
                mBluetooth.SendMessage("$DFS,0,0,0");
                mBluetooth.SendMessage("$FMQ,DMP," + CurrentRemoteDownload.Name + "." + CurrentRemoteDownload.Extension);
                startCommTimeout();
                break;
            case EventBusMSG.REQUEST_STOP_DOWNLOAD:
                Log.w("myApp", "[#] AirDataBridgeApplication.java - REQUEST_STOP_DOWNLOAD");
                mBluetooth.SendMessage("$DFQ");
                startCommTimeout();
                break;
            case EventBusMSG.UPDATE_SETTINGS:
                Log.w("myApp", "[#] AirDataBridgeApplication.java - UPDATE SETTINGS");
                LoadPreferences();
                break;
        }
    }


    @Subscribe
    public void onEvent(final EventBusMSGLogFile msg) {
        switch (msg.MSGType) {
            // -------------------------------------------------------- REMOTE SECTION
            case EventBusMSG.REMOTE_REQUEST_START_RECORDING:
                mBluetooth.SendMessage("$LCS," + msg.logFile.Name + "." + msg.logFile.Extension);
                mBluetooth.SendMessage("$DFS,=,=," + prefSDRecordingFrequency);
                startCommTimeout();
                break;
            case EventBusMSG.REMOTE_FILE_DELETE:
                if (!isCommTimeoutHandler && mBluetooth.isConnected()) {
                    mBluetooth.SendMessage("$FMQ,DEL," + msg.logFile.Name + "." + msg.logFile.Extension);
                    startCommTimeout();
                }
                break;
            case EventBusMSG.REMOTE_FILE_NEW:
                if (!isCommTimeoutHandler && mBluetooth.isConnected()) {
                    String filenameext = msg.logFile.Name + "." + msg.logFile.Extension;
                    boolean existing = false;
                    synchronized(LogfileList_Remote) {
                        for (LogFile lgf : LogfileList_Remote) {
                            if (filenameext.equals(lgf.Name + "." + lgf.Extension)) {
                                existing = true;
                                EventBus.getDefault().post(EventBusMSG.ERROR_FILE_ALREADY_EXISTS);
                                break;
                            }
                        }
                    }
                    if (!existing) {
                        mBluetooth.SendMessage("$FMQ,NEW," + msg.logFile.Name + "." + msg.logFile.Extension);
                        startCommTimeout();
                    }
                }
                break;

            // -------------------------------------------------------- LOCAL SECTION

            case EventBusMSG.LOCAL_FILE_DELETE:
                File sd = new File(Environment.getExternalStorageDirectory() + "/AirDataBridge");
                if (!sd.exists()) {
                    sd.mkdir();
                }
                if (sd.exists()) {
                    Log.w("myApp", "[#] AirDataBridgeApplication.java - Deleting " + msg.logFile.LocalName + "." + msg.logFile.Extension);
                    File NEWfile = new File(sd, (msg.logFile.LocalName + "." + msg.logFile.Extension));
                    if (NEWfile.exists()) NEWfile.delete();
                }
                updateLogFileList_Local();
                break;

            case EventBusMSG.LOCAL_FILE_NEW:
                String filenameext = msg.logFile.LocalName + "." + msg.logFile.Extension;
                boolean existing = false;
                synchronized(LogfileList_Local) {
                    for (LogFile lgf : LogfileList_Local) {
                        if (filenameext.equals(lgf.LocalName + "." + lgf.Extension)) {
                            existing = true;
                            EventBus.getDefault().post(EventBusMSG.ERROR_FILE_ALREADY_EXISTS);
                            break;
                        }
                    }
                }
                if (!existing) {
                    sd = new File(Environment.getExternalStorageDirectory() + "/AirDataBridge");
                    if (!sd.exists()) {
                        sd.mkdir();
                    }
                    if (sd.exists()) {
                        File NEWfile = new File(sd, (msg.logFile.LocalName + "." + msg.logFile.Extension));
                        try {
                            NEWfile.createNewFile();
                        } catch (IOException e) {
                            Log.w("myApp", "[#] AirDataBridgeApplication.java - Unable to create " + msg.logFile.LocalName + "." + msg.logFile.Extension);
                        }
                    }
                    updateLogFileList_Local();
                }
                break;
        }
    }



    private void LoadPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        String oldBluetoothDeviceName = BluetoothDeviceName;
        BluetoothDeviceName = preferences.getString("prefDeviceName", "HC-05");
        if ((BluetoothConnectionStatus != EventBusMSG.BLUETOOTH_NOT_PRESENT) && (!oldBluetoothDeviceName.equals(BluetoothDeviceName))) {
            mBluetooth.Disconnect();
            mBluetooth.Connect(BluetoothDeviceName);
        }

        int oldprefSyncDateTime = prefSyncDateTime;
        prefSyncDateTime = Integer.valueOf(preferences.getString("prefSyncDatetime", "1"));
        if (oldprefSyncDateTime != prefSyncDateTime) {
            EventBus.getDefault().post(EventBusMSG.REMOTE_REQUEST_SYNC);
        }

        prefBTDataFrequency = Float.valueOf(preferences.getString("prefBTDataFrequency", "2"));
        if ((BluetoothConnectionStatus == EventBusMSG.BLUETOOTH_HEARTBEAT_SYNC) && (BluetoothDTAFrequency != 0) && (BluetoothDTAFrequency != prefBTDataFrequency))
            EventBus.getDefault().post(EventBusMSG.REQUEST_ENABLE_REALTIME_VIEW);  // If not local recording, set the new frequency (for realtime view)

        prefSDRecordingFrequency = Float.valueOf(preferences.getString("prefSDRecordingFrequency", "50"));

        //EventBus.getDefault().post(EventBusMSG.APPLY_SETTINGS);
    }


    void updateLogFileList_Local() {
        File sdCardRoot = Environment.getExternalStorageDirectory();
        File yourDir = new File(sdCardRoot, "AirDataBridge");
        synchronized(LogfileList_Local) {
            LogfileList_Local.clear();
            if (StoragePermissionGranted) {
                for (File f : yourDir.listFiles()) {
                    if (f.isFile()) {
                        LogFile lf = new LogFile(f.getName());
                        LogfileList_Local.add(lf);
                        //Log.w("myApp", "[#] AirDataBridgeApplication.java - " + f.getName() + " = " + lf.Name + " " + lf.Extension + " " + lf.Sizekb);
                    }
                }
            }
            if (!LogfileList_Local.isEmpty()) Collections.sort(LogfileList_Local);
        }
        EventBus.getDefault().post(EventBusMSG.LOCAL_UPDATE_LOGLIST);
    }

    void startCommTimeout() {
        isCommTimeoutHandler = true;
        CommTimeoutHandler.postDelayed(CommTimeoutRunnable, COMMTIMEOUTHANDLERTIME);    // starts the Communication timeout
    }

    void stopCommTimeout() {
        CommTimeoutHandler.removeCallbacks(CommTimeoutRunnable);                        // stops the Communication timeout
        isCommTimeoutHandler = false;
    }
}
