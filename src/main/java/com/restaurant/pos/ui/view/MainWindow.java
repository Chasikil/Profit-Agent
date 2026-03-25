package com.restaurant.pos.ui.view;

import com.restaurant.pos.model.AppContext;
import com.restaurant.pos.ui.components.HeaderBar;
import com.restaurant.pos.ui.components.Sidebar;
import com.restaurant.pos.ui.controller.NavigationController;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

public class MainWindow {

    private final BorderPane root;
    private final Sidebar sidebar;
    private final HeaderBar headerBar;
    private final StackPane contentArea;

    private final NavigationController navigationController;

    public MainWindow(NavigationController navigationController) {
        this(navigationController, new AppContext());
    }

    public MainWindow(NavigationController navigationController, AppContext appContext) {
        this.navigationController = navigationController;
        this.root = new BorderPane();
        this.contentArea = new StackPane();
        this.sidebar = new Sidebar(section -> this.navigationController.onSidebarSectionSelected(section), appContext);
        this.headerBar = new HeaderBar();
        initializeLayout();
    }

    public Sidebar getSidebar() {
        return sidebar;
    }

    public HeaderBar getHeaderBar() {
        return headerBar;
    }

    private void initializeLayout() {
        root.setLeft(sidebar);
        root.setTop(headerBar);
        root.setCenter(contentArea);

        BorderPane.setMargin(contentArea, new Insets(8));

        root.getStyleClass().add("main-root");
        contentArea.getStyleClass().add("content-area");
    }

    public BorderPane getRoot() {
        return root;
    }

    public void setContent(Node node) {
        contentArea.getChildren().setAll(node);
    }

    /**
     * Helper method to switch center content to a given view.
     * Used by navigation/controller code when reacting to sidebar clicks.
     */
    public void showView(Node view) {
        setContent(view);
    }

    public void setHeaderTitle(String title) {
        headerBar.setTitle(title);
    }
}

