package cn.yzq25.login;

import cn.nukkit.Player;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;
import cn.yzq25.extension.ExtensionMain;
import cn.yzq25.extension.MySQLDatabase;
import cn.yzq25.extension.RelationalDatabase;
import cn.yzq25.extension.SQLServerDatabase;
import cn.yzq25.login.command.ChangePasswordCommand;
import cn.yzq25.login.event.player.PlayerPasswordChangeEvent;
import cn.yzq25.login.provider.Local;
import cn.yzq25.utils.ZQUtils;

import java.io.File;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.*;

/**
 * Created by Yanziqing25
 */
public class LoginMain extends PluginBase {
    private static LoginMain instance;
    public String playerDataFolder;
    public int logintimeout;
    private ArrayList<String> unloginedPlayers;
    public RelationalDatabase database;
    private Local local;
    private String loginMode;

    public LoginMain() {
    }

    public static LoginMain getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
        saveDefaultConfig();
        this.playerDataFolder = getDataFolder().getPath() + "/players/";
        unloginedPlayers = new ArrayList<>();
        this.logintimeout = (getConfig().getInt("login-timeout")) * 20;
    }

    @Override
    public void onEnable() {
        if (getConfig().getBoolean("check_update", true)) {
            ZQUtils.checkPluginUpdate(this);
        }
        if (ExtensionMain.getDatabase() == null) {
            this.loginMode = "local";
            this.local = new Local();
        } else {
            this.loginMode = ExtensionMain.getDatabase().getName();
        }
        switch (this.loginMode) {
            case "MySQL":
                this.database = (MySQLDatabase) ExtensionMain.getDatabase();
                break;
            case "SQLServer":
                this.database = (SQLServerDatabase) ExtensionMain.getDatabase();
                break;
        }
        createUserTable();
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        getServer().getCommandMap().register("login", new ChangePasswordCommand(), "changepassword");

        getLogger().info(TextFormat.GREEN + "插件加载成功! By:Yanziqing25");
    }

    @Override
    public void onDisable() {
        getLogger().info(TextFormat.RED + "插件已关闭!");
    }

    public boolean isLogined(Player player) {
        return isLogined(player.getName());
    }

    private boolean isLogined(String player) {
        return !this.unloginedPlayers.contains(player);
    }

    public boolean isRegistered(Player player) {
        return isRegistered(player.getName());
    }

    private boolean isRegistered(String player) {
        return getPlayerInfo(player).get("password") != null;
    }

    public void addUnloginedPlayer(Player player) {
        addUnloginedPlayer(player.getName());
    }

    private void addUnloginedPlayer(String player) {
        if (!unloginedPlayers.contains(player)) {
            this.unloginedPlayers.add(player);
        }
    }

    public void removeUnloginedPlayer(Player player) {
        removeUnloginedPlayer(player.getName());
    }

    private void removeUnloginedPlayer(String player) {
        this.unloginedPlayers.remove(player);
    }

    public boolean login(Player player, String password) {
        return login(player.getName(), password);
    }

    private boolean login(String player, String password) {
        Map<String, String> playerinfo = getPlayerInfo(player);
        return playerinfo != null && playerinfo.get("password") != null && playerinfo.get("password").equals(getConfig().getBoolean("using_MD5", false) ? ZQUtils.transformMD5(password, getConfig().getString("salt")) : password);
    }

    public boolean needLogin(Player player) {
        return needLogin(player.getName(), player.getClientId(), player.getAddress());
    }

    private boolean needLogin(String player, Long client_id, String address) {
        Map<String, String> playerinfo = getPlayerInfo(player);
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(playerinfo.get("last_time") == null ? "0" : playerinfo.get("last_time"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return !(playerinfo != null &&
                playerinfo.containsKey("client_id") &&
                playerinfo.containsKey("ip_address") &&
                playerinfo.containsKey("last_time") &&
                playerinfo.get("client_id").equals(client_id == null ? " " : client_id.toString()) &&
                playerinfo.get("ip_address").equals(address) &&
                (ZQUtils.getDateTime().getTime() - date.getTime()) <= 2L * 60 * 60 * 1000);
    }

    public boolean canJoin(Player player, Long client_id) {
        return canJoin(player.getName(), client_id);
    }

    private boolean canJoin(String player, Long client_id) {
        Set<String> playerList = getSameClientIdPlayers(client_id);
        return playerList.isEmpty() || playerList.contains(player.toLowerCase());
    }

    public boolean register(Player player, String password) {
        return register(player.getName(), password, player.getClientId(), player.getAddress(), ZQUtils.getDateTime());
    }

    private boolean register(String player, String password, Long client_id, String ip_address, Date register_time) {
        if (isRegistered(player)) {
            return false;
        }
        switch (this.loginMode) {
            case "MySQL":
                return this.database.executeSQL("INSERT INTO `user` (`username`, `name`, `password`, `client_id`, `ip_address`, `register_time`, `last_time`) VALUES ('" + player.toLowerCase() + "', '" + player + "', '" + (getConfig().getBoolean("using_MD5", false) ? ZQUtils.transformMD5(password, getConfig().getString("salt")) : password) + "', '" + client_id + "', '" + ip_address + "', '" + ZQUtils.transformDateTime(register_time) + "', '" + ZQUtils.transformDateTime(register_time) + "');");
            case "SQLServer":
                return this.database.executeSQL("INSERT INTO [user] ([username], [name], [password], [client_id], [ip_address], [register_time], [last_time]) VALUES ('" + player.toLowerCase() + "', '" + player + "', '" + (getConfig().getBoolean("using_MD5", false) ? ZQUtils.transformMD5(password, getConfig().getString("salt")) : password) + "', '" + client_id + "', '" + ip_address + "', '" + ZQUtils.transformDateTime(register_time) + "', '" + ZQUtils.transformDateTime(register_time) + "');");
            case "local":
                return this.local.register(player, password, client_id, ip_address, register_time);
            default:
                return this.local.register(player, password, client_id, ip_address, register_time);
        }
    }

    public boolean changePassword(Player player, String password) {
        return changePassword(player.getName(), password);
    }

    private boolean changePassword(String player, String password) {
        PlayerPasswordChangeEvent playerPasswordChangeEvent = new PlayerPasswordChangeEvent(getServer().getPlayer(player), PlayerPasswordChangeEvent.TYPE_COMMAND);
        getServer().getPluginManager().callEvent(playerPasswordChangeEvent);
        if (playerPasswordChangeEvent.isCancelled()) {
            return false;
        }
        switch (this.loginMode) {
            case "MySQL":
                return this.database.executeSQL("UPDATE `user` SET `password` = '" + (getConfig().getBoolean("using_MD5", false) ? ZQUtils.transformMD5(password, getConfig().getString("salt")) : password) + "' WHERE (`username` = '" + player.toLowerCase() + "');");
            case "SQLServer":
                return this.database.executeSQL("UPDATE [user] SET [password] = '" + (getConfig().getBoolean("using_MD5", false) ? ZQUtils.transformMD5(password, getConfig().getString("salt")) : password) + "' WHERE ([username] = '" + player.toLowerCase() + "');");
            case "local":
                return this.local.changePassword(player, password);
            default:
                return this.local.changePassword(player, password);
        }
    }

    //TODO 此处可以有优化
    public Map<String, String> getPlayerInfo(String player) {
        ResultSet rs;
        Map<String, String> playerInfo = new HashMap<>();
        switch (this.loginMode) {
            case "MySQL":
                rs = this.database.executeQuery("SELECT * FROM `user` WHERE `username` = '" + player.toLowerCase() + "';");
                try {
                    ResultSetMetaData metadatas = rs.getMetaData();
                    while (rs.next()) {
                        for (int i = 1;i <= metadatas.getColumnCount();i++) {
                            playerInfo.put(metadatas.getColumnLabel(i), rs.getString(i));
                        }
                    }
                    return playerInfo;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            case "SQLServer":
                rs = this.database.executeQuery("SELECT * FROM [user] WHERE ([username] = '" + player.toLowerCase() + "');");
                try {
                    ResultSetMetaData metadatas = rs.getMetaData();
                    while (rs.next()) {
                        for (int i=1;i <= metadatas.getColumnCount();i++) {
                            playerInfo.put(metadatas.getColumnLabel(i), rs.getString(i));
                        }
                    }
                    return playerInfo;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            case "local":
                return this.local.getPlayerInfo(player);
            default:
                return this.local.getPlayerInfo(player);
        }
    }

    public void updatePlayerInfo(Player player) {
        switch (this.loginMode) {
            case "MySQL":
                this.database.executeSQL("UPDATE `user` SET `client_id` = '" + player.getClientId() + "', `ip_address` = '" + player.getAddress() + "', `last_time` = '" + ZQUtils.transformDateTime(ZQUtils.getDateTime()) + "' WHERE (`username` = '" + player.getName().toLowerCase() + "');");
                break;
            case "SQLServer":
                this.database.executeSQL("UPDATE [user] SET [client_id] = '" + player.getClientId() + "', ip_address = '" + player.getAddress() + "', last_time = '" + ZQUtils.transformDateTime(ZQUtils.getDateTime()) + "' WHERE (username = '" + player.getName().toLowerCase() + "');");
                break;
            case "local":
                this.local.updatePlayerInfo(player);
                break;
            default:
                this.local.updatePlayerInfo(player);
                break;
        }
    }

    private boolean createUserTable() {
        switch (this.loginMode) {
            case "MySQL":
                return this.database.executeSQL("CREATE TABLE IF NOT EXISTS `user` (" +
                        "`id` INT (11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户编号'," +
                        "`username` VARCHAR (16) NOT NULL COMMENT '用户名(一般为小写)'," +
                        "`name` VARCHAR (16) NOT NULL COMMENT '昵称'," +
                        "`password` VARCHAR (32) NOT NULL COMMENT '密码(采用MD5+salt加密)'," +
                        "`client_id` VARCHAR (255) NOT NULL COMMENT '最后一次登陆的客户端ID'," +
                        "`ip_address` VARCHAR (15) NOT NULL COMMENT '最后一次登陆的IP地址'," +
                        "`register_time` datetime NOT NULL COMMENT '注册时间'," +
                        "`last_time` datetime NULL COMMENT '最后一次登录的时间'," +
                        "`status` VARCHAR (255) NULL DEFAULT 'normal' COMMENT '用户状态'," +
                        "PRIMARY KEY (`id`)," +
                        "UNIQUE INDEX `username` (`username`) USING BTREE" +
                        ") ENGINE = INNODB COMMENT = '用户主表';");
            case "SQLServer":
                return this.database.executeSQL("IF NOT EXISTS ( SELECT * FROM sysobjects WHERE name = 'user' ) CREATE TABLE [user] (" +
                        "[id] INT IDENTITY ( 1, 1 ) NOT NULL," +
                        "[username] VARCHAR ( 16 ) NOT NULL," +
                        "[name] VARCHAR ( 16 ) NOT NULL," +
                        "[password] VARCHAR ( 32 ) NOT NULL," +
                        "[client_id] VARCHAR ( 255 ) NOT NULL," +
                        "[ip_address] VARCHAR ( 15 ) NOT NULL," +
                        "[register_time] datetime2 NOT NULL," +
                        "[last_time] datetime2 NULL," +
                        "[status] VARCHAR ( 255 ) NULL DEFAULT 'normal'," +
                        "PRIMARY KEY CLUSTERED ( [id] ) WITH ( PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON ) " +
                        ")" +
                        "IF NOT EXISTS ( SELECT * FROM sysindexes WHERE name = 'username' ) CREATE UNIQUE NONCLUSTERED INDEX [username] ON [user] (" +
                        "[username]" +
                        ")");
            case "local":
                return new File(playerDataFolder).mkdirs();
            default:
                return new File(playerDataFolder).mkdirs();
        }
    }

    private Set<String> getSameClientIdPlayers(Long client_id) {
        ResultSet rs;
        Set<String> playerSet;
        switch (this.loginMode) {
            case "MySQL":
                rs = this.database.executeQuery("SELECT `username` FROM `user` WHERE `client_id` = '" + client_id + "';");
                playerSet =  new HashSet<>();
                try {
                    while (rs.next()) {
                        playerSet.add(rs.getString("username"));
                    }
                    return playerSet;
                } catch (Exception e) {
                    e.printStackTrace();
                    return new HashSet<>();
                }
            case "SQLServer":
                rs = this.database.executeQuery("SELECT `username` FROM [user] WHERE [client_id] = '" + client_id + "';");
                playerSet = new HashSet<>();
                try {
                    while (rs.next()) {
                        playerSet.add(rs.getString("username"));
                    }
                    return playerSet;
                } catch (Exception e) {
                    e.printStackTrace();
                    return new HashSet<>();
                }
            case "local":
                //TODO
                return null;
            default:
                return null;
        }
    }

    public String getPlayerPassword(String player) {
        ResultSet rs;
        switch (this.loginMode) {
            case "MySQL":
                rs = this.database.executeQuery("SELECT `password` FROM `user` WHERE `username` = '" + player.toLowerCase() + "';");
                try {
                    rs.next();
                    return rs.getString("password");
                } catch (SQLException e) {
                    e.printStackTrace();
                    return null;
                }
            case "local":
                return null;
            default:
                return null;
        }
    }

    public String getPlayerIPAddress(String player) {
        ResultSet rs;
        switch (this.loginMode) {
            case "MySQL":
                rs = this.database.executeQuery("SELECT `ip_address` FROM `user` WHERE `username` = '" + player.toLowerCase() + "';");
                try {
                    rs.next();
                    return rs.getString("ip_address");
                } catch (SQLException e) {
                    e.printStackTrace();
                    return null;
                }
            case "local":
                return null;
            default:
                return null;
        }
    }

    public String getPlayerClientID(String player) {
        ResultSet rs;
        switch (this.loginMode) {
            case "MySQL":
                rs = this.database.executeQuery("SELECT `client_id` FROM `user` WHERE `username` = '" + player.toLowerCase() + "';");
                try {
                    rs.next();
                    return rs.getString("client_id");
                } catch (SQLException e) {
                    e.printStackTrace();
                    return null;
                }
            case "local":
                return null;
            default:
                return null;
        }
    }

    public Date getPlayerLastTime(String player) {
        ResultSet rs;
        switch (this.loginMode) {
            case "MySQL":
                rs = this.database.executeQuery("SELECT `last_time` FROM `user` WHERE `username` = '" + player.toLowerCase() + "';");
                try {
                    rs.next();
                    return rs.getDate("last_time");
                } catch (SQLException e) {
                    e.printStackTrace();
                    return null;
                }
            case "local":
                return null;
            default:
                return null;
        }
    }
}
