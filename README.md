# newsseacher

Пример CRUD контроллера, взаимодействующего с поисковиковым движком Manticore Search

* [Запуск](#Header1)
  * [База Данных - *PostgreSQL*](#Header1.1)
  * [Поисковик - *Manticore Searh*](#Header1.2)
  * [Индексирование](#Header1.3)
* [Использование](#Header2)

## <a name="Header1"></a>Запуск

### <a name="Header1.1"></a>База Данных - *PostgreSQL*

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

### <a name="Header1.2"></a>Поисковик - *Manticore Searh*

Приложение взаимодействует с поисковой платформой, разверните и её из докер контейнера [docker-compose.yaml](https://gist.github.com/Tkachenko-Ivan/9c8f8b5f98c80f902905b618878486ad#file-docker-compose-yaml):

```yaml
version: '3.1'

services:

  manticore:
    container_name: manticore
    image: manticoresearch/manticore
    restart: always
    ports:
      - 127.0.0.1:9306:9306
      - 127.0.0.1:9308:9308
    ulimits:
      nproc: 65535
      nofile:
         soft: 65535
         hard: 65535
      memlock:
        soft: -1
        hard: -1  
    volumes:
      - ./manticore:/var/lib/manticore
      - ./manticore.conf:/etc/manticoresearch/manticore.conf
```

Обратите внимание на примонтированную папку `manticore` и конфигурацию [manticore.conf](https://gist.github.com/Tkachenko-Ivan/9c8f8b5f98c80f902905b618878486ad#file-manticore-realtime-conf).

### <a name="Header1.3"></a>Индексирование

Наполните БД и постройте поисковый индекс:

```bash
docker exec manticore gosu manticore indexer --all --rotate
```

Чтобы индексирование проходило по расписанию, можно использовать crontab:

```
0 1 * * * docker exec -t manticore gosu manticore indexer --all --rotate  >> /var/log/manticore_indexer.log 2>&1
```

## <a name="Header2"></a>Использование

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

### Поиск

```bash
curl -X 'GET' \
  'http://localhost:8050/news-seacher/news/find?text=костыль' \
  -H 'accept: */*'
```
