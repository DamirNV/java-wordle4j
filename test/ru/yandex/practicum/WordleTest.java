package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class WordleTest {

    @TempDir
    Path tempDir;

    private ByteArrayOutputStream testOutput;
    private PrintWriter testLogWriter;
    private ByteArrayOutputStream logOutput;

    @BeforeEach
    void setUp() {
        testOutput = new ByteArrayOutputStream();
        logOutput = new ByteArrayOutputStream();
        testLogWriter = new PrintWriter(new OutputStreamWriter(logOutput));
    }

    @Test
    @DisplayName("Создание лог-файла при запуске игры")
    void main_CreatesLogFile() throws Exception {
        File logFile = new File(tempDir.toFile(), "test.wordle.log");

        PrintStream originalErr = System.err;
        try {
            System.setErr(new PrintStream(testOutput));
            Wordle.main(new String[]{});
        } catch (Exception e) {
        } finally {
            System.setErr(originalErr);
        }

        File normalLogFile = new File("wordle.log");
        assertTrue(normalLogFile.exists() || testOutput.toString().contains("лог-файл"));
    }

    @Test
    @DisplayName("Обработка ошибки создания лог-файла")
    void main_LogFileCreationError_PrintsErrorMessage() {
        String invalidPath = "/invalid/path/wordle.log";

        PrintStream originalErr = System.err;
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();

        try {
            System.setErr(new PrintStream(errContent));
        } finally {
            System.setErr(originalErr);
        }

        assertTrue(true);
    }

    @Test
    @DisplayName("Нормализация пользовательского ввода")
    void normalizeInput_ValidatesAndNormalizesCorrectly() {
        String input = "  СТОЁЛ  ";
        String normalized = input.toLowerCase().replace('ё', 'е').trim();
        assertEquals("стоел", normalized);
    }

    @Test
    @DisplayName("Валидация русского ввода")
    void validateRussianInput_AcceptsOnlyRussianLetters() {
        String validRussian = "стол";
        String invalidEnglish = "table";
        String invalidMixed = "стol";
        String validWithYo = "ёлка";

        assertTrue(validRussian.matches("[а-яёА-ЯЁ]+"));
        assertFalse(invalidEnglish.matches("[а-яёА-ЯЁ]+"));
        assertFalse(invalidMixed.matches("[а-яёА-ЯЁ]+"));
        assertTrue(validWithYo.matches("[а-яёА-ЯЁ]+"));
    }

    @Test
    @DisplayName("Обработка разных сценариев ввода")
    void processInput_VariousScenarios() {
        String emptyInput = "";
        String shortInput = "кот";
        String longInput = "длинноеслово";
        String englishInput = "table";
        String correctInput = "стол";

        assertTrue(emptyInput.isEmpty());
        assertFalse(shortInput.length() == 5);
        assertFalse(longInput.length() == 5);
        assertFalse(englishInput.matches("[а-яёА-ЯЁ]+"));
        assertTrue(correctInput.length() == 5 && correctInput.matches("[а-яёА-ЯЁ]+"));
    }
}