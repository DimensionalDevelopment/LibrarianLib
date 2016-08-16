package com.teamwizardry.librarianlib.gui.mixin

import com.teamwizardry.librarianlib.gui.EnumMouseButton
import com.teamwizardry.librarianlib.gui.GuiComponent
import com.teamwizardry.librarianlib.gui.HandlerList
import com.teamwizardry.librarianlib.math.Vec2d
import com.teamwizardry.librarianlib.util.event.Event
import com.teamwizardry.librarianlib.util.event.EventCancelable

class DragMixin<T : GuiComponent<T>>(protected var component: T, protected var constraints: (Vec2d) -> Vec2d) {

    class DragPickupEvent<T : GuiComponent<*>>(val component: T, val mousePos: Vec2d, val button: EnumMouseButton) : EventCancelable()
    class DragDropEvent<T : GuiComponent<*>>(val component: T, val mousePos: Vec2d, val button: EnumMouseButton) : EventCancelable()
    class DragMoveEvent<T : GuiComponent<*>>(val component: T, val mousePos: Vec2d, val clickedPoint: Vec2d, val pos: Vec2d, var newPos: Vec2d, val button: EnumMouseButton) : Event()

    var mouseDown: EnumMouseButton? = null
    var clickPos = Vec2d.ZERO

    init {
        init()
    }

    private fun init() {
        component.BUS.hook(GuiComponent.MouseDownEvent::class.java) { event ->
            if (mouseDown == null && event.component.mouseOver && !component.BUS.fire(DragPickupEvent(event.component, event.mousePos, event.button)).isCanceled()) {
                mouseDown = event.button
                clickPos = event.mousePos
                event.cancel()
            }
        }
        component.BUS.hook(GuiComponent.MouseUpEvent::class.java) { event ->
            if (mouseDown == event.button && !component.BUS.fire(DragDropEvent(event.component, event.mousePos, event.button)).isCanceled()) {
                mouseDown = null
                event.cancel()
            }
        }
        component.BUS.hook(GuiComponent.PreDrawEvent::class.java) { event ->
            val mouseButton = mouseDown
            if (mouseButton != null) {
                val newPos = constraints(event.component.pos.add(event.mousePos).sub(clickPos))

                if (newPos != event.component.pos) {
                    event.component.pos = event.component.BUS.fire(
                            DragMoveEvent(event.component, event.mousePos, clickPos, event.component.pos, newPos, mouseButton)
                    ).newPos
                }
            }
        }
    }
}
