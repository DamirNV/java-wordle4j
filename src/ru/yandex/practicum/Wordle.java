package ru.yandex.practicum;

import java.util.Scanner;

/*
в главном классе нам нужно:
    создать лог-файл (он должен передаваться во все классы)
    создать загрузчик словарей WordleDictionaryLoader
    загрузить словарь WordleDictionary с помощью класса WordleDictionaryLoader
    затем создать игру WordleGame и передать ей словарь
    вызвать игровой метод в котором в цикле опрашивать пользователя и передавать информацию в игру
    вывести состояние игры и конечный результат
 */
public class Wordle {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        WordleDictionaryLoader loader = new WordleDictionaryLoader();
        WordleDictionary dictionary = loader.loadDictionary("words_ru.txt");

        WordleGame game = new WordleGame(dictionary);

        System.out.println("Угадайте слово!");

        while (!game.isGameOver()) {
            System.out.print("Ваша догадка: ");
            String guess = scanner.nextLine();

            if (!dictionary.contains(guess)) {
                System.out.println("Слова нет в словаре!");
                continue;
            }

            String result = game.checkGuess(guess);

            if (game.isWordGuessed(guess)) {
                System.out.println("Поздравляем! Вы угадали!");
                break;
            }
        }

        scanner.close();
    }



}


