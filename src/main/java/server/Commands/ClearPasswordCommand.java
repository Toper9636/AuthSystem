package server.Commands;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.form.response.FormResponseModal;
import cn.nukkit.form.window.FormWindowModal;
import server.API.AccountClass;
import server.AuthPlugin;


public class ClearPasswordCommand extends Command {

    public ClearPasswordCommand() {
        super(AuthPlugin.getInstance().config.getString("command.name"), AuthPlugin.getInstance().config.getString("command.description"));
        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[] {
                CommandParameter.newType("password ", CommandParamType.STRING),
                CommandParameter.newType("passwordConfirm ", CommandParamType.STRING)
        });
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] args) {
        if (!commandSender.isPlayer()) return false;
        Player p = (Player) commandSender;
        if (args.length != 2) {
            p.sendMessage(AuthPlugin.getInstance().config.getString("command.message.argsError").replace("%command%", this.getName()));
            return false;
        }
        String password = args[0];
        String password2 = args[1];
        if (!password.equals(password2)) {
            p.sendMessage(AuthPlugin.getInstance().config.getString("command.message.passwordNotEquals"));
            return false;
        }
        if (AuthPlugin.createdAccounts.get(p.getUniqueId()).equalsPassword(password)) {
            p.sendMessage(AuthPlugin.getInstance().config.getString("command.message.passwordEqualsAsOldPassword"));
            return false;
        }
        if (password.length() < AuthPlugin.getInstance().config.getInt("command.minimumLengthToAcceptPassword") || AccountClass.passwordComplexity(password) < AuthPlugin.getInstance().config.getInt("command.minimumPointsToAcceptPassword")) {
            p.sendMessage(AuthPlugin.getInstance().config.getString("command.message.passwordVeryEasy"));
            return false;
        }
        FormWindowModal form = new FormWindowModal(
                AuthPlugin.getInstance().config.getString("command.form.title"),
                AuthPlugin.getInstance().config.getString("command.form.content"),
                AuthPlugin.getInstance().config.getString("command.form.textButtonYes"),
                AuthPlugin.getInstance().config.getString("command.form.textButtonNo")
        );
        form.addHandler((player, formId) -> {
            FormResponseModal response = form.getResponse();
            if (response == null) {
                player.sendMessage(AuthPlugin.getInstance().config.getString("command.form.close"));
                return;
            }
            if (response.getClickedButtonText().contains("Да")) {
                AuthPlugin.createdAccounts.replace(player.getUniqueId(), AuthPlugin.createdAccounts.get(player.getUniqueId()).replacePassword(password));
                player.sendMessage(AuthPlugin.getInstance().config.getString("command.form.buttonYes").replace("%password%", password));
            } else {
                player.sendMessage(AuthPlugin.getInstance().config.getString("command.form.buttonNo"));
            }
        });
        p.showFormWindow(form);
        return false;
    }
}
