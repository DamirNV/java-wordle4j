package ru.yandex.practicum;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/*
этот класс содержит в себе всю рутину по работе с файлами словарей и с кодировками
    ему нужны методы по загрузке списка слов из файла по имени файла
    на выходе должен быть класс WordleDictionary
 */
public class WordleDictionaryLoader {


    public WordleDictionary loadDictionary(String filename) {
        List<String> words = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8))) {

            String line;
            int totalWords = 0;
            int fiveLetterWords = 0;

            while ((line = reader.readLine()) != null) {
                totalWords++;
                String formattedWord = formatWord(line.trim());
                if (formattedWord.length() == 5) {
                    words.add(formattedWord);
                    fiveLetterWords++;
                }
            }



        } catch (IOException e) {

        }

        return new WordleDictionary(words);
    }

    private String formatWord(String word) {
        return word.toLowerCase()
                .replace('ё', 'е')
                .trim();
    }

}
