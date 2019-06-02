package com.teamwizardry.librarianlib.features.facade.provided.pastry.components.dropdown

import com.teamwizardry.librarianlib.features.eventbus.EventCancelable
import com.teamwizardry.librarianlib.features.eventbus.Hook
import com.teamwizardry.librarianlib.features.facade.component.GuiComponentEvents
import com.teamwizardry.librarianlib.features.facade.component.GuiLayer
import com.teamwizardry.librarianlib.features.facade.component.GuiLayerEvents
import com.teamwizardry.librarianlib.features.facade.component.LayerHierarchyException
import com.teamwizardry.librarianlib.features.facade.layers.SpriteLayer
import com.teamwizardry.librarianlib.features.facade.provided.pastry.Pastry
import com.teamwizardry.librarianlib.features.facade.provided.pastry.PastryTexture
import com.teamwizardry.librarianlib.features.facade.provided.pastry.components.PastryActivatedControl
import com.teamwizardry.librarianlib.features.helpers.rect
import com.teamwizardry.librarianlib.features.helpers.vec
import com.teamwizardry.librarianlib.features.math.coordinatespaces.ScreenSpace
import com.teamwizardry.librarianlib.features.utilities.client.LibCursor

class PastryDropdown<T> constructor(
    posX: Int, posY: Int,
    width: Int,
    callback: ((T?) -> Unit)?
) : PastryActivatedControl(posX, posY, width, Pastry.lineHeight) {

    val items = mutableListOf<PastryDropdownItem<T>>()
    var selected: PastryDropdownItem<T>? = null
        private set

    private val sprite = SpriteLayer(PastryTexture.dropdown, 0, 0, widthi, heighti)
    private var menu: DropdownMenu<T>? = null
    internal var buttonContents: GuiLayer? = null

    init {
        if(callback != null)
            this.BUS.hook<SelectEvent<T>> {
                callback(it.value)
            }
        this.cursor = LibCursor.POINT
        this.add(sprite)
    }

    fun select(index: Int) {
        val newItem = items[index]
        if(selected === newItem) return
        if(newItem.decoration) {
            selected = null
            buttonContents?.also { remove(it) }
            buttonContents = null
        } else {
            selected = newItem
            buttonContents?.also { remove(it) }
            buttonContents = newItem.createLayer().also { add(it) }
        }
        BUS.fire(SelectEvent(selected?.value))
    }

    private fun openMenu(mouseActivated: Boolean) {
        val gui = this.gui ?: throw LayerHierarchyException("Can't open dropdown menu without a GUI")

        val menu = DropdownMenu(this, mouseActivated)
        this.menu = menu
        gui.add(menu)

        menu.initialMousePos = gui.convertPointTo(gui.mousePos, ScreenSpace)
        menu.pos = this.convertPointTo(vec(0, 0), gui)
        selected?.also { menu.scrollTo(it) }
    }

    internal fun menuClosed() {

    }

    override fun activate() {
        openMenu(false)
    }

    @Hook
    private fun removeFromParent(e: GuiLayerEvents.RemoveFromParentEvent) {
        menu?.also {
            it.parent?.remove(it)
        }
    }

    @Hook
    private fun mouseDown(e: GuiComponentEvents.MouseDownEvent) {
        if(this.mouseOver) {
            openMenu(true)
        }
    }

    override fun layoutChildren() {
        sprite.size = this.size
        buttonContents?.frame = rect(2, 2, width - 10, height - 4)
    }

    class SelectEvent<T>(val value: T?): EventCancelable()
}