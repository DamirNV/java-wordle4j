package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WordleHintFilterTest {

    private WordleHintFilter filter;
    private Method updateMethod;

    @BeforeEach
    void setUp() throws Exception {
        filter = new WordleHintFilter();
        updateMethod = WordleHintFilter.class.getDeclaredMethod(
                "updateFromGuess", String.class, String.class, String.class);
        updateMethod.setAccessible(true);
    }

    private void update(String guess, String pattern, String answer) throws Exception {
        updateMethod.invoke(filter, guess, pattern, answer);
    }

    @Test
    @DisplayName("Пустой фильтр пропускает все 5-буквенные слова")
    void emptyFilter_AllowsAllFiveLetterWords() {
        assertTrue(filter.matches("ручка"));
        assertTrue(filter.matches("тесто"));
        assertTrue(filter.matches("баран"));
    }

    @Test
    @DisplayName("Присутствующие буквы ограничивают варианты")
    void presentLetters_RestrictOptions() throws Exception {
        update("ручка", "-^---", "арбуз");

        assertTrue(filter.matches("арбуз"));
        assertFalse(filter.matches("домок"));
    }

    @Test
    @DisplayName("Отсутствующие буквы ограничивают варианты")
    void absentLetters_RestrictOptions() throws Exception {
        update("ручка", "-----", "домой");

        assertTrue(filter.matches("домой"));
        assertFalse(filter.matches("ручка"));
    }

    @Test
    @DisplayName("Комбинация условий работает корректно")
    void combinationOfConditions_WorksCorrectly() throws Exception {
        update("гонец", "+^-^-", "герой");

        assertTrue(filter.matches("герой"));
        assertFalse(filter.matches("гонец"));
    }

    @Test
    @DisplayName("Сброс фильтра очищает все ограничения")
    void reset_ClearsAllConstraints() throws Exception {
        update("ручка", "+++++", "ручка");
        assertFalse(filter.matches("тесто"));

        filter.reset();

        assertTrue(filter.matches("тесто"));
        assertTrue(filter.matches("ручка"));
    }

    @Test
    @DisplayName("Null слово не проходит фильтр")
    void nullWord_DoesNotPassFilter() {
        assertFalse(filter.matches(null));
    }

    @Test
    @DisplayName("Слово неправильной длины не проходит фильтр")
    void wordWithWrongLength_DoesNotPassFilter() {
        assertFalse(filter.matches("кот"));
        assertFalse(filter.matches("длинноеслово"));
        assertFalse(filter.matches(""));
    }

    @Test
    @DisplayName("Getters возвращают защитные копии")
    void getters_ReturnCopies() throws Exception {
        update("ручка", "+^-^-", "герой");

        Set<Character> present1 = filter.getPresentLetters();
        assertThrows(UnsupportedOperationException.class, () -> present1.add('x'));

        Set<Character> absent1 = filter.getAbsentLetters();
        assertThrows(UnsupportedOperationException.class, () -> absent1.add('y'));

        Map<Character, Integer> counts1 = filter.getMinLetterCounts();
        assertThrows(UnsupportedOperationException.class, () -> counts1.put('z', 10));
    }

    @Test
    @DisplayName("Полное совпадение слова")
    void exactMatch_OnlyExactWordPasses() throws Exception {
        update("герой", "+++++", "герой");

        assertTrue(filter.matches("герой"));
        assertFalse(filter.matches("гонец"));
    }
}