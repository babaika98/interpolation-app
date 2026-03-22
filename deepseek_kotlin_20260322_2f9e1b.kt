package com.example.interpolationapp

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var startValueEdit: EditText
    private lateinit var endValueEdit: EditText
    private lateinit var totalLengthEdit: EditText
    private lateinit var currentPointEdit: EditText
    private lateinit var calculateBtn: Button
    private lateinit var resultText: TextView
    private lateinit var visualScale: LinearLayout
    private lateinit var currentPointMarker: View
    private lateinit var pointValueText: TextView
    private lateinit var startValueLabel: TextView
    private lateinit var endValueLabel: TextView
    private lateinit var drawingLine: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        startValueEdit = findViewById(R.id.startValueEdit)
        endValueEdit = findViewById(R.id.endValueEdit)
        totalLengthEdit = findViewById(R.id.totalLengthEdit)
        currentPointEdit = findViewById(R.id.currentPointEdit)
        calculateBtn = findViewById(R.id.calculateBtn)
        resultText = findViewById(R.id.resultText)
        visualScale = findViewById(R.id.visualScale)
        currentPointMarker = findViewById(R.id.currentPointMarker)
        pointValueText = findViewById(R.id.pointValueText)
        startValueLabel = findViewById(R.id.startValueLabel)
        endValueLabel = findViewById(R.id.endValueLabel)
        drawingLine = findViewById(R.id.drawingLine)
    }

    private fun setupListeners() {
        calculateBtn.setOnClickListener {
            performInterpolation()
        }
    }

    private fun performInterpolation() {
        // Получаем значения из полей ввода
        val startValueStr = startValueEdit.text.toString()
        val endValueStr = endValueEdit.text.toString()
        val totalLengthStr = totalLengthEdit.text.toString()
        val currentPointStr = currentPointEdit.text.toString()

        // Проверка на пустые поля
        if (TextUtils.isEmpty(startValueStr) || TextUtils.isEmpty(endValueStr) ||
            TextUtils.isEmpty(totalLengthStr) || TextUtils.isEmpty(currentPointStr)) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val startValue = startValueStr.toDouble()
            val endValue = endValueStr.toDouble()
            val totalLength = totalLengthStr.toDouble()
            val currentPoint = currentPointStr.toDouble()

            // Проверка корректности данных
            if (totalLength <= 0) {
                Toast.makeText(this, "Длина должна быть положительной", Toast.LENGTH_SHORT).show()
                return
            }

            if (currentPoint < 0 || currentPoint > totalLength) {
                Toast.makeText(this, "Точка замера должна быть в пределах [0, $totalLength]", Toast.LENGTH_SHORT).show()
                return
            }

            // Выполняем линейную интерполяцию
            val interpolatedValue = interpolate(startValue, endValue, currentPoint, totalLength)
            
            // Обновляем интерфейс
            updateUI(startValue, endValue, totalLength, currentPoint, interpolatedValue)

        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Введите корректные числа", Toast.LENGTH_SHORT).show()
        }
    }

    private fun interpolate(start: Double, end: Double, point: Double, totalLength: Double): Double {
        // Формула линейной интерполяции: y = y1 + (x - x1) * ((y2 - y1) / (x2 - x1))
        // где x1 = 0, x2 = totalLength, y1 = start, y2 = end
        return start + (point / totalLength) * (end - start)
    }

    private fun updateUI(start: Double, end: Double, totalLength: Double, 
                        currentPoint: Double, value: Double) {
        // Обновляем текстовые метки
        startValueLabel.text = String.format("Начало: %.2f", start)
        endValueLabel.text = String.format("Конец: %.2f", end)
        resultText.text = String.format("Результат: %.2f", value)
        pointValueText.text = String.format("%.2f", value)

        // Визуализация на шкале прогресса
        updateProgressScale(start, end, totalLength, currentPoint)

        // Позиционирование маркера на линии
        positionMarkerOnLine(totalLength, currentPoint)
    }

    private fun updateProgressScale(start: Double, end: Double, totalLength: Double, currentPoint: Double) {
        // Очищаем предыдущее содержимое
        visualScale.removeAllViews()

        // Создаем прогресс бар для визуализации
        val progressWidth = (currentPoint / totalLength * 1000).toInt() // Увеличиваем для точности
        
        val progressView = View(this)
        val layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
        
        // Вычисляем цвет на основе интерполяции
        val progressColor = interpolateColor(start, end, currentPoint, totalLength)
        
        progressView.setBackgroundColor(progressColor)
        
        // Создаем левую часть (прогресс)
        val leftPart = View(this)
        leftPart.layoutParams = LinearLayout.LayoutParams(progressWidth, 
            LinearLayout.LayoutParams.MATCH_PARENT)
        leftPart.setBackgroundColor(progressColor)
        
        // Создаем правую часть (остаток)
        val rightPart = View(this)
        rightPart.layoutParams = LinearLayout.LayoutParams(0, 
            LinearLayout.LayoutParams.MATCH_PARENT, 1f)
        rightPart.setBackgroundColor(0xFFE0E0E0.toInt())
        
        visualScale.addView(leftPart)
        visualScale.addView(rightPart)
    }

    private fun positionMarkerOnLine(totalLength: Double, currentPoint: Double) {
        if (totalLength > 0) {
            // Получаем позицию маркера относительно линии
            val lineWidth = drawingLine.width
            val startX = drawingLine.left
            val endX = drawingLine.right
            
            // Вычисляем позицию маркера
            val position = startX + (currentPoint / totalLength) * (endX - startX)
            
            // Устанавливаем маркер
            val markerParams = currentPointMarker.layoutParams as FrameLayout.LayoutParams
            markerParams.leftMargin = (position - currentPointMarker.width / 2).toInt()
            currentPointMarker.layoutParams = markerParams
            currentPointMarker.visibility = View.VISIBLE
        }
    }

    private fun interpolateColor(start: Double, end: Double, point: Double, totalLength: Double): Int {
        // Простая интерполяция цвета от синего к красному
        val t = (point / totalLength).toFloat()
        
        val startColor = android.graphics.Color.BLUE
        val endColor = android.graphics.Color.RED
        
        val startRed = android.graphics.Color.red(startColor)
        val startGreen = android.graphics.Color.green(startColor)
        val startBlue = android.graphics.Color.blue(startColor)
        
        val endRed = android.graphics.Color.red(endColor)
        val endGreen = android.graphics.Color.green(endColor)
        val endBlue = android.graphics.Color.blue(endColor)
        
        val red = (startRed + t * (endRed - startRed)).toInt()
        val green = (startGreen + t * (endGreen - startGreen)).toInt()
        val blue = (startBlue + t * (endBlue - startBlue)).toInt()
        
        return android.graphics.Color.rgb(red, green, blue)
    }

    // Метод для получения позиции на экране
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // Обновляем позицию маркера при изменении фокуса окна
        val totalLengthStr = totalLengthEdit.text.toString()
        val currentPointStr = currentPointEdit.text.toString()
        
        if (totalLengthStr.isNotEmpty() && currentPointStr.isNotEmpty()) {
            try {
                val totalLength = totalLengthStr.toDouble()
                val currentPoint = currentPointStr.toDouble()
                positionMarkerOnLine(totalLength, currentPoint)
            } catch (e: NumberFormatException) {
                // Игнорируем
            }
        }
    }
}