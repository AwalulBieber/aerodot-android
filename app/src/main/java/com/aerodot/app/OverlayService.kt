package com.aerodot.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import kotlin.math.max

class OverlayService : Service() {
    companion object {
        const val ACTION_START = "com.aerodot.app.OVERLAY_START"
        const val ACTION_SHOW = "com.aerodot.app.OVERLAY_SHOW"
        const val ACTION_HIDE = "com.aerodot.app.OVERLAY_HIDE"
        const val ACTION_STOP = "com.aerodot.app.OVERLAY_STOP"
        const val ACTION_UPDATE = "com.aerodot.app.OVERLAY_UPDATE"
        private const val CHANNEL_ID = "aerodot_overlay"
        private const val NOTIFICATION_ID = 410
    }

    private lateinit var windowManager: WindowManager
    private var crosshairView: CrosshairView? = null
    private var attached = false

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action ?: ACTION_START) {
            ACTION_START -> {
                startAsForeground()
                showOverlay()
            }
            ACTION_SHOW -> showOverlay()
            ACTION_HIDE -> hideOverlay()
            ACTION_UPDATE -> crosshairView?.invalidate()
            ACTION_STOP -> {
                hideOverlay()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startAsForeground() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(): Notification {
        fun action(title: String, action: String, requestCode: Int): NotificationCompat.Action {
            val intent = Intent(this, OverlayService::class.java).setAction(action)
            val pending = PendingIntent.getService(
                this, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            return NotificationCompat.Action.Builder(0, title, pending).build()
        }
        val openIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_cross)
            .setContentTitle("AeroDot is active")
            .setContentText("Crosshair overlay controls")
            .setContentIntent(openIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(action("Show", ACTION_SHOW, 1))
            .addAction(action("Hide", ACTION_HIDE, 2))
            .addAction(action("Stop", ACTION_STOP, 3))
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "AeroDot Overlay", NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun showOverlay() {
        if (!Settings.canDrawOverlays(this)) return
        if (crosshairView == null) crosshairView = CrosshairView(this)
        if (attached) {
            crosshairView?.visibility = View.VISIBLE
            crosshairView?.invalidate()
            return
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START }
        try {
            windowManager.addView(crosshairView, params)
            attached = true
        } catch (_: Exception) {
            attached = false
        }
    }

    private fun hideOverlay() {
        if (attached) {
            try { windowManager.removeView(crosshairView) } catch (_: Exception) { }
            attached = false
        }
    }

    override fun onDestroy() {
        hideOverlay()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

class CrosshairView(context: Context) : View(context) {
    private val prefs = context.getSharedPreferences("aerodot_settings", Context.MODE_PRIVATE)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.SQUARE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val size = prefs.getFloat("size", 22f)
        val thickness = prefs.getFloat("thickness", 2.5f)
        val gap = prefs.getFloat("gap", 5f)
        val opacity = prefs.getFloat("opacity", 1f)
        val offsetX = prefs.getFloat("offset_x", 0f)
        val offsetY = prefs.getFloat("offset_y", 0f)
        val color = prefs.getInt("color", Color.rgb(53, 217, 255))
        val shape = prefs.getString("shape", "Cross") ?: "Cross"
        val centerX = width / 2f + offsetX * resources.displayMetrics.density
        val centerY = height / 2f + offsetY * resources.displayMetrics.density
        val outer = size * resources.displayMetrics.density
        val inner = gap * resources.displayMetrics.density
        paint.color = color
        paint.alpha = (opacity.coerceIn(0f, 1f) * 255).toInt()
        paint.strokeWidth = thickness * resources.displayMetrics.density
        paint.style = Paint.Style.STROKE

        fun line(x1: Float, y1: Float, x2: Float, y2: Float) {
            canvas.drawLine(centerX + x1, centerY + y1, centerX + x2, centerY + y2, paint)
        }
        val half = outer
        when (shape) {
            "Dot" -> {
                paint.style = Paint.Style.FILL
                canvas.drawCircle(centerX, centerY, max(2f, thickness * 1.4f), paint)
            }
            "Circle", "Dual Ring" -> {
                canvas.drawCircle(centerX, centerY, half, paint)
                if (shape == "Dual Ring") canvas.drawCircle(centerX, centerY, half * .58f, paint)
            }
            "Sniper" -> {
                line(-half, 0f, half, 0f); line(0f, -half, 0f, half)
                canvas.drawCircle(centerX, centerY, half * .48f, paint)
            }
            "Brackets", "Corners" -> {
                val arm = half * .45f
                line(-half, -half, -half + arm, -half)
                line(-half, -half, -half, -half + arm)
                line(half, -half, half - arm, -half)
                line(half, -half, half, -half + arm)
                line(-half, half, -half + arm, half)
                line(-half, half, -half, half - arm)
                line(half, half, half - arm, half)
                line(half, half, half, half - arm)
            }
            "X-Cross" -> {
                line(-half, -half, -inner, -inner)
                line(half, -half, inner, -inner)
                line(-half, half, -inner, inner)
                line(half, half, inner, inner)
            }
            "Chevron" -> {
                line(-half, inner, 0f, -half)
                line(0f, -half, half, inner)
            }
            "T-Cross" -> {
                line(-half, -half, half, -half)
                line(0f, -half, 0f, -inner)
                line(-half, inner, -inner, inner)
                line(inner, inner, half, inner)
            }
            else -> {
                line(-half, 0f, -inner, 0f)
                line(inner, 0f, half, 0f)
                line(0f, -half, 0f, -inner)
                line(0f, inner, 0f, half)
                if (shape == "Dual Ring") canvas.drawCircle(centerX, centerY, half * .45f, paint)
            }
        }
        if (prefs.getBoolean("center_dot", false) && shape != "Dot") {
            paint.style = Paint.Style.FILL
            canvas.drawCircle(centerX, centerY, max(1.5f, thickness), paint)
        }
    }
}
