package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class WordleHintFilterTest {

    @Test
    @DisplayName("После '+++++' только это слово проходит")
    void matches_ExactMatch() {
        WordleHintFilter filter = new WordleHintFilter();
        filter.updateFromGuess("герой", "+++++", "герой");
        assertTrue(filter.matches("герой"));
        assertFalse(filter.matches("гонец"));
    }

    @Test
    @DisplayName("После '-----' ни одна буква из догадки не должна быть в слове")
    void matches_AllAbsent() {
        WordleHintFilter filter = new WordleHintFilter();
        filter.updateFromGuess("котик", "-----", "герой");
        assertFalse(filter.matches("котик"));
        assertFalse(filter.matches("комар")); // содержит 'к', 'о'
        assertFalse(filter.matches("тракт")); // содержит 'к'
        assertTrue(filter.matches("герой"));
    }

    @Test
    @DisplayName("Частичное совпадение: только подтверждённые буквы разрешены")
    void matches_PartialMatch() {
        WordleHintFilter filter = new WordleHintFilter();
        filter.updateFromGuess("герой", "+----", "герой");
        assertTrue(filter.matches("герой"));
        assertTrue(filter.matches("глина")); // начинается с 'г'
        assertFalse(filter.matches("агерй")); // 'г' не на первой позиции
    }

    @Test
    @DisplayName("Буквы не в позиции: должны присутствовать, но не на своих местах")
    void matches_Misplaced() {
        WordleHintFilter filter = new WordleHintFilter();
        filter.updateFromGuess("гонец", "+^-^-", "герой");
        assertTrue(filter.matches("герой")); // г+ е^ р- о^ й-
        assertFalse(filter.matches("голец")); // нет 'е'
        assertFalse(filter.matches("герий")); // 'е' на второй позиции, а должна быть не на второй
    }

    @Test
    @DisplayName("Несколько попыток: накопление ограничений")
    void matches_MultipleGuesses() {
        WordleHintFilter filter = new WordleHintFilter();
        filter.updateFromGuess("котик", "-----", "герой");
        filter.updateFromGuess("герой", "+----", "герой");

        // Должны проходить слова, начинающиеся с 'г' и не содержащие букв из "котик"
        assertTrue(filter.matches("глина")); // начинается с 'г', нет букв из "котик"
        assertFalse(filter.matches("герой")); // содержит 'е', 'р', 'о', 'й' которые еще не подтверждены
        assertFalse(filter.matches("гроза")); // содержит 'о' из "котик"
    }

    @Test
    @DisplayName("Повторяющиеся буквы: правильный подсчет минимального количества")
    void matches_RepeatedLetters() {
        WordleHintFilter filter = new WordleHintFilter();
        // В слове "сарай" две буквы 'а'
        filter.updateFromGuess("салат", "^+--+", "сарай");

        // Должны проходить слова с минимум двумя 'а'
        assertTrue(filter.matches("сарай")); // две 'а'
        assertTrue(filter.matches("баран")); // две 'а'
        assertFalse(filter.matches("салон")); // только одна 'а'
    }

    @Test
    @DisplayName("Неверная длина — false")
    void matches_WrongLength() {
        WordleHintFilter filter = new WordleHintFilter();
        filter.updateFromGuess("абвгд", "+++++", "абвгд");
        assertFalse(filter.matches("абвг"));
        assertFalse(filter.matches("абвгде"));
        assertFalse(filter.matches(null));
    }

    @Test
    @DisplayName("Геттеры не возвращают null")
    void getStateNotNull() {
        WordleHintFilter filter = new WordleHintFilter();
        filter.updateFromGuess("тест", "----+", "тесто");
        assertNotNull(filter.getCorrectPositionsString());
        assertNotNull(filter.getPresentLetters());
        assertNotNull(filter.getAbsentLetters());
        assertNotNull(filter.getMinLetterCounts());
    }

    @Test
    @DisplayName("Буква присутствует и отсутствует одновременно - приоритет присутствию")
    void matches_PresentOverAbsent() {
        WordleHintFilter filter = new WordleHintFilter();
        // Сначала буква 'а' отсутствует, потом присутствует
        filter.updateFromGuess("котик", "-----", "сарай"); // 'а' нет в слове
        filter.updateFromGuess("салат", "^+--+", "сарай"); // 'а' есть в слове

        // Буква 'а' должна считаться присутствующей, несмотря на первое предположение
        assertTrue(filter.matches("сарай"));
        assertFalse(filter.matches("котик")); // содержит буквы из первого предположения
    }

    @Test
    @DisplayName("Null параметры в updateFromGuess не ломают фильтр")
    void updateFromGuess_NullParameters() {
        WordleHintFilter filter = new WordleHintFilter();

        // Не должно бросать исключение
        filter.updateFromGuess(null, null, null);
        filter.updateFromGuess("тест", null, "слово");
        filter.updateFromGuess(null, "++++", "слово");

        // Фильтр должен оставаться в рабочем состоянии
        assertTrue(filter.matches("тесто"));
        assertTrue(filter.matches("слово"));
    }
}