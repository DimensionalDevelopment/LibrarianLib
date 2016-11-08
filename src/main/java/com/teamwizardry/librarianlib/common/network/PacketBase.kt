package com.teamwizardry.librarianlib.common.network

import com.teamwizardry.librarianlib.common.util.hasNullSignature
import com.teamwizardry.librarianlib.common.util.saving.ByteBufSerializationHandlers
import com.teamwizardry.librarianlib.common.util.saving.SavingFieldCache
import com.teamwizardry.librarianlib.common.util.writeNonnullSignature
import com.teamwizardry.librarianlib.common.util.writeNullSignature
import io.netty.buffer.ByteBuf
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

abstract class PacketBase : IMessage {

    /**
     * Put your handling code for the message in here.
     * Assume all fields are already populated by the read methods.
     */
    abstract fun handle(ctx: MessageContext)

    /**
     * Override this to reply to any incoming messages with your own.
     * Leave the return null to reply with no message.
     * The resulting message is fired along the same channel as the incoming one.
     */
    open fun reply(ctx: MessageContext): PacketBase? = null

    /**
     * Override this to add custom write-to-bytes.
     * Make sure to have the same order for writing and reading.
     */
    open fun writeCustomBytes(buf: ByteBuf) {
        // NO-OP
    }

    /**
     * Override this to add custom read-from-bytes.
     * Make sure to have the same order for writing and reading.
     */
    open fun readCustomBytes(buf: ByteBuf) {
        // NO-OP
    }

    @Suppress("UNUSED_VARIABLE")
    fun writeAutoBytes(buf: ByteBuf) {
        SavingFieldCache.getClassFields(javaClass).forEach {
            val (clazz, getter, setter) = it.value
            val handler = ByteBufSerializationHandlers.getWriterUnchecked(clazz)
            if (handler != null) {
                val field = getter(this)
                if (field == null)
                    buf.writeNullSignature()
                else {
                    buf.writeNonnullSignature()
                    handler(buf, field)
                }
            } else
                buf.writeNullSignature()
        }
    }

    @Suppress("UNUSED_VARIABLE")
    fun readAutoBytes(buf: ByteBuf) {
        SavingFieldCache.getClassFields(javaClass).forEach {
            val (clazz, getter, setter) = it.value
            if (buf.hasNullSignature())
                setter(this, null)
            else {
                val handler = ByteBufSerializationHandlers.getReaderUnchecked(clazz)
                if (handler != null) setter(this, handler(buf, null))
            }
        }
    }

    override fun fromBytes(buf: ByteBuf) {
        readAutoBytes(buf)
        readCustomBytes(buf)
    }

    override fun toBytes(buf: ByteBuf) {
        writeAutoBytes(buf)
        writeCustomBytes(buf)
    }
}
