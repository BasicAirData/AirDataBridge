/**
 * LogFileAdapter - Java Class for Android
 * Created by G.Capelli (BasicAirData) on 20/1/2018
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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.util.List;


class LogFileAdapter extends RecyclerView.Adapter<LogFileAdapter.LogFileHolder> {

    //private final static int NOT_AVAILABLE = -100000;
    //private final static int CARDTYPE_CURRENTTRACK = 0;
    //private final static int CARDTYPE_TRACK = 1;

    private List<LogFile> dataSet;


    private static final Bitmap[] bmpLogFileSource = {
            BitmapFactory.decodeResource(AirDataBridgeApplication.getInstance().getResources(), R.drawable.ic_toys_black_24dp),
            BitmapFactory.decodeResource(AirDataBridgeApplication.getInstance().getResources(), R.drawable.ic_phone_android_black_24dp)
    };


    static class LogFileHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final FrameLayout RLSideColor;
        private final TextView textViewFileName;
        private final TextView textViewFileDatetime;
        private final TextView textViewFileSize;
        private final TextView textViewRecording;
        private final TextView textViewRecordingFreq;
        private final ImageView imageViewFileSource;
        private final CardView cardview;

        private LogFile logFile;


        @Override
        public void onClick(View v) {
            if (logFile.Location == LogFile.LOCATION_REMOTE)
                EventBus.getDefault().post(new EventBusMSGLogFile(EventBusMSG.REMOTE_LOGLIST_SELECTION, logFile));
            else
                EventBus.getDefault().post(new EventBusMSGLogFile(EventBusMSG.LOCAL_LOGLIST_SELECTION, logFile));
            //Log.w("myApp", "[#] LogFileAdapter.java - Selected track id = " + id);
        }


        LogFileHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);

            RLSideColor             = (FrameLayout) itemView.findViewById(R.id.id_sidecolor);
            textViewFileName        = (TextView)  itemView.findViewById(R.id.id_textView_card_filename);
            textViewFileDatetime    = (TextView)  itemView.findViewById(R.id.id_textView_card_filedatetime);
            textViewFileSize        = (TextView)  itemView.findViewById(R.id.id_textView_card_filesize);
            imageViewFileSource     = (ImageView) itemView.findViewById(R.id.id_imageView_card_filesource);
            textViewRecordingFreq   = (TextView)  itemView.findViewById(R.id.id_textView_card_recordingfreq);
            textViewRecording       = (TextView) itemView.findViewById(R.id.id_textView_card_recording);
            cardview                = (CardView)  itemView.findViewById(R.id.card_view);
        }


        void BindLogFile(LogFile lgFile) {
            logFile = lgFile;
            if (logFile.Location == LogFile.LOCATION_LOCAL) {
                RLSideColor.setBackgroundColor(ContextCompat.getColor(AirDataBridgeApplication.getInstance().getApplicationContext(), R.color.colorPrimary));
                imageViewFileSource.setImageBitmap(bmpLogFileSource[1]);
            }
            textViewFileDatetime.setText(logFile.DateTime);
            textViewFileName.setText(logFile.Name);
            textViewFileSize.setText(logFile.Sizekb);
            if ((logFile.Current) && (AirDataBridgeApplication.getInstance().getSDCardDTAFrequency() != 0)) {      // REC
                textViewRecordingFreq.setVisibility(View.VISIBLE);
                textViewRecording.setVisibility(View.VISIBLE);
                textViewFileSize.setVisibility(View.INVISIBLE);
                cardview.setCardBackgroundColor(ContextCompat.getColor(AirDataBridgeApplication.getInstance().getApplicationContext(), R.color.colorCardBackgroundRemoteRecording));
            } else {
                textViewRecordingFreq.setVisibility(View.INVISIBLE);
                textViewRecording.setVisibility(View.INVISIBLE);
                textViewFileSize.setVisibility(View.VISIBLE);
                cardview.setCardBackgroundColor(ContextCompat.getColor(AirDataBridgeApplication.getInstance().getApplicationContext(), R.color.colorCardBackground));
            }
        }
    }


    LogFileAdapter(List<LogFile> data) {
        synchronized(data) {
            this.dataSet = data;
        }
    }


    @Override
    public LogFileHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new LogFileHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_logfileinfo, parent, false));
    }


    //public int getItemViewType (int position) {
    //    return (position == 0) && GPSApplication.getInstance().isCurrentTrackVisible() ? CARDTYPE_CURRENTTRACK : CARDTYPE_TRACK;
    //}


    @Override
    public void onBindViewHolder(final LogFileHolder holder, final int listPosition) {
        holder.BindLogFile(dataSet.get(listPosition));
    }


    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}