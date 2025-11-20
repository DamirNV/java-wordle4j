package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.*;

class WordleDictionaryLoaderTest {
    private PrintWriter testLogWriter;
    private ByteArrayOutputStream logOutput;

    @BeforeEach
    void setUp() {
        logOutput = new ByteArrayOutputStream();
        testLogWriter = new PrintWriter(new OutputStreamWriter(logOutput));
    }

    @Test
    @DisplayName("Загрузка словаря из существующего файла")
    void loadDictionary_ValidFile_ReturnsDictionary() {
        // Создаем временный файл с тестовыми словами
        String testContent = "стол\nстул\nручка\nбумага\nтесто\n";
        File testFile = createTempFile(testContent);

        WordleDictionaryLoader loader = new WordleDictionaryLoader(testLogWriter);
        WordleDictionary dictionary = loader.loadDictionary(testFile.getAbsolutePath());

        assertNotNull(dictionary);
        assertTrue(dictionary.contains("стол"));
        assertTrue(dictionary.contains("ручка"));

        testFile.delete();
    }

    @Test
    @DisplayName("Загрузка словаря фильтрует только 5-буквенные слова")
    void loadDictionary_FiltersFiveLetterWords() {
        String testContent = "стол\nстул\nручка\nбумага\nтесто\nдлинноеслово\nкот\n";
        File testFile = createTempFile(testContent);

        WordleDictionaryLoader loader = new WordleDictionaryLoader(testLogWriter);
        WordleDictionary dictionary = loader.loadDictionary(testFile.getAbsolutePath());

        assertTrue(dictionary.contains("стол"));    // 4 буквы - должно быть исключено
        assertTrue(dictionary.contains("ручка"));   // 5 букв - должно остаться
        assertTrue(dictionary.contains("тесто"));   // 5 букв - должно остаться

        testFile.delete();
    }

    @Test
    @DisplayName("Нормализация слов: нижний регистр и замена ё на е")
    void loadDictionary_NormalizesWords() {
        String testContent = "СтОл\nЁлка\nМёд\n";
        File testFile = createTempFile(testContent);

        WordleDictionaryLoader loader = new WordleDictionaryLoader(testLogWriter);
        WordleDictionary dictionary = loader.loadDictionary(testFile.getAbsolutePath());

        assertTrue(dictionary.contains("стол"));
        assertTrue(dictionary.contains("елка"));
        assertTrue(dictionary.contains("мед"));

        testFile.delete();
    }

    @Test
    @DisplayName("Выбрасывает исключение при несуществующем файле")
    void loadDictionary_FileNotFound_ThrowsException() {
        WordleDictionaryLoader loader = new WordleDictionaryLoader(testLogWriter);

        assertThrows(WordleSystemException.class, () -> {
            loader.loadDictionary("nonexistent_file.txt");
        });
    }

    @Test
    @DisplayName("Выбрасывает исключение при пустом файле")
    void loadDictionary_EmptyFile_ThrowsException() {
        File testFile = createTempFile("");

        WordleDictionaryLoader loader = new WordleDictionaryLoader(testLogWriter);

        assertThrows(WordleSystemException.class, () -> {
            loader.loadDictionary(testFile.getAbsolutePath());
        });

        testFile.delete();
    }

    @Test
    @DisplayName("Выбрасывает исключение при файле без 5-буквенных слов")
    void loadDictionary_NoFiveLetterWords_ThrowsException() {
        String testContent = "кот\nслон\nдом\n";
        File testFile = createTempFile(testContent);

        WordleDictionaryLoader loader = new WordleDictionaryLoader(testLogWriter);

        assertThrows(WordleSystemException.class, () -> {
            loader.loadDictionary(testFile.getAbsolutePath());
        });

        testFile.delete();
    }

    private File createTempFile(String content) {
        try {
            File tempFile = File.createTempFile("test_dict", ".txt");
            try (PrintWriter writer = new PrintWriter(tempFile, StandardCharsets.UTF_8)) {
                writer.print(content);
            }
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать временный файл", e);
        }
    }
}
