package ru.perfomance.lab;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Game {
    private boolean gameOver;
    private String secretNumber;
    private int attempts;
    private int gameNumber;
    private List<String> guesses;

    public Game() {
        this.secretNumber = null;
        this.attempts = 0;
        this.gameNumber = 0;
        this.gameOver = false;
        this.guesses = new ArrayList<>();
    }

    public void generateSecretNumber() {
        Random random = new Random();
        List<Integer> digits = new ArrayList<>();

        while (digits.size() < 4) {
            int digit = random.nextInt(10);
            if (!digits.contains(digit)) {
                digits.add(digit);
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int digit : digits) {
            sb.append(digit);
        }

        this.secretNumber = sb.toString();
    }

    public void checkGuess(String guess) {
        this.attempts++;
        int bulls = 0;
        int cows = 0;

        boolean[] secretDigitsUsed = new boolean[4];
        boolean[] guessDigitsUsed = new boolean[4];

        for (int i = 0; i < 4; i++) {
            char guessChar = guess.charAt(i);
            char secretChar = this.secretNumber.charAt(i);

            if (guessChar == secretChar) {
                bulls++;
                secretDigitsUsed[i] = true;
                guessDigitsUsed[i] = true;
            }
        }

        for (int i = 0; i < 4; i++) {
            char guessChar = guess.charAt(i);

            if (!guessDigitsUsed[i]) {
                for (int j = 0; j < 4; j++) {
                    char secretChar = this.secretNumber.charAt(j);

                    if (!secretDigitsUsed[j] && guessChar == secretChar) {
                        cows++;
                        secretDigitsUsed[j] = true;
                        guessDigitsUsed[i] = true;
                        break;
                    }
                }
            }
        }

        System.out.printf("%d %s, %d %s%n",
                bulls, (bulls == 1) ? "бык" : "быка",
                cows, (cows == 1) ? "корова" : "коровы");

        this.guesses.add(guess);

        if (bulls == 4) {
            saveGameResults(guess);
            String attemptsPhrase = formatAttemptsPhrase(this.attempts);
            System.out.printf("Поздравляем! Вы угадали число за %s.%n", attemptsPhrase);
            this.gameOver = true;
        }
    }

    private String formatAttemptsPhrase(int attempts) {
        if (attempts % 10 == 1 && attempts != 11) {
            return String.format("%d попытку", attempts);
        } else if ((attempts % 10 >= 2 && attempts % 10 <= 4) && !(attempts >= 12 && attempts <= 14)) {
            return String.format("%d попытки", attempts);
        } else {
            return String.format("%d попыток", attempts);
        }
    }

    public void playGame() {
        Scanner scanner = new Scanner(System.in);
        generateSecretNumber();

        while (!this.gameOver) {
            System.out.print("Введите число из 4 разных цифр или слово 'хватит' для завершения игры: ");
            String guess = scanner.nextLine();

            if (guess.equalsIgnoreCase("хватит")) {
                System.out.printf("Игра завершена по команде игрока. Устал играть, %s.%n", guess);
                break;
            }

            try {
                if (guess.length() > 7) {
                    throw new InputException("Длина введенного числа больше 7");
                }
            } catch (InputException e) {
                System.out.println("Ошибка: " + e.getMessage());
                continue;
            }

            checkGuess(guess);
        }
    }

    public void saveGameResults(String lastGuess) {
        this.gameNumber = getLastGameNumber() + 1;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        LocalDateTime timestamp = LocalDateTime.now();
        String formattedTimestamp = timestamp.format(formatter);
        String fileName = "game_results.txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.write(String.format("Game №%d %s Загаданная число %s%n", this.gameNumber, formattedTimestamp, this.secretNumber));

            for (int i = 0; i < this.guesses.size(); i++) {
                String guess = this.guesses.get(i);
                writer.write(String.format("\tПопытка %d: %s%n", (i + 1), guess));
            }

            String attemptsPhrase = formatAttemptsPhrase(this.attempts);
            writer.write(String.format("\tЗагаданное число: %s. Вы угадали число за %s%n", lastGuess, attemptsPhrase));
            writer.write("\t...\n");
        }   catch (IOException e) {
                System.out.println("Ошибка при записи результатов игры в файл.");
            }
    }

    public int getLastGameNumber() {
        File file = new File("game_results.txt");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("Не удалось создать файл game_results.txt.");
                return 0;
            }
        }

        try (Scanner scanner = new Scanner(file)) {
            int gameNumber = 0;

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("Game №")) {
                    gameNumber = Integer.parseInt(line.split("№")[1].split(" ")[0]);
                }
            }

            return gameNumber;
        } catch (IOException e) {
            System.out.println("Файл game_results.txt не найден.");
            return 0;
        }
    }
}