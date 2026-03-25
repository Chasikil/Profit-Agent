package com.restaurant.pos.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

public class HeaderBar extends HBox {

    private final Label titleLabel;
    private final Label userLabel;
    private final Button logoutButton;

    public HeaderBar() {
        setPadding(new Insets(12, 16, 12, 16));
        setSpacing(8);
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().add("header-bar");

        titleLabel = new Label("Dashboard");
        titleLabel.getStyleClass().add("header-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        userLabel = new Label("");
        userLabel.getStyleClass().add("header-user");

        logoutButton = new Button("Выход");
        logoutButton.getStyleClass().add("header-logout");

        getChildren().addAll(titleLabel, spacer, userLabel, logoutButton);
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    /** Set current user name and role in header. */
    public void setUserInfo(String username, String roleName) {
        if (username == null) {
            username = "";
        }
        if (roleName == null) {
            roleName = "";
        }
        userLabel.setText(username + " [" + roleName + "]");
    }

    /** Set logout action (called when Logout button is pressed). */
    public void setOnLogout(Runnable onLogout) {
        logoutButton.setOnAction(e -> {
            if (onLogout != null) {
                onLogout.run();
            }
        });
    }
}

