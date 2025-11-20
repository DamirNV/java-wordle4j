package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

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
        String testContent = "аббат\nавеню\nавгит\nаврал\nабак\n";
        File testFile = createTempFile(testContent);

        WordleDictionaryLoader loader = new WordleDictionaryLoader(testLogWriter);
        WordleDictionary dictionary = loader.loadDictionary(testFile.getAbsolutePath());

        assertNotNull(dictionary);
        assertTrue(dictionary.contains("аббат"));
        assertTrue(dictionary.contains("авеню"));
        assertTrue(dictionary.contains("авгит"));

        testFile.delete();
    }

    @Test
    @DisplayName("Загрузка словаря фильтрует только 5-буквенные слова")
    void loadDictionary_FiltersFiveLetterWords() {
        String testContent = "аббат\nабажур\nбанан\nкот\nабсурд\n";
        File testFile = createTempFile(testContent);

        WordleDictionaryLoader loader = new WordleDictionaryLoader(testLogWriter);
        WordleDictionary dictionary = loader.loadDictionary(testFile.getAbsolutePath());

        List<String> words = dictionary.getWords();

        assertEquals(2, words.size());
        assertTrue(words.contains("аббат"));
        assertTrue(words.contains("банан"));

        assertFalse(words.contains("кот"));
        assertFalse(words.contains("абажур"));
        assertFalse(words.contains("абсурд"));

        testFile.delete();
    }

    @Test
    @DisplayName("Нормализация слов: нижний регистр и замена ё на е")
    void loadDictionary_NormalizesWords() {
        String testContent = "АбБаТ\nчЁлка\nЁршик\nавеню\n";
        File testFile = createTempFile(testContent);

        WordleDictionaryLoader loader = new WordleDictionaryLoader(testLogWriter);
        WordleDictionary dictionary = loader.loadDictionary(testFile.getAbsolutePath());

        assertTrue(dictionary.contains("аббат"));
        assertTrue(dictionary.contains("челка"));
        assertTrue(dictionary.contains("ершик"));
        assertTrue(dictionary.contains("авеню"));

        testFile.delete();
    }

    @Test
    @DisplayName("Игнорирует строки с не-буквенными символами")
    void loadDictionary_IgnoresNonLetterWords() {
        String testContent = "аббат\nword\n12345\nавгит\n";
        File testFile = createTempFile(testContent);

        WordleDictionaryLoader loader = new WordleDictionaryLoader(testLogWriter);
        WordleDictionary dictionary = loader.loadDictionary(testFile.getAbsolutePath());

        assertTrue(dictionary.contains("аббат"));
        assertTrue(dictionary.contains("авгит"));

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
        String testContent = "кот\nслон\nабажур\n";
        File testFile = createTempFile(testContent);

        WordleDictionaryLoader loader = new WordleDictionaryLoader(testLogWriter);

        assertThrows(WordleSystemException.class, () -> {
            loader.loadDictionary(testFile.getAbsolutePath());
        });

        testFile.delete();
    }

    @Test
    @DisplayName("Корректная обработка UTF-8 кодировки с русскими символами")
    void loadDictionary_Utf8Encoding_PreservesRussianCharacters() {
        String testContent = "аббат\nчёлка\nшифон\nавеню\n";
        File testFile = createTempFile(testContent);

        WordleDictionaryLoader loader = new WordleDictionaryLoader(testLogWriter);
        WordleDictionary dictionary = loader.loadDictionary(testFile.getAbsolutePath());

        assertNotNull(dictionary);
        assertTrue(dictionary.contains("аббат"));
        assertTrue(dictionary.contains("челка"));
        assertTrue(dictionary.contains("шифон"));
        assertTrue(dictionary.contains("авеню"));

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