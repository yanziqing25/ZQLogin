package cn.yzq25.login.event.player;

import cn.nukkit.Player;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;
import cn.yzq25.login.event.LoginPluginEvent;

/**
 * Created by Yanziqing25
 */
public class PlayerPasswordChangeEvent extends LoginPluginEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private int type;
    public static final int TYPE_COMMAND = 0;

    public PlayerPasswordChangeEvent(Player player, int type) {
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
