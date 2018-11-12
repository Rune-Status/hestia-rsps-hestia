package world.gregs.hestia.network.update.player

import com.artemis.ComponentMapper
import world.gregs.hestia.game.update.UpdateEncoder
import world.gregs.hestia.core.network.packets.Packet
import world.gregs.hestia.game.component.update.ColourOverlay

class PlayerColourOverlayMask(private val colourOverlayMapper: ComponentMapper<ColourOverlay>) : UpdateEncoder {

    override val encode: Packet.Builder.(Int, Int) -> Unit = { _, other ->
        val colourOverlay = colourOverlayMapper.get(other)
        val colour = colourOverlay.colour
        writeByteS(colour and 0xFF)//Hue
        writeByteS(colour shr 8 and 0xFF)//Saturation
        writeByteC(colour shr 16 and 0xFF)//Luminance
        writeByte(colour shr 24 and 0xFF)//Multiplier
        writeLEShortA(colourOverlay.delay)//Start
        writeShort(colourOverlay.duration)//Finish
    }
}