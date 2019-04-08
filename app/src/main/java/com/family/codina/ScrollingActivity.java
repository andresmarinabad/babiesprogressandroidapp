package com.family.codina;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class ScrollingActivity extends AppCompatActivity {

    public static class Bebe {
        public String nombre;
        public String date_ini;
        public String date_fin;
        public String foto_perfil;
        public Boolean born;

        public Bebe(){

        };

        public Bebe(String nombre, String date_ini, String date_fin, String foto_perfil, Boolean born) {
            this.nombre = nombre;
            this.date_ini = date_ini;
            this.date_fin = date_fin;
            this.foto_perfil = foto_perfil;
            this.born = born;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final LinearLayout parent = (LinearLayout) findViewById(R.id.insertBaby);

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref = database.child("bebes");

        Query bebeQuery = ref.orderByChild("orden");
        bebeQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    Bebe bebe = singleSnapshot.getValue(Bebe.class);
                    fillRows(parent, bebe.nombre, bebe.date_ini, bebe.date_fin, bebe.born, bebe.foto_perfil);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("BEBE", "onCancelled", databaseError.toException());
            }
        });


        /*
        fillRows(parent, "??? Marin Codina", "06.03.2019", "11.12.2019", false, "");
        fillRows(parent, "??? Codina Codina", "15.02.2019", "22.11.2019", false, "codina.png");
        fillRows(parent, "Clara Codina Ferreres", "03.08.2018", "12.05.2019", false, "clara.png");
        fillRows(parent, "Lucas Codina Martinez", "10.10.2018", "10.10.2018", true, "lucas.png");
        fillRows(parent, "Mateo Marin Codina", "28.08.2018", "28.08.2018", true, "mateo.png");
        fillRows(parent, "Marta Marin Codina", "25.09.2017", "25.09.2017", true, "marta.png");*/

    }


    public void fillRows(LinearLayout parent, String nombre, String date_ini, String date_fin, Boolean born, String foto_perfil){
        View newrow = LayoutInflater.from(this).inflate(R.layout.baby, parent, false);

        if (foto_perfil != "") {
            ImageView imageView = (ImageView) newrow.findViewById(R.id.imageBebe);
            Picasso.get().load(foto_perfil).into(imageView);
        }


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

        if (born){
            labelViewDate.setText("Fecha de nacimiento:");
            labelProgress.setText("Edad:");
        }

        ProgressBarAnimation anim = new ProgressBarAnimation(progress, textProgressNum, date_ini, date_fin, born);
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
        private Boolean born;
        private String months;

        public ProgressBarAnimation(ProgressBar progressbar, TextView progressText, String con, String bir, Boolean born) {
            super();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.M.yyyy");
            this.progressBar = progressbar;
            this.progressText = progressText;
            this.born = born;

            try {
                Date date_con = simpleDateFormat.parse(con);
                Date date_bir = simpleDateFormat.parse(bir);
                this.con = new GregorianCalendar();
                this.bir = new GregorianCalendar();
                this.con.setTime(date_con);
                this.bir.setTime(date_bir);
            }catch(Throwable t){};

            this.conception = this.con.getTime();
            this.birth = this.bir.getTime();

            if (!this.born){
                this.current = dateDif(conception, today);

                this.weeks = current/7;
                this.days = current%7;

                this.from = 0;
                this.to = (this.current*100/280);
                if (this.to > 100) this.to = 100;
            } else {
                Calendar today = new GregorianCalendar();
                today.setTime(new Date());
                int yearsInBetween = today.get(Calendar.YEAR) - this.bir.get(Calendar.YEAR);
                int monthsDiff = today.get(Calendar.MONTH) - this.bir.get(Calendar.MONTH);

                this.from = 0;
                int save_birth_year = this.bir.get(Calendar.YEAR);
                this.bir.set(Calendar.YEAR, today.get(Calendar.YEAR));

                if (today.compareTo(this.bir) == 0){
                    this.to = 100;
                } else if (today.compareTo(this.bir) < 0){
                    this.bir.set(Calendar.YEAR, today.get(Calendar.YEAR)-1);
                    long dias = dateDif(this.bir.getTime(), today.getTime());
                    this.to = (dias*100/today.getActualMaximum(Calendar.DAY_OF_YEAR));

                    yearsInBetween = Math.abs(save_birth_year - this.bir.get(Calendar.YEAR));

                    if (today.get(Calendar.MONTH) == this.bir.get(Calendar.MONTH)){
                        if (today.get(Calendar.DAY_OF_WEEK_IN_MONTH) < this.bir.get(Calendar.DAY_OF_WEEK_IN_MONTH)){
                            monthsDiff -= 1;
                        }
                    }else{
                        monthsDiff -= 1;
                    }
                } else {
                    long dias = dateDif(this.bir.getTime(), today.getTime());
                    this.to = (dias*100/today.getActualMaximum(Calendar.DAY_OF_YEAR));
                }

                if (yearsInBetween < 2){
                    yearsInBetween ++;
                    int ageInMonths = (yearsInBetween*12 + monthsDiff);
                    this.months = Integer.toString(ageInMonths)+" meses";
                }else{
                    this.months = Integer.toString(yearsInBetween)+" años";
                }
            }


        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            float value = from + (to - from) * interpolatedTime;
            if (!this.born){
                this.progressText.setText(weeks+"."+days+" ("+Integer.toString(Math.round(value))+"%)");
            } else {
                this.progressText.setText(months+" ("+Integer.toString(Math.round(value))+"%)");
            }

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

    public int getImage(String imageName) {
        int drawableResourceId = this.getResources().getIdentifier(imageName, "drawable", this.getPackageName());
        return drawableResourceId;
    }
}
