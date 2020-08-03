package cn.yzq25.login.task;

import cn.nukkit.Player;
import cn.nukkit.scheduler.PluginTask;
import cn.nukkit.utils.TextFormat;
import cn.yzq25.login.LoginMain;

/**
 * Created by Yanziqing25
 */
public class LoginTimeOutTask extends PluginTask<LoginMain> {
    private Player player;

    public LoginTimeOutTask(LoginMain plugin, Player player) {
        super(plugin);
        this.player = player;
    }

    @Override
    public void onRun(int currentTick) {
        if (!getOwner().isLogined(player)) {
            player.kick(TextFormat.RED + "登陆超时!");
        }
    }
}
