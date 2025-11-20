package ru.yandex.practicum;

import java.io.PrintWriter;
import java.util.*;

public class WordleGame {

    private String answer;
    private int steps;
    private WordleDictionary dictionary;
    private PrintWriter logWriter;

    private List<String> previousGuesses = new ArrayList<>();
    private Map<Integer, Character> correctPositions = new HashMap<>();
    private Set<Character> presentLetters = new HashSet<>();
    private Set<Character> absentLetters = new HashSet<>();
    private Set<Character> triedLetters = new HashSet<>();

    public WordleGame(WordleDictionary dictionary, PrintWriter logWriter) {
        this.answer = dictionary.getRandomWord();
        this.steps = 6;
        this.dictionary = dictionary;
        this.logWriter = logWriter;
        logWriter.println("Игра создана, загаданное слово: " + answer);
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
            String randomHint = getRandomWordExcludingUsed();
            logWriter.println("Первая подсказка: " + randomHint);
            return randomHint;
        }
        logWriter.println("Генерация подсказки на основе " + previousGuesses.size() + " попыток");
        logWriter.println("Известные позиции: " + correctPositions);
        logWriter.println("Присутствующие буквы: " + presentLetters);
        logWriter.println("Отсутствующие буквы: " + absentLetters);
        List<String> allWords = dictionary.getWords();
        logWriter.println("Всего слов в словаре: " + allWords.size());
        List<String> possibleWords = filterWordsByKnownConditions(allWords);
        logWriter.println("Подходящих слов найдено: " + possibleWords.size());
        if (!possibleWords.isEmpty()) {
            String bestHint = findBestHint(possibleWords);
            logWriter.println("Лучшая подсказка: " + bestHint);
            return bestHint;
        }
        String fallbackHint = getRandomWordExcludingUsed();
        logWriter.println("Fallback подсказка: " + fallbackHint);
        return fallbackHint;
    }

    private String findBestHint(List<String> possibleWords) {
        if (possibleWords.isEmpty()) {
            return getRandomWordExcludingUsed();
        }
        if (possibleWords.size() <= 3) {
            Random random = new Random();
            return possibleWords.get(random.nextInt(possibleWords.size()));
        }
        String bestWord = possibleWords.get(0);
        int maxNewLetters = countNewLetters(bestWord);
        for (String word : possibleWords) {
            int newLettersCount = countNewLetters(word);
            if (newLettersCount > maxNewLetters) {
                bestWord = word;
                maxNewLetters = newLettersCount;
            }
        }
        logWriter.println("Выбрано слово с " + maxNewLetters + " новыми буквами: " + bestWord);
        return bestWord;
    }

    private int countNewLetters(String word) {
        int newLetters = 0;
        for (char c : word.toCharArray()) {
            if (!triedLetters.contains(c)) {
                newLetters++;
            }
        }
        return newLetters;
    }

    private void analyzeGuessForHints(String guess) {
        String pattern = generateHint(guess);
        Map<Character, Integer> availableInAnswer = new HashMap<>();
        for (char c : answer.toCharArray()) {
            availableInAnswer.put(c, availableInAnswer.getOrDefault(c, 0) + 1);
        }
        for (int i = 0; i < pattern.length(); i++) {
            char currentChar = guess.charAt(i);
            triedLetters.add(currentChar);
            if (pattern.charAt(i) == '+') {
                correctPositions.put(i, currentChar);
                presentLetters.add(currentChar);
                absentLetters.remove(currentChar);
                availableInAnswer.put(currentChar, availableInAnswer.get(currentChar) - 1);
            }
        }
        for (int i = 0; i < pattern.length(); i++) {
            if (pattern.charAt(i) == '+') continue;
            char currentChar = guess.charAt(i);
            switch (pattern.charAt(i)) {
                case '^':
                    if (availableInAnswer.getOrDefault(currentChar, 0) > 0) {
                        presentLetters.add(currentChar);
                        absentLetters.remove(currentChar);
                        availableInAnswer.put(currentChar, availableInAnswer.get(currentChar) - 1);
                    } else {
                        if (!presentLetters.contains(currentChar)) {
                            absentLetters.add(currentChar);
                        }
                    }
                    break;
                case '-':
                    if (!presentLetters.contains(currentChar) &&
                            !availableInAnswer.containsKey(currentChar)) {
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
