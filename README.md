<h1 align="center">Task Management System</h1>

<h3 align="center">Система управления задачами</h3>

Система обеспечивает создание, редактирование, удаление и просмотр задач.
Каждая задача содержит заголовок, описание, статус, приоритет, автора и исполнителя.

Создать задачу может только аутентифицированный пользователь, т.к. информация о пользователе необходима для заполнения поля author.
По умолчанию созданной задаче присваивается статус WAITING.
Если при создании задачи не указан приоритет - по умолчанию он будет установлен на уровень MIDDLE.
Назначение исполнителя возможно как во время создания задачи, так и после.

Обновление задачи возможно только автору или исполнителю задачи.

Удаление задачи доступно только автору.

Получить задачу по id или список задач с использованием фильтров может любой аутентифицированный пользователь.

Для получения списка задач предусмотрен эндпоинт GET /task. Для получения всех задач, созданных пользователем, необходимо заполнить параметр authorId.
Для получения всех задач, где пользователь указан исполнителем, необходимо заполнить параметр executorId.
Предусмотрена возможность добавления фильтров по полям status и priority. При отсутствии всех параметров будут получены все задачи, с учётом условий пагинации.
Поиск задач по нескольким необязательным условиям реализован с использованием паттерна спецификация.

Изменение статуса задачи доступно автору или исполнителю задачи.

Назначение исполнителя доступно при создании задачи или позже, но только создателю задачи.

Добавление комментария к любой существующей задаче доступно для всех аутентифицированных пользователей. 

Безопасность веб-приложения обеспечена использованием Spring Security с access и refresh токенами.

При вводе корректных логина и пароля в теле ответа будут направлены access и refresh токены.
Время жизни access и refresh токены можно изменять в application.properties, по умолчанию оно составляет 5 минут и 30 дней соответственно.

При окончании действия access токена необходимо обновить токены путём отправки refresh токена на адрес POST /auth/token.
В ответ будут получены обновленные токены.

Для работы с задачами созданы пять учётных записей пользователей. Логины и пароли для получения access и refresh токенов представлены в таблице ниже:

|       логин        |пароль    |
|:------------------:|:--------:|
|  asanta@user.com   |password1 |
|  jchivas@user.com  |password2 |
| jdaniels@user.com  |password3 |
|  jwalker@user.com  |password4 |
| jjameson@user.com  |password5 |

В качестве базы данных использован PostgreSQL.
Для управления миграциями базы данных использована Liquibase.

Протестировать работу сервиса можно с помощью postman-коллекции и написанных тестов.

Документация API создана с использованием swagger и представлена по ссылке: http://localhost:8080/swagger-ui/index.html#/

Для запуска сервиса необходимо запустить файл docker-compose.yml из графического интерфейса IDE или командой docker-compose up.
Для остановки и удаления контейнеров можно использовать команду docker-compose down.