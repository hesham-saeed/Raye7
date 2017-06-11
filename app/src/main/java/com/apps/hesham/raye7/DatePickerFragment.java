package com.apps.hesham.raye7;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Hesham on 09/06/2017.
 */

public class DatePickerFragment extends DialogFragment {
    public String getMonth(int month) {
        return new DateFormatSymbols().getMonths()[month].substring(0,3);
    }
    private String dateString;
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar c = Calendar.getInstance();
        View v = getActivity().getLayoutInflater()
                .inflate(R.layout.dialog_date_picker, null);
        final DatePicker datePicker = (DatePicker) v.findViewById(R.id.date_picker);
        datePicker.init(c.get(Calendar.YEAR),c.get( Calendar.MONTH),c.get( Calendar.DAY_OF_MONTH)
                ,new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                dateString = String.valueOf(dayOfMonth) + " " + getMonth(monthOfYear);
            }
        });
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getDayOfMonth();
        dateString = String.valueOf(day) + " " + getMonth(month);

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.date_picker_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDateSet(dateString);
                    }
                })
                .create();
    }

    private DatePickerFragment.OnDateSetListener listener;
    public interface OnDateSetListener {
        public abstract void onDateSet(String time);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        FragmentActivity activity;
        activity = (FragmentActivity) context;

        if (context instanceof FragmentActivity){
            activity =(FragmentActivity) context;
            try {
                this.listener = (DatePickerFragment.OnDateSetListener)activity;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
