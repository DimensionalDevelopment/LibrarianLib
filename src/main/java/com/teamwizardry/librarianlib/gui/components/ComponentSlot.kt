package com.teamwizardry.librarianlib.gui.components

import com.teamwizardry.librarianlib.gui.GuiComponent
import com.teamwizardry.librarianlib.gui.HandlerList
import com.teamwizardry.librarianlib.gui.Option
import com.teamwizardry.librarianlib.math.Vec2d
import com.teamwizardry.librarianlib.plus
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.item.ItemStack
import net.minecraft.util.text.TextFormatting

class ComponentSlot(posX: Int, posY: Int) : GuiComponent<ComponentSlot>(posX, posY, 16, 16) {

    val stack = Option<ComponentSlot, ItemStack?>(null)
    val tooltip = Option<ComponentSlot, Boolean>(true)
    val quantityText = HandlerList<(ComponentSlot, String?) -> String?>()
    val itemInfo = HandlerList<(ComponentSlot, MutableList<String>) -> Unit>()

    override fun drawComponent(mousePos: Vec2d, partialTicks: Float) {
        RenderHelper.enableGUIStandardItemLighting()
        GlStateManager.enableRescaleNormal()

        val stack = this.stack.getValue(this)
        if (stack != null) {
            var str = "" + stack.stackSize
            str = quantityText.fireModifier(str, { h, v -> h(this, v) }) ?: ""

            val itemRender = Minecraft.getMinecraft().renderItem
            itemRender.zLevel = 200.0f

            val font = stack.item.getFontRenderer(stack)

            itemRender.renderItemAndEffectIntoGUI(stack, pos.xi, pos.yi)
            itemRender.renderItemOverlayIntoGUI(if (font == null) Minecraft.getMinecraft().fontRendererObj else font, stack, pos.xi, pos.yi, str)

            itemRender.zLevel = 0.0f


            if (mouseOverThisFrame && tooltip.getValue(this))
                drawTooltip(stack)
        }

        GlStateManager.disableRescaleNormal()
        RenderHelper.disableStandardItemLighting()
    }

    fun drawTooltip(stack: ItemStack) {
        val list = stack.getTooltip(Minecraft.getMinecraft().thePlayer, Minecraft.getMinecraft().gameSettings.advancedItemTooltips)

        for (i in list.indices) {
            if (i == 0) {
                list[i] = stack.rarity.rarityColor + list[i]
            } else {
                list[i] = TextFormatting.GRAY + list[i]
            }
        }

        itemInfo.fireAll { h -> h(this, list) }

        val font = stack.item.getFontRenderer(stack)
        setTooltip(list, font ?: Minecraft.getMinecraft().fontRendererObj)
    }

}
