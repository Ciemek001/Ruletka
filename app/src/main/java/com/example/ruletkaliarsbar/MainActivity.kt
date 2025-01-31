package com.example.ruletkaliarsbar

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.media.MediaPlayer
import android.os.Build
import android.widget.ImageButton
import androidx.annotation.RequiresApi
import kotlin.system.exitProcess
import kotlinx.coroutines.*

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val btnConfirm = findViewById<Button>(R.id.btn_confirm)
        val btnBack = findViewById<Button>(R.id.btn_back)
        val bulletsInput = findViewById<EditText>(R.id.input_bullets)

        btnConfirm.setOnClickListener {
            val bulletCount = bulletsInput.text.toString().toIntOrNull()
            if (bulletCount in 1..5) {
                val prefs = getSharedPreferences("GameSettings", Context.MODE_PRIVATE)
                if (bulletCount != null) {
                    prefs.edit().putInt("bullet_count", bulletCount).apply()
                }
                Toast.makeText(this, "Ustawiono $bulletCount naboi", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Wprowadź liczbę od 1 do 5", Toast.LENGTH_SHORT).show()
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}

class GameOverActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_over)

        val btnRetry = findViewById<Button>(R.id.btn_play_again)
        val btnMenu = findViewById<Button>(R.id.btn_menu)

        btnRetry.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnMenu.setOnClickListener {
            val intent = Intent(this, MainMenuActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}

class MainMenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        val btnStart = findViewById<Button>(R.id.btn_start)
        val btnExit = findViewById<Button>(R.id.btn_exit)
        val btnOptions = findViewById<ImageButton>(R.id.btn_options)

        btnStart.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnExit.setOnClickListener {
            finish()
            exitProcess(0)
        }

        btnOptions.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
}

class MainActivity : AppCompatActivity() {
    private lateinit var cylinder: Array<Boolean>
    private var currentSlot: Int = 0
    private var roundCounter: Int = 0
    private lateinit var prefs: SharedPreferences
    private lateinit var revolverImage: ImageView
    private lateinit var vibrator: Vibrator
    private lateinit var cameraManager: CameraManager
    private var cameraId: String? = null

    private lateinit var spinSound: MediaPlayer
    private lateinit var shootSound: MediaPlayer
    private lateinit var clickSound: MediaPlayer

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("GameSettings", Context.MODE_PRIVATE)
        revolverImage = findViewById(R.id.revolverImage)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraId = cameraManager.cameraIdList.firstOrNull()

        val backToMenuButton: Button = findViewById(R.id.backToMenuButton)
        val spinButton: Button = findViewById(R.id.spinButton)
        val shootButton: Button = findViewById(R.id.shootButton)
        val roundTextView: TextView = findViewById(R.id.roundTextView)

        val spinSound = MediaPlayer.create(this, R.raw.spin_sound)
        val shootSound = MediaPlayer.create(this, R.raw.shoot_sound)
        val clickSound = MediaPlayer.create(this, R.raw.click_sound)

        initializeCylinder()

        shootButton.setOnClickListener {
            if (cylinder[currentSlot]) {
                revolverImage.setImageResource(R.drawable.flash_effect)
                shootSound.start()
                flashTorch()
                vibratePhone(500)
                updateStats(false)
                showGameOverScreen()
            } else {
                revolverImage.setImageResource(R.drawable.revolver)
                clickSound.start()
                moveToNextSlot()
                animateCylinderRotation(60f)
                roundCounter++
                roundTextView.text = "Runda: $roundCounter"
            }
        }

        spinButton.setOnClickListener {
            spinSound.start()
            spinCylinder()
            vibrateSpinEffect()
        }

        backToMenuButton.setOnClickListener {
            val intent = Intent(this, MainMenuActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun initializeCylinder() {
        val bulletCount = prefs.getInt("bullet_count", 1)
        cylinder = Array(6) { false }
        repeat(bulletCount) {
            var pos: Int
            do {
                pos = (0..5).random()
            } while (cylinder[pos])
            cylinder[pos] = true
        }
        currentSlot = 0
        roundCounter = 0
    }

    private fun showGameOverScreen() {
        val intent = Intent(this, GameOverActivity::class.java)
        startActivity(intent)
        finish()
    }

private fun moveToNextSlot() {
        currentSlot = (currentSlot + 1) % cylinder.size
    }

    private fun spinCylinder() {
        val spins = (3..6).random() * 360
        animateCylinderRotation(spins.toFloat())
        currentSlot = (0..5).random()
    }

    private fun animateCylinderRotation(degrees: Float) {
        val animator = ObjectAnimator.ofFloat(revolverImage, "rotation", revolverImage.rotation, revolverImage.rotation + degrees)
        animator.duration = 500
        animator.start()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun vibratePhone(duration: Long) {
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun vibrateSpinEffect() {
        CoroutineScope(Dispatchers.Main).launch {
            repeat(10) {
                vibratePhone(100)
                delay(200)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun flashTorch() {
        try {
            cameraId?.let {
                cameraManager.setTorchMode(it, true)
                CoroutineScope(Dispatchers.Main).launch {
                    delay(200)
                    cameraManager.setTorchMode(it, false)
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun showGameOver() {
        Toast.makeText(this, "Zginąłeś", Toast.LENGTH_LONG).show()
        val intent = Intent(this, MainMenuActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun updateStats(won: Boolean) {
        val wins = prefs.getInt("wins", 0)
        val losses = prefs.getInt("losses", 0)
        prefs.edit().putInt("wins", if (won) wins + 1 else wins)
            .putInt("losses", if (!won) losses + 1 else losses)
            .apply()
    }
}
