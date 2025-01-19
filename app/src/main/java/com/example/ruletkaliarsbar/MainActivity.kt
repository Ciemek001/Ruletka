package com.example.ruletkaliarsbar

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.media.MediaPlayer
import android.widget.Toast
import kotlin.system.exitProcess

class MainMenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        // Obsługa przycisku Start
        val btnStart = findViewById<Button>(R.id.btn_start)
        btnStart.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Obsługa przycisku Wyjście
        val btnExit = findViewById<Button>(R.id.btn_exit)
        btnExit.setOnClickListener {
            finish() // Zamyka aktywność
            exitProcess(0) // Zamyka aplikację
        }

        // Przycisk Opcje (na razie wyłączony)
        val btnOptions = findViewById<Button>(R.id.btn_options)
        btnOptions.isEnabled = false
    }
}


class MainActivity : AppCompatActivity() {

    private lateinit var cylinder: Array<Boolean> // Stan bębna
    private var currentSlot: Int = 0 // Aktualny slot do strzału

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val revolverImage: ImageView = findViewById(R.id.revolverImage)
        val backToMenuButton: Button = findViewById(R.id.backToMenuButton)
        val spinButton: Button = findViewById(R.id.spinButton)
        val shootButton: Button = findViewById(R.id.shootButton)

        val spinSound = MediaPlayer.create(this, R.raw.spin_sound)
        val shootSound = MediaPlayer.create(this, R.raw.shoot_sound)

        // Inicjalizacja bębna
        initializeCylinder()

        shootButton.setOnClickListener {
            // Sprawdź obecny slot
            if (cylinder[currentSlot]) {
                // Trafiono na pocisk
                revolverImage.setImageResource(R.drawable.flash_effect)
                shootSound.start()
                showGameOver()
            } else {
                // Pusty slot
                revolverImage.setImageResource(R.drawable.revolver)
                shootSound.start()
                moveToNextSlot()
            }
        }

        spinButton.setOnClickListener {
            spinSound.start()
            spinCylinder()
        }

        backToMenuButton.setOnClickListener {
            val intent = Intent(this, MainMenuActivity::class.java)
            startActivity(intent)
            finish() // Zakończenie aktywności
        }
    }

    private fun initializeCylinder() {
        cylinder = Array(6) { false }
        val bulletPosition = (0..5).random()
        cylinder[bulletPosition] = true
        currentSlot = 0
    }

    private fun moveToNextSlot() {
        currentSlot = (currentSlot + 1) % cylinder.size
    }

    private fun spinCylinder() {
        val availableSlots = cylinder.indices.filter { !cylinder[it] || it == currentSlot }
        if (availableSlots.isNotEmpty()) {
            currentSlot = availableSlots.random()
        }
    }

    private fun showGameOver() {
        // Wyświetl komunikat i przejdź do menu
        Toast.makeText(this, "Zginąłeś", Toast.LENGTH_LONG).show()
        val intent = Intent(this, MainMenuActivity::class.java)
        startActivity(intent)
        finish()
    }
}

