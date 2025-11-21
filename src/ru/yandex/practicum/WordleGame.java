package ru.yandex.practicum;

import java.io.PrintWriter;
import java.util.*;

public class WordleGame {

    private final String answer;
    private int remainingAttempts;
    private final WordleDictionary dictionary;
    private final PrintWriter logWriter;
    private final Set<String> previousGuesses = new HashSet<>();
    private final WordleHintFilter hintFilter = new WordleHintFilter();
    private List<String> cachedPossibleWords;
    private boolean filterDirty = true;
    private final Random random = new Random();

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
        if (!dictionary.contains(normalizedGuess)) {
            throw new WordNotFoundInDictionaryException(normalizedGuess);
        }

        previousGuesses.add(normalizedGuess);
        String result = generateHintPattern(normalizedGuess);
        remainingAttempts--;

        hintFilter.updateFromGuess(normalizedGuess, result, answer);
        filterDirty = true;

        logWriter.println("Проверка слова: " + normalizedGuess + " -> " + result + " (осталось попыток: " + remainingAttempts + ")");
        return result;
    }

    public boolean isGameOver() {
        return remainingAttempts <= 0 || isWordGuessed();
    }

    public boolean isWordGuessed() {
        if (previousGuesses.isEmpty()) return false;
        for (String guess : previousGuesses) {
            if (answer.equals(guess)) {
                return true;
            }
        }
        return false;
    }

    public String generateHint() {
        logHintFilterState();
        List<String> possibleWords = getCachedPossibleWords();

        List<String> availableWords = new ArrayList<>();
        for (String word : possibleWords) {
            if (!previousGuesses.contains(word)) {
                availableWords.add(word);
            }
        }

        if (availableWords.isEmpty()) {
            return getRandomWordExcludingUsed();
        }

        return selectBestHint(availableWords);
    }

    private List<String> getCachedPossibleWords() {
        if (filterDirty || cachedPossibleWords == null) {
            cachedPossibleWords = dictionary.getFilteredWords(hintFilter);
            filterDirty = false;
            logWriter.println("Кэш подсказок обновлен, найдено слов: " + cachedPossibleWords.size());
        }
        return cachedPossibleWords;
    }

    private String selectBestHint(List<String> possibleWords) {
        if (possibleWords.size() <= 3) {
            return possibleWords.get(random.nextInt(possibleWords.size()));
        }

        String bestWord = possibleWords.get(0);
        int maxNewLetters = countNewLetters(bestWord);

        for (int i = 1; i < Math.min(possibleWords.size(), 100); i++) {
            String word = possibleWords.get(i);
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
            if (!usedLetters.contains(c)) newLetters++;
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
        char[] result = new char[5];
        Arrays.fill(result, '-');
        char[] answerChars = answer.toCharArray();
        boolean[] used = new boolean[5];

        for (int i = 0; i < 5; i++) {
            if (guess.charAt(i) == answerChars[i]) {
                result[i] = '+';
                used[i] = true;
            }
        }

        for (int i = 0; i < 5; i++) {
            if (result[i] == '+') continue;

            char guessChar = guess.charAt(i);
            for (int j = 0; j < 5; j++) {
                if (!used[j] && answerChars[j] == guessChar) {
                    result[i] = '^';
                    used[j] = true;
                    break;
                }
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
        logWriter.println(state);
    }

    private String getRandomWordExcludingUsed() {
        List<String> allWords = dictionary.getWords();
        List<String> availableWords = new ArrayList<>();

        for (String word : allWords) {
            if (!previousGuesses.contains(word)) {
                availableWords.add(word);
            }
        }

        if (availableWords.isEmpty()) {
            return allWords.get(random.nextInt(allWords.size()));
        }

        return availableWords.get(random.nextInt(availableWords.size()));
    }

    private String normalizeWord(String word) {
        return word.toLowerCase().replace('ё', 'е').trim();
    }

    public String getAnswer() { return answer; }
    public int getRemainingAttempts() { return remainingAttempts; }
    public int getUsedAttempts() { return 6 - remainingAttempts; }
    public Set<String> getPreviousGuesses() { return new HashSet<>(previousGuesses); }
}