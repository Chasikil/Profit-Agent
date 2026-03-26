package com.restaurant.pos.ui.view;

import com.restaurant.pos.domain.enums.Role;
import com.restaurant.pos.domain.model.Employee;
import com.restaurant.pos.service.EmployeeStorageService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.beans.property.SimpleStringProperty;

import java.math.BigDecimal;

/**
 * Employees management view (UI-only).
 * Shows a table of employees with basic data and Add/Edit buttons.
 */
public class EmployeesView extends BorderPane {

    private final EmployeeStorageService employeeStorageService;
    private final ObservableList<Employee> employeesData = FXCollections.observableArrayList();
    private final TableView<Employee> employeesTable;

    private final TextField firstNameField = new TextField();
    private final TextField lastNameField = new TextField();
    private final ComboBox<Role> roleComboBox = new ComboBox<>();
    private final Button addEmployeeButton = new Button("Добавить");

    public EmployeesView(EmployeeStorageService employeeStorageService) {
        this.employeeStorageService = employeeStorageService;

        setPadding(new Insets(16));
        getStyleClass().add("employees-view");

        // Title
        Label titleLabel = new Label("Employees");
        titleLabel.setFont(new Font(18));
        titleLabel.getStyleClass().add("view-title");

        // Employees table
        employeesTable = createEmployeesTable();

        VBox centerBox = new VBox(8, titleLabel, employeesTable);
        VBox.setVgrow(employeesTable, Priority.ALWAYS);

        HBox addForm = createAddForm();

        setCenter(centerBox);
        setBottom(addForm);

        refreshEmployees();
    }

    private TableView<Employee> createEmployeesTable() {
        TableView<Employee> table = new TableView<>();
        table.getStyleClass().add("employees-table");

        TableColumn<Employee, String> nameCol = new TableColumn<>("ФИО");
        nameCol.setPrefWidth(220);
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue() != null ? c.getValue().getFullName() : ""
        ));

        TableColumn<Employee, String> roleCol = new TableColumn<>("Должность");
        roleCol.setPrefWidth(140);
        roleCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue() != null && c.getValue().getRole() != null ? c.getValue().getRole().name() : ""
        ));

        TableColumn<Employee, String> hoursCol = new TableColumn<>("Часы");
        hoursCol.setPrefWidth(110);
        hoursCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue() != null ? String.valueOf(c.getValue().getWorkedHours()) : ""
        ));

        TableColumn<Employee, String> rateCol = new TableColumn<>("Ставка");
        rateCol.setPrefWidth(120);
        rateCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue() != null && c.getValue().getHourlyRate() != null ? c.getValue().getHourlyRate().toPlainString() : "0"
        ));

        table.getColumns().addAll(nameCol, roleCol, hoursCol, rateCol);
        table.setItems(employeesData);
        return table;
    }

    private HBox createAddForm() {
        firstNameField.setPromptText("Имя");
        lastNameField.setPromptText("Фамилия");

        roleComboBox.getItems().setAll(Role.values());
        roleComboBox.setPromptText("Должность");

        addEmployeeButton.setOnAction(e -> onAddEmployee());

        HBox form = new HBox(10);
        form.setPadding(new Insets(12, 0, 0, 0));
        form.setAlignment(Pos.CENTER);

        form.getChildren().addAll(
                new Label("Имя:"), firstNameField,
                new Label("Фамилия:"), lastNameField,
                new Label("Должность:"), roleComboBox,
                addEmployeeButton
        );
        return form;
    }

    private void refreshEmployees() {
        employeesData.clear();
        if (employeeStorageService == null) {
            return;
        }
        employeesData.addAll(employeeStorageService.getAllEmployees());
    }

    private void onAddEmployee() {
        if (employeeStorageService == null) {
            return;
        }

        String first = firstNameField.getText() != null ? firstNameField.getText().trim() : "";
        String last = lastNameField.getText() != null ? lastNameField.getText().trim() : "";
        Role role = roleComboBox.getValue();

        if (first.isEmpty() || last.isEmpty() || role == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText(null);
            alert.setContentText("Заполните имя, фамилию и должность.");
            alert.showAndWait();
            return;
        }

        long nextId = 1L;
        try {
            nextId = employeeStorageService.getEmployeesMap().keySet().stream()
                    .filter(id -> id != null)
                    .mapToLong(Long::longValue)
                    .max()
                    .orElse(0L) + 1L;
        } catch (Exception ignored) {
        }

        Employee emp = new Employee();
        emp.setId(nextId);
        emp.setName(first);
        emp.setFullName(first + " " + last);
        emp.setRole(role);
        emp.setActive(true);
        emp.setHourlyRate(BigDecimal.ZERO);
        emp.setWorkedHours(0.0);
        emp.setSalaryBalance(BigDecimal.ZERO);

        // Login/password not requested on this screen; set them null.
        employeeStorageService.saveEmployee(emp);

        refreshEmployees();

        firstNameField.clear();
        lastNameField.clear();
        roleComboBox.setValue(null);
    }
}

