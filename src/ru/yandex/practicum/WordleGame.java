package ru.yandex.practicum;

import java.util.*;

public class WordleGame {

    private String answer;
    private int steps;
    private WordleDictionary dictionary;

    private List<String> previousGuesses = new ArrayList<>();
    private Map<Integer, Character> correctPositions = new HashMap<>();
    private Set<Character> presentLetters = new HashSet<>();
    private Set<Character> absentLetters = new HashSet<>();
    private Set<Character> triedLetters = new HashSet<>();

    public WordleGame(WordleDictionary dictionary) {
        this.answer = dictionary.getRandomWord();
        this.steps = 6;
        this.dictionary = dictionary;
    }

    public String checkGuess(String guess) {
        steps--;
        String normalizedGuess = guess.toLowerCase().replace('ё', 'е').trim();
        previousGuesses.add(normalizedGuess);
        analyzeGuessForHints(normalizedGuess);
        return generateHint(guess);
    }

    public boolean isGameOver() {
        return steps <= 0;
    }

    public boolean isWordGuessed(String lastGuess) {
        return generateHint(lastGuess).equals("+++++");
    }

    public String generateHint() {
        if (previousGuesses.isEmpty()) {
            return getRandomWordExcludingUsed();
        }
        List<String> allWords = new ArrayList<>(dictionary.getWords());
        List<String> possibleWords = filterWordsByKnownConditions(allWords);
        if (!possibleWords.isEmpty()) {
            return getRandomWordFromList(possibleWords);
        }
        return getRandomWordExcludingUsed();
    }

    private void analyzeGuessForHints(String guess) {
        String pattern = generateHint(guess);
        for (int i = 0; i < pattern.length(); i++) {
            char currentChar = guess.charAt(i);
            triedLetters.add(currentChar);
            switch (pattern.charAt(i)) {
                case '+':
                    correctPositions.put(i, currentChar);
                    presentLetters.add(currentChar);
                    absentLetters.remove(currentChar);
                    break;
                case '^':
                    presentLetters.add(currentChar);
                    absentLetters.remove(currentChar);
                    break;
                case '-':
                    if (!presentLetters.contains(currentChar)) {
                        absentLetters.add(currentChar);
                    }
                    break;
            }
        }
    }

    private List<String> filterWordsByKnownConditions(List<String> words) {
        List<String> filtered = new ArrayList<>();
        for (String word : words) {
            if (matchesAllConditions(word)) {
                filtered.add(word);
            }
        }
        return filtered;
    }

    private boolean matchesAllConditions(String word) {
        for (char absentChar : absentLetters) {
            if (word.indexOf(absentChar) != -1) {
                return false;
            }
        }
        for (char presentChar : presentLetters) {
            if (word.indexOf(presentChar) == -1) {
                return false;
            }
        }
        for (Map.Entry<Integer, Character> entry : correctPositions.entrySet()) {
            int position = entry.getKey();
            char expectedChar = entry.getValue();

            if (word.charAt(position) != expectedChar) {
                return false;
            }
        }
        if (previousGuesses.contains(word)) {
            return false;
        }
        return true;
    }

    private String getRandomWordFromList(List<String> wordList) {
        if (wordList.isEmpty()) {
            return getRandomWordExcludingUsed();
        }
        Random random = new Random();
        return wordList.get(random.nextInt(wordList.size()));
    }

    private String getRandomWordExcludingUsed() {
        List<String> allWords = new ArrayList<>(dictionary.getWords());
        allWords.removeAll(previousGuesses);

        if (allWords.isEmpty()) {
            return dictionary.getRandomWord();
        }

        Random random = new Random();
        return allWords.get(random.nextInt(allWords.size()));
    }

    private String generateHint(String guess) {
        guess = guess.toLowerCase();
        StringBuilder hint = new StringBuilder();
        for (int i = 0; i < guess.length(); i++) {
            char currentChar = guess.charAt(i);
            if (currentChar == answer.charAt(i)) {
                hint.append('+');
            } else if (answer.indexOf(currentChar) != -1) {
                hint.append('^');
            } else {
                hint.append('-');
            }
        }
        return hint.toString();
    }

    public String getAnswer() {
        return answer;
    }

    public int getSteps() {
        return steps;
    }

}
