package ru.yandex.practicum;

import java.io.PrintWriter;
import java.util.*;

public class WordleGame {

    private final String answer;
    private int remainingAttempts;
    private final WordleDictionary dictionary;
    private final PrintWriter logWriter;

    private final List<String> previousGuesses = new ArrayList<>();
    private final WordleHintFilter hintFilter = new WordleHintFilter();

    public WordleGame(WordleDictionary dictionary, PrintWriter logWriter) {
        if (dictionary == null) {
            throw new WordleSystemException("Словарь не может быть null");
        }
        if (logWriter == null) {
            throw new WordleSystemException("Логгер не может быть null");
        }

        this.answer = dictionary.getRandomWord();
        this.remainingAttempts = 6;
        this.dictionary = dictionary;
        this.logWriter = logWriter;

        if (this.answer == null || this.answer.length() != 5) {
            throw new WordleSystemException("Загаданное слово имеет неверный формат");
        }

        logWriter.println("Игра создана, загаданное слово: " + answer);
    }

    public String checkGuess(String guess) {
        if (remainingAttempts <= 0) {
            throw new WordleGameException("Игра уже завершена");
        }
        if (guess == null) {
            throw new WordNotFoundInDictionaryException("Слово не может быть null");
        }

        String normalizedGuess = normalizeWord(guess);
        previousGuesses.add(normalizedGuess);

        String result = generateHintPattern(normalizedGuess);

        remainingAttempts--;

        hintFilter.updateFromGuess(normalizedGuess, result, answer);

        logWriter.println("Проверка слова: " + normalizedGuess + " -> " + result + " (осталось попыток: " + remainingAttempts + ")");

        return result;
    }

    public boolean isGameOver() {
        return remainingAttempts <= 0 || isWordGuessed();
    }

    public boolean isWordGuessed() {
        if (previousGuesses.isEmpty()) {
            return false;
        }
        String lastGuess = previousGuesses.get(previousGuesses.size() - 1);
        return generateHintPattern(lastGuess).equals("+++++");
    }

    public String generateHint() {
        logHintFilterState();

        List<String> possibleWords = dictionary.getFilteredWords(hintFilter);

        possibleWords.removeAll(previousGuesses);

        if (possibleWords.isEmpty()) {
            String fallbackHint = getRandomWordExcludingUsed();
            logWriter.println("Fallback подсказка: " + fallbackHint);
            return fallbackHint;
        }

        String bestHint = selectBestHint(possibleWords);
        logWriter.println("Лучшая подсказка: " + bestHint);
        return bestHint;
    }

    private String selectBestHint(List<String> possibleWords) {
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

        return bestWord;
    }

    private int countNewLetters(String word) {
        Set<Character> usedLetters = getAllUsedLetters();
        int newLetters = 0;

        for (char c : word.toCharArray()) {
            if (!usedLetters.contains(c)) {
                newLetters++;
            }
        }
        return newLetters;
    }

    private Set<Character> getAllUsedLetters() {
        Set<Character> usedLetters = new HashSet<>();
        for (String guess : previousGuesses) {
            for (char c : guess.toCharArray()) {
                usedLetters.add(c);
            }
        }
        return usedLetters;
    }

    private String generateHintPattern(String guess) {
        if (!guess.equals(answer) && guess.length() != 5) {
            return "-----";
        }

        char[] result = new char[5];
        Map<Character, Integer> availableCounts = new HashMap<>();

        for (int i = 0; i < 5; i++) {
            char answerChar = answer.charAt(i);
            if (guess.charAt(i) != answerChar) {
                availableCounts.put(answerChar, availableCounts.getOrDefault(answerChar, 0) + 1);
            }
        }

        for (int i = 0; i < 5; i++) {
            if (guess.charAt(i) == answer.charAt(i)) {
                result[i] = '+';
            } else {
                result[i] = '-';
            }
        }

        for (int i = 0; i < 5; i++) {
            if (result[i] == '+') continue;

            char guessChar = guess.charAt(i);
            if (availableCounts.getOrDefault(guessChar, 0) > 0) {
                result[i] = '^';
                availableCounts.put(guessChar, availableCounts.get(guessChar) - 1);
            }
        }

        return new String(result);
    }

    private void logHintFilterState() {
        StringBuilder state = new StringBuilder();
        state.append("=== СОСТОЯНИЕ ФИЛЬТРА ПОДСКАЗОК ===\n");
        state.append("   Попытки: ").append(previousGuesses.size()).append("\n");
        state.append("   Известные позиции: ").append(hintFilter.getCorrectPositionsString()).append("\n");
        state.append("   Присутствующие буквы: ").append(hintFilter.getPresentLetters()).append("\n");
        state.append("   Отсутствующие буквы: ").append(hintFilter.getAbsentLetters()).append("\n");
        state.append("   Минимальные количества букв: ").append(hintFilter.getMinLetterCounts());
        logWriter.println(state.toString());
    }

    private String getRandomWordExcludingUsed() {
        List<String> allWords = new ArrayList<>(dictionary.getWords());
        allWords.removeAll(previousGuesses);

        if (allWords.isEmpty()) {
            logWriter.println("Все слова использованы, возвращаем случайное слово");
            return dictionary.getRandomWord();
        }

        Random random = new Random();
        return allWords.get(random.nextInt(allWords.size()));
    }

    private String normalizeWord(String word) {
        return word.toLowerCase().replace('ё', 'е').trim();
    }

    public String getAnswer() {
        return answer;
    }

    public int getRemainingAttempts() {
        return remainingAttempts;
    }

    public int getUsedAttempts() {
        return 6 - remainingAttempts;
    }

    public List<String> getPreviousGuesses() {
        return new ArrayList<>(previousGuesses);
    }
}