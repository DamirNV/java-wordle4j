package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class WordleDictionaryTest {
    private WordleDictionary dictionary;
    private PrintWriter testLogWriter;
    private ByteArrayOutputStream logOutput;

    @BeforeEach
    void setUp() {
        logOutput = new ByteArrayOutputStream();
        testLogWriter = new PrintWriter(new OutputStreamWriter(logOutput, StandardCharsets.UTF_8), true);

        List<String> testWords = Arrays.asList("ручка", "тесто", "баран", "сарай", "салат", "челка");
        dictionary = new WordleDictionary(testWords, testLogWriter);
    }

    @Test
    @DisplayName("Проверка существующего слова в словаре")
    void contains_ExistingWord_ReturnsTrue() {
        assertTrue(dictionary.contains("тесто"));
        assertTrue(dictionary.contains("ручка"));
    }

    @Test
    @DisplayName("Проверка отсутствующего слова в словаре")
    void contains_NonExistingWord_ReturnsFalse() {
        assertFalse(dictionary.contains("компьютер"));
    }

    @Test
    @DisplayName("Проверка слова с неправильной длиной")
    void contains_WordWithWrongLength_ReturnsFalse() {
        assertFalse(dictionary.contains("кот"));
        assertFalse(dictionary.contains("длинноеслово"));
    }

    @Test
    @DisplayName("Проверка null слова")
    void contains_NullWord_ReturnsFalse() {
        assertFalse(dictionary.contains(null));
    }

    @Test
    @DisplayName("Нормализация ввода: нижний регистр и замена ё на е")
    void contains_NormalizesInput() {
        assertTrue(dictionary.contains("РУЧКА"));
        assertTrue(dictionary.contains(" РуЧкА "));
        assertTrue(dictionary.contains("чёлка"));
        assertTrue(dictionary.contains("ЧЁЛКА"));
    }

    @Test
    @DisplayName("Получение случайного слова")
    void getRandomWord_ReturnsValidWord() {
        String randomWord = dictionary.getRandomWord();
        assertNotNull(randomWord);
        assertEquals(5, randomWord.length());
        assertTrue(dictionary.contains(randomWord));
    }

    @Test
    @DisplayName("Повторные вызовы getRandomWord могут возвращать одинаковые слова")
    void getRandomWord_MultipleCalls_CanReturnSameWord() {
        Set<String> results = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            results.add(dictionary.getRandomWord());
        }
        assertTrue(results.size() >= 1 && results.size() <= 6);
    }

    @Test
    @DisplayName("Получение списка слов возвращает копию")
    void getWords_ReturnsCopyOfWords() {
        List<String> words = dictionary.getWords();
        assertEquals(6, words.size());

        words.add("новоеслово");
        assertEquals(6, dictionary.getWords().size());
        assertFalse(dictionary.getWords().contains("новоеслово"));
    }

    @Test
    @DisplayName("Фильтрация слов по пустому фильтру возвращает все слова")
    void getFilteredWords_EmptyFilter_ReturnsAllWords() {
        WordleHintFilter filter = new WordleHintFilter();
        List<String> filtered = dictionary.getFilteredWords(filter);

        assertEquals(6, filtered.size());
        assertTrue(filtered.containsAll(Arrays.asList("ручка", "тесто", "баран", "сарай", "салат", "челка")));
    }

    @Test
    @DisplayName("Создание словаря с null списком слов")
    void constructor_NullWords_ThrowsException() {
        assertThrows(WordleSystemException.class, () -> new WordleDictionary(null, testLogWriter));
    }

    @Test
    @DisplayName("Создание словаря с null логгером")
    void constructor_NullLogger_ThrowsException() {
        assertThrows(WordleSystemException.class, () -> new WordleDictionary(Arrays.asList("слово"), null));
    }

    @Test
    @DisplayName("Создание словаря с пустым списком слов")
    void constructor_EmptyWords_ThrowsException() {
        assertThrows(WordleSystemException.class, () -> new WordleDictionary(new ArrayList<>(), testLogWriter));
    }

    @Test
    @DisplayName("Работа с словарем из одного корректного слова")
    void singleWordDictionary_WorksCorrectly() {
        List<String> singleWord = Arrays.asList("молот");  // ← нормальное 5-буквенное слово
        WordleDictionary singleDict = new WordleDictionary(singleWord, testLogWriter);

        assertTrue(singleDict.contains("молот"));
        assertEquals("молот", singleDict.getRandomWord());
        assertEquals(1, singleDict.getWords().size());
    }

    @Test
    @DisplayName("Логгирование создания словаря и выбора слова")
    void operations_AreLogged() {
        testLogWriter.flush();
        String log = logOutput.toString(StandardCharsets.UTF_8);

        assertTrue(log.contains("Словарь создан"));
        assertTrue(log.contains("слов: 6"));

        dictionary.getRandomWord();
        testLogWriter.flush();
        log = logOutput.toString(StandardCharsets.UTF_8);

        assertTrue(log.contains("Выбрано случайное слово"));
    }
}