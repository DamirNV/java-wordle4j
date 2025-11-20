package ru.yandex.practicum;

import java.util.*;

public class WordleHintFilter {
    private final Map<Integer, Character> correctPositions = new HashMap<>();
    private final Set<Character> presentLetters = new HashSet<>();
    private final Set<Character> absentLetters = new HashSet<>();
    private final Map<Character, Integer> minLetterCounts = new HashMap<>();

    public void updateFromGuess(String guess, String pattern) {
        Map<Character, Integer> confirmed = new HashMap<>();
        correctPositions.clear();

        for (int i = 0; i < 5; i++) {
            char c = guess.charAt(i);
            char p = pattern.charAt(i);
            if (p == '+') {
                correctPositions.put(i, c);
                confirmed.put(c, confirmed.getOrDefault(c, 0) + 1);
                presentLetters.add(c);
            } else if (p == '^') {
                confirmed.put(c, confirmed.getOrDefault(c, 0) + 1);
                presentLetters.add(c);
            }
        }

        Set<Character> allGuessedLetters = new HashSet<>();
        for (char c : guess.toCharArray()) allGuessedLetters.add(c);

        for (char c : allGuessedLetters) {
            if (!confirmed.containsKey(c)) {
                absentLetters.add(c);
            }
        }

        for (Map.Entry<Character, Integer> entry : confirmed.entrySet()) {
            char letter = entry.getKey();
            int count = entry.getValue();
            minLetterCounts.put(letter, Math.max(minLetterCounts.getOrDefault(letter, 0), count));
        }
    }

    public boolean matches(String word) {
        if (word == null || word.length() != 5) return false;

        for (Map.Entry<Integer, Character> entry : correctPositions.entrySet()) {
            if (word.charAt(entry.getKey()) != entry.getValue()) return false;
        }

        for (char c : absentLetters) {
            if (word.indexOf(c) != -1) return false;
        }

        for (char c : presentLetters) {
            if (word.indexOf(c) == -1) return false;
            if (countOccurrences(word, c) < minLetterCounts.getOrDefault(c, 1)) return false;
        }

        return true;
    }

    private int countOccurrences(String word, char character) {
        int count = 0;
        for (char c : word.toCharArray()) if (c == character) count++;
        return count;
    }

    public String getCorrectPositionsString() {
        if (correctPositions.isEmpty()) return "нет";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (correctPositions.containsKey(i)) {
                sb.append("поз.").append(i).append("=").append(correctPositions.get(i)).append(" ");
            }
        }
        return sb.toString();
    }

    public Set<Character> getPresentLetters() { return new HashSet<>(presentLetters); }
    public Set<Character> getAbsentLetters() { return new HashSet<>(absentLetters); }
    public Map<Character, Integer> getMinLetterCounts() { return new HashMap<>(minLetterCounts); }
}