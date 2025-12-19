package com.gym.controllers.admin;

import com.gym.dao.SalaryDAO;
import com.gym.dao.TrainerDAO;
import com.gym. models. Salary;
import com.gym.models. Trainer;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections. ObservableList;
import javafx.fxml.FXML;
import javafx.scene. chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene. control.*;

import java.time.LocalDate;
import java. time.Month;
import java.util.*;

public class SalaryReportsController {

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> trainerComboBox;
    @FXML private Button generateReportButton;
    @FXML private Button exportPdfButton;

    @FXML private Label totalSalariesLabel;
    @FXML private Label averageSalaryLabel;
    @FXML private Label totalRecordsLabel;

    @FXML private LineChart<String, Number> salaryTrendsChart;

    @FXML private TableView<Salary> salariesTable;
    @FXML private TableColumn<Salary, String> trainerNameColumn;
    @FXML private TableColumn<Salary, String> monthYearColumn;
    @FXML private TableColumn<Salary, String> baseSalaryColumn;
    @FXML private TableColumn<Salary, String> bonusColumn;
    @FXML private TableColumn<Salary, String> deductionsColumn;
    @FXML private TableColumn<Salary, String> netSalaryColumn;
    @FXML private TableColumn<Salary, String> statusColumn;

    private SalaryDAO salaryDAO;
    private TrainerDAO trainerDAO;
    private Map<String, Integer> trainerNameToIdMap;

    public SalaryReportsController() {
        salaryDAO = new SalaryDAO();
        trainerDAO = new TrainerDAO();
        trainerNameToIdMap = new HashMap<>();
    }

    @FXML
    public void initialize() {
        System.out.println("SalaryReportsController initialized");

        setupDatePickers();
        setupTrainerComboBox();
        setupTableColumns();
        setupEventHandlers();
    }

    private void setupDatePickers() {
        LocalDate now = LocalDate.now();
        startDatePicker.setValue(now. withDayOfMonth(1));
        endDatePicker.setValue(now);
    }

    private void setupTrainerComboBox() {
        try {
            List<Trainer> trainers = trainerDAO.getAllTrainers();

            ObservableList<String> trainerNames = FXCollections.observableArrayList();
            trainerNames.add("All Trainers");

            trainerNameToIdMap.clear();

            for (Trainer trainer : trainers) {
                trainerNames.add(trainer.getFullName());
                trainerNameToIdMap.put(trainer.getFullName(), trainer.getTrainerId());
            }

            trainerComboBox.setItems(trainerNames);
            trainerComboBox.setValue("All Trainers");

        } catch (Exception e) {
            System.err.println("Error loading trainers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupTableColumns() {
        trainerNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTrainerName()));

        monthYearColumn.setCellValueFactory(cellData -> {
            Salary salary = cellData.getValue();
            String monthName = Month.of(salary.getMonth()).toString();
            monthName = monthName.charAt(0) + monthName.substring(1).toLowerCase();
            return new SimpleStringProperty(monthName + " " + salary.getYear());
        });

        baseSalaryColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("%.2f", cellData.getValue().getBaseSalary())));

        bonusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("%.2f", cellData.getValue().getBonus())));

        deductionsColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("%.2f", cellData.getValue().getDeductions())));

        netSalaryColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("%.2f", cellData. getValue().getNetSalary())));

        statusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData. getValue().getStatus()));
    }

    private void setupEventHandlers() {
        if (generateReportButton != null) {
            generateReportButton.setOnAction(e -> handleGenerateReport());
        }

        if (exportPdfButton != null) {
            exportPdfButton.setOnAction(e -> handleExportPdf());
        }
    }

    private void handleGenerateReport() {
        try {
            if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
                showAlert("Validation Error", "Please select both start and end dates", Alert.AlertType.WARNING);
                return;
            }

            LocalDate start = startDatePicker.getValue();
            LocalDate end = endDatePicker.getValue();

            if (start.isAfter(end)) {
                showAlert("Validation Error", "Start date must be before end date", Alert.AlertType.WARNING);
                return;
            }

            List<Salary> salaries;
            String selectedTrainer = trainerComboBox.getValue();

            if ("All Trainers".equals(selectedTrainer)) {
                salaries = salaryDAO.getSalariesByDateRange(start, end);
            } else {
                Integer trainerId = trainerNameToIdMap.get(selectedTrainer);
                if (trainerId != null) {
                    salaries = salaryDAO.getSalariesByTrainer(trainerId, start, end);
                } else {
                    salaries = new ArrayList<>();
                }
            }

            displayReport(salaries);
            updateSummary(salaries);
            updateChart(salaries);

        } catch (Exception e) {
            System.err.println("Error generating report: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to generate report:  " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void displayReport(List<Salary> salaries) {
        ObservableList<Salary> salariesList = FXCollections.observableArrayList(salaries);
        salariesTable.setItems(salariesList);
    }

    private void updateSummary(List<Salary> salaries) {
        double totalSalaries = salaries.stream()
                .mapToDouble(Salary::getNetSalary)
                .sum();

        double averageSalary = salaries.isEmpty() ? 0 : totalSalaries / salaries.size();

        totalSalariesLabel.setText(String. format("%.2f", totalSalaries));
        averageSalaryLabel.setText(String.format("%.2f", averageSalary));
        totalRecordsLabel.setText(String.valueOf(salaries.size()));
    }

    private void updateChart(List<Salary> salaries) {
        salaryTrendsChart.getData().clear();

        Map<String, Double> monthlyTotals = new TreeMap<>();

        for (Salary salary : salaries) {
            String monthYear = Month.of(salary.getMonth()).toString().substring(0, 3) + " " + salary.getYear();
            monthlyTotals.merge(monthYear, salary.getNetSalary(), Double::sum);
        }

        XYChart.Series<String, Number> series = new XYChart. Series<>();
        series.setName("Total Salaries");

        for (Map.Entry<String, Double> entry : monthlyTotals.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        salaryTrendsChart.getData().add(series);
    }

    private void handleExportPdf() {
        showAlert("Export PDF", "PDF export feature coming soon!", Alert.AlertType. INFORMATION);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}