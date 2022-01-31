package com.example.stopwatchtimer

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.Menu
import android.view.View
import android.view.ViewDebug
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.stopwatchtimer.databinding.ActivityMainBinding
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var timerStarted = false
    private lateinit var serviceIntent: Intent
    private var time = 0.0
    private var lapCount = 0
    private var lapText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)


        binding.floatingResetButton.visibility = View.INVISIBLE
        binding.floatingLapButton.visibility = View.INVISIBLE

        binding.floatingStartStopButton.setOnClickListener {
            startStopTimer()
            binding.floatingResetButton.visibility = View.VISIBLE
            binding.floatingLapButton.visibility = View.VISIBLE
        }

        binding.floatingResetButton.setOnClickListener {
            Handler(Looper.getMainLooper()).postDelayed({
                /* Create an Intent that will start the Menu-Activity. */
                resetTimer()
            }, 100)
            stopTimer()
            resetTimer()
            binding.floatingResetButton.visibility = View.INVISIBLE
            binding.floatingLapButton.visibility = View.INVISIBLE
        }




        binding.floatingLapButton.setOnClickListener {
            lapTime()
        }

        drawCircle()

        serviceIntent = Intent(applicationContext, TimerService::class.java)
        registerReceiver(updateTime, IntentFilter(TimerService.TIMER_UPDATED))
    }

    @SuppressLint("SetTextI18n")
    private fun lapTime() {
        lapCount ++
        lapText = getTimeStringFromDouble(time)
        val textView: TextView = TextView(this)
        textView.id = lapCount
        textView.gravity = Gravity.CENTER
        textView.text = "#$lapCount $lapText"
        textView.setTextColor(Color.parseColor("#FFFFFF"))
        binding.lapLinearView.addView(textView)
    }

    private fun drawCircle() {
        val bitmap = Bitmap.createBitmap(1500, 2000, Bitmap.Config.ARGB_4444)
        val canvas = Canvas(bitmap)

        // canvas background color
        //canvas.drawARGB(255, 78, 168, 186);

        val paint = Paint()
        paint.color = Color.parseColor("#FFFFFF")
        paint.strokeWidth = 10F
        paint.style = Paint.Style.STROKE
        paint.isAntiAlias = true
        paint.isDither = true

        // get device dimensions


        // circle center
        var center_x = (750).toFloat()
        var center_y = (1250).toFloat()
        var radius = 375F

        // draw circle
        canvas.drawCircle(center_x, center_y, radius, paint)
        // now bitmap holds the updated pixels

        // set bitmap as background to ImageView
        binding.imageV.background = BitmapDrawable(resources, bitmap)
    }

    private val updateTime: BroadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            time = intent.getDoubleExtra(TimerService.TIME_EXTRA, 0.0)
            binding.timeTV.text = getTimeStringFromDouble(time)
        }

    }

    private fun getTimeStringFromDouble(time: Double): String {
        val resultInt = time.roundToInt()
        val hours = resultInt % 864000 / 36000
        val minutes = resultInt % 864000 % 36000 / 6000
        val seconds = resultInt  % 864000 % 36000 % 6000 / 100
        var centiseconds = resultInt % 864000 % 3600 % 100
        if (hours > 0) {
            return makeTimeStringHours(hours, minutes, seconds, centiseconds)
        }
        return makeTimeString(minutes, seconds, centiseconds)

    }
    // Handling the case where the timer exceeds 1 hour
    private fun makeTimeStringHours(hours: Int, minutes: Int, seconds: Int, centiseconds: Int): String {
        binding.timeTV.textSize = 36F
        return String.format("%02d:%02d:%02d", hours, minutes, seconds, centiseconds)
    }

    // If timer is less than 1 hour, don't show hour count.
    private fun makeTimeString(hour: Int, min: Int, sec: Int): String {
        return String.format("%02d:%02d:%02d", hour, min, sec)
    }

    private fun resetTimer() {
        stopTimer()
        clearLaps()
    }

    private fun clearLaps() {
        time = 0.0
        binding.timeTV.text = "00:00:00"
        binding.lapLinearView.removeAllViews()
        lapCount = 0
        lapText = ""
    }

    private fun startStopTimer() {
        if (timerStarted) {
            stopTimer()
        }
        else {
            startTimer()
        }
    }

    private fun startTimer() {
        serviceIntent.putExtra(TimerService.TIME_EXTRA, time)
        startService(serviceIntent)
        binding.floatingStartStopButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_pause_24))
        timerStarted = true
    }

    private fun stopTimer() {
        stopService(serviceIntent)
        binding.floatingStartStopButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_play_arrow_24))
        timerStarted = false
    }
}