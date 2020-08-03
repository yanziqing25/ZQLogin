package cn.yzq25.login.command;

import cn.nukkit.command.*;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.TextFormat;
import cn.yzq25.login.LoginMain;

import java.util.LinkedHashMap;

/**
 * Created by Yanziqing25
 */
public class BindEmailCommand extends PluginCommand<LoginMain> implements CommandExecutor {

    public BindEmailCommand() {
        super("bindemail", LoginMain.getInstance());
        this.setExecutor(this);
        this.setCommandParameters(new LinkedHashMap<String, CommandParameter[]>(){{put("default", new CommandParameter[]{new CommandParameter("email", CommandParamType.TEXT, false)});}});
        this.setAliases(new String[]{"be"});
        this.setPermission("login.command.bindemail");
        this.setDescription("绑定邮箱命令");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage(TextFormat.RED + "请在游戏中使用此命令!");
            return false;
        }
        if (args.length != 1) {
            return false;
        }
        return false;
    }
}
