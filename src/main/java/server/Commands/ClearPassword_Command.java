package server.Commands;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.form.response.FormResponseModal;
import cn.nukkit.form.window.FormWindowModal;
import server.API.Account_Class;
import server.Auth_Plugin;


public class ClearPassword_Command extends Command {

    public ClearPassword_Command() {
        super(Auth_Plugin.getInstance().config.getString("command.name"), Auth_Plugin.getInstance().config.getString("command.description"));
        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[] {
                CommandParameter.newType("password ", CommandParamType.STRING),
                CommandParameter.newType("password_confirm ", CommandParamType.STRING)
        });
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] args) {
        if (!commandSender.isPlayer()) return false;
        Player p = (Player) commandSender;
        if (args.length != 2) {
            p.sendMessage(Auth_Plugin.getInstance().config.getString("command.message.args_error").replace("%command%", this.getName()));
            return false;
        }
        String password = args[0];
        String password_2 = args[1];
        if (!password.equals(password_2)) {
            p.sendMessage(Auth_Plugin.getInstance().config.getString("command.message.password_not_equals"));
            return false;
        }
        if (Auth_Plugin.createdAccounts.get(p.getUniqueId()).equalsPassword(password)) {
            p.sendMessage(Auth_Plugin.getInstance().config.getString("command.message.password_equals_as_old_password"));
            return false;
        }
        if (password.length() < Auth_Plugin.getInstance().config.getInt("command.minimum_length_to_accept_password") || Account_Class.passwordComplexity(password) < Auth_Plugin.getInstance().config.getInt("command.minimum_points_to_accept_password")) {
            p.sendMessage(Auth_Plugin.getInstance().config.getString("command.message.password_very_easy"));
            return false;
        }
        FormWindowModal form = new FormWindowModal(
                Auth_Plugin.getInstance().config.getString("command.form.title"),
                Auth_Plugin.getInstance().config.getString("command.form.content"),
                Auth_Plugin.getInstance().config.getString("command.form.text_button_yes"),
                Auth_Plugin.getInstance().config.getString("command.form.text_button_no")
        );
        form.addHandler((player, form_id) -> {
            FormResponseModal response = form.getResponse();
            if (response == null) {
                player.sendMessage(Auth_Plugin.getInstance().config.getString("command.form.close"));
                return;
            }
            if (response.getClickedButtonText().contains("Да")) {
                Auth_Plugin.createdAccounts.replace(player.getUniqueId(), Auth_Plugin.createdAccounts.get(player.getUniqueId()).replacePassword(password));
                player.sendMessage(
                        Auth_Plugin.getInstance().config.getString("command.form.button_yes").replace("%password%", password)
                );
            } else {
                player.sendMessage(Auth_Plugin.getInstance().config.getString("command.form.button_no"));
            }
        });
        p.showFormWindow(form);
        return false;
    }
}
