![API](https://img.shields.io/badge/API-19%2B-blue.svg?style=flat)
# ForPDA #

**ForPDA** – это простой и удобный клиент для сайта [4pda.ru](http://4pda.ru/)

<a href="http://4pda.ru/forum/index.php?showtopic=820313" target="_blank"><img src="https://raw.githubusercontent.com/RadiationX/ForPDA/master/gh_res/logo.png" height="192px" alt="Логотип ForPDA" /></a>

<a href="https://play.google.com/store/apps/details?id=ru.forpdateam.forpda"><img alt="Get it on Google Play" src="https://play.google.com/intl/ru_ru/badges/images/apps/ru-play-badge.png" height="48px"/></a>
<a href="http://4pda.ru/forum/index.php?showtopic=820313" target="_blank"><img src="https://raw.githubusercontent.com/RadiationX/ForPDA/master/gh_res/icon_4pda.png" height="48px" alt="Тема на форуме 4PDA" /></a>

##
**Скриншоты:**

![](https://raw.githubusercontent.com/RadiationX/ForPDA/master/gh_res/screen1.png)![](https://raw.githubusercontent.com/RadiationX/ForPDA/master/gh_res/screen2.png)![](https://raw.githubusercontent.com/RadiationX/ForPDA/master/gh_res/screen3.png)
##

Вы можете просматривать информацию с [сайта](http://4pda.ru/) в удобном виде, писать и редактировать сообщения на [форуме](http://4pda.ru/forum/index.php?act=idx), искать нужную вам информацию, скачивать файлы, общаться с другими [пользователями](http://4pda.ru/forum/index.php?act=Members) в чате [QMS](http://4pda.ru/forum/index.php?act=qms&code=no) и многое другое! 

**Основные возможности**

- Просмотр новостей сайта
- Возможность оставлять комментарии на сайте
- Просмотр форумов и списков их тем
- Поиск по сайту и форуму, с возможностью настроить параметры поиска
- Возможность создавать/редактировать/удалять сообщения на форуме
- Возможность редактировать темы на форуме
- Возможность скачивать и загружать файлы на форум
- Простой и удобный доступ к избранному
- Доступ к каталогу устройств [DevDB](http://4pda.ru/devdb)
- Доступ к [QMS](http://4pda.ru/forum/index.php?act=qms&code=no) (создание/удаление диалогов, а также управление черным списком)
- Доступ профилю пользователей
- Просмотр упоминаний
- История посещённых тем
- Заметки и форумный блокнот

**Некоторые особенности**

- Простой и понятный интерфейс в стиле Material Design
- Две темы оформления (светлая и темная)
- Отсутствие лишнего функционала и настроек
- Команда разработчиков стремится к идеалам современных приложений

##
# Сборка проекта #
Проект разрабатывается с помощью [Android Studio](https://developer.android.com/studio/index.html) и использует Gradle для сборки. Для корректной сборки нужно установить JDK 8, обновить SDK до версии 25, и Gradle до версии 3.3

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
Тестовые html для всех основных разделов форума уже включены в проект. Смотрите папку `/assets/forpda/`  модуля `app`.

Для удобного редактирования стилей вам необходимо уметь работать с [LESS](http://lesscss.org/)
Основной код лежит в `../modules/`, для компиляции нужно использовать соответствующие файлы из папок `../light/` и `../dark/`.

Также имеются конфигурационные файлы (`config_*.less`), в которых можно удобно изменять нужные цвета. После изменения конфигурационных файлов, обязательно нужно скомпилировать все модули стилей.

**Файлы javascript трогать не нужно, т.к. их работа тесно связана с java кодом клиента, и любые изменения в критичных местах, могут повлиять на работу клиента.**

Разработка стилей делалась в [Brackets](http://brackets.io/) с модулями "Emmet", "LESS AutoCompile" и "LESSHints".

# Лицензия #
Исходный код распостраняется под лицензией GPL v3

> Copyright (C) 2016-2018  Evgeniy Nizamiev [(radiationx@yandex.ru)](mailto:radiationx@yandex.ru)
> 
> This program is free software; you can redistribute it and/or modify
> it under the terms of the GNU General Public License as published by
> the Free Software Foundation; either version 3 of the License.


Составитель справки: [Snow Volf](https://github.com/SnowVolf)
