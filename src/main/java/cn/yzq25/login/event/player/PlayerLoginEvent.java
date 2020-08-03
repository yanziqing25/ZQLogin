package cn.yzq25.login.event.player;

import cn.nukkit.Player;
import cn.nukkit.event.HandlerList;
import cn.yzq25.login.event.LoginPluginEvent;

/**
 * Created by Yanziqing25
 */
public class PlayerLoginEvent extends LoginPluginEvent {
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private int type;
    public static final int TYPE_REGISTER = 0;
    public static final int TYPE_PASSWORD = 1;
    public static final int TYPE_AUTO = 2;

    public PlayerLoginEvent (Player player, int type) {
        this.player = player;
        this.type = type;
    }

    public static HandlerList getHandlers() {
        return handlers;
    }

    public int getType() {
        return this.type;
    }

    public Player getPlayer() {
        return this.player;
    }
}
