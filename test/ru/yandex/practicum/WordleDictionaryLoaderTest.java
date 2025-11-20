package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.io.*;
import java.nio.charset.StandardCharsets;

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
        String testContent = "ручка\nбумага\nтесто\nслово\n";
        File testFile = createTempFile(testContent);

        WordleDictionaryLoader loader = new WordleDictionaryLoader(testLogWriter);
        WordleDictionary dictionary = loader.loadDictionary(testFile.getAbsolutePath());

        assertNotNull(dictionary);
        assertTrue(dictionary.contains("ручка"));
        assertTrue(dictionary.contains("слово"));

        testFile.delete();
    }

    @Test
    @DisplayName("Загрузка словаря фильтрует только 5-буквенные слова")
    void loadDictionary_FiltersFiveLetterWords() {
        String testContent = "стол\nстул\nручка\nбумага\nтесто\nдлинноеслово\nкот\n";
        File testFile = createTempFile(testContent);

        WordleDictionaryLoader loader = new WordleDictionaryLoader(testLogWriter);
        WordleDictionary dictionary = loader.loadDictionary(testFile.getAbsolutePath());

        assertFalse(dictionary.contains("стол"));
        assertFalse(dictionary.contains("стул"));
        assertTrue(dictionary.contains("ручка"));
        assertTrue(dictionary.contains("тесто"));

        testFile.delete();
    }

    @Test
    @DisplayName("Нормализация слов: нижний регистр и замена ё на е")
    void loadDictionary_NormalizesWords() {
        String testContent = "Ручка\nЁлка\nМёд\nСлово\n";
        File testFile = createTempFile(testContent);

        WordleDictionaryLoader loader = new WordleDictionaryLoader(testLogWriter);
        WordleDictionary dictionary = loader.loadDictionary(testFile.getAbsolutePath());

        assertTrue(dictionary.contains("ручка"));
        assertTrue(dictionary.contains("елка"));
        assertTrue(dictionary.contains("мед"));
        assertTrue(dictionary.contains("слово"));

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