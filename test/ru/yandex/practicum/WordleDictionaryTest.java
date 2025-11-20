package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WordleDictionaryTest {
    private WordleDictionary dictionary;
    private PrintWriter testLogWriter;
    private ByteArrayOutputStream logOutput;

    @BeforeEach
    void setUp() {
        logOutput = new ByteArrayOutputStream();
        testLogWriter = new PrintWriter(new OutputStreamWriter(logOutput));

        List<String> testWords = Arrays.asList("стол", "стул", "ручка", "бумага", "тесто");
        dictionary = new WordleDictionary(testWords, testLogWriter);
    }

    @Test
    @DisplayName("Проверка существующего слова в словаре")
    void contains_ExistingWord_ReturnsTrue() {
        assertTrue(dictionary.contains("стол"));
        assertTrue(dictionary.contains("ручка"));
    }

    @Test
    @DisplayName("Проверка отсутствующего слова в словаре")
    void contains_NonExistingWord_ThrowsException() {
        assertThrows(WordNotFoundInDictionaryException.class, () -> {
            dictionary.contains("компьютер");
        });
    }

    @Test
    @DisplayName("Проверка слова с неправильной длиной")
    void contains_WordWithWrongLength_ThrowsException() {
        assertThrows(WordNotFoundInDictionaryException.class, () -> {
            dictionary.contains("кот");
        });

        assertThrows(WordNotFoundInDictionaryException.class, () -> {
            dictionary.contains("длинноеслово");
        });
    }

    @Test
    @DisplayName("Проверка null слова")
    void contains_NullWord_ThrowsException() {
        assertThrows(WordNotFoundInDictionaryException.class, () -> {
            dictionary.contains(null);
        });
    }

    @Test
    @DisplayName("Проверка пустой строки")
    void contains_EmptyWord_ThrowsException() {
        assertThrows(WordNotFoundInDictionaryException.class, () -> {
            dictionary.contains("");
        });

        assertThrows(WordNotFoundInDictionaryException.class, () -> {
            dictionary.contains("   ");
        });
    }

    @Test
    @DisplayName("Нормализация ввода: нижний регистр и замена ё на е")
    void contains_NormalizesInput() {
        assertTrue(dictionary.contains("СТОЛ"));
        assertTrue(dictionary.contains(" СтОл "));
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
    @DisplayName("Получение списка слов")
    void getWords_ReturnsCopyOfWords() {
        List<String> words = dictionary.getWords();
        assertNotNull(words);
        assertFalse(words.isEmpty());
        assertEquals(5, words.size());

        words.add("новоеслово");
        List<String> originalWords = dictionary.getWords();
        assertFalse(originalWords.contains("новоеслово"));
    }

    @Test
    @DisplayName("Создание словаря с null списком слов")
    void constructor_NullWords_ThrowsException() {
        assertThrows(WordleSystemException.class, () -> {
            new WordleDictionary(null, testLogWriter);
        });
    }

    @Test
    @DisplayName("Создание словаря с null логгером")
    void constructor_NullLogger_ThrowsException() {
        List<String> words = Arrays.asList("слово");
        assertThrows(WordleSystemException.class, () -> {
            new WordleDictionary(words, null);
        });
    }

    @Test
    @DisplayName("Создание словаря с пустым списком слов")
    void constructor_EmptyWords_ThrowsException() {
        assertThrows(WordleSystemException.class, () -> {
            new WordleDictionary(Arrays.asList(), testLogWriter);
        });
    }
}