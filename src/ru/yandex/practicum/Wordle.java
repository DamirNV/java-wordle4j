package ru.yandex.practicum;

import java.util.Scanner;
import java.io.*;

public class Wordle {

    public static void main(String[] args) {

        PrintWriter logWriter = null;
        try {
            logWriter = new PrintWriter("wordle.log", "UTF-8");
        } catch (Exception e) {
            System.err.println("Не удалось создать лог-файл");
            return;
        }

        Scanner scanner = new Scanner(System.in, "UTF-8");

        try {
            logWriter.println("Запуск игры Wordle");
            WordleDictionaryLoader loader = new WordleDictionaryLoader();
            WordleDictionary dictionary = loader.loadDictionary("words_ru.txt");
            logWriter.println("Словарь загружен, слов: " + dictionary.getWords().size());

            WordleGame game = new WordleGame(dictionary);
            logWriter.println("Игра создана, загаданное слово: " + game.getAnswer());

            System.out.println("🎯 Добро пожаловать в Wordle!");
            System.out.println("У вас 6 попыток чтобы угадать 5-буквенное слово");
            System.out.println("📝 Правила:");
            System.out.println("   '+' - буква на правильной позиции");
            System.out.println("   '^' - буква есть в слове, но в другой позиции");
            System.out.println("   '-' - буквы нет в слове");
            System.out.println("💡 Нажмите Enter для подсказки");

            boolean gameWon = false;

            while (!game.isGameOver() && !gameWon) {
                System.out.println("\n➡️ Осталось попыток: " + game.getSteps());
                System.out.print("Ваше слово: ");
                String guess = scanner.nextLine();

                if (guess.isEmpty()) {
                    String hint = game.generateHint();
                    System.out.println("💡 Подсказка: попробуйте слово - " + hint);
                    logWriter.println("Пользователь запросил подсказку: " + hint);
                    continue;
                }

                if (guess.length() != 5) {
                    System.out.println("❌ Слово должно состоять из 5 букв!");
                    logWriter.println("Пользователь ввел слово неверной длины: " + guess);
                    continue;
                }

                try {
                    String normalizedGuess = guess.toLowerCase().replace('ё', 'е').trim();
                    if (!dictionary.contains(normalizedGuess)) {
                        System.out.println("❌ Слова нет в словаре!");
                        continue;
                    }
                    String result = game.checkGuess(normalizedGuess);
                    logWriter.println("Догадка: " + normalizedGuess + " -> " + result);
                    System.out.println("📊 Результат:");
                    System.out.println("   Слово:    " + normalizedGuess);
                    System.out.println("   Паттерн:  " + result);
                    if (game.isWordGuessed(normalizedGuess)) {
                        System.out.println("\n🎉 ПОЗДРАВЛЯЕМ! Вы угадали слово!");
                        logWriter.println("Игра выиграна! Слово: " + normalizedGuess);
                        gameWon = true;
                    }
                } catch (WordNotFoundInDictionaryException e) {
                    System.out.println("❌ " + e.getMessage());
                    logWriter.println("Ошибка: " + e.getMessage());
                }
            }

            if (!gameWon) {
                System.out.println("\n💀 ИГРА ОКОНЧЕНА!");
                System.out.println("Загаданное слово было: " + game.getAnswer());
                logWriter.println("Игра проиграна. Загаданное слово: " + game.getAnswer());
            }

            System.out.println("\n📈 Статистика игры:");
            System.out.println("   Использовано попыток: " + (6 - game.getSteps()));
            System.out.println("   Слово: " + game.getAnswer());

        } catch (WordleGameException e) {
            System.err.println("🚨 Ошибка игры: " + e.getMessage());
            logWriter.println("Ошибка игры: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("🚨 Неожиданная ошибка: " + e.getMessage());
            logWriter.println("Критическая ошибка: " + e.getMessage());
            e.printStackTrace(logWriter);
        } finally {
            System.out.println("\n👋 Спасибо за игру!");
            scanner.close();
            if (logWriter != null) {
                logWriter.close();
            }
        }
    }

}


