/**
 * FragmentLogList_Remote - Java Class for Android
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

import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FragmentLogList_Remote extends Fragment {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    private View view;
    private TextView textViewRemoteListEmpty;

    private RecyclerView.Adapter adapter;
    private List<LogFile> data = Collections.synchronizedList(new ArrayList<LogFile>());
    private LogFile SelectedLogFile;


    public FragmentLogList_Remote() {
        // Required empty public constructor
    }

    @Subscribe
    public void onEvent(Short msg) {
        if (msg == EventBusMSG.REMOTE_UPDATE_LOGLIST) {
            (getActivity()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Update();
                }
            });
        }
    }

    @Subscribe
    public void onEvent(final EventBusMSGLogFile msg) {
        if (msg.MSGType == EventBusMSG.REMOTE_LOGLIST_SELECTION) {
            SelectedLogFile = msg.logFile;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    registerForContextMenu(view);
                    getActivity().openContextMenu(view);
                    unregisterForContextMenu(view);
                }
            });
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
        view = inflater.inflate(R.layout.fragment_loglist, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.getItemAnimator().setChangeDuration(0);
        adapter = new LogFileAdapter(data);
        recyclerView.setAdapter(adapter);
        textViewRemoteListEmpty = (TextView) view.findViewById(R.id.id_textView_loglistEmpty);
        return view;
    }

    @Override
    public void onResume() {
        //Log.w("myApp", "[#] FragmentLogList_Remote: onResume()");
        EventBus.getDefault().register(this);
        Update();
        super.onResume();
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        //Log.w("myApp", "[#] FragmentLogList_Remote: onPause()");
        super.onPause();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_card_remote, menu);
        //menu.setHeaderTitle(SelectedLogFile.Name);

        if (AirDataBridgeApplication.getInstance().getSDCardDTAFrequency() != 0) {    // REC
            if (SelectedLogFile.Current) {
                menu.findItem(R.id.cardmenu_remote_stop_recording).setVisible(true);
            } else {
                menu.findItem(R.id.cardmenu_remote_delete).setVisible(true);
            }
        } else {
            menu.findItem(R.id.cardmenu_remote_start_recording).setVisible(true);
            menu.findItem(R.id.cardmenu_remote_delete).setVisible(true);

            if (AirDataBridgeApplication.getInstance().isStoragePermissionGranted())
                menu.findItem(R.id.cardmenu_remote_download).setVisible(true);
        }

    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.cardmenu_remote_delete:
                AlertDialog.Builder builder;
                builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(getResources().getString(R.string.card_message_delete_remote_file_confirmation));
                builder.setIcon(android.R.drawable.ic_menu_info_details);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EventBus.getDefault().post(new EventBusMSGLogFile(EventBusMSG.REMOTE_FILE_DELETE, SelectedLogFile));
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            case R.id.cardmenu_remote_download:
                AirDataBridgeApplication.getInstance().setPrefNotifyDownloadFinished(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("prefNotifyDownloadFinished", false));
                AirDataBridgeApplication.getInstance().setPrefDeleteRemoteFileWhenDownloadFinished(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("prefDeleteRemoteFileWhenDownloadFinished", false));
                AirDataBridgeApplication.getInstance().setDownloadDialogVisible(true);
                AirDataBridgeApplication.getInstance().setCurrentRemoteDownload(SelectedLogFile);
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentDownloadDialog dwDialog = new FragmentDownloadDialog();
                dwDialog.setCancelable(false);
                dwDialog.show(fm, "");
                EventBus.getDefault().post(EventBusMSG.REQUEST_START_DOWNLOAD);
                break;
            case R.id.cardmenu_remote_start_recording:
                EventBus.getDefault().post(new EventBusMSGLogFile(EventBusMSG.REMOTE_REQUEST_START_RECORDING, SelectedLogFile));
                break;
            case R.id.cardmenu_remote_stop_recording:
                EventBus.getDefault().post(EventBusMSG.REMOTE_REQUEST_STOP_RECORDING);
                break;
            default:
                return false;
        }
        return true;
    }

    public void Update() {
        if (isAdded()) {
            if (AirDataBridgeApplication.getInstance().getBluetoothConnectionStatus() == EventBusMSG.BLUETOOTH_HEARTBEAT_SYNC) {
                final List<LogFile> TI = AirDataBridgeApplication.getInstance().getLogfileList_Remote();
                synchronized (data) {
                    if (data != null) data.clear();
                    if (!TI.isEmpty()) {
                        data.addAll(TI);
                        textViewRemoteListEmpty.setVisibility(View.INVISIBLE);
                    } else {
                        if (AirDataBridgeApplication.getInstance().getSD_Status() == AirDataBridgeApplication.SD_STATUS_EMPTY)
                            textViewRemoteListEmpty.setText(R.string.remote_sd_empty);
                        if (AirDataBridgeApplication.getInstance().getSD_Status() == AirDataBridgeApplication.SD_STATUS_NOT_PRESENT)
                            textViewRemoteListEmpty.setText(R.string.remote_sd_not_present);
                        textViewRemoteListEmpty.setVisibility(View.VISIBLE);
                        // LogFile List empty
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            } else {
                textViewRemoteListEmpty.setText(R.string.remote_sd_not_connected);
                textViewRemoteListEmpty.setVisibility(View.VISIBLE);
            }
        }
    }
}