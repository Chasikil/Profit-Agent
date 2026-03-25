package com.restaurant.pos.ui.view;

import com.restaurant.pos.ui.controller.LoginController;
import com.restaurant.pos.auth.AuthenticationException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * First screen at startup. Username, password, Login button, error label.
 * On successful login the callback opens MainDashboard.
 */
public class LoginView extends VBox {

    private final LoginController loginController;
    private Label errorLabel;

    public LoginView(LoginController loginController) {
        this.loginController = loginController;
        initialize();
    }

    private void initialize() {
        setPadding(new Insets(32));
        setSpacing(16);
        setAlignment(Pos.CENTER);

        VBox card = new VBox();
        card.setPadding(new Insets(24));
        card.setSpacing(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("login-card");

        Label title = new Label("Profit Agent");
        title.getStyleClass().add("login-title");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Логин");
        usernameField.setPrefWidth(220);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Пароль");
        passwordField.setPrefWidth(220);

        errorLabel = new Label();
        errorLabel.getStyleClass().add("login-error");
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(220);

        Button loginButton = new Button("Войти");
        loginButton.getStyleClass().add("login-button");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setOnAction(e -> {
            errorLabel.setText("");
            String username = usernameField.getText();
            String password = passwordField.getText();
            try {
                loginController.handleLogin(username, password);
            } catch (AuthenticationException ex) {
                String msg = ex.getMessage();
                errorLabel.setText(msg != null ? msg : "Ошибка входа.");
            }
        });

        card.getChildren().addAll(title, usernameField, passwordField, errorLabel, loginButton);
        getChildren().add(card);
    }
}
