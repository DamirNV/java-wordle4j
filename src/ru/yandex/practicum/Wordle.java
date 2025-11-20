package ru.yandex.practicum;

import java.util.Scanner;
import java.io.*;

public class Wordle {

    public static void main(String[] args) {
        PrintWriter logWriter = null;
        try {
            logWriter = new PrintWriter("wordle.log", "UTF-8");
            logWriter.println("=== ЗАПУСК ИГРЫ WORDLE ===");

            runGame(logWriter);

        } catch (WordleSystemException e) {

            if (logWriter != null) {
                logWriter.println("СИСТЕМНАЯ ОШИБКА: " + e.getMessage());
                e.printStackTrace(logWriter);
            }
        } catch (WordleGameException e) {

            if (logWriter != null) {
                logWriter.println("ИГРОВАЯ ОШИБКА: " + e.getMessage());
            }
            System.err.println("Ошибка: " + e.getMessage());
        } catch (Exception e) {
            if (logWriter != null) {
                logWriter.println("НЕИЗВЕСТНАЯ ОШИБКА: " + e.getMessage());
                e.printStackTrace(logWriter);
            }
            System.err.println("Критическая ошибка: " + e.getMessage());
        } finally {
            if (logWriter != null) {
                logWriter.println("=== ИГРА ЗАВЕРШЕНА ===");
                logWriter.close();
            }
        }
    }

    private static void runGame(PrintWriter logWriter) {
        Scanner scanner = new Scanner(System.in, "UTF-8");

        try {
            WordleDictionaryLoader loader = new WordleDictionaryLoader(logWriter);
            WordleDictionary dictionary = loader.loadDictionary("words_ru.txt");

            WordleGame game = new WordleGame(dictionary, logWriter);

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

                if (!guess.matches("[а-яёА-ЯЁ]+")) {
                    System.out.println("❌ Слово должно содержать только русские буквы!");
                    logWriter.println("Пользователь ввел слово с неверными символами: " + guess);
                    continue;
                }

                if (guess.length() != 5) {
                    System.out.println("❌ Слово должно состоять из 5 букв!");
                    logWriter.println("Пользователь ввел слово неверной длины: " + guess);
                    continue;
                }

                try {
                    String normalizedGuess = guess.toLowerCase().replace('ё', 'е').trim();

                    dictionary.contains(normalizedGuess);

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
                    logWriter.println("Ошибка словаря: " + e.getMessage());
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

        } finally {
            scanner.close();
            System.out.println("\n👋 Спасибо за игру!");
        }
    }
}