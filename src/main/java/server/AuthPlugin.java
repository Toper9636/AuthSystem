package server;

import cn.nukkit.IPlayer;
import cn.nukkit.OfflinePlayer;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.SimpleCommandMap;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.inventory.InventoryTransactionEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.level.Location;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.potion.Effect;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.Config;
import com.google.common.io.Files;
import lombok.SneakyThrows;
import server.API.AccountClass;
import server.API.ConfigInitialization;
import server.API.EncryptSettings;
import server.Commands.ClearPasswordCommand;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.UUID;

public class AuthPlugin extends PluginBase implements Listener {
    public static EncryptSettings encryptSettings;
    public static Connection connection;
    public static HashMap<UUID, AccountClass> createdAccounts = new HashMap<>();
    public static HashMap<UUID, String> lastIp = new HashMap<>();
    public static Location joinLocation;
    public static Location spawnLocation;
    public boolean mysql = false;
    private static AuthPlugin instance;
    public Config config = new Config(new File(this.getDataFolder() + "/config.yml"), Config.YAML);
    public int countCreatedTask;

    public static AuthPlugin getInstance() {
        return instance;
    }

    public static IPlayer optimizeGetOfflinePlayer(UUID uuid) {
        return new OfflinePlayer(Server.getInstance(), uuid);
    }

    public void onLoad() {
        instance = this;
    }

    @SneakyThrows
    public void onEnable() {
        ConfigInitialization.init();

        mysql = config.getBoolean("load-and-save-from-mysql");

        encryptSettings = new EncryptSettings(
                config.getString("encrypt-settings.key").toCharArray(),
                config.getString("encrypt-settings.salt").getBytes(),
                config.getInt("encrypt-settings.iteration-count"),
                config.getInt("encrypt-settings.key-length"),
                this.loadVector(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 45, 0, 0, 0})
        );

        if (mysql) {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try {
                Connection connection = DriverManager.getConnection("jdbc:mysql://" + getMessage(config.getString("database.host")) + ":" + getMessage(config.getString("database.port")) + "/" + getMessage(config.getString("database.database")) + "?autoReconnect=true&useGmtMillisForDatetimes=true&rewriteBatchedStatements=true&serverTimezone=GMT&useSSL=false", getMessage(config.getString("database.username")), getMessage(config.getString("database.password")));
                if (connection == null || connection.isClosed()) {
                    Server.getInstance().getLogger().info("§cКритическая ошибка в получении базы данных!");
                    this.getPluginLoader().disablePlugin(this);
                    return;
                }
                AuthPlugin.connection = connection;
                this.loadAccounts(true);
            } catch (SQLException e) {
                Server.getInstance().getLogger().info("§cКритическая ошибка в получении базы данных!");
                this.getPluginLoader().disablePlugin(this);
            }
            Server.getInstance().getLogger().info("§l§aБаза данных успешно запущена!");
        } else {
            this.loadAccounts(false);
        }
        String defaultLevelName = Server.getInstance().getDefaultLevel().getFolderName();
        joinLocation = new Location(
                config.getDouble("joinLocation.x"),
                config.getDouble("joinLocation.y"),
                config.getDouble("joinLocation.z"),
                Server.getInstance().getLevelByName(getMessage(config.getString("joinLocation.levelName")).replace("%defaultLevel%", defaultLevelName))
        );
        spawnLocation = new Location(
                config.getDouble("spawnLocation.x"),
                config.getDouble("spawnLocation.y"),
                config.getDouble("spawnLocation.z"),
                Server.getInstance().getLevelByName(getMessage(config.getString("joinLocation.levelName")).replace("%defaultLevel%", defaultLevelName))
        );
        this.getLogger().info("§eПлагин запущен и работает. Author: Toper9636.");
        Server.getInstance().getPluginManager().registerEvents(this, this);
        this.registerCommand();
    }

    public byte[] loadVector(byte[] defaultVector) {
        try {
            String[] stringBytes = config.getString("encrypt-settings.vector").replaceAll("(?u)[^0-9,]", "").split(",");
            byte[] bytes = new byte[stringBytes.length];
            int i = 0;
            for (String s : stringBytes) {
                bytes[i] = Byte.parseByte(s);
                i++;
            }
            return bytes;
        } catch (Exception e) {
            return defaultVector;
        }
    }

    public void onDisable() {
        this.getLogger().info("§cDisable...");
        saveAccounts(mysql);
    }

    private void registerCommand() {
        SimpleCommandMap simpleCommandMap = this.getServer().getCommandMap();
        simpleCommandMap.register("clear_password", new ClearPasswordCommand());
    }

    @EventHandler
    public void joinCreateAccountListener(PlayerJoinEvent event) {
        config.reload();

        if (AccountClass.isRegistered(event.getPlayer().getUniqueId()) && AccountClass.isLogined(event.getPlayer().getUniqueId(), event.getPlayer().getAddress())) {
            event.getPlayer().sendMessage(getMessage(config.getString("joinEvent.successfullyLogin")));
            return;
        }
        boolean created = AccountClass.isRegistered(event.getPlayer().getUniqueId());
        event.getPlayer().setImmobile(true);
        event.getPlayer().addEffect(Effect.getEffect(Effect.INVISIBILITY).setAmplifier(0).setDuration(2000000000).setVisible(false));
        if (countCreatedTask <= 50) { // анти-лаг
            countCreatedTask++;
            Server.getInstance().getScheduler().scheduleRepeatingTask(new Task() {
                @Override
                public void onRun(int i) {
                    if (event.getPlayer() == null ||
                            !event.getPlayer().isOnline() ||
                            (!created && AccountClass.isRegistered(event.getPlayer().getUniqueId())) ||
                            (created && AccountClass.isLogined(event.getPlayer().getUniqueId(), event.getPlayer().getAddress()))) {
                        countCreatedTask--;
                        this.cancel();
                        return;
                    }
                    if (!created) event.getPlayer().teleport(joinLocation);
                }
            }, 20);
        }
        if (!created) {
            sendFormToCreateAccount(event.getPlayer(), null);
        } else {
            sendFormToLogin(event.getPlayer(), null);
        }
    }

    public String getMessage(String message) {
        return message.replace("%n%", "\n").replace("%t%", "\t");
    }

    @EventHandler
    public void authCommandEvent(PlayerCommandPreprocessEvent event) {
        if (!AccountClass.isRegistered(event.getPlayer().getUniqueId()) ||
                !AccountClass.isLogined(event.getPlayer().getUniqueId(), event.getPlayer().getAddress())) {
            event.setCancelled();
        }
    }

    @EventHandler
    public void authAttackEvent(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        if (!AccountClass.isRegistered(((Player) event.getDamager()).getUniqueId()) ||
                !AccountClass.isLogined(((Player) event.getDamager()).getUniqueId(), ((Player) event.getDamager()).getAddress())) {
            event.setCancelled();
        }
    }

    @EventHandler
    public void authDamageEvent(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        if (!AccountClass.isRegistered(((Player) event.getEntity()).getUniqueId()) ||
                !AccountClass.isLogined(((Player) event.getEntity()).getUniqueId(), ((Player) event.getEntity()).getAddress())) {
            event.setCancelled();
        }
    }

    @EventHandler
    public void authInteractEvent(PlayerInteractEvent event) {
        if (!AccountClass.isRegistered(event.getPlayer().getUniqueId()) ||
                !AccountClass.isLogined(event.getPlayer().getUniqueId(), event.getPlayer().getAddress())) {
            event.setCancelled();
        }
    }

    @EventHandler
    public void authInteractByEntityEvent(PlayerInteractEntityEvent event) {
        if (!AccountClass.isRegistered(event.getPlayer().getUniqueId()) ||
                !AccountClass.isLogined(event.getPlayer().getUniqueId(), event.getPlayer().getAddress())) {
            event.setCancelled();
        }
    }

    @EventHandler
    public void authTransactionEvent(InventoryTransactionEvent event) {
        if (!AccountClass.isRegistered(event.getTransaction().getSource().getUniqueId()) ||
                !AccountClass.isLogined(event.getTransaction().getSource().getUniqueId(), event.getTransaction().getSource().getAddress())) {
            event.setCancelled();
        }
    }

    @EventHandler
    public void authDropItemEvent(PlayerDropItemEvent event) {
        if (!AccountClass.isRegistered(event.getPlayer().getUniqueId()) ||
                !AccountClass.isLogined(event.getPlayer().getUniqueId(), event.getPlayer().getAddress())) {
            event.setCancelled();
        }
    }

    public void sendFormToCreateAccount(Player p, String addText) {
        FormWindowCustom form = new FormWindowCustom(getMessage(config.getString("createAccount.form.title")));
        if (addText != null && !addText.isEmpty()) {
            form.addElement(new ElementLabel(addText));
        }
        form.addElement(new ElementInput(getMessage(config.getString("createAccount.form.buttons.password-1.text")), getMessage(config.getString("createAccount.form.buttons.password-1.placeholder"))));
        form.addElement(new ElementInput(getMessage(config.getString("createAccount.form.buttons.password-2.text")), getMessage(config.getString("createAccount.form.buttons.password-2.placeholder"))));
        form.addElement(new ElementInput(getMessage(config.getString("createAccount.form.buttons.email.text")), getMessage(config.getString("createAccount.form.buttons.email.placeholder"))));
        form.addHandler((player, formId) -> {
            FormResponseCustom response = form.getResponse();
            if (response == null) {
                player.sendMessage(getMessage(config.getString("createAccount.form.message.formClose")));
                sendFormToCreateAccount(player, getMessage(config.getString("createAccount.form.addText.formClose")));
                return;
            }
            String password;
            String password2;
            String email;
            if (response.getInputResponse(0) == null) {
                password = response.getInputResponse(1);
                password2 = response.getInputResponse(2);
                email = response.getInputResponse(3);
            } else {
                password = response.getInputResponse(0);
                password2 = response.getInputResponse(1);
                email = response.getInputResponse(2);
            }
            if (!password.equals(password2)) {
                sendFormToCreateAccount(player, getMessage(config.getString("createAccount.form.addText.passwordNotEquals")));
                return;
            }
            if (password.isEmpty()) {
                sendFormToCreateAccount(player, getMessage(config.getString("createAccount.form.addText.passwordIsEmpty")));
                return;
            }
            if (password.length() < config.getInt("createAccount.form.minimumLengthToAcceptPassword") ||
                    AccountClass.passwordComplexity(password) < config.getInt("createAccount.form.minimumPointsToAcceptPassword")) {
                sendFormToCreateAccount(player, getMessage(config.getString("createAccount.form.addText.passwordVeryEasy")));
                return;
            }
            if (!email.isEmpty() && !AccountClass.validateGmail(email)) {
                sendFormToCreateAccount(player, getMessage(config.getString("createAccount.form.addText.emailNotValidate")));
                return;
            }
            if (!AccountClass.createAccount(player.getName(), password, email)) {
                sendFormToCreateAccount(player, getMessage(config.getString("createAccount.form.addText.errorRegistration")).replace("%name%", player.getName()));
                return;
            }
            String strEmail;
            if (email.isEmpty()) {
                strEmail = "Не указана";
            } else {
                strEmail = email;
            }
            lastIp.remove(player.getUniqueId());
            lastIp.put(player.getUniqueId(), player.getAddress());
            player.teleport(spawnLocation);
            player.sendMessage(
                    getMessage(config.getString("createAccount.form.message.successfullyRegistration"))
                            .replace("%name%", player.getName())
                            .replace("%password%", password)
                            .replace("%email%", strEmail)
                            .replace("%maskedIp%", maskedIp(player.getAddress()))
                            .replace("%ip%", player.getAddress())
            );
            player.removeEffect(Effect.INVISIBILITY);
            player.setImmobile(false);
        });
        p.showFormWindow(form);
    }

    public void sendFormToLogin(Player p, String addText) {
        FormWindowCustom form = new FormWindowCustom(getMessage(config.getString("loginAccount.form.title")));
        if (addText != null && !addText.isEmpty()) form.addElement(new ElementLabel(addText));
        form.addElement(new ElementInput(getMessage(config.getString("loginAccount.form.buttons.password.text")), getMessage(config.getString("loginAccount.form.buttons.password.placeholder"))));
        form.addHandler((player, formId) -> {
            FormResponseCustom response = form.getResponse();
            if (response == null) {
                player.sendMessage(getMessage(config.getString("loginAccount.form.message.formClose")));
                sendFormToLogin(player, getMessage(config.getString("loginAccount.form.addText.formClose")));
                return;
            }
            String password;
            if (response.getInputResponse(0) == null) {
                password = response.getInputResponse(1);
            } else {
                password = response.getInputResponse(0);
            }
            if (!createdAccounts.get(player.getUniqueId()).equalsPassword(password)) {
                sendFormToLogin(player, getMessage(config.getString("loginAccount.form.addText.passwordNotEquals")));
                return;
            }
            lastIp.remove(player.getUniqueId());
            lastIp.put(player.getUniqueId(), player.getAddress());
            player.sendMessage(getMessage(config.getString("loginAccount.form.message.successfullyLogin")));
            player.removeEffect(Effect.INVISIBILITY);
            player.setImmobile(false);
        });
        p.showFormWindow(form);
    }

    public String maskedIp(String ip) {
        String[] ipContent = ip.split("\\.");
        StringBuilder stringBuilder = new StringBuilder(ipContent[0] + "." + ipContent[1] + ".");
        for (int i = 0; i < ipContent[2].length(); i++) stringBuilder.append("*");
        stringBuilder.append(".");
        for (int i = 0; i < ipContent[3].length(); i++) stringBuilder.append("*");
        return stringBuilder.toString();
    }

    @SneakyThrows
    public void loadAccounts(boolean mysql) {
        if (mysql) {
            PreparedStatement preparedStatementCreateTable = connection.prepareStatement("CREATE TABLE if not exists`" + getMessage(config.getString("database.database")) + "`.`auth` (`id` INT NOT NULL AUTO_INCREMENT,`uuid` VARCHAR(37) NOT NULL,`password` TEXT NOT NULL,`email` TEXT NOT NULL,PRIMARY KEY (`id`),UNIQUE INDEX `uuid_UNIQUE` (`uuid` ASC) VISIBLE);");
            preparedStatementCreateTable.executeUpdate();
            preparedStatementCreateTable.close();

            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + getMessage(config.getString("database.database")) + ".auth");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                try {
                    AuthPlugin.createdAccounts.put(uuid, new AccountClass(AuthPlugin.optimizeGetOfflinePlayer(uuid).getName(), uuid, resultSet.getString("password"), resultSet.getString("email")));
                } catch (Exception e) {
                    Server.getInstance().getLogger().warning("Данные об игроке §b" + uuid + "§c не были загружены!");
                }
            }
            preparedStatement.executeBatch();
            resultSet.close();
            preparedStatement.close();

            this.getLogger().info("Информация загружена из mysql");
        } else {
            File[] files = new File(this.getDataFolder() + "/store/").listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!Files.getFileExtension(file.getName()).equals("yml")) continue;
                    try {
                        Config cfg = new Config(file, Config.YAML);
                        UUID uuid = UUID.fromString(cfg.getString("uuid"));
                        if (!AuthPlugin.createdAccounts.containsKey(uuid)) {
                            AuthPlugin.createdAccounts.put(uuid, new AccountClass(AuthPlugin.optimizeGetOfflinePlayer(uuid).getName(), uuid, cfg.getString("password"), cfg.getString("email")));
                        }
                    } catch (Exception e) {
                        Server.getInstance().getLogger().warning("Данные с конфига §b" + file.getName() + "§c не были загружены!");
                    }
                }
            }
            this.getLogger().info("Информация загружена из конфигов");
        }
    }

    @SneakyThrows
    public void saveAccounts(boolean mysql) {
        if (mysql) {
            if (connection == null) {
                Server.getInstance().getLogger().warning("Сохранение данных было отменено!");
                return;
            }

            PreparedStatement preparedStatementDeleteAll = connection.prepareStatement("DELETE FROM " + getMessage(config.getString("database.database")) + ".auth");
            preparedStatementDeleteAll.executeUpdate();
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + getMessage(config.getString("database.database")) + ".auth(uuid,password,email) VALUES(?,?,?)");
            for (AccountClass accountClass : createdAccounts.values()) {
                preparedStatement.setString(1, String.valueOf(accountClass.getUuid()));
                preparedStatement.setString(2, accountClass.getEncryptedPassword());
                preparedStatement.setString(3, accountClass.getEmail());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            preparedStatementDeleteAll.close();
            preparedStatement.close();

            this.getLogger().info("Информация сохранена в mysql");
        } else {
            for (AccountClass accountClass : createdAccounts.values()) {
                Config cfg = new Config(new File(AuthPlugin.getInstance().getDataFolder() + "/store/" + accountClass.getUuid() + ".yml"), Config.YAML);
                cfg.reload();
                cfg.set("uuid", String.valueOf(accountClass.getUuid()));
                cfg.set("password", accountClass.getEncryptedPassword());
                cfg.set("email", accountClass.getEmail());
                cfg.save();
            }
            this.getLogger().info("Информация сохранена в конфиги");
        }
    }
}