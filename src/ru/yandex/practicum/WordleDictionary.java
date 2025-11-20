package ru.yandex.practicum;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WordleDictionary {

    private List<String> words;
    private Random random;
    private PrintWriter logWriter;

    public WordleDictionary(List<String> words, PrintWriter logWriter) {
        if (words == null) {
            throw new WordleSystemException("Список слов не может быть null");
        }
        if (logWriter == null) {
            throw new WordleSystemException("Логгер не может быть null");
        }

        this.words = new ArrayList<>(words);
        this.random = new Random();
        this.logWriter = logWriter;
        logWriter.println("Словарь создан, слов: " + words.size());

        if (this.words.isEmpty()) {
            throw new WordleSystemException("Передан пустой список слов в словарь");
        }
    }

    public boolean contains(String word) {
        if (word == null) {
            throw new WordNotFoundInDictionaryException("Слово не может быть null");
        }

        String normalizedWord = word.toLowerCase().replace('ё', 'е').trim();
        logWriter.println("Проверка слова в словаре: " + normalizedWord);

        if (normalizedWord.isEmpty()) {
            throw new WordNotFoundInDictionaryException("Пустая строка");
        }

        if (normalizedWord.length() != 5) {
            throw new WordNotFoundInDictionaryException("Слово должно содержать 5 букв");
        }

        if (!words.contains(normalizedWord)) {
            throw new WordNotFoundInDictionaryException(word);
        }

        return true;
    }

    public String getRandomWord() {
        if (words.isEmpty()) {
            throw new WordleSystemException("Попытка получить слово из пустого словаря");
        }
        String word = words.get(random.nextInt(words.size()));
        logWriter.println("Выбрано случайное слово: " + word);
        return word;
    }

    public List<String> getWords() {
        return new ArrayList<>(words);
    }
}