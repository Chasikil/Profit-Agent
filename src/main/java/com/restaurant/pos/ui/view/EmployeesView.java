package com.restaurant.pos.ui.view;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

/**
 * Employees management view (UI-only).
 * Shows a table of employees with basic data and Add/Edit buttons.
 */
public class EmployeesView extends BorderPane {

    private final TableView<?> employeesTable; // type can be wired later (EmployeeModel / DTO)
    private Button addButton;
    private Button editButton;

    public EmployeesView() {
        setPadding(new Insets(16));
        getStyleClass().add("employees-view");

        // Title
        Label titleLabel = new Label("Employees");
        titleLabel.setFont(new Font(18));
        titleLabel.getStyleClass().add("view-title");

        // Employees table
        employeesTable = createEmployeesTable();

        // Bottom toolbar with buttons
        ToolBar toolbar = createToolbar();

        VBox centerBox = new VBox(8, titleLabel, employeesTable);
        VBox.setVgrow(employeesTable, Priority.ALWAYS);

        setCenter(centerBox);
        setBottom(toolbar);
    }

    @SuppressWarnings("unchecked")
    private TableView<?> createEmployeesTable() {
        TableView<Object> table = new TableView<>();
        table.getStyleClass().add("employees-table");

        TableColumn<Object, String> nameCol = new TableColumn<>("Name");
        nameCol.setPrefWidth(200);

        TableColumn<Object, String> roleCol = new TableColumn<>("Role");
        roleCol.setPrefWidth(120);

        TableColumn<Object, String> hoursCol = new TableColumn<>("Hours");
        hoursCol.setPrefWidth(100);

        TableColumn<Object, String> rateCol = new TableColumn<>("Rate");
        rateCol.setPrefWidth(100);

        table.getColumns().addAll(nameCol, roleCol, hoursCol, rateCol);
        return table;
    }

    private ToolBar createToolbar() {
        addButton = new Button("Add");
        addButton.setDisable(false); // logic will be wired later

        editButton = new Button("Edit");
        editButton.setDisable(false); // logic will be wired later

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        ToolBar toolbar = new ToolBar();
        toolbar.getItems().addAll(spacer, addButton, editButton);
        return toolbar;
    }
}

