# newsseacher

Пример CRUD контроллера, взаимодействующего с поисковиковым движком Manticore Search

## Запуск

Для работы требует подключение к БД, разверните её из docker контейнера перед запускам приложения:

```yaml
version: '3.1'

services:

  db:
    image: postgres
    restart: always
    environment:
      POSTGRES_DB: searchdb
      POSTGRES_USER: searchuser
      POSTGRES_PASSWORD: search
    ports:
      - 5432:5432
```

Hibernate самостоятельно создаст необходимые таблицы при запуске приложения.

```properties
spring.jpa.hibernate.ddl-auto=update
```

## Использование

**Context:** http://localhost:8050/news-seacher

**Swagger UI:** http://localhost:8050/news-seacher/swagger-ui/index.html

### Создание

```bash
curl -X 'POST' \
  'http://localhost:8050/news-seacher/news' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "headline": "Старик расстрелял пенсионный фонд из костыля",
  "content": "В Нижневартовске возбуждено уголовное дело по факту стрельбы пенсионера из костыля. В начале апреля 61-летний мужчина отправился в пенсионный фонд, чтобы оформить документ на оказание социальной помощи. На входе в здание он попытался костылем сбить наледь.",
  "postDate": "2014-07-29"
}'
```

### Чтение 

```bash
curl -X 'GET' \
  'http://localhost:8050/news-seacher/news?page=0&size=50' \
  -H 'accept: */*'
```

```bash
curl -X 'GET' \
  'http://localhost:8050/news-seacher/news/1' \
  -H 'accept: */*'
```

### Изменение

```bash
curl -X 'PUT' \
  'http://localhost:8050/news-seacher/news/1' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "headline": "Старик расстрелял пенсионный фонд из костыля",
  "content": "В Нижневартовске возбуждено уголовное дело по факту стрельбы пенсионера из костыля. В начале апреля 61-летний мужчина отправился в пенсионный фонд, чтобы оформить документ на оказание социальной помощи. На входе в здание он попытался костылем сбить наледь. После нескольких ударов предмет внезапно выстрелил 13 раз.",
  "postDate": "2014-07-29"
}'
```

### Удаление

```bash
curl -X 'DELETE' \
  'http://localhost:8050/news-seacher/news/1' \
  -H 'accept: */*'
```
