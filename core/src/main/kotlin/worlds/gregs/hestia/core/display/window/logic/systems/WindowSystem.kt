package worlds.gregs.hestia.core.display.window.logic.systems

import com.artemis.ComponentMapper
import net.mostlyoriginal.api.event.common.EventSystem
import net.mostlyoriginal.api.event.common.Subscribe
import worlds.gregs.hestia.artemis.send
import worlds.gregs.hestia.core.display.window.api.Windows
import worlds.gregs.hestia.core.display.window.model.WindowPane
import worlds.gregs.hestia.core.display.window.model.actions.CloseWindow
import worlds.gregs.hestia.core.display.window.model.actions.OpenWindow
import worlds.gregs.hestia.core.display.window.model.actions.RefreshWindow
import worlds.gregs.hestia.core.display.window.model.components.GameFrame
import worlds.gregs.hestia.core.display.window.model.components.WindowRelationships
import worlds.gregs.hestia.core.display.window.model.events.*
import worlds.gregs.hestia.network.client.encoders.messages.WidgetClose
import worlds.gregs.hestia.network.client.encoders.messages.WidgetOpen
import worlds.gregs.hestia.network.client.encoders.messages.WidgetWindowsPane

class WindowSystem : Windows() {

    private lateinit var windowRelationshipsMapper: ComponentMapper<WindowRelationships>
    private lateinit var gameFrameMapper: ComponentMapper<GameFrame>
    private val panes = mutableMapOf<Int, WindowPane>()
    private lateinit var es: EventSystem

    override fun initialize() {
        super.initialize()
        WindowPane.values().forEach { pane ->
            pane.windows.forEach { window ->
                panes[window] = pane
            }
        }
    }

    override fun inserted(entityId: Int) {
        val gameFrame = gameFrameMapper.get(entityId)
        //Gameframe components
        listOf(
                if (gameFrame.resizable) ResizableGameframe else FixedGameframe,//Gameframe
                ChatBox, ChatBackground, ChatSettings, PrivateChat,//Chat box
                HealthOrb, PrayerOrb, EnergyOrb, SummoningOrb,//Minimap
                CombatStyles, TaskSystem, Stats, QuestJournals, Inventory, WornEquipment, PrayerList, ModernSpellbook,//Tabs
                FriendsList, FriendsChat, ClanChat, Options, Emotes, MusicPlayer, Notes
        ).forEach {
            openWindow(entityId, it)
        }
    }

    override fun setWindowPane(id: Int, pane: WindowPane) {
        panes[id] = pane
    }

    override fun openWindow(entityId: Int, id: Int): WindowResult {
        val windowRelationships = windowRelationshipsMapper.get(entityId)
        val relationships = windowRelationships.relationships
        val gameFrame = gameFrameMapper.get(entityId)

        val pane = getPane(id)
        val parent = when (pane) {
            WindowPane.CHAT_BACKGROUND -> ChatBox
            WindowPane.FULL_SCREEN -> 0
            else -> if (gameFrame.resizable) ResizableGameframe else FixedGameframe
        }

        val index = pane.getIndex(gameFrame.resizable)
        if (relationships.containsKey(parent)) {
            if (relationships[parent] == null) {
                relationships[parent] = mutableListOf()
            }

            if (relationships[parent]!!.contains(id)) {//Window already open
                return WindowResult.Issue.AlreadyOpen
            }

            //If the slot is already in use
            if (relationships[parent]!!.any { getIndex(it, gameFrame.resizable) == index }) {
                return WindowResult.Issue.AnotherOpen
            }

            relationships[id] = null
            relationships[parent]!!.add(id)//Add child
            send(entityId, parent, index, id)
            return WindowResult.Opened
        }
        return WindowResult.Issue.MissingParent(parent)
    }

    private fun send(entityId: Int, parent: Int, index: Int, id: Int) {
        val pane = getPane(id)
        if (pane == WindowPane.FULL_SCREEN) {
            es.send(entityId, WidgetWindowsPane(id, 0))
        } else {
            es.send(entityId, WidgetOpen(isPermanent(pane), parent, index, id))
        }
        es.dispatch(WindowOpened(entityId, id))
    }

    override fun closeWindow(entityId: Int, id: Int, silent: Boolean) {
        val windowRelationships = windowRelationshipsMapper.get(entityId)
        val relationships = windowRelationships.relationships
        val parent = relationships.getParent(id)
        if (parent != id) {
            val gameFrame = gameFrameMapper.get(entityId)
            val index = getIndex(id, gameFrame.resizable)!!
            relationships[parent]!!.remove(id)
            es.send(entityId, WidgetClose(parent, index))
            es.dispatch(WindowClosed(entityId, id, silent))
        } else {
            //Window not open
        }
    }

    override fun closeWindows(entityId: Int, pane: WindowPane, silent: Boolean) {
        val window = getWindow(entityId, pane)
        if (window != null) {
            closeWindow(entityId, window, silent)
        }
    }

    override fun updateGameframe(entityId: Int) {
        val windowRelationships = windowRelationshipsMapper.get(entityId)
        val relationships = windowRelationships.relationships
        val gameFrame = gameFrameMapper.get(entityId)
        val previous = if (gameFrame.resizable) FixedGameframe else ResizableGameframe
        val current = if (gameFrame.resizable) ResizableGameframe else FixedGameframe
        val relationship = relationships[previous]
        if (relationship != null) {//If has children
            relationships[current] = relationship
            relationships.remove(previous)//Update id
        }
        //Re-open all windows
        relationships.walk(current) { child, parent ->
            send(entityId, parent, getIndex(child, gameFrame.resizable) ?: 0, child)
            false
        }
    }

    override fun getWindow(entityId: Int, pane: WindowPane): Int? {
        val relationshipsMapper = windowRelationshipsMapper.get(entityId)
        val relationships = relationshipsMapper.relationships
        val gameFrame = gameFrameMapper.get(entityId)

        val gameframe = if (gameFrame.resizable) ResizableGameframe else FixedGameframe
        val index = if (gameFrame.resizable) pane.resizable else pane.fixed

        return relationships.walk(0) { window, parent ->
            parent == gameframe && getIndex(window, gameFrame.resizable) == index
        }
    }

    override fun hasWindow(entityId: Int, pane: WindowPane): Boolean {
        return hasWindow(entityId, getWindow(entityId, pane) ?: return false)
    }

    override fun hasWindow(entityId: Int, window: Int): Boolean {
        val relationshipsMapper = windowRelationshipsMapper.get(entityId)
        val relationships = relationshipsMapper.relationships
        return relationships.containsKey(window)
    }

    override fun verifyWindow(entityId: Int, hash: Int): Boolean {
        val relationships = windowRelationshipsMapper.get(entityId)?.relationships
        return relationships?.containsKey(hash shr 16) ?: false
    }

    override fun refreshWindow(entityId: Int, window: Int) {
        val relationships = windowRelationshipsMapper.get(entityId).relationships
        relationships.walk(window) { id, _ ->
            es.dispatch(WindowRefresh(entityId, id))
            false
        }
    }

    @Subscribe
    private fun openWindow(event: OpenWindow) = openWindow(event.entity, event.target)

    @Subscribe
    private fun closeWindow(event: CloseWindow) = closeWindow(event.entity, event.target)

    @Subscribe
    private fun refreshWindow(event: RefreshWindow) = refreshWindow(event.entity, event.target)

    @Subscribe
    private fun click(event: ButtonClick) {
        val (entityId, hash, from, to, option) = event
        if (verifyWindow(entityId, hash)) {
            val widgetId = hash shr 16
            val componentId = hash - (widgetId shl 16)
            es.dispatch(WindowInteraction(entityId, widgetId, componentId, from, to, option))
        }
    }

    private fun getIndex(id: Int, resizeable: Boolean): Int {
        return getPane(id).getIndex(resizeable)
    }

    private fun getPane(id: Int): WindowPane = panes[id] ?: WindowPane.MAIN_SCREEN

    private fun WindowPane.getIndex(resizeable: Boolean): Int {
        return if (resizeable) resizable else fixed
    }

    private fun isPermanent(pane: WindowPane): Boolean {
        return pane != WindowPane.MAIN_SCREEN
    }

    /**
     * Walks recursively through [root] and all of it's children calling [predicate] each time
     * @param root The window to walk down
     * @param parent The [root]'s parent id
     * @param predicate If true then walk returns the found id
     * @return The id of the first window when [predicate] returns true
     */
    private fun Map<Int, List<Int>?>.walk(root: Int, parent: Int = getParent(root), predicate: (window: Int, parent: Int) -> Boolean): Int? {
        if (predicate.invoke(root, parent)) {
            return root
        }

        if (containsKey(root)) {
            for (child in getValue(root) ?: return null) {
                val result = walk(child, root, predicate)
                if (result != null) {
                    return result
                }
            }
        }
        return null
    }

    /**
     * Finds the parent of a window
     * @return The parent id OR the child's id
     */
    private fun Map<Int, List<Int>?>.getParent(child: Int): Int {
        for ((parentId, children) in this) {
            if (children != null && children.contains(child)) {
                return parentId
            }
        }
        return child
    }

}