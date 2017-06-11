package com.apps.hesham.raye7;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

/**
 * Created by Hesham on 09/06/2017.
 */

public class TimePickerFragment extends DialogFragment {
    private String timeString;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = getActivity().getLayoutInflater()
                .inflate(R.layout.dialog_time_picker, null);
        final TimePicker timePicker= (TimePicker) v.findViewById(R.id.time_picker);
        int hr, min;
        if (Build.VERSION.SDK_INT < 23){
            hr = timePicker.getCurrentHour();
            min = timePicker.getCurrentMinute();
        } else {
            hr = timePicker.getHour();
            min = timePicker.getMinute();
        }
       timeString = generateTimeString(hr,min);

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                timeString = generateTimeString(hourOfDay, minute);
            }
        });
        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.time_picker_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onTimeSet(timeString);
                    }
                })
                .create();
    }

    private String generateTimeString(int hourOfDay, int minute){
        String timeString;
        String AM_PM = "AM";
        if (hourOfDay>12)
            AM_PM = "PM";
        String hour= String.valueOf(hourOfDay);
        if (hourOfDay<10)
            hour = "0" + hour;
        String min = String.valueOf(minute);
        if (minute<10)
            min = "0" + min;
        timeString = hour + ":" +  min + " " + AM_PM;
        return timeString;
    }

    private OnTimeSetListener listener;
    public interface OnTimeSetListener {
        public abstract void onTimeSet(String time);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        FragmentActivity activity;
        activity = (FragmentActivity) context;

        if (context instanceof FragmentActivity){
            activity =(FragmentActivity) context;
            try {
                this.listener = (TimePickerFragment.OnTimeSetListener)activity;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
