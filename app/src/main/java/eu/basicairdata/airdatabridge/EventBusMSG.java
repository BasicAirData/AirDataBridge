/**
 * EventBusMSG - Java Class for Android
 * Created by G.Capelli (BasicAirData) on 05/08/17.
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

public class EventBusMSG {

    static final short APP_RESUME                       =   1;  // Sent to components on app resume
    static final short APP_PAUSE                        =   2;  // Sent to components on app pause

    static final short BLUETOOTH_NOT_PRESENT            =   3;  // Unable to find a Bluetooth adapter
    static final short BLUETOOTH_OFF                    =   4;  // The Bluetooth is turned off
    static final short BLUETOOTH_DISCONNECTED           =   5;  // The ADC is disconnected
    static final short BLUETOOTH_CONNECTING             =   6;  // Trying to connect to ADC via Bluetooth
    static final short BLUETOOTH_CONNECTED              =   7;  // The ADC is connected via Bluetooth
    static final short BLUETOOTH_HEARTBEAT_SYNC         =   8;  // The ADC is responding
    static final short BLUETOOTH_HEARTBEAT_OUTOFSYNC    =   9;  // The ADC is out of sync

    static final short REMOTE_UPDATE_LOGLIST            =  11;  // Update loglist signal
    static final short REMOTE_REQUEST_SYNC              =  12;  // A synchronization is requested

    static final short REMOTE_LOGLIST_SELECTION         =  20;  // A logfile is selected on remote loglist

    static final short REMOTE_FILE_DELETE               =  31;  // Delete a remote file
    static final short REMOTE_FILE_NEW                  =  32;  // Add a new file           ($FMQ,NEW)
    static final short REMOTE_REQUEST_START_RECORDING   =  33;  // Start to record on ADC   ($DFS,=,=,50)
    static final short REMOTE_REQUEST_STOP_RECORDING    =  34;  // Stop to record on ADC    ($DFS,=,=,0)

    static final short REQUEST_DISABLE_REALTIME_VIEW    =  35;  // Stop to view the data (Status tab)
    static final short REQUEST_ENABLE_REALTIME_VIEW     =  36;  // Start to view the data (Status tab)
    static final short DISABLE_REALTIME_VIEW            =  37;  // Stop to view the data (Status tab)
    static final short ENABLE_REALTIME_VIEW             =  38;  // Start to view the data (Status tab)

    static final short DTA_UPDATED                      =  40;  // A Data message is received

    static final short REQUEST_START_DOWNLOAD           =  41;  // Request to start download a file
    static final short REQUEST_STOP_DOWNLOAD            =  42;  // Request to stop download a file (interruption)
    static final short START_DOWNLOAD                   =  43;  // Start to download a file
    static final short END_DOWNLOAD                     =  44;  // End of the download

    static final short STORAGE_PERMISSION_GRANTED       =  119;  // Storage permission granted
    static final short LOCAL_LOGLIST_SELECTION          =  120;  // A logfile is selected on local loglist

    static final short LOCAL_FILE_DELETE                =  131;  // Delete a local file
    static final short LOCAL_FILE_NEW                   =  132;  // Add a new local file

    static final short LOCAL_UPDATE_LOGLIST             =  111;  // Update local loglist signal
    static final short LOCAL_REQUEST_SYNC               =  112;  // A synchronization is requested

    static final short UPDATE_DOWNLOAD_PROGRESS         =  200;
    static final short UPDATE_SETTINGS                  =  201;  // Signal to update (read) preferences

    static final short ERROR_FILE_ALREADY_EXISTS        =  210;  // You are trying to create a file that already exists

    static final short START_APP                        =  254;  // Start the app
    static final short EXIT_APP                         =  255;  // Close the app
}
