package server.API;

import cn.nukkit.utils.Config;
import com.google.gson.Gson;
import server.Auth_Plugin;

import java.io.File;

public class ConfigInitialization {
    public static final Gson JSON = new Gson();

    public static void addDefault(String path, Object object) {
        Auth_Plugin.getInstance().config.reload();
        if (!Auth_Plugin.getInstance().config.exists(path)) {
            Auth_Plugin.getInstance().config.set(path, object);
            Auth_Plugin.getInstance().config.save();
        }
    }


    public static void init() {
        Auth_Plugin.getInstance().saveConfig();
        Auth_Plugin.getInstance().config = new Config(new File(Auth_Plugin.getInstance().getDataFolder() + "/config.yml"), Config.YAML);
        addDefault("database.host", "127.0.0.1");
        addDefault("database.port", "3306");
        addDefault("database.database", "base_name");
        addDefault("database.username", "root");
        addDefault("database.password", "root");

        addDefault("load-and-save-from-mysql", false);

        addDefault("encrypt-settings.key", "IFJDHJOFDJOSFHJOSDHOJFOJD384362689422828483472379479237947923ENCRYPTPASSWORD3493927943790279043792794------EDIT-THIS");
        addDefault("encrypt-settings.salt", "minecraft_server_password_encrypt------EDIT-THIS");
        addDefault("encrypt-settings.iteration-count", 92734);
        addDefault("encrypt-settings.key-length", 256);
        addDefault("encrypt-settings.vector", JSON.toJson(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 45, 0, 0, 0}));

        addDefault("joinLocation.x", 0);
        addDefault("joinLocation.y", 2000);
        addDefault("joinLocation.z", 0);
        addDefault("joinLocation.level_name", "%defaultLevel%");

        addDefault("spawnLocation.x", "%defaultLevel_spawnLocationX%");
        addDefault("spawnLocation.y", "%defaultLevel_spawnLocationY%");
        addDefault("spawnLocation.z", "%defaultLevel_spawnLocationZ%");
        addDefault("spawnLocation.level_name", "%defaultLevel%");


        addDefault("command.name", "clear_password");
        addDefault("command.description", "§r§l§aсбросить пароль");
        addDefault("command.message.args_error", "§sОшибка §7-> §cИспользование: §7/§e%command% <пароль> <повтор пароля>");
        addDefault("command.message.password_not_equals", "§sОшибка §7-> §cВведенные пароли должны совпадать!");
        addDefault("command.minimum_length_to_accept_password", 6);
        addDefault("command.minimum_points_to_accept_password", 30);
        addDefault("command.message.password_very_easy", "§sОшибка §7-> §cВведенные пароль слишком простой!");
        addDefault("command.message.password_equals_as_old_password", "§sОшибка §7-> §cВведенные пароль должен отличаться от регистрационного!");
        addDefault("command.form.title", "§8Смена пароля");
        addDefault("command.form.content", "§qВы действительно хотите сменить пароль? §cОбязательно запишите новый пароль куда-нибудь, это необходимо чтобы не потерять доступ к своему аккаунту!");
        addDefault("command.form.text_button_yes", "§cДа");
        addDefault("command.form.text_button_no", "§aНет");
        addDefault("command.form.close", "§sОшибка §7-> §cСмена пароля отменена");
        addDefault("command.form.button_yes", "§eИнформация §7-> §aВы успешно изменили пароль §7(§bНовый пароль:  %password%§7)§a!");
        addDefault("command.form.button_no", "§sОшибка §7-> §cСмена пароля отменена");

        addDefault("joinEvent.successfully_login", "§eИнформация §7-> §aВы успешно авторизовались!");

        addDefault("createAccount.form.title", "§8Создание аккаунта");
        addDefault("createAccount.form.buttons.password_1.text", "§fПридумайте и введите пароль:");
        addDefault("createAccount.form.buttons.password_1.placeholder", "password");
        addDefault("createAccount.form.buttons.password_2.text", "§fПовторите придуманный Вами пароль:");
        addDefault("createAccount.form.buttons.password_2.placeholder", "password");
        addDefault("createAccount.form.buttons.email.text", "§fВведите почту (Необязательно):");
        addDefault("createAccount.form.buttons.email.placeholder", "steve@gmail.com");
        addDefault("createAccount.form.message.formClose", "§sОшибка §7-> §cЧтобы продолжить необходимо зарегистрироваться на сервере!");
        addDefault("createAccount.form.addText.formClose", "§cДля продолжения необходимо зарегистрироваться на сервере!%n%%n%");
        addDefault("createAccount.form.addText.passwordNotEquals", "§cВведенные пароли должны совпадать!%n%%n%");
        addDefault("createAccount.form.addText.passwordIsEmpty", "§cПароль не может быть пустым!%n%%n%");
        addDefault("createAccount.form.minimum_length_to_accept_password", 6);
        addDefault("createAccount.form.minimum_points_to_accept_password", 30);
        addDefault("createAccount.form.addText.password_very_easy", "§cВведенный пароль слишком простой!%n%%n%");
        addDefault("createAccount.form.addText.emailNotValidate", "§cПочта введена неправильно%n%%n%");
        addDefault("createAccount.form.addText.errorRegistration", "§cПроизошла ошибка при регистрации игрока §b%name%§c!%n%%n%");
        addDefault("createAccount.form.message.successfully_registration", "§eИнформация §7-> §aВы успешно зарегистрированы на сервере!%n%%n%Ваши данные:%n%Пароль: %password%%n%Почта: %email%%n%Ip: %maskedIp%");

        addDefault("loginAccount.form.title", "§8Вход в аккаунт");
        addDefault("loginAccount.form.buttons.password.text", "§fВведите пароль:");
        addDefault("loginAccount.form.buttons.password.placeholder", "password");
        addDefault("loginAccount.form.message.formClose", "§sОшибка §7-> §cЧтобы продолжить необходимо авторизоваться на сервере!");
        addDefault("loginAccount.form.addText.formClose", "§cДля продолжения необходимо авторизоваться на сервере!%n%%n%");
        addDefault("loginAccount.form.addText.password_not_equals", "§cВведенный Вами пароль не совпадает с регистрационным паролем!%n%%n%");
        addDefault("loginAccount.form.message.successfully_login", "§eИнформация §7-> §aВы успешно авторизовались!");
    }
}
