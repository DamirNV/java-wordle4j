package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WordleIntegrationTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Интеграционный тест полного игрового процесса")
    void integrationTest_CompleteGameFlow() throws Exception {
        File dictFile = tempDir.resolve("test_dict.txt").toFile();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(dictFile), "UTF-8"))) {
            writer.println("ручка");
            writer.println("тесто");
            writer.println("баран");
            writer.println("сарай");
            writer.println("салат");
        }

        File logFile = tempDir.resolve("test_integration.log").toFile();

        try (PrintWriter logWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(logFile), "UTF-8"))) {
            WordleDictionaryLoader loader = new WordleDictionaryLoader(logWriter);
            WordleDictionary dictionary = loader.loadDictionary(dictFile.getAbsolutePath());
            WordleGame game = new WordleGame(dictionary, logWriter);

            assertFalse(game.isGameOver());
            assertEquals(6, game.getRemainingAttempts());
            assertEquals(0, game.getUsedAttempts());

            String result1 = game.checkGuess("ручка");
            assertNotNull(result1);
            assertEquals(5, game.getRemainingAttempts());
            assertEquals(1, game.getUsedAttempts());

            String result2 = game.checkGuess("тесто");
            assertNotNull(result2);
            assertEquals(4, game.getRemainingAttempts());
            assertEquals(2, game.getUsedAttempts());

            String hint = game.generateHint();
            assertNotNull(hint);
            assertEquals(5, hint.length());
            assertTrue(dictionary.contains(hint));
            assertNotEquals("ручка", hint);
            assertNotEquals("тесто", hint);

            for (int i = 0; i < 4; i++) {
                game.checkGuess("баран");
            }
            assertTrue(game.isGameOver());
        }

        assertTrue(logFile.exists());
        assertTrue(logFile.length() > 0);
    }

    @Test
    @DisplayName("Интеграционный тест выигрышного сценария")
    void integrationTest_WinningScenario() throws Exception {
        File dictFile = tempDir.resolve("single_word_dict.txt").toFile();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(dictFile), "UTF-8"))) {
            writer.println("ручка");
        }

        File logFile = tempDir.resolve("test_win.log").toFile();

        try (PrintWriter logWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(logFile), "UTF-8"))) {
            WordleDictionaryLoader loader = new WordleDictionaryLoader(logWriter);
            WordleDictionary dictionary = loader.loadDictionary(dictFile.getAbsolutePath());
            WordleGame game = new WordleGame(dictionary, logWriter);
            setFixedAnswer(game, "ручка");

            String result = game.checkGuess("ручка");
            assertEquals("+++++", result);
            assertTrue(game.isWordGuessed());
            assertTrue(game.isGameOver());
            assertEquals(5, game.getRemainingAttempts());
        }

        assertTrue(logFile.exists());
        assertTrue(logFile.length() > 0);
    }

    @Test
    @DisplayName("Интеграционный тест с подсказками")
    void integrationTest_WithHints() throws Exception {
        File dictFile = tempDir.resolve("hint_dict.txt").toFile();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(dictFile), "UTF-8"))) {
            writer.println("ручка");
            writer.println("тесто");
            writer.println("баран");
        }

        File logFile = tempDir.resolve("test_hints.log").toFile();

        try (PrintWriter logWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(logFile), "UTF-8"))) {
            WordleDictionaryLoader loader = new WordleDictionaryLoader(logWriter);
            WordleDictionary dictionary = loader.loadDictionary(dictFile.getAbsolutePath());
            WordleGame game = new WordleGame(dictionary, logWriter);

            String hint1 = game.generateHint();
            assertNotNull(hint1);
            assertTrue(dictionary.contains(hint1));

            game.checkGuess("ручка");

            String hint2 = game.generateHint();
            assertNotNull(hint2);
            assertTrue(dictionary.contains(hint2));
            assertNotEquals("ручка", hint2);
        }

        assertTrue(logFile.exists());
        assertTrue(logFile.length() > 0);
    }

    @Test
    @DisplayName("Интеграционный тест алгоритма сравнения")
    void integrationTest_ComparisonAlgorithm() throws Exception {
        File dictFile = tempDir.resolve("algo_dict.txt").toFile();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(dictFile), "UTF-8"))) {
            writer.println("герой");
            writer.println("гонец");
            writer.println("травы");
            writer.println("тараа");
        }

        File logFile = tempDir.resolve("test_algo.log").toFile();

        try (PrintWriter logWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(logFile), "UTF-8"))) {
            WordleDictionaryLoader loader = new WordleDictionaryLoader(logWriter);
            loader.loadDictionary(dictFile.getAbsolutePath());

            WordleGame game1 = createGameWithWordsAndAnswer(List.of("герой", "гонец"), "герой", logWriter);
            String result1 = game1.checkGuess("гонец");
            assertEquals("+^-^-", result1);

            WordleGame game2 = createGameWithWordsAndAnswer(List.of("травы", "тараа"), "травы", logWriter);
            String result2 = game2.checkGuess("тараа");
            assertEquals("+^^--", result2);
        }

        assertTrue(logFile.exists());
        assertTrue(logFile.length() > 0);
    }

    @Test
    @DisplayName("Интеграционный тест обработки ошибок")
    void integrationTest_ErrorHandling() throws Exception {
        File dictFile = tempDir.resolve("error_dict.txt").toFile();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(dictFile), "UTF-8"))) {
            writer.println("ручка");
            writer.println("тесто");
        }

        File logFile = tempDir.resolve("test_errors.log").toFile();

        try (PrintWriter logWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(logFile), "UTF-8"))) {
            WordleDictionaryLoader loader = new WordleDictionaryLoader(logWriter);
            WordleDictionary dictionary = loader.loadDictionary(dictFile.getAbsolutePath());
            WordleGame game = new WordleGame(dictionary, logWriter);

            assertThrows(WordNotFoundInDictionaryException.class, () -> game.checkGuess("несуществующееслово"));

            for (int i = 0; i < 6; i++) {
                game.checkGuess("ручка");
            }

            assertThrows(WordleGameException.class, () -> game.checkGuess("тесто"));
        }

        assertTrue(logFile.exists());
        assertTrue(logFile.length() > 0);
    }

    @Test
    @DisplayName("Интеграционный тест нормализации")
    void integrationTest_Normalization() throws Exception {
        File dictFile = tempDir.resolve("norm_dict.txt").toFile();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(dictFile), "UTF-8"))) {
            writer.println("чёлка");
            writer.println("Ёршик");
            writer.println("АбБаТ");
        }

        File logFile = tempDir.resolve("test_norm.log").toFile();

        try (PrintWriter logWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(logFile), "UTF-8"))) {
            WordleDictionaryLoader loader = new WordleDictionaryLoader(logWriter);
            WordleDictionary dictionary = loader.loadDictionary(dictFile.getAbsolutePath());
            WordleGame game = new WordleGame(dictionary, logWriter);

            assertTrue(dictionary.contains("челка"));
            assertTrue(dictionary.contains("ершик"));
            assertTrue(dictionary.contains("аббат"));

            String result1 = game.checkGuess(" ЧЁЛКА ");
            assertNotNull(result1);

            String result2 = game.checkGuess("ЁРШИК");
            assertNotNull(result2);
        }

        assertTrue(logFile.exists());
        assertTrue(logFile.length() > 0);
    }

    private WordleGame createGameWithWordsAndAnswer(List<String> words, String answer, PrintWriter logWriter) {
        WordleDictionary dict = new WordleDictionary(words, logWriter);
        WordleGame game = new WordleGame(dict, logWriter);
        setFixedAnswer(game, answer);
        return game;
    }

    private void setFixedAnswer(WordleGame game, String answer) {
        try {
            Field field = WordleGame.class.getDeclaredField("answer");
            field.setAccessible(true);
            field.set(game, answer);
        } catch (Exception e) {
            fail("Не удалось установить answer через рефлексию: " + e.getMessage());
        }
    }
}