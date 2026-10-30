
package dev.autoitemclear;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class AutoItemClear extends JavaPlugin implements Listener {

    private NamespacedKey droppedKey;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        droppedKey = new NamespacedKey(this, "player_dropped");

        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("AutoItemClear enabled!");
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDrop(PlayerDropItemEvent event) {
        Item item = event.getItemDrop();

        item.getPersistentDataContainer().set(
            droppedKey, PersistentDataType.BYTE, (byte) 1
        );

        long seconds = Math.max(
            1, getConfig().getLong("despawn-seconds", 30)
        );

        getServer().getScheduler().runTaskLater(this, () -> {
            if (item.isValid() && !item.isDead()) {
                item.remove();
            }
        }, seconds * 20L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemMerge(ItemMergeEvent event) {
        if (isPlayerDropped(event.getEntity())
                || isPlayerDropped(event.getTarget())) {
            event.setCancelled(true);
        }
    }

    private boolean isPlayerDropped(Item item) {
        return item.getPersistentDataContainer().has(
            droppedKey, PersistentDataType.BYTE
        );
    }
}
