package com.example.stringservice.service;

import com.example.stringservice.model.*;
import com.example.stringservice.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
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
        switch (command) {
            case "/start":
                sendText(chatId, "Бот уже работает! Используйте /help для списка команд.");
                break;
                
            case "/sources":
                showSourcesMenu(chatId);
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
    
    private void showSourcesMenu(Long chatId) {
        List<NewsSource> sources = sourceRepository.findAll();
        TgUser user = userRepository.findByChatId(chatId).orElse(null);
        
        if (user == null) return;
        
        StringBuilder message = new StringBuilder("📰 *Доступные источники:*\n\n");
        
        for (NewsSource source : sources) {
            boolean isEnabled = userSourceRepository.existsByUserIdAndSourceIdAndIsEnabledTrue(user.getId(), source.getId());
            String status = isEnabled ? "✅" : "❌";
            message.append(status).append(" ").append(source.getName()).append("\n");
        }
        
        message.append("\nЧтобы включить/выключить источник, отправьте:\n");
        message.append("`включить Название` или `выключить Название`");
        
        sendText(chatId, message.toString());
    }
    
    private void showTagsMenu(Long chatId) {
        List<Tag> allTags = tagRepository.findAll();
        TgUser user = userRepository.findByChatId(chatId).orElse(null);
        
        if (user == null) return;
        
        List<Tag> userTags = tagRepository.findByUserId(user.getId());
        
        StringBuilder message = new StringBuilder("🏷 *Ваши теги:*\n\n");
        
        if (userTags.isEmpty()) {
            message.append("У вас пока нет тегов\n\n");
        } else {
            for (Tag tag : userTags) {
                message.append("✅ ").append(tag.getName()).append("\n");
            }
            message.append("\n");
        }
        
        message.append("*Доступные теги:*\n");
        for (Tag tag : allTags) {
            if (!userTags.contains(tag)) {
                message.append("⚪️ ").append(tag.getName()).append("\n");
            }
        }
        
        message.append("\nЧтобы добавить тег: `добавить тег Название`\n");
        message.append("Чтобы удалить тег: `удалить тег Название`\n");
        message.append("Чтобы создать новый тег: `новый тег Название`");
        
        sendText(chatId, message.toString());
    }
    
    private void showSettingsMenu(Long chatId) {
        TgUser user = userRepository.findByChatId(chatId).orElse(null);
        
        if (user == null) return;
        
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
                "/sources - управление источниками\n" +
                "/tags - управление тегами\n" +
                "/settings - настройки\n" +
                "/help - эта справка";
        
        sendText(chatId, help);
    }
    
    private void handleMessage(Long chatId, String message) {
        message = message.toLowerCase().trim();
        TgUser user = userRepository.findByChatId(chatId).orElse(null);
        
        if (user == null) return;
        
        if (message.startsWith("включить ")) {
            enableSource(chatId, message.substring(9).trim(), user);
        } else if (message.startsWith("выключить ")) {
            disableSource(chatId, message.substring(10).trim(), user);
        } else if (message.startsWith("добавить тег ")) {
            addTag(chatId, message.substring(13).trim(), user);
        } else if (message.startsWith("удалить тег ")) {
            removeTag(chatId, message.substring(12).trim(), user);
        } else if (message.startsWith("новый тег ")) {
            createNewTag(chatId, message.substring(10).trim(), user);
        } else if (message.startsWith("интервал ")) {
            setInterval(chatId, message.substring(9).trim(), user);
        } else if (message.startsWith("количество ")) {
            setNewsCount(chatId, message.substring(11).trim(), user);
        } else {
            sendText(chatId, "Не понимаю команду. Введите /help для списка команд.");
        }
    }
    
    private void enableSource(Long chatId, String sourceName, TgUser user) {
        Optional<NewsSource> source = sourceRepository.findByNameIgnoreCase(sourceName);
        
        if (source.isEmpty()) {
            sendText(chatId, "Источник \"" + sourceName + "\" не найден");
            return;
        }
        
        userSourceRepository.enableSource(user.getId(), source.get().getId());
        sendText(chatId, "✅ Источник \"" + source.get().getName() + "\" включён");
    }
    
    private void disableSource(Long chatId, String sourceName, TgUser user) {
        Optional<NewsSource> source = sourceRepository.findByNameIgnoreCase(sourceName);
        
        if (source.isEmpty()) {
            sendText(chatId, "Источник \"" + sourceName + "\" не найден");
            return;
        }
        
        userSourceRepository.disableSource(user.getId(), source.get().getId());
        sendText(chatId, "❌ Источник \"" + source.get().getName() + "\" выключён");
    }
    
    private void addTag(Long chatId, String tagName, TgUser user) {
        Optional<Tag> tag = tagRepository.findByNameIgnoreCase(tagName);
        
        if (tag.isEmpty()) {
            sendText(chatId, "Тег \"" + tagName + "\" не найден. Создайте его командой `новый тег " + tagName + "`");
            return;
        }
        
        if (userTagRepository.existsByUserIdAndTagId(user.getId(), tag.get().getId())) {
            sendText(chatId, "Тег \"" + tag.get().getName() + "\" уже добавлен");
            return;
        }
        
        UserTag userTag = new UserTag();
        userTag.setUser(user);
        userTag.setTag(tag.get());
        userTag.setCreatedAt(LocalDateTime.now());
        userTagRepository.save(userTag);
        
        sendText(chatId, "✅ Тег \"" + tag.get().getName() + "\" добавлен");
    }
    
    private void removeTag(Long chatId, String tagName, TgUser user) {
        Optional<Tag> tag = tagRepository.findByNameIgnoreCase(tagName);
        
        if (tag.isEmpty()) {
            sendText(chatId, "Тег \"" + tagName + "\" не найден");
            return;
        }
        
        userTagRepository.deleteByUserIdAndTagId(user.getId(), tag.get().getId());
        sendText(chatId, "❌ Тег \"" + tag.get().getName() + "\" удалён");
    }
    
    private void createNewTag(Long chatId, String tagName, TgUser user) {
        if (tagRepository.findByNameIgnoreCase(tagName).isPresent()) {
            sendText(chatId, "Тег \"" + tagName + "\" уже существует");
            return;
        }
        
        Tag newTag = new Tag();
        newTag.setName(tagName);
        newTag.setCreatedAt(LocalDateTime.now());
        tagRepository.save(newTag);
        
        // Сразу добавляем пользователю
        UserTag userTag = new UserTag();
        userTag.setUser(user);
        userTag.setTag(newTag);
        userTag.setCreatedAt(LocalDateTime.now());
        userTagRepository.save(userTag);
        
        sendText(chatId, "✅ Создан и добавлен новый тег: \"" + tagName + "\"");
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
            e.printStackTrace();
        }
    }
    
    @PostConstruct
    public void init() {
        // Добавляем тестовые источники и теги при первом запуске
        if (sourceRepository.count() == 0) {
            addSampleSources();
        }
        
        if (tagRepository.count() == 0) {
            addSampleTags();
        }
    }
    
    private void addSampleSources() {
        List<NewsSource> sources = List.of(
            createSource("Lenta.ru", "https://lenta.ru/rss", "RSS"),
            createSource("РИА Новости", "https://ria.ru/export/rss2/index.xml", "RSS"),
            createSource("TJournal", "https://tjournal.ru/rss", "RSS"),
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
            createTag("Спорт")
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
        message.append(news.getContent() != null ? 
                      (news.getContent().length() > 200 ? 
                       news.getContent().substring(0, 200) + "..." : 
                       news.getContent()) : "");
        message.append("\n\n🔗 [Читать далее](").append(news.getLink()).append(")");
        
        sendText(chatId, message.toString());
    }
    
    private String formatDate(LocalDateTime date) {
        java.time.format.DateTimeFormatter formatter = 
            java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        return date.format(formatter);
    }
}