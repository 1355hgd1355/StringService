# Добро пожаловать!

Это приложение для осуществления POST и GET запросов. Теперь с телеграм-ботом, который может присылать новости.

# Как запустить?

1. Убедитесь, что у вас установлены JDK 17 и PostgreSQL.
2. Склонируйте данное приложение.
3. Создайте телеграм-бот и вставьте его токен в `application.properties`
```
telegram.bot.token=ТОКЕН_ВАШЕГО_БОТА
```
4. Создайте базу данных на локальном сервере с портом 5432 под именем "stringdb".
```
-- Таблица пользователей Telegram
CREATE TABLE tg_users (
    id BIGSERIAL PRIMARY KEY,
    chat_id BIGINT UNIQUE NOT NULL,  -- Telegram chat ID
    username VARCHAR(255),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    registered_at TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT true,
    scan_interval_minutes INTEGER DEFAULT 60,  -- Период сканирования в минутах
    news_count INTEGER DEFAULT 5  -- Количество последних новостей для проверки
);

-- Таблица источников новостей
CREATE TABLE news_sources (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,  -- Название источника (например, "Lenta.ru")
    url VARCHAR(512) NOT NULL,   -- URL RSS ленты
    feed_type VARCHAR(50) DEFAULT 'RSS',  -- RSS, ATOM, etc.
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    last_scanned_at TIMESTAMP  -- Время последнего сканирования
);

-- Таблица тегов для отслеживания
CREATE TABLE tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,  -- Название тега (например, "Java", "IT", "Политика")
    created_at TIMESTAMP NOT NULL
);

-- Связь пользователей с источниками (какие источники включены у пользователя)
CREATE TABLE user_sources (
    user_id BIGINT REFERENCES tg_users(id) ON DELETE CASCADE,
    source_id BIGINT REFERENCES news_sources(id) ON DELETE CASCADE,
    is_enabled BOOLEAN DEFAULT true,
    PRIMARY KEY (user_id, source_id)
);

-- Связь пользователей с тегами (какие теги отслеживает пользователь)
CREATE TABLE user_tags (
    user_id BIGINT REFERENCES tg_users(id) ON DELETE CASCADE,
    tag_id BIGINT REFERENCES tags(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL,
    PRIMARY KEY (user_id, tag_id)
);

-- Таблица новостей
CREATE TABLE news_items (
    id BIGSERIAL PRIMARY KEY,
    source_id BIGINT REFERENCES news_sources(id) ON DELETE CASCADE,
    title VARCHAR(1024) NOT NULL,
    content TEXT,  -- Полный текст новости или описание
    link VARCHAR(512) NOT NULL UNIQUE,  -- Ссылка на оригинал
    published_at TIMESTAMP NOT NULL,  -- Время публикации
    found_at TIMESTAMP NOT NULL,  -- Когда нашли
    guid VARCHAR(512)  -- Уникальный идентификатор из RSS
);

-- Связь новостей с тегами (какие теги найдены в новости)
CREATE TABLE news_tags (
    news_id BIGINT REFERENCES news_items(id) ON DELETE CASCADE,
    tag_id BIGINT REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (news_id, tag_id)
);

-- История отправленных сообщений (чтобы не дублировать)
CREATE TABLE sent_notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES tg_users(id) ON DELETE CASCADE,
    news_id BIGINT REFERENCES news_items(id) ON DELETE CASCADE,
    sent_at TIMESTAMP NOT NULL,
    UNIQUE(user_id, news_id)
);
```
5. Подключитесь к базе данных (используемый пароль - "database")
6. Через командную строку (Windows) выполните следующюю команду, предварительно перейдя в папку с приложением:
```
.\mvnw.cmd spring-boot:run
```
7. Наслаждайтесь.

# Как использовать телеграм-бот?
```
/start - начать работу
/sources - управление источниками
/tags - управление тегами
/settings - настройки (интервал, количество новостей)
/help - помощь
```
# Как использовать сервис строк?

### POST-запрос
```
curl -X POST http://localhost:8080/api/strings ^
  -H "Content-Type: application/json" ^
  -d "{\"data\": \"Привет!\"}"
```

### GET-запрос
```
curl http://localhost:8080/api/strings/1
```