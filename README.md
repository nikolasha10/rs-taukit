# Tau-Tests
Фреймворк автотестирования, входящий в набор инструментов автоматизации тестирования TAU-KIT (Test Automation Utilities KIT)

Перед началом работы:
1. Склонировать проект фреймворка с репозитория GitLab: 
    <p> git clone <a>http://git.dos.softlab.ru/kudryashov/rs-taukit.git</a> </p>
2. Открыть в терминале папку src
3. Склонировать проект автотестов ЕФР с репозитория GitLab:
    <p>git clone <a>http://git.dos.softlab.ru/kudryashov/efr-at.git</a></p>
4. Пометить папку <a>src/efr-at/java</a> ,как <i>Test Sources Root</i>
5. Пометить папку <a>src/efr-at/resources</a>, как <i>Test Resources Root</i>
6. Открыть File -> Setting -> Plugins и проверить, установлены ли плагины:
   - Cucumber for Java
   - Gherkin
   - Lombok
7. Открыть любой сценарий, например <a>src/efr-at/resources/scenarios/000. Подготовка тестовых условий/001. Установка настроек.feature</a>
8. Убедиться в том, что
   - файлы *.feature ассоциированы с Cucumber (ассоциировать можно в настройках File -> Settings -> Editor -> File Types)
   - ключевые слова (Функционал, Сценарий) подсвечиваются;
   - шаги не подсвечиваются (если выделены желтым, какая-то проблема с плагином cucumber for java);
   - русские слова не подчеркнуты (можно отключить проверку в настройках File -> Settings -> Editor -> Inspections -> Internationalization -> Non-ASCII characters);
9. Открыть конфигурацию запуска
10. Добавить новую конфигурацию TestNG
11. Настроить параметры:
    - Test kind: Method
    - Class: EFRTestRunner
    - Method: runByTag()
12. Открыть файл с настройками запуска тестов <a>src/efr-at/resources/configs/taukit.properties</a>
13. Сделать его копию с названием runtime.properties (этот файл добавлен в .gitignor, так что можно его править, не боясь случайно закоммитить неправильные настройки)

 


