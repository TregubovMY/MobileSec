# Make it So: аутентификация

Лабораторная работа №1. Todo-приложение на Kotlin, Compose, Hilt и Firebase (Auth, Firestore). Основа: [Make it So v2](https://github.com/FirebaseExtended/make-it-so-android/tree/main/v2).

## Что реализовано

- Регистрация и вход по почте и паролю.
- После регистрации Credential Manager предлагает сохранить пароль, приложение пароль не хранит.
- На экране входа сохранённая запись, Google-аккаунт и passkey предлагаются в одном системном диалоге.
- Вход через Google: ID-токен передаётся в Firebase Auth.
- Профиль: способ входа, почта, имя, uid, статус подтверждения почты.
- Список задач открывается только после входа, в том числе после перезапуска приложения.
- Задачи одного пользователя недоступны другому: доступ проверяют правила Firestore (`firestore.rules`).
- Выход завершает сессию и очищает стек навигации, кнопка «назад» в список задач не возвращает.
- Вход по passkey. Серверная часть лежит в `server/`.

## Запуск приложения

1. Создайте проект в [консоли Firebase](https://console.firebase.google.com/).
2. В разделе Authentication включите провайдеры Email/Password и Google.
3. В настройках проекта добавьте Android-приложение `com.google.firebase.example.makeitso`, укажите SHA-1 debug-ключа и скачайте `google-services.json` в папку `app/`. Файл нужно скачивать после включения Google, иначе в нём не будет клиента для входа.
4. Создайте базу Firestore в production mode, на вкладке Rules вставьте содержимое `firestore.rules` и опубликуйте.
5. Откройте проект в Android Studio и запустите на эмуляторе с Google Play и добавленным Google-аккаунтом (или на устройстве).

SHA-1 debug-ключа:

```
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

## Passkeys

Для passkeys нужен домен с файлом `/.well-known/assetlinks.json`, связывающим приложение с доменом, и сервер, который проверяет подпись и выдаёт токен Firebase. Домен проекта `<project-id>.firebaseapp.com` отдаёт Firebase Hosting, сервер запускается локально.

1. Если ваш debug-ключ отличается от указанного в `hosting/.well-known/assetlinks.json` и `server/lib.js` (`CERT_SHA256`), замените отпечаток SHA-256 в обоих файлах.
2. Выложите файл привязки, из папки `lab1`:

   ```
   npx firebase-tools login
   npx firebase-tools deploy --only hosting --project <project-id>
   ```

   После этого должен открываться `https://<project-id>.firebaseapp.com/.well-known/assetlinks.json`.
3. В консоли Firebase (Project settings, Service accounts) создайте ключ сервисного аккаунта. Храните его вне репозитория.
4. Запустите сервер (нужен Node 20 или новее):

   ```
   cd server
   npm install
   FIREBASE_SERVICE_ACCOUNT="$(cat /путь/к/ключу.json)" RP_ID=<project-id>.firebaseapp.com node local.js
   ```

   Эмулятор обращается к нему по адресу `10.0.2.2:3000` (значение `passkey_server_url` в `app/src/main/res/values/strings.xml`).
5. Войдите в приложение, откройте профиль и нажмите Create passkey. После выхода passkey появится в диалоге на экране входа.

Сервер также можно задеплоить на Vercel: корень проекта `lab1/server`, переменные `FIREBASE_SERVICE_ACCOUNT` и `RP_ID`, адрес сервера записать в `passkey_server_url`.

### Замечания по эмулятору

- Нужен образ с Google Play. На образе Android 37.1 проверка домена у меня не проходила, на API 35 всё работает.
