package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class WordleIntegrationTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Интеграционный тест полного игрового процесса")
    void integrationTest_CompleteGameFlow() throws Exception {
        File dictFile = new File(tempDir.toFile(), "test_dict.txt");
        try (PrintWriter writer = new PrintWriter(dictFile, "UTF-8")) {
            writer.println("ручка");
            writer.println("тесто");
            writer.println("баран");
            writer.println("сарай");
            writer.println("салат");
        }

        File logFile = new File(tempDir.toFile(), "test_integration.log");

        try (PrintWriter logWriter = new PrintWriter(logFile, "UTF-8")) {
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

            for (int i = 0; i < 4; i++) {
                game.checkGuess("баран");
            }
            assertTrue(game.isGameOver());
        }

        assertTrue(logFile.exists());
        assertTrue(logFile.length() > 0);
    }

    @Test
    @DisplayName("Интеграционный тест обработки ошибок")
    void integrationTest_ErrorHandling() throws Exception {
        File logFile = new File(tempDir.toFile(), "test_error.log");

        try (PrintWriter logWriter = new PrintWriter(logFile, "UTF-8")) {
            WordleDictionaryLoader loader = new WordleDictionaryLoader(logWriter);
            assertThrows(WordleSystemException.class, () -> {
                loader.loadDictionary("nonexistent_file.txt");
            });

            File dictFile = new File(tempDir.toFile(), "small_dict.txt");
            try (PrintWriter writer = new PrintWriter(dictFile, "UTF-8")) {
                writer.println("ручка");
                writer.println("тесто");
            }

            WordleDictionary dictionary = loader.loadDictionary(dictFile.getAbsolutePath());
            WordleGame game = new WordleGame(dictionary, logWriter);

            assertThrows(WordNotFoundInDictionaryException.class, () -> {
                game.checkGuess("ложка");
            });
        }

        assertTrue(logFile.exists());
    }

    @Test
    @DisplayName("Интеграционный тест выигрышного сценария")
    void integrationTest_WinningScenario() throws Exception {
        File dictFile = new File(tempDir.toFile(), "single_word_dict.txt");
        try (PrintWriter writer = new PrintWriter(dictFile, "UTF-8")) {
            writer.println("ручка");
        }

        File logFile = new File(tempDir.toFile(), "test_win.log");

        try (PrintWriter logWriter = new PrintWriter(logFile, "UTF-8")) {
            WordleDictionaryLoader loader = new WordleDictionaryLoader(logWriter);
            WordleDictionary dictionary = loader.loadDictionary(dictFile.getAbsolutePath());
            WordleGame game = new WordleGame(dictionary, logWriter);

            String answer = game.getAnswer();
            assertEquals("ручка", answer);

            String result = game.checkGuess("ручка");
            assertEquals("+++++", result);
            assertTrue(game.isWordGuessed());
            assertTrue(game.isGameOver());
        }

        assertTrue(logFile.exists());
    }
}