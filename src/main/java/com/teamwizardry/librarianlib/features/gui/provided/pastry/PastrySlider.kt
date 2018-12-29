package com.teamwizardry.librarianlib.features.gui.provided.pastry

import com.teamwizardry.librarianlib.features.eventbus.Event
import com.teamwizardry.librarianlib.features.eventbus.EventCancelable
import com.teamwizardry.librarianlib.features.gui.component.GuiComponent
import com.teamwizardry.librarianlib.features.gui.component.GuiComponentEvents
import com.teamwizardry.librarianlib.features.gui.component.GuiLayer
import com.teamwizardry.librarianlib.features.gui.components.FixedSizeComponent
import com.teamwizardry.librarianlib.features.gui.layers.SpriteLayer
import com.teamwizardry.librarianlib.features.gui.mixin.DragMixin
import com.teamwizardry.librarianlib.features.gui.value.IMValueDouble
import com.teamwizardry.librarianlib.features.gui.value.RMValueDouble
import com.teamwizardry.librarianlib.features.helpers.vec
import com.teamwizardry.librarianlib.features.kotlin.clamp
import com.teamwizardry.librarianlib.features.math.Align2d
import com.teamwizardry.librarianlib.features.math.Axis2d
import com.teamwizardry.librarianlib.features.math.Cardinal2d
import com.teamwizardry.librarianlib.features.math.Vec2d
import com.teamwizardry.librarianlib.features.sprite.Sprite
import net.minecraft.util.math.MathHelper
import kotlin.math.PI
import kotlin.math.roundToInt

class PastrySlider(posX: Int, posY: Int, length: Int, var pointed: Boolean, facing: Cardinal2d) : FixedSizeComponent(
    posX, posY,
    if(facing.axis == Axis2d.Y) length else 7,
    if(facing.axis == Axis2d.X) length else 7
) {
    private val inner = GuiComponent(0, 0, length, 7)

    var length: Int = length
        set(value) {
            field = value
            fixedSize = vec(
                if(facing.axis == Axis2d.Y) length else 7,
                if(facing.axis == Axis2d.X) 7 else length
            )
            inner.size = vec(length, 7)
        }
    var facing: Cardinal2d = facing
        set(value) {
            val changed = field != value
            field = value
            if(changed) {
                length = length // update size
                when(value) {
                    Cardinal2d.NEGATIVE_Y -> { // up
                        inner.anchor = vec(1, 1)
                        inner.rotation = 2*PI
                        handleLayer.sprite = PastryTexture.sliderHandleUp
                        handleLayer.rotation = 0.0
                    }
                    Cardinal2d.POSITIVE_Y -> { // down
                        inner.anchor = vec(0, 0)
                        inner.rotation = 0.0
                        handleLayer.sprite = PastryTexture.sliderHandleDown
                        handleLayer.rotation = 0.0
                    }
                    Cardinal2d.NEGATIVE_X -> { // left
                        inner.anchor = vec(0, 1)
                        inner.rotation = PI
                        handleLayer.sprite = PastryTexture.sliderHandleLeft
                        handleLayer.rotation = 0.0
                    }
                    Cardinal2d.POSITIVE_X -> { // right
                        inner.anchor = vec(1, 0)
                        inner.rotation = -PI
                        handleLayer.sprite = PastryTexture.sliderHandleRight
                        handleLayer.rotation = 0.0
                    }
                }
                if(!pointed) {
                    handleLayer.sprite = PastryTexture.sliderHandle
                    handleLayer.rotation = -inner.rotation
                }
                this.setNeedsLayout()
            }
        }

    var range: ClosedRange<Double> = 0.0 .. 1.0
        set(value) {
            val changed = field != value
            field = value
            if(changed) this.setNeedsLayout()
        }
    val value_rm: RMValueDouble = RMValueDouble(0.0) { oldValue, newValue ->
        if(oldValue != newValue) this.setNeedsLayout()
    }
    var value: Double by value_rm

    private val handleLayer = SpriteLayer(PastryTexture.sliderHandle, 0, 0, 7, 7)

    init {
        handleLayer.anchor = vec(0.5, 0.5)
        this.add(inner)
        inner.add(handleLayer.componentWrapper())

        DragMixin(handleLayer.componentWrapper()) { it }

        handleLayer.BUS.hook<DragMixin.DragMoveEvent> { event ->
            mouseAdjustValue(event.newPos)
            event.newPos = posFromValue(value)
        }

        BUS.hook<GuiComponentEvents.MouseClickEvent> {
            if (mouseOver) {
                mouseAdjustValue(mousePos)
            }
        }
    }

    private fun mouseAdjustValue(mousePos: Vec2d) {
        val newValue = snapValue(valueFromPos(mousePos)).clamp(range.start, range.endInclusive)
        this.value = BUS.fire(ValueChangeEvent(this.value, newValue)).newValue
    }

    private fun snapValue(value: Double): Double {
        return value
    }

    private fun valueFromPos(mousePos: Vec2d): Double {
        val fraction = ((mousePos.x - 3.5) / (width - 7)).clamp(0.0, 1.0)
        return range.start + fraction * (range.endInclusive - range.start)
    }

    private fun posFromValue(value: Double): Vec2d {
        if(range.endInclusive == range.start) return vec(3.5, 3.5)
        val fraction = (value - range.start) / (range.endInclusive - range.start)
        return vec(
            fraction * (inner.width - 7) + 3.5,
            3.5
        )
    }

    override fun draw(partialTicks: Float) {
        super.draw(partialTicks)
    }

    override fun layoutChildren() {
        handleLayer.pos = posFromValue(value)
    }

    /**
     * Called before the value is changes so the new value can be adjusted
     */
    class ValueChangeEvent(val oldValue: Double, var newValue: Double): Event()
}