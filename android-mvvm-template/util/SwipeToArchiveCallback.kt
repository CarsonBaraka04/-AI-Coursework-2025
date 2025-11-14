package com.example.noteapp.util

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * ItemTouchHelper callback for swipe-to-archive gesture
 * Provides visual feedback during swipe and triggers archive action
 */
class SwipeToArchiveCallback(
    context: Context,
    private val onSwiped: (position: Int) -> Unit
) : ItemTouchHelper.SimpleCallback(
    0, // No drag directions
    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT // Swipe left or right
) {
    
    private val archiveIcon: Bitmap
    private val archiveBackground = ColorDrawable(Color.parseColor("#4CAF50")) // Green
    private val deleteBackground = ColorDrawable(Color.parseColor("#F44336")) // Red
    private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }
    
    init {
        // Create archive icon (in real app, use drawable resource)
        archiveIcon = createArchiveIcon()
    }
    
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false // We don't support move
    }
    
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        onSwiped(position)
    }
    
    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val itemHeight = itemView.bottom - itemView.top
        val isCanceled = dX == 0f && !isCurrentlyActive
        
        if (isCanceled) {
            clearCanvas(c, itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }
        
        // Swipe right (archive)
        if (dX > 0) {
            drawArchiveBackground(c, itemView, dX, itemHeight)
        }
        // Swipe left (archive or delete)
        else if (dX < 0) {
            drawArchiveBackground(c, itemView, dX, itemHeight)
        }
        
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
    
    /**
     * Draw archive background and icon
     */
    private fun drawArchiveBackground(c: Canvas, itemView: android.view.View, dX: Float, itemHeight: Int) {
        val background = if (dX > 0) archiveBackground else archiveBackground
        
        if (dX > 0) {
            // Swipe right
            background.setBounds(
                itemView.left,
                itemView.top,
                itemView.left + dX.toInt(),
                itemView.bottom
            )
        } else {
            // Swipe left
            background.setBounds(
                itemView.right + dX.toInt(),
                itemView.top,
                itemView.right,
                itemView.bottom
            )
        }
        
        background.draw(c)
        
        // Draw icon
        val iconMargin = (itemHeight - archiveIcon.height) / 2
        val iconTop = itemView.top + iconMargin
        val iconBottom = iconTop + archiveIcon.height
        
        if (dX > 0) {
            // Icon on left side
            val iconLeft = itemView.left + iconMargin
            val iconRight = iconLeft + archiveIcon.width
            c.drawBitmap(archiveIcon, null, Rect(iconLeft, iconTop, iconRight, iconBottom), null)
        } else {
            // Icon on right side
            val iconRight = itemView.right - iconMargin
            val iconLeft = iconRight - archiveIcon.width
            c.drawBitmap(archiveIcon, null, Rect(iconLeft, iconTop, iconRight, iconBottom), null)
        }
    }
    
    /**
     * Clear canvas
     */
    private fun clearCanvas(c: Canvas, left: Float, top: Float, right: Float, bottom: Float) {
        c.drawRect(left, top, right, bottom, clearPaint)
    }
    
    /**
     * Create a simple archive icon bitmap
     * In a real app, you would load this from drawable resources
     */
    private fun createArchiveIcon(): Bitmap {
        val size = 64
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        val paint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 4f
            isAntiAlias = true
        }
        
        // Draw simple archive box icon
        val rect = RectF(8f, 16f, 56f, 56f)
        canvas.drawRect(rect, paint)
        
        // Draw lid
        val lidRect = RectF(8f, 8f, 56f, 20f)
        canvas.drawRect(lidRect, paint)
        
        // Draw handle
        canvas.drawLine(24f, 28f, 40f, 28f, paint)
        
        return bitmap
    }
    
    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return 0.3f // 30% swipe to trigger action
    }
    
    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return defaultValue * 1.5f // Faster swipe needed to escape
    }
}

/**
 * Alternative: Swipe with different actions for left and right
 */
class SwipeActionsCallback(
    context: Context,
    private val onSwipeLeft: (position: Int) -> Unit,
    private val onSwipeRight: (position: Int) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
    
    private val archiveBackground = ColorDrawable(Color.parseColor("#4CAF50"))
    private val deleteBackground = ColorDrawable(Color.parseColor("#F44336"))
    
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = false
    
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        when (direction) {
            ItemTouchHelper.LEFT -> onSwipeLeft(position)
            ItemTouchHelper.RIGHT -> onSwipeRight(position)
        }
    }
    
    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        
        if (dX > 0) {
            // Swipe right - archive (green)
            archiveBackground.setBounds(
                itemView.left,
                itemView.top,
                itemView.left + dX.toInt(),
                itemView.bottom
            )
            archiveBackground.draw(c)
        } else if (dX < 0) {
            // Swipe left - delete (red)
            deleteBackground.setBounds(
                itemView.right + dX.toInt(),
                itemView.top,
                itemView.right,
                itemView.bottom
            )
            deleteBackground.draw(c)
        }
        
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}
