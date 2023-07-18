package server.API;

import cn.nukkit.IPlayer;
import cn.nukkit.Server;
import lombok.AllArgsConstructor;
import lombok.Getter;
import server.Auth_Plugin;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Pattern;

import static server.Auth_Plugin.encryptSettings;

@Getter
@AllArgsConstructor
public class Account_Class {
    private String name;
    private final UUID uuid;
    private String encryptedPassword;
    private String email;

    public static boolean createAccount(String name, String password, String email) {
        IPlayer player = Server.getInstance().getOfflinePlayer(name);
        if (player == null || player.getUniqueId() == null) return false;
        Auth_Plugin.createdAccounts.put(player.getUniqueId(), new Account_Class(name, player.getUniqueId(), encryptString(password), email));
        return true;
    }
    public Account_Class replacePassword(String password) {
        this.encryptedPassword = encryptString(password);
        return this;
    }
    public boolean equalsPassword(String password) {
        return this.encryptedPassword.equals(encryptString(password));
    }
    public static boolean isRegistered(UUID uuid) {
        return Auth_Plugin.createdAccounts.containsKey(uuid);
    }
    public static boolean isLogined(UUID uuid, String ip) {
        return Auth_Plugin.lastIp.getOrDefault(uuid, "000.000.000.000").equals(ip);
    }

    public static String encryptString(String string) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(
                            new PBEKeySpec(
                                    encryptSettings.getKey(), // key
                                    encryptSettings.getSalt(), // salt
                                    encryptSettings.getIterationCount(), // iteration-count
                                    encryptSettings.getKeyLength() // key-length
                            )
                    ).getEncoded(), "AES"),
                    new IvParameterSpec(encryptSettings.getVector()) // vector
            );
            return Base64.getEncoder().encodeToString(cipher.doFinal(string.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            System.out.println("Ошибка при шифровке пароля: " + e);
            return null;
        }
    }

    // Код этой функции не мой
    public static int passwordComplexity(String psw) {
        int balls = 0;
        balls += psw.length() >= 8? 10 : 0;  // более 8 знаков
        balls += psw.chars()                // 2 буквы
                .filter(i -> "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".contains(String.valueOf((char) i))).count() > 1 ? 10: 0;
        balls += psw.chars()                // 2 цифры
                .filter(i -> "0123456789".contains(String.valueOf((char) i))).count() > 1 ? 10: 0;
        balls += psw.chars()                // одна или больше больших букв
                .anyMatch(i -> "ABCDEFGHIJKLMNOPQRSTUVWXYZ".contains(String.valueOf((char) i))) ? 10: 0;
        balls += psw.chars()                // одна или больше маленьких букв
                .anyMatch(i -> "abcdefghijklmnopqrstuvwxyz".contains(String.valueOf((char) i))) ? 10: 0;
        balls += psw.chars()                // содержатся спецсимволы (дополнить список символов по желанию)
                .anyMatch(i -> "!@#$%^&*[]".contains(String.valueOf((char) i))) ? 10: 0;
        return balls;
    }
    public static boolean validateGmail(String email) {
        return Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE).matcher(email.replaceAll("(?u)[^a-zA-Z0-9@.]", "")).find();
    }
}
