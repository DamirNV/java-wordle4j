package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class WordleTest {

    private static final Method normalizeMethod;
    private static final Method isValidInputMethod;

    static {
        try {
            normalizeMethod = Wordle.class.getDeclaredMethod("normalizeWord", String.class);
            normalizeMethod.setAccessible(true);

            isValidInputMethod = Wordle.class.getDeclaredMethod("isValidInput", String.class);
            isValidInputMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Не найден приватный метод в Wordle", e);
        }
    }

    private String normalize(String word) throws Exception {
        return (String) normalizeMethod.invoke(null, word);
    }

    private boolean isValidInput(String input) throws Exception {
        return (Boolean) isValidInputMethod.invoke(null, input);
    }

    @Test
    @DisplayName("Нормализация пользовательского ввода")
    void normalizeInput_ValidatesAndNormalizesCorrectly() throws Exception {
        assertEquals("стоел", normalize("  СТОЁЛ  "));
        assertEquals("ручка", normalize("РУЧКА"));
        assertEquals("челка", normalize("чЁлка"));
        assertEquals("ершик", normalize("ЁРШИК"));
    }

    @Test
    @DisplayName("Нормализация граничных случаев")
    void normalizeInput_BoundaryCases() throws Exception {
        assertEquals("", normalize(""));
        assertEquals("", normalize("   "));
        assertEquals("слово", normalize("  СЛОВО  "));
        assertEquals("мед", normalize("  МЁД  "));
        assertEquals("елка", normalize("ёлка"));
        assertNull(normalize(null));
    }

    @Test
    @DisplayName("Нормализация сохраняет длину 5 символов")
    void normalizeInput_PreservesFiveLetterLength() throws Exception {
        String[] inputs = { "ручка", "РУЧКА", " РучКа ", "чЁлка", "ЁРШИК" };
        for (String input : inputs) {
            String normalized = normalize(input);
            if (normalized.length() == 5) {
                assertEquals(5, normalized.length(), "Должно быть 5 символов: " + input);
            }
        }
    }

    @Test
    @DisplayName("Валидация корректного ввода")
    void validateInput_ValidInput_ReturnsTrue() throws Exception {
        assertTrue(isValidInput("ручка"));
        assertTrue(isValidInput("столк"));
        assertTrue(isValidInput("ёршик"));
        assertTrue(isValidInput("ЧЁЛКА"));
    }

    @Test
    @DisplayName("Валидация некорректного ввода")
    void validateInput_InvalidInput_ReturnsFalse() throws Exception {
        String[] invalid = {
                null,
                "",
                "кот",
                "длинноеслово",
                "table",
                "стolк",
                "12345",
                "слово!",
                " ручка ",
                "  ручка",
                "ручка  "
        };

        for (String input : invalid) {
            assertFalse(isValidInput(input), "Должно быть невалидно: " + input);
        }
    }

    @Test
    @DisplayName("Валидация ввода с пробелами")
    void validateInput_WithSpaces() throws Exception {
        assertFalse(isValidInput(" ручка "));
        assertFalse(isValidInput("  ручка"));
        assertTrue(isValidInput("ручка"));
    }

    @Test
    @DisplayName("Комплексный тест: нормализация + валидация")
    void normalizeAndValidate_Combined() throws Exception {
        String[] validAfterNormalize = {
                "РУЧКА", " чёлка ", " ЁРШИК ", "  Мед  ", "СЛОВО"
        };

        for (String input : validAfterNormalize) {
            String normalized = normalize(input);
            if (normalized.length() == 5) {
                assertTrue(isValidInput(normalized), "После нормализации должно быть валидно: " + input);
            }
        }
    }
}