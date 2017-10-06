![API](https://img.shields.io/badge/API-19%2B-blue.svg?style=flat)
# ForPDA #

<a href="http://4pda.ru/forum/index.php?showtopic=820313" target="_blank"><img src="https://lh3.googleusercontent.com/CSHwChA4QhrV3sDtXT53OP3ZCnDQaW5cEVUxdQo0xBTueRrl23U1HwK9u_qyGyTYlRI=w300" height="200px" alt="Логотип 4pda" /></a>

##
**Скриншоты:**

![](https://raw.githubusercontent.com/SnowVolf/GirlUpdater/master/files/ForPDA/screen1.png)![](https://raw.githubusercontent.com/SnowVolf/GirlUpdater/master/files/ForPDA/screen2.png)![](https://raw.githubusercontent.com/SnowVolf/GirlUpdater/master/files/ForPDA/screen3.png)
##

<a href="https://play.google.com/store/apps/details?id=ru.forpdateam.forpda"><img alt="Get it on Google Play" src="https://play.google.com/intl/ru_ru/badges/images/apps/ru-play-badge.png" height="48px"/></a>
<a href="http://4pda.ru/forum/index.php?showtopic=820313" target="_blank"><img src="http://s.4pda.to/hoDPUCVswtc80crtp5RkFz1z0jON6RW9If6z2r6nq7o5nTqz2s2r1Ii2.png" height="48px" alt="Тема на форуме 4PDA" /></a>
##

**ForPDA** - это простой и удобный клиент для сайта [4pda.ru](http://4pda.ru/)

Вы можете просматривать информацию с [сайта](http://4pda.ru/) в удобном виде, писать и редактировать сообщения на [форуме](http://4pda.ru/forum/index.php?act=idx), искать нужную вам информацию, скачивать файлы, общаться с другими [пользователями](http://4pda.ru/forum/index.php?act=Members) в чате [QMS](http://4pda.ru/forum/index.php?act=qms&code=no) и многое другое! 

На данный момент приложение находится на стадии бета тестирования, поэтому некоторые функции недоступны, но наша команда усердно трудится, чтобы внедрить весь необходимый функционал и новые крутые фичи, **присоединяйтесь к нам, и мы вас не разочаруем!**

**Некоторые особенности**

1. Простой и понятный интерфейс в стиле Material Design
2. 2 темы оформления (светлая и темная)
2. Отсутствие лишнего функционала и настроек
3. Команда разработчиков стремится к идеалам современных приложений


**Основные возможности**

1. Просмотр новостей сайта
2. Возможность оставлять комментарии на сайте
3. Просмотр форумов и списков их тем
4. Поиск по сайту и форуму, с возможностью настроить параметры поиска
5. Возможность создавать/редактировать/удалять сообщения на форуме
6. Возможность редактировать темы на форуме
7. Возможность скачивать и загружать файлы на форум
8. Простой и удобный доступ к избранному
9. Доступ к каталогу устройств [DevDB](http://4pda.ru/devdb)
10. Доступ к [QMS](http://4pda.ru/forum/index.php?act=qms&code=no) (создание/удаление диалогов, а также управление черным списком)
11. Доступ профилю пользователей
12. Просмотр упоминаний
13. История посещённых тем
14. Заметки и форумный блокнот

##
# Сборка проекта #
Проект разразрабатывается с помощью [Android Studio](https://developer.android.com/studio/index.html) и использует Gradle для сборки. Для корректной сборки нужно установить JDK 8, обновить SDK до версии 25, и Gradle до версии 3.3

    // Top-level build file where you can add configuration options common to all sub-projects/modules.
    //...
    
    dependencies {
    classpath 'com.android.tools.build:gradle:2.3.3'
    // Other plugins
    
    // NOTE: Do not place your application dependencies here; they belong
    // in the individual module build.gradle files
    }
    //...

Сборка призводится командой Build -> Build APK (в Android Studio). Результирующий APK находится в `%PROJECT_DIR%/apk/`

# Для разработчиков стилей #
На данный момент приложение не поддерживает пользовательские стили, но вы можете отредактировать стандартные стили приложения. Стандартные стили находятся в папке `/assets/forpda/styles/`  модуля `app`.
Тестовые html для всех основных разделов форума уже включены в проект. Сморите папку `/assets/forpda/`  модуля `app`.

Для удобного редактирования стилей вам необходимо уметь работать с [LESS](http://lesscss.org/)
Основной код лежит в `../modules/`, для компиляции нужно использовать соответствующие файлы из папок `../light/` и `../dark/`.
Так-же имеются конфигурационные файлы, в которых можно удобно изменять нужные цвета. После изменения конфигурационных файлов, обязательно нужно скомпилировать все модули стилей.

Файлы javascript трогать не нужно, т.к. их работа тесно связана с java кодом клиента, и любые изменения в критичных местах, могут повлиять на работу клиента.

Разработку стилей лучше всего вести с помощью [Brackets](http://brackets.io/) с модулями: "Emmet", "LESS AutoCompile" и "LESSHints".

# Лицензия #
Исходный код распостраняется под лицензией GPL v3

> Copyright (C) 2016-2017  Evgeniy Nizamiev [(radiationx@yandex.ru)](mailto:radiationx@yandex.ru)
> 
> This program is free software; you can redistribute it and/or modify
> it under the terms of the GNU General Public License as published by
> the Free Software Foundation; either version 3 of the License.


Составитель справки: [Snow Volf](https://github.com/SnowVolf)
