package cn.lacknb.randomtp.listen;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author gitsilence
 * @date 2023-10-15
 */
public class RandomTpListener implements Listener, CommandExecutor {

    private final Map<Player, Long> teleportCooldowns = new HashMap<>();
    private static final long TELEPORT_COOLDOWN = 60 * 1000; // 冷却时间，单位为毫秒

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        ItemStack item = event.getItem();

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            if (item != null && item.getType() == Material.COMPASS && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta.getDisplayName().equals("随机传送器")) {
                    // 检查传送冷却
                    if (teleportCooldowns.containsKey(player)) {
                        long lastTeleportTime = teleportCooldowns.get(player);
                        long currentTime = System.currentTimeMillis();
                        long cooldownRemaining = lastTeleportTime + TELEPORT_COOLDOWN - currentTime;

                        if (cooldownRemaining > 0) {
                            player.sendMessage("你需要等待 " + (cooldownRemaining / 1000) + " 秒才能再次使用随机传送器。");
                            event.setCancelled(true);
                            return;
                        }
                    }

                    // 执行传送逻辑
                    teleportPlayerRandomly(player);

                    // 更新传送冷却时间
                    teleportCooldowns.put(player, System.currentTimeMillis());
                    teleportPlayerRandomly(player);
                }
            }
        }
    }

    private void teleportPlayerRandomly(Player player) {
        World world = player.getWorld();
        Random random = new Random();

        int x = random.nextInt(10000) - 5000; // 随机生成 x 坐标
        int z = random.nextInt(10000) - 5000; // 随机生成 z 坐标
        int y = world.getHighestBlockYAt(x, z); // 获取最高方块的高度

        Location location = new Location(world, x, y, z);
        Block block = world.getBlockAt(location);

        // 检查最高方块是否安全
        if (isSafeLocation(block)) {
            player.teleport(location);
            // 检查玩家是否被卡住
            Block playerBlock = player.getLocation().getBlock();
            if (playerBlock.getType().isSolid()) {
                Location safeLocation = findSafeLocation(player.getLocation());
                // 玩家被卡住，将其移动到一个安全的位置
                player.teleport(safeLocation);
            }
            player.sendMessage(String.format("玩家%s传送到了 %s(x,y,z)", player.getName(),
                    formatLocation(location)));
        } else {
            // 重新生成坐标，直到找到安全的位置
            teleportPlayerRandomly(player);
        }
    }

    private Location findSafeLocation(Location location) {
        // 在附近搜索一个安全的位置，例如找到一个不是方块的空气方块
        // 你可以使用循环来搜索周围的方块，或者使用递归方法来扩大搜索范围
        // 这里只是一个示例，你可以根据实际需求进行修改
        for (int y = location.getBlockY(); y < location.getWorld().getMaxHeight(); y++) {
            Location tempLocation = new Location(location.getWorld(), location.getBlockX(), y, location.getBlockZ());
            Block block = tempLocation.getBlock();
            if (!block.getType().isSolid()) {
                return tempLocation;
            }
        }
        return location; // 如果找不到安全位置，则返回原始位置
    }

    private String formatLocation(Location location) {
        return location.getWorld().getName() + " (" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ")";
    }

    private boolean isSafeLocation(Block block) {
        // 检查方块是否安全，例如检查是否是岩浆、火焰等危险方块
        Material type = block.getType();
        return type != Material.LAVA && type != Material.FIRE;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            // 创建物品
            ItemStack teleportItem = new ItemStack(Material.COMPASS);
            ItemMeta meta = teleportItem.getItemMeta();
            assert meta != null;
            // 创建一个EnchantmentStorageMeta对象，并设置颜色
            EnchantmentStorageMeta enchantmentMeta = (EnchantmentStorageMeta) meta;
            enchantmentMeta.setDisplayName("随机传送器");
            enchantmentMeta.addStoredEnchant(Enchantment.DAMAGE_ALL, 1, true);

            teleportItem.setItemMeta(meta);

             // 创建的随机传送器物品

            player.getInventory().addItem(teleportItem);
            player.updateInventory(); // 更新玩家的背包显示
            sender.sendMessage("你获得了随机传送器物品！");
            return true;
        }
        return false;
    }
}
