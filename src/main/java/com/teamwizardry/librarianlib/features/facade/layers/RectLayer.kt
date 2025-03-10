package com.teamwizardry.librarianlib.features.facade.layers

import com.teamwizardry.librarianlib.features.facade.component.GuiLayer
import com.teamwizardry.librarianlib.features.facade.value.IMValue
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.opengl.GL11
import java.awt.Color

class RectLayer(color: Color, x: Int, y: Int, width: Int, height: Int): GuiLayer(x, y, width, height) {
    constructor(color: Color, x: Int, y: Int): this(color, x, y, 0, 0)
    constructor(x: Int, y: Int): this(Color.white, x, y)
    constructor(color: Color): this(color, 0, 0)
    constructor(): this(Color.white)

    val color_im: IMValue<Color> = IMValue(color)
    var color: Color by color_im

    override fun draw(partialTicks: Float) {
        val minX = 0.0
        val minY = 0.0
        val maxX = size.xi.toDouble()
        val maxY = size.yi.toDouble()

        val c = color

        val tessellator = Tessellator.getInstance()
        val vb = tessellator.buffer

        GlStateManager.disableTexture2D()

        GlStateManager.enableBlend()
        GlStateManager.color(c.red / 255f, c.green / 255f, c.blue / 255f, c.alpha / 255f)

        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
        vb.pos(minX, minY, 0.0).endVertex()
        vb.pos(minX, maxY, 0.0).endVertex()
        vb.pos(maxX, maxY, 0.0).endVertex()
        vb.pos(maxX, minY, 0.0).endVertex()
        tessellator.draw()

        GlStateManager.enableTexture2D()
    }

    override fun debugInfo(): MutableList<String> {
        val list = super.debugInfo()
        list.add("color = $color")
        return list
    }
}