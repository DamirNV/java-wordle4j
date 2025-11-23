package ru.yandex.practicum;

import java.util.*;

public class WordleHintFilter {
    private final char[] correct = new char[5];
    private final Set<Character> present = new HashSet<>();
    private final Set<Character> absent = new HashSet<>();
    private final Map<Character, Integer> minCount = new HashMap<>();

    public WordleHintFilter() {
        Arrays.fill(correct, '_');
    }

    public void updateFromGuess(String guess, String pattern, String answer) {
        // Валидация входных данных
        if (!isValidInput(guess, pattern, answer)) {
            return;
        }

        guess = Wordle.normalizeWord(guess);
        answer = Wordle.normalizeWord(answer);

        Map<Character, Integer> answerFreq = getCharacterFrequency(answer);
        Map<Character, Integer> confirmed = new HashMap<>();

        processGreenPositions(guess, pattern, confirmed);
        processYellowPositions(guess, pattern, answerFreq, confirmed);
        updateMinCounts(confirmed);
        processGrayPositions(guess, pattern, answerFreq, confirmed);
    }

    private boolean isValidInput(String guess, String pattern, String answer) {
        return guess != null && pattern != null && answer != null &&
                guess.length() == 5 && pattern.length() == 5 && answer.length() == 5 &&
                isValidPattern(pattern);
    }

    private boolean isValidPattern(String pattern) {
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (c != '+' && c != '^' && c != '-') {
                return false;
            }
        }
        return true;
    }

    private Map<Character, Integer> getCharacterFrequency(String word) {
        Map<Character, Integer> freq = new HashMap<>();
        for (char c : word.toCharArray()) {
            freq.merge(c, 1, Integer::sum);
        }
        return freq;
    }

    private void processGreenPositions(String guess, String pattern,
                                       Map<Character, Integer> confirmed) {
        for (int i = 0; i < 5; i++) {
            if (pattern.charAt(i) == '+') {
                char ch = guess.charAt(i);
                correct[i] = ch;
                confirmed.merge(ch, 1, Integer::sum);
            }
        }
    }

    private void processYellowPositions(String guess, String pattern,
                                        Map<Character, Integer> answerFreq,
                                        Map<Character, Integer> confirmed) {
        for (int i = 0; i < 5; i++) {
            if (pattern.charAt(i) == '^') {
                char ch = guess.charAt(i);
                int currentConfirmed = confirmed.getOrDefault(ch, 0);
                int requiredInAnswer = answerFreq.getOrDefault(ch, 0);

                if (currentConfirmed < requiredInAnswer) {
                    present.add(ch);
                    confirmed.merge(ch, 1, Integer::sum);
                }
            }
        }
    }

    private void updateMinCounts(Map<Character, Integer> confirmed) {
        for (Map.Entry<Character, Integer> entry : confirmed.entrySet()) {
            minCount.merge(entry.getKey(), entry.getValue(), Math::max);
        }
    }

    private void processGrayPositions(String guess, String pattern,
                                      Map<Character, Integer> answerFreq,
                                      Map<Character, Integer> confirmed) {
        for (int i = 0; i < 5; i++) {
            if (pattern.charAt(i) == '-') {
                char ch = guess.charAt(i);
                int currentConfirmed = confirmed.getOrDefault(ch, 0);
                int requiredInAnswer = answerFreq.getOrDefault(ch, 0);

                if (currentConfirmed >= requiredInAnswer) {
                    absent.add(ch);
                }
            }
        }
    }

    public boolean matches(String word) {
        if (word == null || word.length() != 5) {
            return false;
        }

        word = Wordle.normalizeWord(word);

        return checkCorrectPositions(word) &&
                checkAbsentLetters(word) &&
                checkPresentLetters(word) &&
                checkMinCounts(word);
    }

    private boolean checkCorrectPositions(String word) {
        for (int i = 0; i < 5; i++) {
            if (correct[i] != '_' && correct[i] != word.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkAbsentLetters(String word) {
        for (char c : absent) {
            if (word.indexOf(c) != -1) {
                return false;
            }
        }
        return true;
    }

    private boolean checkPresentLetters(String word) {
        for (char c : present) {
            if (word.indexOf(c) == -1) {
                return false;
            }
        }
        return true;
    }

    private boolean checkMinCounts(String word) {
        Map<Character, Integer> count = getCharacterFrequency(word);
        for (Map.Entry<Character, Integer> entry : minCount.entrySet()) {
            if (count.getOrDefault(entry.getKey(), 0) < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    public void reset() {
        Arrays.fill(correct, '_');
        present.clear();
        absent.clear();
        minCount.clear();
    }

    public Set<Character> getPresentLetters() {
        return Collections.unmodifiableSet(present);
    }

    public Set<Character> getAbsentLetters() {
        return Collections.unmodifiableSet(absent);
    }

    public Map<Character, Integer> getMinLetterCounts() {
        return Collections.unmodifiableMap(minCount);
    }

    public char[] getCorrectPositions() {
        return correct.clone(); // возвращаем копию для безопасности
    }

    public String getCorrectPositionsString() {
        List<String> parts = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            if (correct[i] != '_') {
                parts.add("поз." + (i + 1) + "=" + correct[i]);
            }
        }
        return parts.isEmpty() ? "нет" : String.join(", ", parts);
    }

    @Override
    public String toString() {
        return String.format(
                "WordleHintFilter{correct=%s, present=%s, absent=%s, minCount=%s}",
                Arrays.toString(correct), present, absent, minCount
        );
    }
}