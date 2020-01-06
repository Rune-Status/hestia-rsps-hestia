package worlds.gregs.hestia.core.display.window.model.actions

import worlds.gregs.hestia.artemis.InstantEvent
import worlds.gregs.hestia.core.action.Action

data class RefreshWindow(val target: Int): Action(), InstantEvent