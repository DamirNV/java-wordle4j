package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WordleGameTest {
    private WordleGame game;
    private WordleDictionary dictionary;
    private PrintWriter testLogWriter;
    private ByteArrayOutputStream logOutput;

    @BeforeEach
    void setUp() {
        logOutput = new ByteArrayOutputStream();
        testLogWriter = new PrintWriter(new OutputStreamWriter(logOutput));

        List<String> testWords = Arrays.asList("ручка", "тесто", "баран", "сарай", "салат", "герой", "гонец");
        dictionary = new WordleDictionary(testWords, testLogWriter);
        game = new WordleGame(dictionary, testLogWriter);
    }

    @Test
    @DisplayName("Проверка правильного слова")
    void checkGuess_CorrectWord_ReturnsAllPluses() {
        String answer = game.getAnswer();
        String result = game.checkGuess(answer);

        assertEquals("+++++", result);
        assertTrue(game.isWordGuessed());
    }

    @Test
    @DisplayName("Проверка слова с правильными и неправильными буквами")
    void checkGuess_MixedLetters_ReturnsCorrectPattern() {
        List<String> words = Arrays.asList("герой");
        WordleDictionary testDict = new WordleDictionary(words, testLogWriter);
        WordleGame testGame = new WordleGame(testDict, testLogWriter);

        String result = testGame.checkGuess("гонец");

        assertEquals("+^-^-", result);
    }

    @Test
    @DisplayName("Уменьшение количества попыток после догадки")
    void checkGuess_DecreasesAttempts() {
        int initialAttempts = game.getRemainingAttempts();
        game.checkGuess("ручка");
        int attemptsAfterGuess = game.getRemainingAttempts();

        assertEquals(initialAttempts - 1, attemptsAfterGuess);
    }

    @Test
    @DisplayName("Игра завершается при исчерпании попыток")
    void isGameOver_AfterSixGuesses_ReturnsTrue() {
        for (int i = 0; i < 6; i++) {
            game.checkGuess("ручка");
        }

        assertTrue(game.isGameOver());
    }

    @Test
    @DisplayName("Игра не завершена в начале")
    void isGameOver_Initially_ReturnsFalse() {
        assertFalse(game.isGameOver());
    }

    @Test
    @DisplayName("Проверка угаданного слова")
    void isWordGuessed_CorrectWord_ReturnsTrue() {
        String answer = game.getAnswer();
        game.checkGuess(answer);

        assertTrue(game.isWordGuessed());
    }

    @Test
    @DisplayName("Проверка неугаданного слова")
    void isWordGuessed_WrongWord_ReturnsFalse() {
        game.checkGuess("ручка");

        assertFalse(game.isWordGuessed());
    }

    @Test
    @DisplayName("Генерация подсказки при отсутствии попыток")
    void generateHint_NoPreviousGuesses_ReturnsRandomWord() {
        String hint = game.generateHint();

        assertNotNull(hint);
        assertEquals(5, hint.length());
        assertTrue(dictionary.contains(hint));
    }

    @Test
    @DisplayName("Генерация подсказки после нескольких попыток")
    void generateHint_WithPreviousGuesses_ReturnsFilteredWord() {
        game.checkGuess("ручка");
        game.checkGuess("тесто");

        String hint = game.generateHint();

        assertNotNull(hint);
        assertEquals(5, hint.length());
        assertTrue(dictionary.contains(hint));
        assertNotEquals("ручка", hint);
        assertNotEquals("тесто", hint);
    }

    @Test
    @DisplayName("Выбрасывает исключение при null догадке")
    void checkGuess_NullGuess_ThrowsException() {
        assertThrows(WordNotFoundInDictionaryException.class, () -> {
            game.checkGuess(null);
        });
    }

    @Test
    @DisplayName("Выбрасывает исключение при игре после завершения")
    void checkGuess_AfterGameOver_ThrowsException() {
        for (int i = 0; i < 6; i++) {
            game.checkGuess("ручка");
        }

        assertThrows(WordleGameException.class, () -> {
            game.checkGuess("тесто");
        });
    }

    @Test
    @DisplayName("Нормализация ввода в checkGuess")
    void checkGuess_NormalizesInput() {
        String result = game.checkGuess(" РУЧКА ");

        assertNotNull(result);
        assertEquals(5, result.length());
    }

    @Test
    @DisplayName("Получение количества использованных попыток")
    void getUsedAttempts_ReturnsCorrectNumber() {
        assertEquals(0, game.getUsedAttempts());

        game.checkGuess("ручка");
        assertEquals(1, game.getUsedAttempts());

        game.checkGuess("тесто");
        assertEquals(2, game.getUsedAttempts());
    }

    @Test
    @DisplayName("Создание игры с null словарем")
    void constructor_NullDictionary_ThrowsException() {
        assertThrows(WordleSystemException.class, () -> {
            new WordleGame(null, testLogWriter);
        });
    }

    @Test
    @DisplayName("Создание игры с null логгером")
    void constructor_NullLogger_ThrowsException() {
        assertThrows(WordleSystemException.class, () -> {
            new WordleGame(dictionary, null);
        });
    }

    @Test
    @DisplayName("Получение предыдущих догадок")
    void getPreviousGuesses_ReturnsGuesses() {
        game.checkGuess("ручка");
        game.checkGuess("тесто");

        List<String> guesses = game.getPreviousGuesses();

        assertEquals(2, guesses.size());
        assertTrue(guesses.contains("ручка"));
        assertTrue(guesses.contains("тесто"));
    }
}