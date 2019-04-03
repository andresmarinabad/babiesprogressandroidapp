package com.family.codina;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class ScrollingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final LinearLayout parent = (LinearLayout) findViewById(R.id.insertBaby);

        fillRows(parent, "Clara Codina Ferreres", "05.08.2018", "12.05.2019");
        fillRows(parent, "??? Codina Codina", "15.02.2019", "22.11.2019");

    }

    public void fillRows(LinearLayout parent, String nombre, String date_ini, String date_fin){
        View newrow = LayoutInflater.from(this).inflate(R.layout.baby, parent, false);
        TextView labelViewName = (TextView) newrow.findViewById(R.id.labelName);
        TextView textViewName = (TextView) newrow.findViewById(R.id.textNombre);
        TextView labelViewDate = (TextView) newrow.findViewById(R.id.labelDate);
        TextView textViewDate = (TextView) newrow.findViewById(R.id.textDate);
        TextView labelProgress = (TextView) newrow.findViewById(R.id.labelProgress);
        TextView textProgressNum = (TextView) newrow.findViewById(R.id.progressNum);
        ProgressBar progress = (ProgressBar) newrow.findViewById(R.id.progressBar);

        textViewName.setText(nombre);
        textViewDate.setText(date_fin);

        labelViewName.setTypeface(null, Typeface.BOLD);
        labelViewDate.setTypeface(null, Typeface.BOLD);
        labelProgress.setTypeface(null, Typeface.BOLD);

        ProgressBarAnimation anim = new ProgressBarAnimation(progress, textProgressNum, date_ini, date_fin);
        anim.setDuration(3000);
        progress.startAnimation(anim);
        parent.addView(newrow);
    }

    public class ProgressBarAnimation extends Animation {

        private ProgressBar progressBar;
        private TextView progressText;
        private Date today = new Date();
        private float from;
        private float  to;
        private Calendar con;
        private Calendar bir;
        private Date conception;
        private Date birth;
        private long current;
        private long weeks;
        private long days;

        public ProgressBarAnimation(ProgressBar progressbar, TextView progressText, String con, String bir) {
            super();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.M.yyyy");
            this.progressBar = progressbar;
            this.progressText = progressText;

            try {
                Date date_con = simpleDateFormat.parse(con);
                Date date_bir = simpleDateFormat.parse(bir);
                this.con = new GregorianCalendar(); //2016,11,12
                this.bir = new GregorianCalendar(); //2017,8,17
                this.con.setTime(date_con);
                this.bir.setTime(date_bir);
            }catch(Throwable t){};

            this.conception = this.con.getTime();
            this.birth = this.bir.getTime();

            //private long total = dateDif(conception, birth);
            this.current = dateDif(conception, today);

            this.weeks = current/7;
            this.days = current%7;

            this.from = 0;
            this.to = (this.current*100/280);
            if (this.to > 100) this.to = 100;
            //this.progressText.setText(weeks+"."+days);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            float value = from + (to - from) * interpolatedTime;
            this.progressText.setText(weeks+"."+days+" ("+Integer.toString(Math.round(value))+"%)");
            if (value < 30) {
                this.progressBar.getProgressDrawable().setColorFilter(
                        Color.rgb(204,0,0), android.graphics.PorterDuff.Mode.SRC_IN);
            } else if (value < 60) {
                this.progressBar.getProgressDrawable().setColorFilter(
                        Color.rgb(255,165,0), android.graphics.PorterDuff.Mode.SRC_IN);
            } else if (value < 90) {
                this.progressBar.getProgressDrawable().setColorFilter(
                        Color.rgb(0,204,0), android.graphics.PorterDuff.Mode.SRC_IN);
            } else {
                this.progressBar.getProgressDrawable().setColorFilter(
                        Color.rgb(0,0,204), android.graphics.PorterDuff.Mode.SRC_IN);
            }

            progressBar.setProgress((int) value);
        }

    }

    public long dateDif(Date startDate, Date endDate){

        long different = endDate.getTime() - startDate.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;

        return elapsedDays;

    }
}
