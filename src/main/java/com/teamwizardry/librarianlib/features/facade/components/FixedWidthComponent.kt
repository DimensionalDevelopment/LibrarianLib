package com.teamwizardry.librarianlib.features.facade.components

import com.teamwizardry.librarianlib.features.facade.component.GuiComponent
import com.teamwizardry.librarianlib.features.helpers.vec
import com.teamwizardry.librarianlib.features.math.Vec2d

open class FixedWidthComponent(posX: Int, posY: Int, width: Int, height: Int): GuiComponent(posX, posY, width, height) {
    private val fixedWidth = width
    override var size: Vec2d
        get() = vec(fixedWidth, super.size.y)
        set(value) { super.size = vec(fixedWidth, value.y) }
}