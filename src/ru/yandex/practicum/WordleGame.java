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
        return generateHint(guess);
    }

    public boolean isGameOver() {
        return steps <= 0;
    }

    public boolean isWordGuessed(String lastGuess) {
        return generateHint(lastGuess).equals("+++++");
    }

    private String generateHint(String guess) {
        guess = guess.toLowerCase();
        StringBuilder hint = new StringBuilder();
        for (int i = 0; i < guess.length(); i++) {
            char currentChar = guess.charAt(i);
            if (currentChar == answer.charAt(i)) {
                hint.append('+');
            } else if (answer.indexOf(currentChar) != -1) {
                hint.append('^');
            } else {
                hint.append('-');
            }
        }
        return hint.toString();
    }

}
