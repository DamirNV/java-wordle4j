package ru.yandex.practicum;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class WordleDictionaryLoader {

    public WordleDictionary loadDictionary(String filename) {
        List<String> words = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename),
                StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String formattedWord = formatWord(line.trim());
                if (formattedWord.length() == 5) {
                    words.add(formattedWord);
                }
            }
            if (words.isEmpty()) {
                throw new WordleGameException("Словарь пуст или не содержит 5-буквенных слов");
            }
        } catch (IOException e) {
            throw new WordleGameException("Ошибка загрузки файла: " + e.getMessage());
        }
        return new WordleDictionary(words);
    }

    private String formatWord(String word) {
        return word.toLowerCase()
                .replace('ё', 'е')
                .trim();
    }

}
