package cn.yzq25.login;

import cn.nukkit.AdventureSettings;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityRegainHealthEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.utils.TextFormat;
import cn.yzq25.login.event.player.*;
import cn.yzq25.login.event.player.PlayerLoginEvent;
import cn.yzq25.login.task.LoginTimeOutTask;
import cn.yzq25.utils.ZQUtils;

import java.util.HashMap;
import java.util.Objects;

/**
 * Created by Yanziqing25
 */
public class EventListener implements Listener {
    private static LoginMain api = LoginMain.getInstance();
    private static HashMap<String, String> registering = new HashMap<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!api.isLogined(event.getPlayer())) {
            event.setCancelled();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!api.isLogined(event.getPlayer())) {
            event.getPlayer().sendTip(TextFormat.GOLD + "欢迎来到服务器, 请先进行注册或登录!");
            event.setCancelled();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!api.isLogined(event.getPlayer())) {
            event.setCancelled();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!api.isLogined(event.getPlayer())) {
            event.setCancelled();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        api.removeUnloginedPlayer(event.getPlayer());
        registering.clear();
        //api.getServer().getScheduler().cancelTask(api);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if (!api.isLogined(event.getPlayer())) {
            event.setCancelled();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (!api.isLogined(event.getPlayer())) {
            event.setCancelled();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        if (!api.isLogined(event.getPlayer())) {
            event.setCancelled();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!api.isLogined(event.getPlayer())) {
            event.setCancelled();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!api.isLogined(event.getPlayer())) {
            event.setCancelled();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player) {
            if (!api.isLogined((Player) event.getEntity())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamageEvent(EntityDamageEvent event) {
        //如果这个被打实体是玩家（不允许被打）
        if (event.getEntity() instanceof Player) {
            if (!api.isLogined((Player) event.getEntity())) {
                event.setCancelled();
            }
        }
        //如果这个是玩家的实体打了另一个实体（不允许打别人）
        if (event instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) event).getDamager() instanceof Player) {
            if (!api.isLogined((Player) ((EntityDamageByEntityEvent) event).getDamager())) {
                event.setCancelled();
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.setJoinMessage(TextFormat.GOLD + player.getName() + "加入了服务器!");
        if (api.isRegistered(player)) {
            if (!api.needLogin(player)) {
                api.updatePlayerInfo(player);
                api.getServer().getPluginManager().callEvent(new cn.yzq25.login.event.player.PlayerLoginEvent(player, PlayerLoginEvent.TYPE_AUTO));
                player.sendMessage(TextFormat.GOLD + "欢迎回到服务器,已自动登录!");
            } else {
                api.addUnloginedPlayer(player);
                if (!player.getAdventureSettings().get(AdventureSettings.Type.WORLD_IMMUTABLE)) {
                    player.getAdventureSettings().set(AdventureSettings.Type.WORLD_IMMUTABLE, true);
                    player.getAdventureSettings().update();
                }
                api.getServer().getScheduler().scheduleDelayedTask(new LoginTimeOutTask(api, player), api.logintimeout);
                player.sendMessage(TextFormat.GOLD + "欢迎回到服务器,请输入密码登录!");
            }
        } else {
            api.addUnloginedPlayer(player);
            if (!player.getAdventureSettings().get(AdventureSettings.Type.WORLD_IMMUTABLE)) {
                player.getAdventureSettings().set(AdventureSettings.Type.WORLD_IMMUTABLE, true);
                player.getAdventureSettings().update();
            }
            api.getServer().getScheduler().scheduleDelayedTask(new LoginTimeOutTask(api, player), api.logintimeout);
            player.sendMessage(TextFormat.GOLD + "欢迎来到服务器,请输入你的ID(" + TextFormat.AQUA + player.getName() + TextFormat.GOLD + ")开始注册!");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        if (api.isLogined(player)) {
            if ((api.getConfig().getBoolean("using_MD5", false) ? ZQUtils.transformMD5(event.getMessage(), api.getConfig().getString("salt")) : event.getMessage()).equals(api.getPlayerInfo(player.getName()).get("password"))) {
                player.sendMessage(TextFormat.GOLD + "你差点暴露密码哦!");
                event.setCancelled();
            }
            return;
        }
        event.setCancelled();
        if (!api.isRegistered(player)) {
            if (!registering.containsKey(player.getName())) {//如果说不存在player的name这个key
                if (!Objects.equals(event.getMessage(), player.getName())) {
                    player.sendMessage(TextFormat.RED + "ID验证失败,请正确输入你的ID!");
                    return;
                }
                registering.put(player.getName(), null);//那就创建player的name这个key跳到②
                player.sendMessage(TextFormat.GREEN + "ID验证成功,请输入你想要注册的密码!");
                return;
            } else if (Objects.equals(registering.get(player.getName()), null)) {//②
                if (event.getMessage().equals(player.getName().toLowerCase()) || event.getMessage().equals(player.getName())) {
                    player.sendMessage(TextFormat.RED + "密码不能与ID相同,请重新输入!");
                    return;
                }
                if (!event.getMessage().matches("^[0-9a-zA-Z]{6,16}$")) {
                    player.sendMessage(TextFormat.RED + "密码只能是6-16位的字母或数字组成!");
                    return;
                }
                registering.put(player.getName(), event.getMessage());//跳到③
                player.sendMessage(TextFormat.BLUE + "请再输一次密码,错误输入将重新开始设置密码!");
                return;
            } else if (registering.get(player.getName()) != null) {//③
                if (Objects.equals(event.getMessage(), registering.get(player.getName()))) {
                    registering.remove(player.getName());
                    if (api.register(player, event.getMessage())) {
                        api.removeUnloginedPlayer(player);
                        registering.clear();
                        if (player.getAdventureSettings().get(AdventureSettings.Type.WORLD_IMMUTABLE)) {
                            player.getAdventureSettings().set(AdventureSettings.Type.WORLD_IMMUTABLE, false);
                            player.getAdventureSettings().update();
                        }
                        api.getServer().getPluginManager().callEvent(new PlayerLoginEvent(player, PlayerLoginEvent.TYPE_REGISTER));
                        player.sendMessage(TextFormat.GREEN + "注册成功~ 2小时内同IP将会自动登录,免输密码!");
                    } else {
                        player.sendMessage(TextFormat.RED + "注册失败!未知错误");
                    }
                } else {
                    player.sendMessage(TextFormat.RED + "两次输入密码不同,请重新设置密码!");
                    registering.put(player.getName(), null);
                }
            }
        } else {
            if (api.login(player, event.getMessage())) {
                api.removeUnloginedPlayer(player);
                registering.clear();
                if (player.getAdventureSettings().get(AdventureSettings.Type.WORLD_IMMUTABLE)) {
                    player.getAdventureSettings().set(AdventureSettings.Type.WORLD_IMMUTABLE, false);
                    player.getAdventureSettings().update();
                }
                api.updatePlayerInfo(player);
                api.getServer().getPluginManager().callEvent(new PlayerLoginEvent(player, PlayerLoginEvent.TYPE_PASSWORD));
                player.sendMessage(TextFormat.GREEN + "登录成功!");
            } else {
                player.sendMessage(TextFormat.RED + "登录失败,密码错误!");
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPreLogin(PlayerPreLoginEvent event) {
        Player player = event.getPlayer();
        if (player.getName().equals("Steve") || player.getName().equals("steve")) {
            event.setKickMessage(TextFormat.RED + "客户端默认用户名不允许注册!");
            event.setCancelled();
            return;
        }
        if (player.getName().length() > 16) {
            event.setKickMessage(TextFormat.RED + "用户名过长,不允许注册!");
            event.setCancelled();
            return;
        }
        if (api.getConfig().getBoolean("bind-cid")) {
            if (!api.canJoin(player, player.getClientId())) {
                event.setKickMessage(TextFormat.RED + "恶意注册小号或非法登录!如有疑问请联系QQ群399698274");
                event.setCancelled();
            }
        }
        if (LoginMain.getInstance().getServer().getOnlinePlayers().containsValue(player)) {
            event.setKickMessage(TextFormat.RED + "您已在游戏中，禁止重复登录!");
            event.setCancelled();
        }
    }
}
