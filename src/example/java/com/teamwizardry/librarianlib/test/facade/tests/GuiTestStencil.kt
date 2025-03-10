package com.teamwizardry.librarianlib.test.facade.tests

import com.teamwizardry.librarianlib.features.animator.Animator
import com.teamwizardry.librarianlib.features.animator.animations.BasicAnimation
import com.teamwizardry.librarianlib.features.facade.GuiBase
import com.teamwizardry.librarianlib.features.facade.layers.RectLayer
import com.teamwizardry.librarianlib.features.helpers.vec
import java.awt.Color

/**
 * Created by TheCodeWarrior
 */
class GuiTestStencil : GuiBase() {
    init {
        main.size = vec(100, 100)

        val unclipped = RectLayer(Color.PINK, 0, 0, 100, 100)
        val clipped = RectLayer(Color.RED, -25, -25, 100, 100)
        val clipping = RectLayer(Color.GREEN, 25, 25, 50, 50)

        main.add(unclipped, clipping)
        clipping.add(clipped)

        unclipped.rotation = Math.toRadians(45.0)
        clipped.rotation = Math.toRadians(45.0)

        clipping.clipToBounds = true
        clipping.cornerRadius = 15.0

        val anim = BasicAnimation(clipping, "cornerRadius")
        anim.from = 0
        anim.to = 25
        anim.duration = 2 * 20f
        anim.shouldReverse = true
        anim.repeatCount = -1
        Animator.global.add(anim)
    }
}
