package cn.yzq25.login.command;

import cn.nukkit.Player;
import cn.nukkit.command.*;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.TextFormat;
import cn.yzq25.login.LoginMain;

import java.util.LinkedHashMap;

/**
 * Created by Yanziqing25
 */
public class ChangePasswordCommand extends PluginCommand<LoginMain> implements CommandExecutor {

    public ChangePasswordCommand() {
        super("changepassword", LoginMain.getInstance());
        this.setExecutor(this);
        this.setCommandParameters(new LinkedHashMap<String, CommandParameter[]>(){{put("default", new CommandParameter[]{new CommandParameter("新密码", CommandParamType.TEXT, false)});}});
        this.setAliases(new String[]{"cpw", "修改密码"});
        this.setPermission("login.command.changepassword");
        this.setDescription("修改密码命令");
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
        if (args[0].equals(sender.getName().trim().toLowerCase()) || args[0].equals(sender.getName().trim())) {
            sender.sendMessage(TextFormat.RED + "密码不能与 ID 相同. 请重新输入");
            return false;
        }
        if (!args[0].matches("^[0-9a-zA-Z]{6,16}$")) {
            sender.sendMessage(TextFormat.RED + "密码只能是6-16位的字母或数字组成");
            return false;
        }
        if (getPlugin().changePassword((Player) sender, args[0])) {
            sender.sendMessage(TextFormat.GREEN + "修改密码成功!新密码为:" + args[0]);
            return true;
        } else {
            sender.sendMessage(TextFormat.RED + "修改密码失败,发生未知错误!");
            return false;
        }
    }
}
