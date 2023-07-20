# AuthSystem

AuthSystem - система авторизации под ядро nukkit. Позволяет заходить на сервер с использованием пароля (Так же работает без xbox авторизации)

---

Основные фишки плагина:
- Плагин надежно хранит данные, шефруя их по стандарту `AES-256` (Для их расшифроки необхоимо знать key, salt, iterationCount, keyLength и Vector)
- Возможность использовать MySQL/Config для хранения данных
- Реализована команда для смены пароля игроку
- Возможность гибко настраивать плагин, начиная от текста и прав для использования команды, заканчивая кнопками в формах
- Пока игрок не совершил вход по нему не проходит урон, и он не может двигаться
- При регистрации возможно ввести почту

---

Процесс установки:
1. Скачайте последнюю версию плагина с гитхаба ([Ссылка на релизы](https://github.com/Toper9636/AuthSystem/releases/))
2. Скачайте плагин DbLib с cloudburst, если планируете использовать MySQL для хранения данных ([DbLib](https://cloudburstmc.org/resources/dblib.12/))
3. Поместите плагины в папку plugins вашего сервера
4. Запустите сервер и дайте сгенерироваться конфигу
5. **ОБЯЗАТЕЛЬНО ОТРЕДАКТИРУЙТЕ КЛЮЧИ ШИФРОВАНИЯ**, это нужно для безопасности данных (Если сменить что-либо в `encrypt-settings`, то игроки зарегестрированные ранее потеряют доступ к своим аккаунтам)
6. Готово, наслаждайтесь использованием плагина

---

Что делать если Что-то не работает/возникли ошибки?
1. Проверить консоль, туда обычно выводятся все возможные ошибки кода
2. Если Вы не смогли понять в чем проблема и исправить её, то можете отправить issue с данной проблемой

## Форма при входе: 
![Форма](https://github.com/Toper9636/AuthSystem/assets/102699826/94865df0-102e-4ab0-8086-72b53820ab93)

## `config.yml`:
```yaml
database:
  host: 127.0.0.1
  port: '3306'
  database: baseName
  username: root
  password: root
load-and-save-from-mysql: false
encrypt-settings:
  key: IFJDHJOFDJOSFHJOSDHOJFOJD384362689422828483472379479237947923ENCRYPTPASSWORD3493927943790279043792794------EDIT-THIS
  salt: minecraftServerPasswordEncrypt------EDIT-THIS
  iteration-count: 92734
  key-length: 256
  vector: '[0,0,0,0,0,0,0,0,3,0,0,0,45,0,0,0]'
joinLocation:
  x: 0
  y: 2000
  z: 0
  levelName: '%defaultLevel%'
spawnLocation:
  x: 40
  y: 2000
  z: 40
  levelName: '%defaultLevel%'
command:
  name: clearPassword
  description: §r§l§aсбросить пароль
  message:
    argsError: '§sОшибка §7-> §cИспользование: §7/§e%command% <пароль> <повтор пароля>'
    passwordNotEquals: §sОшибка §7-> §cВведенные пароли должны совпадать!
    passwordVeryEasy: §sОшибка §7-> §cВведенные пароль слишком простой!
    passwordEqualsAsOldPassword: §sОшибка §7-> §cВведенные пароль должен отличаться
      от регистрационного!
  minimumLengthToAcceptPassword: 6
  minimumPointsToAcceptPassword: 30
  form:
    title: §8Смена пароля
    content: §qВы действительно хотите сменить пароль? §cОбязательно запишите новый
      пароль куда-нибудь, это необходимо чтобы не потерять доступ к своему аккаунту!
    textButtonYes: §cДа
    textButtonNo: §aНет
    close: §sОшибка §7-> §cСмена пароля отменена
    buttonYes: '§eИнформация §7-> §aВы успешно изменили пароль §7(§bНовый пароль:  %password%§7)§a!'
    buttonNo: §sОшибка §7-> §cСмена пароля отменена
joinEvent:
  successfullyLogin: §eИнформация §7-> §aВы успешно авторизовались!
createAccount:
  form:
    title: §8Создание аккаунта
    buttons:
      password-1:
        text: '§fПридумайте и введите пароль:'
        placeholder: password
      password-2:
        text: '§fПовторите придуманный Вами пароль:'
        placeholder: password
      email:
        text: '§fВведите почту (Необязательно):'
        placeholder: steve@gmail.com
    message:
      formClose: §sОшибка §7-> §cЧтобы продолжить необходимо зарегистрироваться на
        сервере!
      successfullyRegistration: '§eИнформация §7-> §aВы успешно зарегистрированы на
        сервере!%n%%n%Ваши данные:%n%Пароль: %password%%n%Почта: %email%%n%Ip: %maskedIp%'
    addText:
      formClose: §cДля продолжения необходимо зарегистрироваться на сервере!%n%%n%
      passwordNotEquals: §cВведенные пароли должны совпадать!%n%%n%
      passwordIsEmpty: §cПароль не может быть пустым!%n%%n%
      passwordVeryEasy: §cВведенный пароль слишком простой!%n%%n%
      emailNotValidate: §cПочта введена неправильно%n%%n%
      errorRegistration: §cПроизошла ошибка при регистрации игрока §b%name%§c!%n%%n%
    minimumLengthToAcceptPassword: 6
    minimumPointsToAcceptPassword: 30
loginAccount:
  form:
    title: §8Вход в аккаунт
    buttons:
      password:
        text: '§fВведите пароль:'
        placeholder: password
    message:
      formClose: §sОшибка §7-> §cЧтобы продолжить необходимо авторизоваться на сервере!
      successfullyLogin: §eИнформация §7-> §aВы успешно авторизовались!
    addText:
      formClose: §cДля продолжения необходимо авторизоваться на сервере!%n%%n%
      passwordNotEquals: §cВведенный Вами пароль не совпадает с регистрационным паролем!%n%%n%

```
