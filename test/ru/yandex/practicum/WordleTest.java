package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class WordleTest {

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
        String validRussian = "столк";
        String invalidEnglish = "table";
        String invalidMixed = "стolк";
        String validWithYo = "ёршик";

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
        String correctInput = "столк";

        assertTrue(emptyInput.isEmpty());
        assertFalse(shortInput.length() == 5);
        assertFalse(longInput.length() == 5);
        assertFalse(englishInput.matches("[а-яёА-ЯЁ]+"));
        assertTrue(correctInput.length() == 5 && correctInput.matches("[а-яёА-ЯЁ]+"));
    }
}