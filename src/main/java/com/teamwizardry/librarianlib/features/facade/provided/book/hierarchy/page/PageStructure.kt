package com.teamwizardry.librarianlib.features.facade.provided.book.hierarchy.page

import com.google.gson.JsonObject
import com.teamwizardry.librarianlib.features.facade.component.GuiComponent
import com.teamwizardry.librarianlib.features.facade.provided.book.IBookGui
import com.teamwizardry.librarianlib.features.facade.provided.book.context.Bookmark
import com.teamwizardry.librarianlib.features.facade.provided.book.helper.TranslationHolder
import com.teamwizardry.librarianlib.features.facade.provided.book.hierarchy.entry.Entry
import com.teamwizardry.librarianlib.features.facade.provided.book.structure.BookmarkRenderableStructure
import com.teamwizardry.librarianlib.features.facade.provided.book.structure.ComponentRenderableStructure
import com.teamwizardry.librarianlib.features.facade.provided.book.structure.StructureCacheRegistry
import com.teamwizardry.librarianlib.features.math.Vec2d
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

class PageStructure(override val entry: Entry, element: JsonObject) : Page {

    private val structureName: String = element.getAsJsonPrimitive("name").asString
    private val structure by lazy(StructureCacheRegistry.getPromise(structureName))
    private val subtext = TranslationHolder.fromJson(element.get("subtext"))

    override val searchableStrings: Collection<String>?
        get() = mutableListOf(structureName)

    override val extraBookmarks: List<Bookmark>
        get() = listOf(BookmarkRenderableStructure(::structure))

    @SideOnly(Side.CLIENT)
    override fun createBookComponents(book: IBookGui, size: Vec2d): List<() -> GuiComponent> {
        return mutableListOf({ ComponentRenderableStructure(book, 16, 16, size.xi, size.yi, structure, subtext) })
    }
}
