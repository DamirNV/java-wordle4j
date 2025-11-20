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
            writer.println("стол");
            writer.println("стул");
            writer.println("ручка");
            writer.println("бумага");
            writer.println("тесто");
        }

        File logFile = new File(tempDir.toFile(), "test_integration.log");

        WordleDictionaryLoader loader = new WordleDictionaryLoader(new PrintWriter(logFile));
        WordleDictionary dictionary = loader.loadDictionary(dictFile.getAbsolutePath());
        WordleGame game = new WordleGame(dictionary, new PrintWriter(logFile));

        assertFalse(game.isGameOver());
        assertEquals(6, game.getSteps());
        assertEquals(0, game.getPreviousGuessesCount());

        String result1 = game.checkGuess("стол");
        assertNotNull(result1);
        assertEquals(5, game.getSteps());
        assertEquals(1, game.getPreviousGuessesCount());

        String result2 = game.checkGuess("ручка");
        assertNotNull(result2);
        assertEquals(4, game.getSteps());
        assertEquals(2, game.getPreviousGuessesCount());

        String hint = game.generateHint();
        assertNotNull(hint);
        assertEquals(5, hint.length());
        assertTrue(dictionary.contains(hint));

        for (int i = 0; i < 4; i++) {
            game.checkGuess("тесто");
        }
        assertTrue(game.isGameOver());

        assertTrue(logFile.exists());
        assertTrue(logFile.length() > 0);
    }

    @Test
    @DisplayName("Интеграционный тест обработки ошибок")
    void integrationTest_ErrorHandling() throws Exception {
        File logFile = new File(tempDir.toFile(), "test_error.log");
        PrintWriter logWriter = new PrintWriter(logFile, "UTF-8");

        WordleDictionaryLoader loader = new WordleDictionaryLoader(logWriter);
        assertThrows(WordleSystemException.class, () -> {
            loader.loadDictionary("nonexistent_file.txt");
        });

        File dictFile = new File(tempDir.toFile(), "small_dict.txt");
        try (PrintWriter writer = new PrintWriter(dictFile, "UTF-8")) {
            writer.println("стол");
            writer.println("ручка");
        }

        WordleDictionary dictionary = loader.loadDictionary(dictFile.getAbsolutePath());
        WordleGame game = new WordleGame(dictionary, logWriter);

        assertThrows(WordNotFoundInDictionaryException.class, () -> {
            game.checkGuess("несуществующее");
        });

        logWriter.close();

        assertTrue(logFile.exists());
        String logContent = new String(java.nio.file.Files.readAllBytes(logFile.toPath()));
        assertTrue(logContent.contains("Ошибка") || logContent.contains("ERROR"));
    }

    @Test
    @DisplayName("Интеграционный тест выигрышного сценария")
    void integrationTest_WinningScenario() throws Exception {
        File dictFile = new File(tempDir.toFile(), "single_word_dict.txt");
        try (PrintWriter writer = new PrintWriter(dictFile, "UTF-8")) {
            writer.println("победа");
        }

        File logFile = new File(tempDir.toFile(), "test_win.log");
        WordleDictionaryLoader loader = new WordleDictionaryLoader(new PrintWriter(logFile));
        WordleDictionary dictionary = loader.loadDictionary(dictFile.getAbsolutePath());
        WordleGame game = new WordleGame(dictionary, new PrintWriter(logFile));

        String answer = game.getAnswer();
        assertEquals("победа", answer);

        String result = game.checkGuess("победа");
        assertEquals("+++++", result);
        assertTrue(game.isWordGuessed("победа"));
        assertFalse(game.isGameOver());
    }
}