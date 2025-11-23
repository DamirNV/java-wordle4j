package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class WordleGameTest {

    private WordleDictionary dictionary;
    private WordleGame game;
    private PrintWriter testLogWriter;
    private ByteArrayOutputStream logOutput;

    @BeforeEach
    void setUp() {
        logOutput = new ByteArrayOutputStream();
        testLogWriter = new PrintWriter(new OutputStreamWriter(logOutput, StandardCharsets.UTF_8), true);

        List<String> words = Arrays.asList(
                "ручка", "тесто", "баран", "сарай", "салат",
                "герой", "гонец", "травы", "оборо", "огоно",
                "тараа", "стоит", "молот", "роман", "ветер"
        );
        dictionary = new WordleDictionary(words, testLogWriter);
        game = new WordleGame(dictionary, testLogWriter);
    }

    @Test
    @DisplayName("Проверка правильного слова — все плюсы")
    void checkGuess_CorrectWord_ReturnsAllPluses() {
        WordleGame g = createGameWithFixedAnswer("ручка");
        String result = g.checkGuess("ручка");
        assertEquals("+++++", result);
        assertTrue(g.isWordGuessed());
    }

    @Test
    @DisplayName("Алгоритм сравнения: правильные позиции")
    void checkGuess_CorrectPositions() {
        WordleGame g = createGameWithFixedAnswer("тесто");
        String result = g.checkGuess("тесто");
        assertEquals("+++++", result);
    }

    @Test
    @DisplayName("Алгоритм сравнения: буквы в других позициях")
    void checkGuess_PresentLetters() {
        WordleGame g = createGameWithFixedAnswer("тесто");
        String result = g.checkGuess("стоит");
        assertEquals("^^^-^", result);
    }

    @Test
    @DisplayName("Алгоритм сравнения: отсутствующие буквы")
    void checkGuess_AbsentLetters() {
        WordleGame g = createGameWithFixedAnswer("ручка");
        String result = g.checkGuess("молот");
        assertEquals("-----", result);
    }

    @Test
    @DisplayName("Алгоритм сравнения: повторяющиеся буквы")
    void checkGuess_DuplicateLetters() {
        WordleGame g = createGameWithFixedAnswer("травы");
        String result = g.checkGuess("тараа");
        assertEquals("+^^--", result);
    }

    @Test
    @DisplayName("Алгоритм сравнения: сложный случай с повторениями")
    void checkGuess_ComplexDuplicateCase() {
        WordleGame g = createGameWithFixedAnswer("оборо");
        String result = g.checkGuess("огоно");
        assertEquals("+-+-+", result);
    }

    @Test
    @DisplayName("Пример из ТЗ: герой ← гонец")
    void checkGuess_ExampleFromRequirements() {
        WordleGame g = createGameWithFixedAnswer("герой");
        String result = g.checkGuess("гонец");
        assertEquals("+^-^-", result);
    }

    @Test
    @DisplayName("Уменьшение количества попыток после догадки")
    void checkGuess_DecreasesAttempts() {
        int before = game.getRemainingAttempts();
        game.checkGuess("ручка");
        assertEquals(before - 1, game.getRemainingAttempts());
    }

    @Test
    @DisplayName("Игра завершается после 6 попыток")
    void isGameOver_AfterSixGuesses_ReturnsTrue() {
        for (int i = 0; i < 6; i++) game.checkGuess("ручка");
        assertTrue(game.isGameOver());
    }

    @Test
    @DisplayName("Игра не завершена в начале")
    void isGameOver_Initially_ReturnsFalse() {
        assertFalse(game.isGameOver());
    }

    @Test
    @DisplayName("Угаданное слово — isWordGuessed возвращает true")
    void isWordGuessed_CorrectWord_ReturnsTrue() {
        WordleGame g = createGameWithFixedAnswer("салат");
        g.checkGuess("салат");
        assertTrue(g.isWordGuessed());
    }

    @Test
    @DisplayName("Неправильное слово — isWordGuessed возвращает false")
    void isWordGuessed_WrongWord_ReturnsFalse() {
        game.checkGuess("ручка");
        assertFalse(game.isWordGuessed());
    }

    @Test
    @DisplayName("Генерация подсказки без предыдущих догадок")
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
    @DisplayName("Генерация подсказки после угадывания слова")
    void generateHint_AfterWordGuessed_ReturnsAnyWord() {
        WordleGame g = createGameWithFixedAnswer("баран");
        g.checkGuess("баран");
        String hint = g.generateHint();
        assertNotNull(hint);
        assertEquals(5, hint.length());
    }

    @Test
    @DisplayName("Генерация подсказки когда все слова использованы")
    void generateHint_AllWordsUsed_ReturnsAnyWord() {
        // Использовать только часть слов, чтобы не превысить лимит попыток
        List<String> words = dictionary.getWords();
        int maxAttempts = Math.min(words.size(), 6); // Не больше 6 попыток

        for (int i = 0; i < maxAttempts; i++) {
            game.checkGuess(words.get(i));
        }

        String hint = game.generateHint();
        assertNotNull(hint);
        assertEquals(5, hint.length());
    }

    @Test
    @DisplayName("checkGuess с null — бросает исключение")
    void checkGuess_NullGuess_ThrowsException() {
        assertThrows(WordNotFoundInDictionaryException.class, () -> game.checkGuess(null));
    }

    @Test
    @DisplayName("checkGuess после окончания игры — бросает исключение")
    void checkGuess_AfterGameOver_ThrowsException() {
        for (int i = 0; i < 6; i++) game.checkGuess("ручка");
        assertThrows(WordleGameException.class, () -> game.checkGuess("тесто"));
    }

    @Test
    @DisplayName("Нормализация ввода в checkGuess")
    void checkGuess_NormalizesInput() {
        String result = game.checkGuess(" РУЧКА ");
        assertNotNull(result);
        assertEquals(5, result.length());
    }

    @Test
    @DisplayName("getUsedAttempts возвращает правильное количество")
    void getUsedAttempts_ReturnsCorrectNumber() {
        assertEquals(0, game.getUsedAttempts());
        game.checkGuess("ручка");
        assertEquals(1, game.getUsedAttempts());
        game.checkGuess("тесто");
        assertEquals(2, game.getUsedAttempts());
    }

    @Test
    @DisplayName("Конструктор с null-словарём бросает исключение")
    void constructor_NullDictionary_ThrowsException() {
        assertThrows(WordleSystemException.class, () -> new WordleGame(null, testLogWriter));
    }

    @Test
    @DisplayName("Конструктор с null-логгером бросает исключение")
    void constructor_NullLogger_ThrowsException() {
        assertThrows(WordleSystemException.class, () -> new WordleGame(dictionary, null));
    }

    @Test
    @DisplayName("getPreviousGuesses возвращает список предыдущих догадок")
    void getPreviousGuesses_ReturnsGuesses() {
        game.checkGuess("ручка");
        game.checkGuess("тесто");
        Set<String> guesses = game.getPreviousGuesses();
        assertEquals(2, guesses.size());
        assertTrue(guesses.contains("ручка"));
        assertTrue(guesses.contains("тесто"));
    }

    @Test
    @DisplayName("Логгирование игровых событий")
    void operations_AreLogged() {
        game.checkGuess("ручка");
        testLogWriter.flush();
        String log = logOutput.toString(StandardCharsets.UTF_8);
        assertTrue(log.contains("Проверка слова"));
        assertTrue(log.contains("ручка"));
    }

    @Test
    @DisplayName("getAnswer возвращает загаданное слово")
    void getAnswer_ReturnsAnswer() {
        String answer = game.getAnswer();
        assertNotNull(answer);
        assertEquals(5, answer.length());
        assertTrue(dictionary.contains(answer));
    }

    private WordleGame createGameWithFixedAnswer(String answer) {
        WordleGame g = new WordleGame(dictionary, testLogWriter);
        setAnswerViaReflection(g, answer);
        return g;
    }

    private void setAnswerViaReflection(WordleGame game, String answer) {
        try {
            Field field = WordleGame.class.getDeclaredField("answer");
            field.setAccessible(true);
            field.set(game, answer);
        } catch (Exception e) {
            fail("Не удалось установить answer через рефлексию: " + e.getMessage());
        }
    }
}