package ru.yandex.practicum;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/*
этот класс содержит в себе список слов List<String>
    его методы похожи на методы списка, но учитывают особенности игры
    также этот класс может содержать рутинные функции по сравнению слов, букв и т.д.
 */
public class WordleDictionary {

    private List<String> words;
    private Random random;

    public WordleDictionary(List<String> words) {
        this.words = new ArrayList<>(words);
        this.random = new Random();
    }

    public boolean contains(String word) {
        return words.contains(word.toLowerCase());
    }

    public String getRandomWord() {
        return words.get(random.nextInt(words.size()));
    }

}
