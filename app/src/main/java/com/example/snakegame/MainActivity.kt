package com.example.snakegame

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import java.util.*

class MainActivity() : AppCompatActivity(), SurfaceHolder.Callback, Parcelable {
        private lateinit var surfaceView: SurfaceView
        private lateinit var scoreView: TextView
        private lateinit var surfaceHolder:SurfaceHolder
        private var snakePointsList = ArrayList<SnakePoints>()
        private var direction: String = "top"
        private var realScore = 0
        private val speed = 600
        private val pointSize = 24
        private val defaultTale = 2
        private val snakeColor = Color.YELLOW
        private var positionX : Int = 0
        private var  positionY : Int = 0
        private lateinit var time : Timer
        private lateinit var canvas: Canvas
        private lateinit var pointColor: Paint

    constructor(parcel: Parcel) : this() {
        direction = parcel.readString().toString()
        realScore = parcel.readInt()
        positionX = parcel.readInt()
        positionY = parcel.readInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        surfaceView = findViewById(R.id.surfaceView)
        scoreView = findViewById(R.id.score)
        val topBtn : AppCompatImageButton = findViewById(R.id.topButton)
        val bottomBtn : AppCompatImageButton = findViewById(R.id.bottomButton)
        val leftBtn : AppCompatImageButton = findViewById(R.id.leftButton)
        val rightBtn : AppCompatImageButton = findViewById(R.id.rightButton)

        surfaceView.holder.addCallback(this)
        topBtn.setOnClickListener{
            if(direction != "bottom"){
                direction = "top"
            }
        }
        bottomBtn.setOnClickListener{
            if(direction != "top"){
                direction = "bottom"
            }
        }
        leftBtn.setOnClickListener{
            if(direction != "right"){
                direction = "left"
            }
        }
        rightBtn.setOnClickListener{
            if(direction != "left"){
                direction = "right"
            }
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        this.surfaceHolder = holder
        init()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }

    private fun init() {
        snakePointsList.clear()
        scoreView.text = "0"
        realScore = 0
        direction = "right"
        var startPositionX = pointSize * defaultTale
        for (i in 0 until defaultTale) {
            val snake = SnakePoints(startPositionX, pointSize)
            snakePointsList.add(snake)
            startPositionX -= pointSize * 2
        }
        addPoint()
        move()
    }

    private fun addPoint() {
        val surfaceWidth = surfaceView.width - pointSize * 4
        val surfaceHeight = surfaceView.height - pointSize * 4
        var randomXPosition = Random().nextInt(surfaceWidth / pointSize)
        var randomYPosition = Random().nextInt(surfaceHeight / pointSize)
        if (randomXPosition % 2 != 0) {
            randomXPosition += 1
        }
        if (randomYPosition % 2 != 0) {
            randomYPosition += 1
        }
        positionX = pointSize * randomXPosition + pointSize*2
        positionY = pointSize * randomYPosition + pointSize*2
    }

    private fun growSnake() {
        val snakePoints = SnakePoints(0, 0)
        snakePointsList.add(snakePoints)
        realScore++
        runOnUiThread { scoreView.text = realScore.toString() }
    }

    private fun checkGameOver(headPositionX: Int, headPositionY: Int): Boolean {
        var gameOver = false
        if (snakePointsList[0].positionX < 0 || snakePointsList[0].positionY < 0
            || snakePointsList[0].positionX >= surfaceView.width
            || snakePointsList[0].positionY >= surfaceView.height) {
            gameOver = true
        } else {
            for (i in 1 until snakePointsList.size) {
                if (headPositionX == snakePointsList[i].positionX
                    && headPositionY == snakePointsList[i].positionY
                ) {
                    gameOver = true
                }
            }
        }
        return gameOver
    }
    private  fun checkIfEaten(headPositionX:Int, positionX:Int, headPositionY:Int, positionY:Int):Boolean{
        if(headPositionX + (pointSize*2) >positionX && headPositionX - (pointSize*2) <positionX){
            if (headPositionY + (pointSize*2) >positionY && headPositionY - (pointSize*2) < positionY){
                return true
            }}
        return false
    }

    private fun move() {
        time = Timer()
        time.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                var headPositionX: Int = snakePointsList.get(0).getPositionX()
                var headPositionY: Int = snakePointsList.get(0).getPositionY()
                if (checkIfEaten(headPositionX, positionX , headPositionY ,positionY)) {
                    growSnake()
                    addPoint()
                }
                when (direction) {
                    "right" -> {
                        snakePointsList.get(0).setPositionX(headPositionX + pointSize * 2)
                        snakePointsList.get(0).setPositionY(headPositionY)
                    }
                    "left" -> {
                        snakePointsList.get(0).setPositionX(headPositionX - pointSize * 2)
                        snakePointsList.get(0).setPositionY(headPositionY)
                    }
                    "top" -> {
                        snakePointsList.get(0).setPositionX(headPositionX)
                        snakePointsList.get(0).setPositionY(headPositionY - pointSize * 2)
                    }
                    "bottom" -> {
                        snakePointsList.get(0).setPositionX(headPositionX)
                        snakePointsList.get(0).setPositionY(headPositionY + pointSize * 2)
                    }
                }
                if (checkGameOver(headPositionX, headPositionY)) {
                    time.purge()
                    time.cancel()
                    val builder: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
                    builder.setTitle("Game over")
                    builder.setMessage("Your score was $realScore")
                    builder.setCancelable(false)
                    builder.setPositiveButton(
                        "Start Again"
                    ) { dialog, which -> init() }
                    runOnUiThread { builder.show() }
                } else {
                    canvas = surfaceHolder.lockCanvas()
                    canvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR)
                    canvas.drawCircle(
                        snakePointsList.get(0).getPositionX().toFloat(),
                        snakePointsList.get(0).getPositionY().toFloat(),
                        pointSize.toFloat(),
                        paintPoint()
                    )
                    canvas.drawCircle(
                        positionX.toFloat(),
                        positionY.toFloat(),
                        pointSize.toFloat(),
                        paintPoint()
                    )
                    var i = 1
                    while (i < snakePointsList.size) {
                        val getXPosition: Int = snakePointsList.get(i).getPositionX()
                        val getYPosition: Int = snakePointsList.get(i).getPositionY()
                        snakePointsList.get(i).setPositionX(headPositionX)
                        snakePointsList.get(i).setPositionY(headPositionY)
                        canvas.drawCircle(
                            snakePointsList.get(i).getPositionX().toFloat(),
                            snakePointsList.get(i).getPositionY().toFloat(), pointSize.toFloat(), paintPoint()
                        )
                        headPositionX = getXPosition
                        headPositionY = getYPosition
                        i++
                    }
                    surfaceHolder.unlockCanvasAndPost(canvas)
                }
            }
        }, 1000 - speed.toLong(), 1000 - speed.toLong())
    }
    private fun paintPoint(): Paint {
        pointColor = Paint()
        pointColor.setColor(snakeColor)
        pointColor.setStyle(Paint.Style.FILL)
        pointColor.setAntiAlias(true)
             return pointColor
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(direction)
        parcel.writeInt(realScore)
        parcel.writeInt(positionX)
        parcel.writeInt(positionY)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MainActivity> {
        override fun createFromParcel(parcel: Parcel): MainActivity {
            return MainActivity(parcel)
        }

        override fun newArray(size: Int): Array<MainActivity?> {
            return arrayOfNulls(size)
        }
    }

}
