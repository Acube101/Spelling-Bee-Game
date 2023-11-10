package com.example.test;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SpellingBeeGameGUI extends Application {
    private List<String> words;
    private char specialLetter;
    private int points;
    private TextArea userInputArea;
    private Connection connection;
    private Set<String> enteredWords = new HashSet<>();

    public SpellingBeeGameGUI() {
        words = new ArrayList<>();
        points = 0;
        specialLetter = 'y';
        try {
            connection = connect();
            loadWordsFromDatabase();
        } catch (SQLException e) {
            System.out.println("An error occurred while connecting to the database: " + e.getMessage());
        }
    }

    private Connection connect() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/Word_List";
        String username = "root";
        String password = "SRMPassword12345678-";
        return DriverManager.getConnection(url, username, password);
    }

    public void loadWordsFromDatabase() {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT word FROM words");
            while (resultSet.next()) {
                String word = resultSet.getString("word");
                words.add(word);
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while loading words from the database: " + e.getMessage());
        }
    }

    public boolean isValidWord(String word) {
        String lowerCaseWord = word.toLowerCase(); // Convert user input to lowercase
        boolean isValid = words.stream().anyMatch(w -> w.toLowerCase().equals(lowerCaseWord));
        if (isValid && !enteredWords.contains(lowerCaseWord)) {
            enteredWords.add(lowerCaseWord);
            return true;
        }
        return false;
    }

    private boolean isPangram(String word) {
        Set<Character> uniqueLetters = new HashSet<>();
        for (char c : word.toCharArray()) {
            uniqueLetters.add(c);
        }
        return uniqueLetters.size() == 7 && uniqueLetters.contains(specialLetter);
    }

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox();
        root.setPadding(new Insets(10));
        root.setSpacing(10);

        Label welcomeLabel = new Label("Welcome to the Spelling Bee Game!");
        Label specialLetterLabel = new Label("The special letter is: " + specialLetter);
        Label availableLettersLabel = new Label("The available letters are: y, a, f, i, l ,m ,r");
        Label pointsLabel = new Label("Points: " + points);

        userInputArea = new TextArea();
        userInputArea.setPromptText("Enter a word...");

        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> {
            String userInput = userInputArea.getText();
            if (userInput.equalsIgnoreCase("exit4")) {
                primaryStage.close();
            } else {
                if (isValidWord(userInput)) {
                    userInputArea.clear();
                    if (isPangram(userInput)) {
                        points += 7;
                        pointsLabel.setText("Points: " + points + " (Pangram Bonus: +7)");
                        System.out.println("Congratulations! You found a pangram!");
                    } else {
                        points += Math.max(0, userInput.length() - 3);
                        pointsLabel.setText("Points: " + points);
                        System.out.println("Points updated. Current points: " + points);
                    }
                } else {
                    userInputArea.clear();
                    System.out.println("Invalid word. Try again!");
                }
            }
        });

        root.getChildren().addAll(welcomeLabel, specialLetterLabel, availableLettersLabel, userInputArea, submitButton, pointsLabel);

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Spelling Bee Game");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
