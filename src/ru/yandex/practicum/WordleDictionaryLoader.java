package ru.yandex.practicum;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class WordleDictionaryLoader {

    private final PrintWriter logWriter;

    public WordleDictionaryLoader(PrintWriter logWriter) {
        if (logWriter == null) {
            throw new WordleSystemException("Логгер не может быть null");
        }
        this.logWriter = logWriter;
    }

    public WordleDictionary loadDictionary(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new WordleSystemException("Имя файла не может быть пустым");
        }

        File file = new File(filename);
        if (!file.exists()) {
            throw new WordleSystemException("Файл словаря не найден: " + filename);
        }

        Set<String> uniqueWords = new HashSet<>();
        logWriter.println("Загрузка словаря из файла: " + filename);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8))) {

            String line;
            int loadedWords = 0;

            while ((line = reader.readLine()) != null) {
                String formattedWord = formatWord(line.trim());
                if (formattedWord.length() == 5 && uniqueWords.add(formattedWord)) {
                    loadedWords++;
                }
            }

            if (uniqueWords.isEmpty()) {
                throw new WordleSystemException("Словарь пуст или не содержит 5-буквенных слов");
            }

            logWriter.println("Успешно загружено " + loadedWords + " уникальных слов");

        } catch (FileNotFoundException e) {
            throw new WordleSystemException("Файл словаря не найден: " + filename, e);
        } catch (IOException e) {
            throw new WordleSystemException("Ошибка чтения файла словаря: " + e.getMessage(), e);
        }

        return new WordleDictionary(new ArrayList<>(uniqueWords), logWriter);
    }

    private String formatWord(String word) {
        if (word == null) return "";
        return word.toLowerCase().replace('ё', 'е').trim();
    }
}