package ru.yandex.practicum;

/*
в этом классе хранится словарь и состояние игры
    текущий шаг
    всё что пользователь вводил
    правильный ответ

в этом классе нужны методы, которые
    проанализируют совпадение слова с ответом
    предложат слово-подсказку с учётом всего, что вводил пользователь ранее

не забудьте про специальные типы исключений для игровых и неигровых ошибок
 */
public class WordleGame {

    private String answer;
    private int steps;
    private WordleDictionary dictionary;

    public WordleGame(WordleDictionary dictionary) {
        this.answer = dictionary.getRandomWord();
        this.steps = 6;
        this.dictionary = dictionary;
    }

    public String checkGuess(String guess) {
        steps--;

        if (guess.equalsIgnoreCase(answer)) {
            return "Угадал!";
        }

        return "Не угадал!";
    }

    public boolean isGameOver() {
        return steps <= 0;
    }

    public boolean isWordGuessed(String lastGuess) {
        return lastGuess.equalsIgnoreCase(answer);
    }


}
