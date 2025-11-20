package ru.yandex.practicum;

import java.util.*;

public class WordleHintFilter {
    private final Map<Integer, Character> correctPositions = new HashMap<>();
    private final Set<Character> presentLetters = new HashSet<>();
    private final Set<Character> absentLetters = new HashSet<>();
    private final Map<Character, Integer> minLetterCounts = new HashMap<>();

    public void updateFromGuess(String guess, String pattern, String answer) {
        if (guess == null || pattern == null || answer == null) return;

        Map<Character, Integer> letterCountsInGuess = new HashMap<>();
        Map<Character, Integer> confirmedCounts = new HashMap<>();

        for (int i = 0; i < pattern.length(); i++) {
            char guessChar = guess.charAt(i);
            char patternChar = pattern.charAt(i);

            letterCountsInGuess.put(guessChar, letterCountsInGuess.getOrDefault(guessChar, 0) + 1);

            switch (patternChar) {
                case '+':
                    correctPositions.put(i, guessChar);
                    presentLetters.add(guessChar);
                    absentLetters.remove(guessChar);
                    confirmedCounts.put(guessChar, confirmedCounts.getOrDefault(guessChar, 0) + 1);
                    break;
                case '^':
                    presentLetters.add(guessChar);
                    absentLetters.remove(guessChar);
                    confirmedCounts.put(guessChar, confirmedCounts.getOrDefault(guessChar, 0) + 1);
                    break;
                case '-':
                    if (!presentLetters.contains(guessChar)) {
                        absentLetters.add(guessChar);
                    }
                    break;
            }
        }

        for (Map.Entry<Character, Integer> entry : confirmedCounts.entrySet()) {
            char letter = entry.getKey();
            int count = entry.getValue();
            minLetterCounts.put(letter, Math.max(minLetterCounts.getOrDefault(letter, 0), count));
        }
    }

    public boolean matches(String word) {
        if (word == null || word.length() != 5) return false;

        for (Map.Entry<Integer, Character> entry : correctPositions.entrySet()) {
            int position = entry.getKey();
            char expectedChar = entry.getValue();
            if (word.charAt(position) != expectedChar) {
                return false;
            }
        }

        for (char absentChar : absentLetters) {
            if (word.indexOf(absentChar) != -1) {
                return false;
            }
        }

        for (char presentChar : presentLetters) {
            if (word.indexOf(presentChar) == -1) {
                return false;
            }
            int minCount = minLetterCounts.getOrDefault(presentChar, 1);
            if (countOccurrences(word, presentChar) < minCount) {
                return false;
            }
        }

        return true;
    }

    private int countOccurrences(String word, char character) {
        int count = 0;
        for (char c : word.toCharArray()) {
            if (c == character) count++;
        }
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

    public Set<Character> getPresentLetters() {
        return new HashSet<>(presentLetters);
    }

    public Set<Character> getAbsentLetters() {
        return new HashSet<>(absentLetters);
    }

    public Map<Character, Integer> getMinLetterCounts() {
        return new HashMap<>(minLetterCounts);
    }
}