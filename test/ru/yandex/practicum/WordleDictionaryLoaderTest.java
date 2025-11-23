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
        testLogWriter = new PrintWriter(new OutputStreamWriter(logOutput, StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("Загрузка словаря из существующего файла")
    void loadDictionary_ValidFile_ReturnsDictionary() throws IOException {
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
    void loadDictionary_FiltersFiveLetterWords() throws IOException {
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
    void loadDictionary_NormalizesWords() throws IOException {
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
    @DisplayName("Удаление дубликатов после нормализации")
    void loadDictionary_RemovesDuplicates() throws IOException {
        String testContent = "аббат\nАбБат\nчелка\nЧЁлка\n";
        File testFile = createTempFile(testContent);

        WordleDictionaryLoader loader = new WordleDictionaryLoader(testLogWriter);
        WordleDictionary dictionary = loader.loadDictionary(testFile.getAbsolutePath());

        List<String> words = dictionary.getWords();
        assertEquals(2, words.size());

        testFile.delete();
    }

    @Test
    @DisplayName("Выбрасывает исключение при несуществующем файле")
    void loadDictionary_FileNotFound_ThrowsException() {
        WordleDictionaryLoader loader = new WordleDictionaryLoader(testLogWriter);
        assertThrows(WordleSystemException.class, () -> loader.loadDictionary("nonexistent_file.txt"));
    }

    @Test
    @DisplayName("Выбрасывает исключение при пустом файле")
    void loadDictionary_EmptyFile_ThrowsException() throws IOException {
        File testFile = createTempFile("");
        WordleDictionaryLoader loader = new WordleDictionaryLoader(testLogWriter);

        assertThrows(WordleSystemException.class, () -> loader.loadDictionary(testFile.getAbsolutePath()));

        testFile.delete();
    }

    @Test
    @DisplayName("Выбрасывает исключение при файле без 5-буквенных слов")
    void loadDictionary_NoFiveLetterWords_ThrowsException() throws IOException {
        String testContent = "кот\nслон\nабажур\n";
        File testFile = createTempFile(testContent);
        WordleDictionaryLoader loader = new WordleDictionaryLoader(testLogWriter);

        assertThrows(WordleSystemException.class, () -> loader.loadDictionary(testFile.getAbsolutePath()));

        testFile.delete();
    }

    @Test
    @DisplayName("Обработка слов с пробелами")
    void loadDictionary_TrimsSpaces() throws IOException {
        String testContent = "  аббат  \n  авеню \n";
        File testFile = createTempFile(testContent);

        WordleDictionaryLoader loader = new WordleDictionaryLoader(testLogWriter);
        WordleDictionary dictionary = loader.loadDictionary(testFile.getAbsolutePath());

        assertTrue(dictionary.contains("аббат"));
        assertTrue(dictionary.contains("авеню"));

        testFile.delete();
    }

    @Test
    @DisplayName("Игнорирует пустые строки в файле")
    void loadDictionary_IgnoresEmptyLines() throws IOException {
        String testContent = "аббат\n\nавеню\n\n\nавгит\n";
        File testFile = createTempFile(testContent);

        WordleDictionaryLoader loader = new WordleDictionaryLoader(testLogWriter);
        WordleDictionary dictionary = loader.loadDictionary(testFile.getAbsolutePath());

        assertEquals(3, dictionary.getWords().size());

        testFile.delete();
    }

    @Test
    @DisplayName("Игнорирует строки с не-буквенными символами")
    void loadDictionary_IgnoresNonLetterWords() throws IOException {
        String testContent = "аббат\nword\n12345\nтест!\nавеню\n";
        File testFile = createTempFile(testContent);

        WordleDictionaryLoader loader = new WordleDictionaryLoader(testLogWriter);
        WordleDictionary dictionary = loader.loadDictionary(testFile.getAbsolutePath());

        assertEquals(2, dictionary.getWords().size());
        assertTrue(dictionary.contains("аббат"));
        assertTrue(dictionary.contains("авеню"));

        testFile.delete();
    }

    @Test
    @DisplayName("Корректная обработка различных UTF-8 символов")
    void loadDictionary_HandlesVariousUtf8Characters() throws IOException {
        String testContent = "аббат\nлёнка\nшифон\nэтнос\nюнкер\nбанан\n";
        File testFile = createTempFile(testContent);

        WordleDictionaryLoader loader = new WordleDictionaryLoader(testLogWriter);
        WordleDictionary dictionary = loader.loadDictionary(testFile.getAbsolutePath());

        List<String> words = dictionary.getWords();

        assertTrue(words.contains("ленка"));
        assertFalse(words.contains("лёнка"));
        assertTrue(dictionary.contains("шифон"));
        assertTrue(dictionary.contains("этнос"));
        assertTrue(dictionary.contains("юнкер"));
        assertTrue(dictionary.contains("банан"));

        testFile.delete();
    }

    @Test
    @DisplayName("Логгирование процесса загрузки")
    void loadDictionary_LogsLoadingProcess() throws IOException {
        String testContent = "аббат\nавеню\nавгит\n";
        File testFile = createTempFile(testContent);

        WordleDictionaryLoader loader = new WordleDictionaryLoader(testLogWriter);
        loader.loadDictionary(testFile.getAbsolutePath());

        testLogWriter.flush();

        String log = logOutput.toString(StandardCharsets.UTF_8);
        assertTrue(log.contains("Загрузка словаря из файла"));
        assertTrue(log.contains("Успешно загружено"));

        testFile.delete();
    }

    @Test
    @DisplayName("Точные сообщения логгирования")
    void loadDictionary_ExactLogMessages() throws IOException {
        String testContent = "аббат\nавеню\n";
        File testFile = createTempFile(testContent);

        WordleDictionaryLoader loader = new WordleDictionaryLoader(testLogWriter);
        loader.loadDictionary(testFile.getAbsolutePath());

        testLogWriter.flush();
        String log = logOutput.toString(StandardCharsets.UTF_8);

        assertTrue(log.contains("Загрузка словаря из файла: " + testFile.getAbsolutePath()));
        assertTrue(log.contains("Успешно загружено 2 уникальных слов"));

        testFile.delete();
    }

    private File createTempFile(String content) throws IOException {
        File tempFile = File.createTempFile("test_dict", ".txt");
        tempFile.deleteOnExit();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8))) {
            writer.print(content);
        }
        return tempFile;
    }
}