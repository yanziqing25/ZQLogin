package cn.yzq25.login.provider;

import cn.nukkit.Player;
import cn.nukkit.utils.Config;
import cn.yzq25.login.LoginMain;
import cn.yzq25.utils.ZQUtils;

import java.util.*;

/**
 * Created by Yanziqing25
 */
public class Local {
    private LoginMain login;

    public Local() {
        this.login = LoginMain.getInstance();
    }

    private String getPlayerPath(String player) {
        return login.playerDataFolder + player.toLowerCase() + ".yml";
    }

    public Map<String, String> getPlayerInfo(String player) {
        Map<String, String> playerinfo = new HashMap<>();
        new Config(getPlayerPath(player), Config.YAML).getAll().forEach((key, value) -> playerinfo.put(key, value.toString()));
        return playerinfo;
    }

    public void updatePlayerInfo(Player player) {
        Map<String, String> playerinfo = getPlayerInfo(player.getName());
        playerinfo.put("client_id", player.getClientId().toString());
        playerinfo.put("ip_address", player.getAddress());
        playerinfo.put("last_time", ZQUtils.transformDateTime(ZQUtils.getDateTime()));
        savePlayerInfo(player.getName(), playerinfo);
    }

    private boolean savePlayerInfo(String player, Map<String, String> playerinfo) {
        return new Config(getPlayerPath(player), Config.YAML){
            @Override
            public boolean save() {
                setAll(new LinkedHashMap<String, Object>(){
                    {
                        putAll(playerinfo);
                    }
                });
                return super.save();
            }
        }.save();
    }

    public boolean register(String player, String password, Long client_id, String address, Date registertime) {
        Map<String, String> playerinfo = new HashMap<>();
        playerinfo.put("password", (login.getConfig().getBoolean("using_MD5", false) ? ZQUtils.transformMD5(password, login.getConfig().getString("salt")) : password));
        playerinfo.put("client_id", client_id == null ? null : client_id.toString());
        playerinfo.put("ip_address", address);
        playerinfo.put("register_time", ZQUtils.transformDateTime(registertime));
        playerinfo.put("last_time", ZQUtils.transformDateTime(registertime));
        return savePlayerInfo(player, playerinfo);
    }

    public boolean changePassword(String player, String password) {
        Map<String, String> playerinfo = getPlayerInfo(player);
        playerinfo.put("password", (login.getConfig().getBoolean("using_MD5", false) ? ZQUtils.transformMD5(password, login.getConfig().getString("salt")) : password));
        return savePlayerInfo(player, playerinfo);
    }
}
