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
