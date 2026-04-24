package com.example.stringservice.service;

import com.example.stringservice.model.*;
import com.example.stringservice.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TelegramBotService extends TelegramLongPollingBot {
    
    @Value("${telegram.bot.token}")
    private String botToken;
    
    @Value("${telegram.bot.username}")
    private String botUsername;
    
    @Autowired
    private TgUserRepository userRepository;
    
    @Autowired
    private NewsSourceRepository sourceRepository;
    
    @Autowired
    private TagRepository tagRepository;
    
    @Autowired
    private UserSourceRepository userSourceRepository;
    
    @Autowired
    private UserTagRepository userTagRepository;
    
    @Override
    public String getBotUsername() {
        return botUsername;
    }
    
    @Override
    public String getBotToken() {
        return botToken;
    }
    
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            String userName = update.getMessage().getFrom().getUserName();
            
            // Регистрируем пользователя при первом обращении
            registerUser(chatId, userName, update);
            
            // Обработка команд
            if (messageText.startsWith("/")) {
                handleCommand(chatId, messageText);
            } else {
                handleMessage(chatId, messageText);
            }
        }
    }
    
    private void registerUser(Long chatId, String userName, Update update) {
        Optional<TgUser> existingUser = userRepository.findByChatId(chatId);
        
        if (existingUser.isEmpty()) {
            TgUser newUser = new TgUser();
            newUser.setChatId(chatId);
            newUser.setUsername(userName);
            newUser.setFirstName(update.getMessage().getFrom().getFirstName());
            newUser.setLastName(update.getMessage().getFrom().getLastName());
            newUser.setRegisteredAt(LocalDateTime.now());
            newUser.setIsActive(true);
            newUser.setScanIntervalMinutes(60);
            newUser.setNewsCount(5);
            
            userRepository.save(newUser);
            
            sendWelcomeMessage(chatId);
        }
    }
    
    private void sendWelcomeMessage(Long chatId) {
        String welcome = "Добро пожаловать в новостной бот!\n\n" +
                "Я буду искать новости по вашим тегам и присылать их вам.\n\n" +
                "Доступные команды:\n" +
                "/start - начать работу\n" +
                "/sources - управление источниками\n" +
                "/tags - управление тегами\n" +
                "/settings - настройки (интервал, количество новостей)\n" +
                "/help - помощь";
        
        sendText(chatId, welcome);
    }
    
    private void handleCommand(Long chatId, String command) {
        System.out.println("Обработка команды: " + command);
        
        switch (command) {
            case "/start":
                sendWelcomeMessage(chatId);
                break;
                
            case "/sources":
                showSourcesMenu(chatId);
                break;
                
            case "/enabled":
            case "/my_sources":
                showEnabledSources(chatId);
                break;
                
            case "/tags":
                showTagsMenu(chatId);
                break;
                
            case "/settings":
                showSettingsMenu(chatId);
                break;
                
            case "/help":
                sendHelp(chatId);
                break;
                
            default:
                sendText(chatId, "Неизвестная команда. Введите /help для списка команд.");
        }
    }
    
    private void showEnabledSources(Long chatId) {
        Optional<TgUser> userOpt = userRepository.findByChatId(chatId);
        
        if (userOpt.isEmpty()) return;
        
        TgUser user = userOpt.get();
        List<NewsSource> enabledSources = userSourceRepository.findEnabledSourcesByUserId(user.getId());
        
        if (enabledSources.isEmpty()) {
            sendText(chatId, "📭 У вас нет включённых источников.\n\nИспользуйте `/sources` чтобы включить источники.");
            return;
        }
        
        StringBuilder message = new StringBuilder("✅ *Ваши включённые источники:*\n\n");
        for (NewsSource source : enabledSources) {
            message.append("• ").append(source.getName()).append("\n");
        }
        
        sendText(chatId, message.toString());
    }

    private void showSourcesMenu(Long chatId) {
        List<NewsSource> sources = sourceRepository.findAll();
        Optional<TgUser> userOpt = userRepository.findByChatId(chatId);
        
        if (userOpt.isEmpty()) return;
        
        TgUser user = userOpt.get();
        
        StringBuilder message = new StringBuilder("📰 *Доступные источники:*\n\n");
        
        for (NewsSource source : sources) {
            // Проверяем, включен ли источник для этого пользователя
            boolean isEnabled = userSourceRepository.existsByUserIdAndSourceIdAndIsEnabledTrue(user.getId(), source.getId());
            
            // Добавляем эмодзи статуса
            String status = isEnabled ? "✅" : "❌";
            
            message.append(status).append(" *").append(source.getName()).append("*\n");
        }
        
        message.append("\n---\n");
        message.append("*Управление:*\n");
        message.append("`включить Название` - включить источник\n");
        message.append("`выключить Название` - выключить источник\n");
        message.append("\n*Пример:* `включить Lenta.ru`");
        
        sendText(chatId, message.toString());
    }
    
    private void showTagsMenu(Long chatId) {
        Optional<TgUser> userOpt = userRepository.findByChatId(chatId);
        if (userOpt.isEmpty()) return;

        TgUser user = userOpt.get();

        List<Tag> userTags = tagRepository.findByUserId(user.getId());

        StringBuilder message = new StringBuilder("🏷️*Ваши персональные теги:*\n\n");

        if (userTags.isEmpty()) {
            message.append("У вас пока нет тегов\n\n");
        } else {
            for (Tag tag : userTags) {
                message.append("✅ ").append(tag.getName()).append("\n");
            }
            message.append("\n");
        }

        message.append("*Управление тегами:*\n");
        message.append("`добавить тег Название` - добавить тег\n");
        message.append("`удалить тег Название` - удалить тег\n");
        message.append("`очистить теги` - удалить все теги");

        sendText(chatId, message.toString());
    }
    
    private void showSettingsMenu(Long chatId) {
        Optional<TgUser> userOpt = userRepository.findByChatId(chatId);
        
        if (userOpt.isEmpty()) return;
        
        TgUser user = userOpt.get();
        
        String settings = "⚙️ *Настройки*\n\n" +
                "Интервал сканирования: " + user.getScanIntervalMinutes() + " минут\n" +
                "Количество новостей: " + user.getNewsCount() + "\n\n" +
                "Чтобы изменить:\n" +
                "`интервал 30` - сканировать каждые 30 минут\n" +
                "`количество 10` - проверять 10 последних новостей";
        
        sendText(chatId, settings);
    }
    
    private void sendHelp(Long chatId) {
        String help = "🤖 *Новостной бот - помощь*\n\n" +
                "Как это работает:\n" +
                "1. Выберите источники новостей (/sources)\n" +
                "2. Добавьте теги для отслеживания (/tags)\n" +
                "3. Настройте интервал сканирования (/settings)\n\n" +
                "Бот будет автоматически проверять новости и присылать вам те, где встречаются ваши теги.\n\n" +
                "*Команды:*\n" +
                "/start - начать работу\n" +
                "/sources - управление источниками\n" +
                "/my_sources - показать включённые источники\n" +
                "/tags - управление тегами\n" +
                "/settings - настройки\n" +
                "/help - эта справка";
        
        sendText(chatId, help);
    }
    
    private void handleMessage(Long chatId, String message) {
        message = message.toLowerCase().trim();
        Optional<TgUser> userOpt = userRepository.findByChatId(chatId);
        
        if (userOpt.isEmpty()) return;
        
        TgUser user = userOpt.get();
        
        if (message.startsWith("включить ")) {
            enableSource(chatId, message.substring(9).trim(), user);
        } else if (message.startsWith("выключить ")) {
            disableSource(chatId, message.substring(10).trim(), user);
        } else if (message.startsWith("добавить тег ")) {
            addTag(chatId, message.substring(13).trim(), user);
        } else if (message.startsWith("удалить тег ")) {
            removeTag(chatId, message.substring(12).trim(), user);
        } else if (message.startsWith("очистить теги")) {
            clearTags(chatId, user);
        } else if (message.startsWith("интервал ")) {
            setInterval(chatId, message.substring(9).trim(), user);
        } else if (message.startsWith("количество ")) {
            setNewsCount(chatId, message.substring(11).trim(), user);
        } else {
            sendText(chatId, "Не понимаю команду. Введите /help для списка команд.");
        }
    }
    
    private void enableSource(Long chatId, String sourceName, TgUser user) {
        Optional<NewsSource> sourceOpt = sourceRepository.findByNameIgnoreCase(sourceName);
        
        if (sourceOpt.isEmpty()) {
            sendText(chatId, "❌ Источник \"" + sourceName + "\" не найден\n\nДоступные источники:\n" + getSourcesList());
            return;
        }
        
        NewsSource source = sourceOpt.get();
        
        try {
            // Используем исправленный метод enableSourceNative
            userSourceRepository.enableSourceNative(user.getId(), source.getId());
            
            sendText(chatId, "✅ Источник \"" + source.getName() + "\" успешно включён");
            
        } catch (Exception e) {
            sendText(chatId, "❌ Ошибка при включении источника: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void disableSource(Long chatId, String sourceName, TgUser user) {
        Optional<NewsSource> sourceOpt = sourceRepository.findByNameIgnoreCase(sourceName);
        
        if (sourceOpt.isEmpty()) {
            sendText(chatId, "❌ Источник \"" + sourceName + "\" не найден\n\nДоступные источники:\n" + getSourcesList());
            return;
        }
        
        NewsSource source = sourceOpt.get();
        
        try {
            // Обновляем статус
            userSourceRepository.disableSource(user.getId(), source.getId());
            
            sendText(chatId, "❌ Источник \"" + source.getName() + "\" успешно выключен");
            
        } catch (Exception e) {
            sendText(chatId, "❌ Ошибка при выключении источника: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getSourcesList() {
        List<NewsSource> sources = sourceRepository.findAll();
        StringBuilder sb = new StringBuilder();
        for (NewsSource source : sources) {
            sb.append("• ").append(source.getName()).append("\n");
        }
        return sb.toString();
    }
    
    private void addTag(Long chatId, String tagName, TgUser user) {
        boolean tagExists = userTagRepository.existsByUserIdAndTagName(user.getId(), tagName);

        if (tagExists) {
            sendText(chatId, "ℹ️ Тег \"" + tagName + "\" уже есть в вашем списке");
            return;
        }

        Tag newTag = new Tag();
        newTag.setName(tagName);
        newTag.setCreatedAt(LocalDateTime.now());
        tagRepository.save(newTag);

        UserTag userTag = new UserTag();
        userTag.setUser(user);
        userTag.setTag(newTag);
        userTag.setCreatedAt(LocalDateTime.now());
        userTagRepository.save(userTag);

        sendText(chatId, "✅ Тег \"" + tagName + "\" добавлен в ваш список");
    }
    
    private void removeTag(Long chatId, String tagName, TgUser user) {
        Optional<UserTag> userTagOpt = userTagRepository.findByUserIdAndTagName(user.getId(), tagName);

        if (userTagOpt.isEmpty()) {
            sendText(chatId, "❌ Тег \"" + tagName + "\" не найден в вашем списке");
            return;
        }

        UserTag userTag = userTagOpt.get();
        Tag tag = userTag.getTag();

        userTagRepository.delete(userTag);

        boolean otherUsersUseTag = userTagRepository.existsByTagId(tag.getId());

        if (!otherUsersUseTag) {
            tagRepository.delete(tag);
        }
        sendText(chatId, "✅ Тег \"" + tagName + "\" удалён из вашего списка");
    }

    private void clearTags(Long chatId, TgUser user) {
        List<UserTag> userTags = userTagRepository.findAllByUserId(user.getId());

        if (userTags.isEmpty()) {
            sendText(chatId, "ℹ️ У вас нет тегов для удаления");
            return;
        }

        List<Long> tagIdsToDelete = new ArrayList<>();
        for (UserTag userTag : userTags) {
            tagIdsToDelete.add(userTag.getTag().getId());
        }

        userTagRepository.deleteAllByUserId(user.getId());

        for (Long tagId : tagIdsToDelete) {
            boolean otherUsersUseTag = userTagRepository.existsByTagId(tagId);
            if (!otherUsersUseTag) {tagRepository.deleteById(tagId);}
        }

        sendText(chatId, "✅ Все ваши теги удалены");
    }
    
    private void setInterval(Long chatId, String intervalStr, TgUser user) {
        try {
            int interval = Integer.parseInt(intervalStr);
            if (interval < 5 || interval > 1440) {
                sendText(chatId, "Интервал должен быть от 5 до 1440 минут");
                return;
            }
            
            user.setScanIntervalMinutes(interval);
            userRepository.save(user);
            sendText(chatId, "✅ Интервал сканирования установлен: " + interval + " минут");
        } catch (NumberFormatException e) {
            sendText(chatId, "Некорректное число");
        }
    }
    
    private void setNewsCount(Long chatId, String countStr, TgUser user) {
        try {
            int count = Integer.parseInt(countStr);
            if (count < 1 || count > 50) {
                sendText(chatId, "Количество новостей должно быть от 1 до 50");
                return;
            }
            
            user.setNewsCount(count);
            userRepository.save(user);
            sendText(chatId, "✅ Количество новостей установлено: " + count);
        } catch (NumberFormatException e) {
            sendText(chatId, "Некорректное число");
        }
    }
    
    private void sendText(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setParseMode("Markdown");
        
        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Ошибка отправки сообщения: " + e.getMessage());
        }
    }
    
    @PostConstruct
    public void init() {
        // Добавляем тестовые источники при первом запуске
        if (sourceRepository.count() == 0) {
            addSampleSources();
        }
        
        // Добавляем тестовые теги при первом запуске
        if (tagRepository.count() == 0) {
            addSampleTags();
        }
    }
    
    private void addSampleSources() {
        List<NewsSource> sources = List.of(
            createSource("Lenta.ru", "https://lenta.ru/rss/news", "RSS"),
            createSource("Аргументы и Факты", "https://aif.ru/rss/news.php", "RSS"),
            createSource("РИА Новости", "https://ria.ru/export/rss2/index.xml", "RSS"),
            createSource("Habr", "https://habr.com/ru/rss/all/", "RSS"),
            createSource("BBC Russian", "http://feeds.bbci.co.uk/russian/news/rss.xml", "RSS")
        );
        
        sourceRepository.saveAll(sources);
    }
    
    private NewsSource createSource(String name, String url, String type) {
        NewsSource source = new NewsSource();
        source.setName(name);
        source.setUrl(url);
        source.setFeedType(type);
        source.setIsActive(true);
        source.setCreatedAt(LocalDateTime.now());
        return source;
    }
    
    private void addSampleTags() {
        List<Tag> tags = List.of(
            createTag("Java"),
            createTag("Python"),
            createTag("IT"),
            createTag("Технологии"),
            createTag("Наука"),
            createTag("Политика"),
            createTag("Экономика"),
            createTag("Спорт"),
            createTag("Программирование"),
            createTag("Искусственный интеллект")
        );
        
        tagRepository.saveAll(tags);
    }
    
    private Tag createTag(String name) {
        Tag tag = new Tag();
        tag.setName(name);
        tag.setCreatedAt(LocalDateTime.now());
        return tag;
    }
    
    public void sendNewsNotification(Long chatId, NewsItem news, List<Tag> matchedTags) {
        StringBuilder message = new StringBuilder();
        message.append("📰 *Найдена новость по вашим тегам:*\n\n");
        message.append("📅 *Дата:* ").append(formatDate(news.getPublishedAt())).append("\n");
        message.append("📌 *Источник:* ").append(news.getSource().getName()).append("\n");
        message.append("🔖 *Теги:* ");
        
        for (int i = 0; i < matchedTags.size(); i++) {
            if (i > 0) message.append(", ");
            message.append("#").append(matchedTags.get(i).getName());
        }
        
        message.append("\n\n*").append(news.getTitle()).append("*\n\n");
        
        if (news.getContent() != null && !news.getContent().isEmpty()) {
            String content = news.getContent().replaceAll("<[^>]*>", "");
            if (content.length() > 300) {
                content = content.substring(0, 300) + "...";
            }
            message.append(content).append("\n\n");
        }
        
        message.append("🔗 [Читать далее](").append(news.getLink()).append(")");
        
        sendText(chatId, message.toString());
    }
    
    private String formatDate(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        return date.format(formatter);
    }
}