package worlds.gregs.hestia.core.action

import com.artemis.BaseSystem
import net.mostlyoriginal.api.event.common.EventSystem
import net.mostlyoriginal.api.event.common.Subscribe
import net.mostlyoriginal.api.system.core.PassiveSystem

class ActionSystem : PassiveSystem() {

    /**
     * We need to hack the world here as the one in [BaseSystem] world is protected and so can't be reached internally for
     * [EventSystem] extension.
     */
    @Subscribe(priority = Int.MAX_VALUE)
    private fun handleAction(action: WorldEvent) {
        action.world = world
    }

}