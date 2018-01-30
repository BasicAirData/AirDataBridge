/**
 * FragmentDownloadDialog - Java Class for Android
 * Created by G.Capelli (BasicAirData) on 21/01/2018
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

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


public class FragmentDownloadDialog extends DialogFragment {

    AirDataBridgeApplication ADBApplication = AirDataBridgeApplication.getInstance();
    LogFile dwFile = new LogFile();
    TextView TVDownloadDesc;
    TextView TVDownloadPercent;
    TextView TVDownloadkb;
    ProgressBar PBProgress;

    //@SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder downloadAlert = new AlertDialog.Builder(getActivity());

        dwFile = ADBApplication.getCurrentRemoteDownload();
        Log.w("myApp", "[#] FragmentDownloadDialog: REQUEST_START_DOWNLOAD = " + dwFile.Name);

        //downloadAlert.setTitle(R.string.card_menu_download);
        //downloadAlert.setIcon(R.drawable.ic_add_white_24dp);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = (View) inflater.inflate(R.layout.fragment_downloaddialog, null);

        TVDownloadDesc = (TextView) view.findViewById(R.id.id_downloaddialog_desc);
        TVDownloadPercent = (TextView) view.findViewById(R.id.id_downloaddialog_percent);
        TVDownloadkb = (TextView) view.findViewById(R.id.id_downloaddialog_kb);
        PBProgress = (ProgressBar) view.findViewById(R.id.id_downloaddialog_progressBar);

        TVDownloadDesc.setText(getResources().getString(R.string.dlg_download_the_file, dwFile.Name));

        downloadAlert.setView(view)

                //.setPositiveButton(R.string.conti_nue, new DialogInterface.OnClickListener() {
                /*.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (isAdded()) {
                            String FileDescription;
                            if (DescEditText.getText().length() != 0) {
                                FileDescription = DescEditText.getText().toString().trim().toUpperCase();
                                if (FileDescription.length() > 8) FileDescription = FileDescription.substring(0, 8);
                            } else {
                                FileDescription = DescEditText.getHint().toString();
                            }
                            if(!FileDescription.isEmpty()) {
                                FileDescription += ".CSV";
                                LogFile lg = new LogFile(FileDescription, "0", "0");
                                EventBus.getDefault().post(new EventBusMSGLogFile(EventBusMSG.REMOTE_FILE_NEW, lg));
                            }
                        }
                    }
                })*/
                //.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Log.w("myApp", "[#] FragmentDownloadDialog: REQUEST_STOP_DOWNLOAD");
                        EventBus.getDefault().post(EventBusMSG.REQUEST_STOP_DOWNLOAD);
                    }
                });
        return downloadAlert.create();
    }

    @Override
    public void onResume() {
        //Log.w("myApp", "[#] FragmentLogList_Remote: onResume()");
        if (!ADBApplication.isDownloadDialogVisible()) this.dismiss();
        EventBus.getDefault().register(this);
        Update();
        super.onResume();
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Subscribe
    public void onEvent(Short msg) {
        if (msg == EventBusMSG.END_DOWNLOAD) {
            this.dismiss();
        }
        if ((msg == EventBusMSG.UPDATE_DOWNLOAD_PROGRESS) && isAdded()) Update();
        /*if (msg == EventBusMSG.END_DOWNLOAD) {
            (getActivity()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Update();
                }
            });
        }*/
    }

    void Update() {
        final int p;
        p = (dwFile.lsize > 0) ? (int)(100 * ADBApplication.getDownloadedSize() / dwFile.lsize) : 0;
        (getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PBProgress.setProgress (p);
                TVDownloadPercent.setText(p + "%");
                TVDownloadkb.setText((int)(ADBApplication.getDownloadedSize() / 1024) + "/" + dwFile.Sizekb);
            }
        });
    }
}