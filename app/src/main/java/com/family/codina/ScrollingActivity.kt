package com.family.codina

import android.Manifest
import android.accounts.AccountManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*

class ScrollingActivity : AppCompatActivity() {
    class Bebe {
        var nombre: String? = null
        var date_ini: String? = null
        var date_fin: String? = null
        var foto_perfil: String? = null
        var born: Boolean? = null

        constructor (){}
        constructor(nombre: String?, date_ini: String?, date_fin: String?, foto_perfil: String?, born: Boolean?) {
            this.nombre = nombre
            this.date_ini = date_ini
            this.date_fin = date_fin
            this.foto_perfil = foto_perfil
            this.born = born
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrolling)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        if (ContextCompat.checkSelfPermission(this@ScrollingActivity, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@ScrollingActivity, Manifest.permission.GET_ACCOUNTS)) {
                ActivityCompat.requestPermissions(this@ScrollingActivity, arrayOf(Manifest.permission.GET_ACCOUNTS), PERMS_REQUEST_CODE)
            } else {
                ActivityCompat.requestPermissions(this@ScrollingActivity, arrayOf(Manifest.permission.GET_ACCOUNTS), PERMS_REQUEST_CODE)
            }
        }
        if (ContextCompat.checkSelfPermission(this@ScrollingActivity, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@ScrollingActivity, Manifest.permission.READ_CONTACTS)) {
                ActivityCompat.requestPermissions(this@ScrollingActivity, arrayOf(Manifest.permission.READ_CONTACTS), MY_PERMISSIONS_REQUEST_READ_CONTACTS)
            } else {
                ActivityCompat.requestPermissions(this@ScrollingActivity, arrayOf(Manifest.permission.READ_CONTACTS), MY_PERMISSIONS_REQUEST_READ_CONTACTS)
            }
        }
        val parent = findViewById<View>(R.id.insertBaby) as LinearLayout
        val database = FirebaseDatabase.getInstance().reference
        val ref = database.child("bebes")
        val bebeQuery = ref.orderByChild("orden")
        bebeQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    val bebe = singleSnapshot.getValue(Bebe::class.java)!!
                    fillRows(parent, bebe.nombre, bebe.date_ini, bebe.date_fin, bebe.born, bebe.foto_perfil)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("BEBE", "onCancelled", databaseError.toException())
            }
        })

        if (checkSuperUser()) {
            //TODO: load admin menu layout to CRUD items in firebase
        }
    }

    fun fillRows(parent: LinearLayout, nombre: String?, date_ini: String?, date_fin: String?, born: Boolean?, foto_perfil: String?) {
        val newrow = LayoutInflater.from(this).inflate(R.layout.baby, parent, false)
        if (!foto_perfil!!.isEmpty()) {
            val imageView = newrow.findViewById<View>(R.id.imageBebe) as ImageView
            Picasso.get().load(foto_perfil).into(imageView)
        }
        val labelViewName = newrow.findViewById<View>(R.id.labelName) as TextView
        val textViewName = newrow.findViewById<View>(R.id.textNombre) as TextView
        val labelViewDate = newrow.findViewById<View>(R.id.labelDate) as TextView
        val textViewDate = newrow.findViewById<View>(R.id.textDate) as TextView
        val labelProgress = newrow.findViewById<View>(R.id.labelProgress) as TextView
        val textProgressNum = newrow.findViewById<View>(R.id.progressNum) as TextView
        val progress = newrow.findViewById<View>(R.id.progressBar) as ProgressBar
        textViewName.text = nombre
        textViewDate.text = date_fin
        labelViewName.setTypeface(null, Typeface.BOLD)
        labelViewDate.setTypeface(null, Typeface.BOLD)
        labelProgress.setTypeface(null, Typeface.BOLD)
        if (born!!) {
            labelViewDate.text = "Fecha de nacimiento:"
            labelProgress.text = "Edad:"
        }
        val anim = ProgressBarAnimation(progress, textProgressNum, date_ini, date_fin, born)
        anim.duration = 3000
        progress.startAnimation(anim)
        parent.addView(newrow)
    }

    inner class ProgressBarAnimation(progressbar: ProgressBar, progressText: TextView, con: String?, bir: String?, born: Boolean?) : Animation() {
        private val progressBar: ProgressBar
        private val progressText: TextView
        private val today = Date()
        private var from = 0f
        private var to = 0f
        private var con: Calendar? = null
        private var bir: Calendar? = null
        private val conception: Date
        private val birth: Date
        private var current: Long = 0
        private var weeks: Long = 0
        private var days: Long = 0
        private val born: Boolean?
        private var months: String? = null
        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            super.applyTransformation(interpolatedTime, t)
            val value = from + (to - from) * interpolatedTime
            if (!born!!) {
                progressText.text = weeks.toString() + "." + days + " (" + Integer.toString(Math.round(value)) + "%)"
            } else {
                progressText.text = months + " (" + Integer.toString(Math.round(value)) + "%)"
            }
            if (value < 30) {
                progressBar.progressDrawable.setColorFilter(
                        Color.rgb(204, 0, 0), PorterDuff.Mode.SRC_IN)
            } else if (value < 60) {
                progressBar.progressDrawable.setColorFilter(
                        Color.rgb(255, 165, 0), PorterDuff.Mode.SRC_IN)
            } else if (value < 90) {
                progressBar.progressDrawable.setColorFilter(
                        Color.rgb(0, 204, 0), PorterDuff.Mode.SRC_IN)
            } else {
                progressBar.progressDrawable.setColorFilter(
                        Color.rgb(0, 0, 204), PorterDuff.Mode.SRC_IN)
            }
            progressBar.progress = value.toInt()
        }

        init {
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
            progressBar = progressbar
            this.progressText = progressText
            this.born = born
            val any = try {
                //Date date_con = simpleDateFormat.parse(con);
                //Date date_bir = simpleDateFormat.parse(bir);
                this.con = GregorianCalendar();
                this.bir = GregorianCalendar()
                (this.con as GregorianCalendar).setTime(simpleDateFormat.parse(con))
                (this.bir as GregorianCalendar).setTime(simpleDateFormat.parse(bir))
            } catch (t: Throwable) {
                Log.e("Parsing Date Error:", t.message)
            }
            conception = this.con!!.time
            birth = this.bir!!.time
            if (!this.born!!) {
                current = dateDif(conception, today)
                weeks = current / 7
                days = current % 7
                from = 0f
                to = (current * 100 / 280).toFloat()
                if (to > 100) to = 100f
            } else {
                val today: Calendar = GregorianCalendar()
                today.time = Date()
                var yearsInBetween = today[Calendar.YEAR] - this.bir!![Calendar.YEAR]
                var monthsDiff = today[Calendar.MONTH] - this.bir!![Calendar.MONTH]
                from = 0f
                val save_birth_year = this.bir!![Calendar.YEAR]
                this.bir!![Calendar.YEAR] = today[Calendar.YEAR]
                if (today.compareTo(this.bir) == 0) {
                    to = 100f
                } else if (today.compareTo(this.bir) < 0) {
                    this.bir!![Calendar.YEAR] = today[Calendar.YEAR] - 1
                    val dias = dateDif(this.bir!!.time, today.time)
                    to = (dias * 100 / today.getActualMaximum(Calendar.DAY_OF_YEAR)).toFloat()
                    yearsInBetween = Math.abs(save_birth_year - this.bir!![Calendar.YEAR])
                    if (today[Calendar.MONTH] == this.bir!![Calendar.MONTH]) {
                        if (today[Calendar.DAY_OF_WEEK_IN_MONTH] < this.bir!![Calendar.DAY_OF_WEEK_IN_MONTH]) {
                            monthsDiff -= 1
                        }
                    } else {
                        monthsDiff -= 1
                    }
                } else {
                    val dias = dateDif(this.bir!!.time, today.time)
                    to = (dias * 100 / today.getActualMaximum(Calendar.DAY_OF_YEAR)).toFloat()
                }
                if (yearsInBetween == 0) {
                    months = Integer.toString(monthsDiff) + " meses"
                } else if (yearsInBetween == 1) {
                    yearsInBetween++
                    val ageInMonths = yearsInBetween * 12 + monthsDiff
                    months = Integer.toString(ageInMonths) + " meses"
                } else {
                    months = Integer.toString(yearsInBetween) + " años"
                }
            }
        }
    }

    fun dateDif(startDate: Date, endDate: Date): Long {
        val different = endDate.time - startDate.time
        val secondsInMilli: Long = 1000
        val minutesInMilli = secondsInMilli * 60
        val hoursInMilli = minutesInMilli * 60
        val daysInMilli = hoursInMilli * 24
        return different / daysInMilli
    }

    fun getImage(imageName: String?): Int {
        return resources.getIdentifier(imageName, "drawable", packageName)
    }

    fun checkSuperUser(): Boolean {
        val manager = getSystemService(Context.ACCOUNT_SERVICE) as AccountManager
        val list = manager.accounts
        var gmail: String? = null
        for (account in list) {
            if (account.type.equals("com.google", ignoreCase = true)) {
                gmail = account.name
                val gmd5 = md5(gmail)
                if (gmd5 == "62e1ea3440f6b3b541fba3161b1e7a64") {
                    return true
                }
            }
        }
        return false
    }

    fun md5(s: String?): String {
        try {
            val digest = MessageDigest.getInstance("MD5")
            digest.update(s!!.toByteArray())
            val messageDigest = digest.digest()
            val hexString = StringBuffer()
            for (i in messageDigest.indices) hexString.append(Integer.toHexString(0xFF and messageDigest[i].toInt()))
            return hexString.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }

    companion object {
        const val PERMS_REQUEST_CODE = 1
        const val MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1
    }
}