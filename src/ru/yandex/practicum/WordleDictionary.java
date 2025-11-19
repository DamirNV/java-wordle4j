package ru.yandex.practicum;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class WordleDictionary {

    private List<String> words;
    private Random random;

    public WordleDictionary(List<String> words) {
        this.words = new ArrayList<>(words);
        this.random = new Random();
    }

    public boolean contains(String word) {
        if (word == null || word.trim().isEmpty()) {
            throw new WordNotFoundInDictionaryException("Пустая строка");
        }
        String formattedWord = word.toLowerCase().replace('ё', 'е').trim();
        if (!words.contains(formattedWord)) {
            throw new WordNotFoundInDictionaryException(word);
        }
        return true;
    }

    public String getRandomWord() {
        if (words.isEmpty()) {
            throw new WordleGameException("Словарь пуст");
        }
        return words.get(random.nextInt(words.size()));
    }

    public List<String> getWords() {
        return new ArrayList<>(words);
    }

}
