package com.teamwizardry.librarianlib.features.facade.components

import com.teamwizardry.librarianlib.features.facade.component.GuiComponent

import java.util.function.Consumer

@Deprecated("Just use an anonymous subclass")
class ComponentRaw : GuiComponent {

    var func: Consumer<ComponentRaw>

    constructor(posX: Int, posY: Int, func: Consumer<ComponentRaw>) : super(posX, posY) {
        this.func = func
    }

    constructor(posX: Int, posY: Int, width: Int, height: Int, func: Consumer<ComponentRaw>) : super(posX, posY, width, height) {
        this.func = func
    }

    override fun draw(partialTicks: Float) {
        func.accept(this)
    }

}
