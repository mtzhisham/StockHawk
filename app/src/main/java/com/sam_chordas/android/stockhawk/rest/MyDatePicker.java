package com.sam_chordas.android.stockhawk.rest;

import android.app.DatePickerDialog;
import android.content.Context;

import android.widget.DatePicker;
import android.widget.TextView;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by moataz on 22/09/16.
 */
public class MyDatePicker {

    Calendar myCalendar = Calendar.getInstance();
    TextView textView;
    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            view.setMaxDate(new Date().getTime());

            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH , dayOfMonth);
            updateLabel();
        }

    };

        public void showPicker(Context context, TextView textView){
            this.textView = textView;

            new DatePickerDialog(context, date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();
        }
    private void updateLabel() {

        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat df = new SimpleDateFormat(myFormat, Locale.US);

        textView.setText(df.format(myCalendar.getTime()));
    }

}
