package com. gym.controllers.admin;

import com.gym.dao.SalaryDAO;
import com.gym.models.Admin;
import com.gym.models.Salary;
import com. gym.services.Session;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections. ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx. scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

public class SalaryManagementController {

    @FXML private ComboBox<String> monthComboBox;
    @FXML private ComboBox<Integer> yearComboBox;
    @FXML private Button loadSalariesButton;
    @FXML private Button generateSalariesButton;
    @FXML private Label totalPendingLabel;
    @FXML private Label totalPaidLabel;
    @FXML private TableView<Salary> salariesTable;
    @FXML private TableColumn<Salary, Integer> idColumn;
    @FXML private TableColumn<Salary, String> trainerNameColumn;
    @FXML private TableColumn<Salary, String> monthYearColumn;
    @FXML private TableColumn<Salary, String> baseSalaryColumn;
    @FXML private TableColumn<Salary, String> bonusColumn;
    @FXML private TableColumn<Salary, String> deductionsColumn;
    @FXML private TableColumn<Salary, String> netSalaryColumn;
    @FXML private TableColumn<Salary, String> statusColumn;
    @FXML private TableColumn<Salary, Void> actionsColumn;

    private SalaryDAO salaryDAO;
    private ObservableList<Salary> salariesList;

    public SalaryManagementController() {
        try {
            salaryDAO = new SalaryDAO();
        } catch (Exception e) {
            System.err.println("Error creating SalaryDAO: " + e. getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        System.out.println("SalaryManagementController initialized");

        try {
            setupMonthComboBox();
            setupYearComboBox();
            setupTableColumns();
            setupEventHandlers();
            setCurrentMonthYear();
            loadSummary();
        } catch (Exception e) {
            System.err.println("Error during initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupMonthComboBox() {
        ObservableList<String> months = FXCollections.observableArrayList(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        );
        monthComboBox.setItems(months);
    }

    private void setupYearComboBox() {
        ObservableList<Integer> years = FXCollections.observableArrayList();
        int currentYear = LocalDate.now().getYear();

        for (int i = currentYear - 2; i <= currentYear + 1; i++) {
            years. add(i);
        }

        yearComboBox. setItems(years);
        yearComboBox.setValue(currentYear);
    }

    private void setCurrentMonthYear() {
        LocalDate now = LocalDate.now();
        String currentMonth = now.getMonth().toString();
        currentMonth = currentMonth.charAt(0) + currentMonth.substring(1).toLowerCase();

        monthComboBox.setValue(currentMonth);
        yearComboBox. setValue(now.getYear());
    }

    private void setupTableColumns() {
        idColumn. setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getSalaryId()).asObject());

        trainerNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTrainerName()));

        monthYearColumn.setCellValueFactory(cellData -> {
            Salary salary = cellData.getValue();
            String monthName = Month. of(salary.getMonth()).toString();
            monthName = monthName.charAt(0) + monthName.substring(1).toLowerCase();
            return new SimpleStringProperty(monthName + " " + salary.getYear());
        });

        baseSalaryColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("%.2f", cellData.getValue().getBaseSalary())));

        bonusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("%.2f", cellData. getValue().getBonus())));

        deductionsColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("%.2f", cellData. getValue().getDeductions())));

        netSalaryColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String. format("%.2f", cellData.getValue().getNetSalary())));

        statusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus()));

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button payButton = new Button("Pay");
            private final Button editButton = new Button("Edit");
            private final Button viewButton = new Button("Details");
            private final HBox buttons = new HBox(5, payButton, editButton, viewButton);

            {
                payButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 5 10;");
                editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 5 10;");
                viewButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 5 10;");

                payButton.setOnAction(e -> {
                    Salary salary = getTableView().getItems().get(getIndex());
                    handlePaySalary(salary);
                });

                editButton.setOnAction(e -> {
                    Salary salary = getTableView().getItems().get(getIndex());
                    handleEditSalary(salary);
                });

                viewButton.setOnAction(e -> {
                    Salary salary = getTableView().getItems().get(getIndex());
                    handleViewDetails(salary);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super. updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Salary salary = getTableView().getItems().get(getIndex());
                    payButton.setDisable("PAID". equals(salary.getStatus()));
                    setGraphic(buttons);
                }
            }
        });
    }

    private void setupEventHandlers() {
        if (loadSalariesButton != null) {
            loadSalariesButton.setOnAction(e -> handleLoadSalaries());
        }

        if (generateSalariesButton != null) {
            generateSalariesButton.setOnAction(e -> handleGenerateSalaries());
        }
    }

    private void handleLoadSalaries() {
        if (monthComboBox.getValue() == null || yearComboBox.getValue() == null) {
            showAlert("Error", "Please select month and year", Alert.AlertType.WARNING);
            return;
        }

        int month = getMonthNumber(monthComboBox.getValue());
        int year = yearComboBox.getValue();
        loadSalariesForMonth(month, year);
    }

    private void loadSalariesForMonth(int month, int year) {
        try {
            List<Salary> salaries = salaryDAO.getSalariesForMonth(month, year);
            salariesList = FXCollections.observableArrayList(salaries);
            salariesTable.setItems(salariesList);

            double totalPaid = salaryDAO.getTotalPaidSalaries(month, year);
            totalPaidLabel.setText(String.format("%.2f", totalPaid));  // LINE 197 - FIXED

            System.out.println("Loaded " + salaries.size() + " salary records");
        } catch (Exception e) {
            System.err. println("Error loading salaries:  " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to load salaries:  " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleGenerateSalaries() {
        if (monthComboBox.getValue() == null || yearComboBox.getValue() == null) {
            showAlert("Error", "Please select month and year", Alert.AlertType.WARNING);
            return;
        }

        int month = getMonthNumber(monthComboBox.getValue());
        int year = yearComboBox.getValue();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Generate Salaries");
        confirm.setHeaderText("Generate for " + monthComboBox.getValue() + " " + year);
        confirm.setContentText("Create salary records for all active trainers?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            generateSalariesForMonth(month, year);
        }
    }

    private void generateSalariesForMonth(int month, int year) {
        Admin admin = (Admin) Session.getInstance().getCurrentUser();
        if (admin == null) {
            showAlert("Error", "Session expired", Alert.AlertType.ERROR);
            return;
        }

        boolean success = salaryDAO.generateMonthlySalaries(month, year, admin.getAdminId());

        if (success) {
            showAlert("Success", "Salaries generated successfully", Alert.AlertType.INFORMATION);
            loadSalariesForMonth(month, year);
            loadSummary();
        } else {
            showAlert("Error", "Failed to generate salaries", Alert.AlertType.ERROR);
        }
    }

    private void handlePaySalary(Salary salary) {
        if ("PAID".equals(salary.getStatus())) {
            showAlert("Info", "Already paid", Alert.AlertType. INFORMATION);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Mark as Paid");
        confirm.setContentText("Mark salary as PAID for " + salary.getTrainerName() + "?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result. isPresent() && result.get() == ButtonType.OK) {
            markAsPaid(salary. getSalaryId());
        }
    }

    private void markAsPaid(int salaryId) {
        boolean success = salaryDAO.updateSalaryStatus(salaryId, "PAID", LocalDate.now());

        if (success) {
            showAlert("Success", "Salary marked as paid", Alert.AlertType.INFORMATION);

            if (monthComboBox.getValue() != null && yearComboBox.getValue() != null) {
                int month = getMonthNumber(monthComboBox.getValue());
                int year = yearComboBox. getValue();
                loadSalariesForMonth(month, year);
            }
            loadSummary();
        } else {
            showAlert("Error", "Failed to update", Alert.AlertType.ERROR);
        }
    }

    private void handleEditSalary(Salary salary) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Salary");
        dialog.setHeaderText("Edit for " + salary.getTrainerName());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField bonusField = new TextField(String. valueOf(salary.getBonus()));
        TextField deductionsField = new TextField(String.valueOf(salary. getDeductions()));

        grid.add(new Label("Base Salary:"), 0, 0);
        grid.add(new Label(String.format("%.2f", salary.getBaseSalary())), 1, 0);
        grid.add(new Label("Bonus: "), 0, 1);
        grid.add(bonusField, 1, 1);
        grid.add(new Label("Deductions:"), 0, 2);
        grid.add(deductionsField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType. CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                double bonus = Double.parseDouble(bonusField.getText());
                double deductions = Double.parseDouble(deductionsField.getText());

                boolean success = salaryDAO.updateSalaryDetails(salary.getSalaryId(), bonus, deductions);

                if (success) {
                    showAlert("Success", "Salary updated", Alert.AlertType.INFORMATION);

                    if (monthComboBox.getValue() != null && yearComboBox.getValue() != null) {
                        int month = getMonthNumber(monthComboBox.getValue());
                        int year = yearComboBox.getValue();
                        loadSalariesForMonth(month, year);
                    }
                } else {
                    showAlert("Error", "Failed to update", Alert.AlertType.ERROR);
                }
            } catch (NumberFormatException e) {
                showAlert("Error", "Invalid numbers", Alert.AlertType.ERROR);
            }
        }
    }

    private void handleViewDetails(Salary salary) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Salary Details");
        alert.setHeaderText(salary.getTrainerName());

        String details = String.format(
                "Salary ID: %d\n" +
                        "Trainer ID: %d\n\n" +
                        "Base Salary: %.2f\n" +
                        "Bonus: %.2f\n" +
                        "Deductions: %.2f\n" +
                        "Net Salary: %.2f\n\n" +
                        "Status: %s\n" +
                        "Payment Date: %s",
                salary.getSalaryId(),
                salary.getTrainerId(),
                salary.getBaseSalary(),
                salary.getBonus(),
                salary.getDeductions(),
                salary.getNetSalary(),
                salary.getStatus(),
                salary.getPaymentDate() != null ? salary.getPaymentDate().toString() : "N/A"
        );

        alert.setContentText(details);
        alert.showAndWait();
    }

    private void loadSummary() {
        try {
            double totalPending = salaryDAO.getTotalPendingSalaries();
            totalPendingLabel.setText(String.format("%.2f", totalPending));

            if (monthComboBox.getValue() != null && yearComboBox.getValue() != null) {
                int month = getMonthNumber(monthComboBox.getValue());
                int year = yearComboBox.getValue();
                double totalPaid = salaryDAO. getTotalPaidSalaries(month, year);
                totalPaidLabel.setText(String.format("%.2f", totalPaid));
            }
        } catch (Exception e) {
            System.err.println("Error loading summary: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int getMonthNumber(String monthName) {
        return Month.valueOf(monthName.toUpperCase()).getValue();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}