/**
 * FragmentNewFileDialog_Local - Java Class for Android
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
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import static android.text.InputType.TYPE_CLASS_TEXT;
import static android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES;

public class FragmentNewFileDialog_Local extends DialogFragment {

    EditText DescEditText;

    //@SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder NewFileDialogAlert = new AlertDialog.Builder(getActivity());

        NewFileDialogAlert.setTitle(R.string.dlg_new_local_logfile_title);
        NewFileDialogAlert.setIcon(R.drawable.ic_add_white_24dp);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = (View) inflater.inflate(R.layout.fragment_newfiledialog, null);

        final List<LogFile> LogfileList = AirDataBridgeApplication.getInstance().getLogfileList_Local();
        String filenameext = "Recording ";
        boolean existing = true;
        for (int i = 0; (i <= 9999) && (existing); i++) {
            filenameext = "Recording " + String.format("%04d", i);
            existing = false;
            for (LogFile lgf : LogfileList) {
                if (filenameext.equals(lgf.Name)) {
                    existing = true;
                    break;
                }
            }
        }

        DescEditText = (EditText) view.findViewById(R.id.placemark_description);
        DescEditText.setInputType(TYPE_CLASS_TEXT | TYPE_TEXT_FLAG_CAP_SENTENCES);
        DescEditText.setHint(filenameext);
        DescEditText.postDelayed(new Runnable()
        {
            public void run()
            {
                if (isAdded()) {
                    DescEditText.requestFocus();
                    InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    mgr.showSoftInput(DescEditText, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        }, 200);

        NewFileDialogAlert.setView(view)

                //.setPositiveButton(R.string.conti_nue, new DialogInterface.OnClickListener() {
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (isAdded()) {
                            String FileDescription;
                            if (DescEditText.getText().length() != 0) {
                                FileDescription = DescEditText.getText().toString().trim();
                            } else {
                                FileDescription = DescEditText.getHint().toString();
                            }
                            if(!FileDescription.isEmpty()) {
                                FileDescription += ".CSV";
                                LogFile lg = new LogFile(new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(System.currentTimeMillis())
                                        + "-0-" + FileDescription);
                                EventBus.getDefault().post(new EventBusMSGLogFile(EventBusMSG.LOCAL_FILE_NEW, lg));
                            }
                        }
                    }
                })
                //.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        return NewFileDialogAlert.create();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }
}