# Tau-Tests

Фреймворк автотестирования, входящий в набор инструментов автоматизации тестирования TAU-KIT (Test Automation Utilities
KIT)

<h3>Перед началом работы</h3>
<ol>
<li>Клонировать проект фреймворка с репозитория GitLab: <p> git clone http://git.dos.softlab.ru/autotest/rs-taukit.git </p></li>
<li>Открыть проект в IntelliJ Idea</li>
<li>Открыть в терминале папку src</li>
<li>Клонировать проект автотестов в папку test (например, с репозитория GitLab: <p> git clone http://git.dos.softlab.ru/autotest/efr-at.git test</p></li>
<li>Открыть File -> Setting -> Plugins и проверить, установлены ли плагины:
    <ul>
        <li>Cucumber for Java</li>
        <li>Gherkin</li>
        <li>Lombok</li>
    </ul>
</li>
<li>Открыть любой сценарий, например, src/efr-at/resources/scenarios/000. Подготовка тестовых условий/001. Установка настроек.feature</li>
<li>Убедиться в том, что
    <ul>
       <li>файлы *.feature ассоциированы с Cucumber (если автоматически не ассоциировались, это можно исправить в настройках File -> Settings -> Editor -> File Types)</li>
       <li>ключевые слова (Функционал, Сценарий) подсвечиваются;</li>
       <li>шаги не подсвечиваются (если выделены желтым, какая-то проблема с плагином cucumber for java);</li>
       <li>русские слова не подчеркнуты (можно отключить проверку в настройках File -> Settings -> Editor -> Inspections -> Internationalization -> Non-ASCII characters);</li>
    </ul>
</li>
<li>Открыть конфигурацию запуска</li>
<li>Добавить новую конфигурацию TestNG</li>
<li>Настроить параметры:
    <ul>
        <li>Test kind: Method</li>
        <li>Class: TestRunner</li>
        <li>Method: runByTag()</li>
    </ul>
</li>
<li>Открыть файл с настройками запуска тестов

[taukit.properties](src/test/resources/configs/taukit.properties)
</li>
<li>Если русские буквы отображаются некорректно, например, в виде кодов ascii:
<ul>
<li>открыть настройки Settings -> File Encodings
<li>указать кодировку для Default encoding for properties files: UTF-8</li>
<li>снять чекбокс Transparent native-to-ascii conversion и нажать Apply</li>
<li>снова установить чекбокс Transparent native-to-ascii conversion и нажать Apply</li>
</ul>
</li>
<li>Сделать его копию с названием runtime.properties (этот файл добавлен в .gitignor, так что можно его править, не
боясь случайно закоммитить неправильные настройки)</li>
<li>Изменить настройки в <u><i>runtime.properties</i></u> в соответствии с текущими
условиями (например, указать стенд в опции <i>standName</i>)</li>
<li>Указать для запуска какой-нибудь сценарий из набора <i>000. Подготовка тестовых условий</i>, прописав его тег в настройке <i>scenario.tags</i>. Например, scenario.tags = @000.001.001</li>
<li>Запустить конфигурацию кнопкой или с помощью Shift+F10 (будут загружены браузеры, которые использует фреймворк. Тест
в итоге может упасть по таймауту или с другой ошибкой, при первом запуске это не имеет значения). Повторный запуск
должен пройти успешно.</li>
</ol>

<h3>Автотесты</h3>

Сценарии автотестов описаны в feature-файлах в папке src/test/resources/scenarios.
[Структура feature-файла со сценариями автотестирования](instructions/Структура feature-файла со сценариями автотестирования.md) 


<h3>Результаты автотестов</h3>
В результате выполнения автотестов в папке target/allure-results формируются отчеты Allure.
При локальном запуске тестов посмотреть отчеты можно из меню Maven - Plugins - allure - allure:serve
Также, можно установить Allure Report следуя инструкциям
с [официального сайта](https://allurereport.org/docs/gettingstarted-installation/), и открывать отчеты из терминала
командой allure serve target/allure-results
После каждого нового выполнения тестов отчеты необходимо переоткрывать заново с помощью той же команды, т. к. содержимое
уже сгенерированных не обновляется.