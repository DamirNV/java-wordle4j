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
        if (dictionary == null) {
            throw new WordleSystemException("Словарь не может быть null");
        }
        if (logWriter == null) {
            throw new WordleSystemException("Логгер не может быть null");
        }

        this.answer = dictionary.getRandomWord();
        this.steps = 6;
        this.dictionary = dictionary;
        this.logWriter = logWriter;

        if (this.answer == null || this.answer.length() != 5) {
            throw new WordleSystemException("Загаданное слово имеет неверный формат");
        }

        logWriter.println("Игра создана, загаданное слово: " + answer);
    }

    public String checkGuess(String guess) {
        if (steps <= 0) {
            throw new WordleGameException("Игра уже завершена");
        }
        if (guess == null) {
            throw new WordNotFoundInDictionaryException("Слово не может быть null");
        }

        steps--;
        String normalizedGuess = guess.toLowerCase().replace('ё', 'е').trim();
        previousGuesses.add(normalizedGuess);

        logWriter.println("Проверка слова: " + normalizedGuess + " (осталось попыток: " + steps + ")");

        analyzeGuessForHints(normalizedGuess);
        return generateHint(guess);
    }

    public boolean isGameOver() {
        return steps <= 0;
    }

    public boolean isWordGuessed(String lastGuess) {
        if (lastGuess == null) {
            return false;
        }
        return generateHint(lastGuess).equals("+++++");
    }

    public String generateHint() {
        logMessageWithState("Генерация подсказки");

        if (previousGuesses.isEmpty()) {
            String randomHint = getRandomWordExcludingUsed();
            logWriter.println("Первая подсказка: " + randomHint);
            return randomHint;
        }

        List<String> allWords = dictionary.getWords();
        List<String> possibleWords = filterWordsByKnownConditions(allWords);

        if (!possibleWords.isEmpty()) {
            String bestHint = findBestHint(possibleWords);
            logWriter.println("Лучшая подсказка: " + bestHint);
            return bestHint;
        }

        String fallbackHint = getRandomWordExcludingUsed();
        logWriter.println("Fallback подсказка: " + fallbackHint);
        return fallbackHint;
    }

    private void logMessageWithState(String message) {
        StringBuilder state = new StringBuilder();
        state.append("=== ").append(message).append(" ===\n");
        state.append("   Попытки: ").append(previousGuesses.size()).append("\n");

        if (!correctPositions.isEmpty()) {
            state.append("   Известные позиции: ");
            for (int i = 0; i < 5; i++) {
                if (correctPositions.containsKey(i)) {
                    state.append(i).append("=").append(correctPositions.get(i)).append(" ");
                }
            }
            state.append("\n");
        }

        if (!presentLetters.isEmpty()) {
            state.append("   Есть в слове: ").append(presentLetters).append("\n");
        }

        if (!absentLetters.isEmpty()) {
            state.append("   Отсутствуют: ").append(absentLetters).append("\n");
        }

        state.append("   Использовано букв: ").append(triedLetters.size());
        logWriter.println(state.toString());
    }

    private String findBestHint(List<String> possibleWords) {
        if (possibleWords.isEmpty()) {
            throw new WordleSystemException("Список возможных слов пуст");
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
        if (word == null) {
            return 0;
        }

        int newLetters = 0;
        for (char c : word.toCharArray()) {
            if (!triedLetters.contains(c)) {
                newLetters++;
            }
        }
        return newLetters;
    }

    private void analyzeGuessForHints(String guess) {
        if (guess == null) {
            return;
        }

        String pattern = generateHint(guess);
        Map<Character, Integer> availableInAnswer = new HashMap<>();

        StringBuilder analysisLog = new StringBuilder();
        analysisLog.append("Анализ догадки: '").append(guess).append("' -> '").append(pattern).append("'");

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
            if (pattern.charAt(i) == '+') {
                continue;
            }

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
                    if (!presentLetters.contains(currentChar) && !availableInAnswer.containsKey(currentChar)) {
                        absentLetters.add(currentChar);
                    }
                    break;
            }
        }

        analysisLog.append(" [Результат: correctPositions=").append(correctPositions)
                .append(", presentLetters=").append(presentLetters)
                .append(", absentLetters=").append(absentLetters).append("]");
        logWriter.println(analysisLog.toString());
    }

    private List<String> filterWordsByKnownConditions(List<String> words) {
        if (words == null || words.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> filtered = new ArrayList<>();
        for (String word : words) {
            if (matchesAllConditions(word)) {
                filtered.add(word);
            }
        }
        return filtered;
    }

    private boolean matchesAllConditions(String word) {
        if (word == null) {
            return false;
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

    private String generateHint(String guess) {
        if (guess == null) {
            return "-----";
        }

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

    public int getPreviousGuessesCount() {
        return previousGuesses.size();
    }
}