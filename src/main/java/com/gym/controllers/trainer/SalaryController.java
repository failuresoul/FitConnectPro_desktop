package com.gym.controllers.trainer;

import com.gym.dao.PaymentDAO;
import com.gym.models.Payment;
import com.gym.models.Trainer;
import com.gym.services.Session;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class SalaryController {

    @FXML private VBox salariesContainer;
    @FXML private Label totalPendingLabel;
    @FXML private Label nextPaymentLabel;
    @FXML private Label totalPaidLabel;
    @FXML private TabPane salaryTabPane;

    private PaymentDAO paymentDAO;
    private Trainer currentTrainer;

    @FXML
    public void initialize() {
        paymentDAO = new PaymentDAO();
        currentTrainer = (Trainer) Session.getInstance().getCurrentUser();

        if (currentTrainer == null) {
            showAlert("Error", "No trainer session found!", Alert.AlertType.ERROR);
            return;
        }

        System.out.println("💰 Loading salary for Trainer ID: " + currentTrainer.getTrainerId());

        loadPendingSalaries();
        loadPaidSalaries();

        System.out.println("✅ Salary controller initialized for Trainer ID: " + currentTrainer.getTrainerId());
    }

    private void loadPendingSalaries() {
        try {
            paymentDAO.updateOverduePayments();
            List<Payment> salaries = paymentDAO.getPendingPayments(
                currentTrainer.getTrainerId(), "TRAINER");

            System.out.println("📊 Found " + salaries.size() + " pending salary payments");

            salariesContainer.getChildren().clear();

            if (salaries.isEmpty()) {
                Label noSalariesLabel = new Label("✅ No pending salary payments");
                noSalariesLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
                VBox emptyBox = new VBox(noSalariesLabel);
                emptyBox.setAlignment(Pos.CENTER);
                emptyBox.setPadding(new Insets(50));
                salariesContainer.getChildren().add(emptyBox);
                totalPendingLabel.setText("$0.00");
                nextPaymentLabel.setText("No pending payments");
                return;
            }

            double totalPending = 0;
            Payment nextPayment = salaries.get(0);

            for (Payment salary : salaries) {
                totalPending += salary.getAmount();
                VBox salaryCard = createSalaryCard(salary, true);
                salariesContainer.getChildren().add(salaryCard);
            }

            totalPendingLabel.setText(String.format("$%.2f", totalPending));
            nextPaymentLabel.setText("Next: " +
                nextPayment.getDueDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));

        } catch (Exception e) {
            System.err.println("❌ Error loading pending salaries: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to load pending salaries: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadPaidSalaries() {
        try {
            List<Payment> paidSalaries = paymentDAO.getPaidPayments(
                currentTrainer.getTrainerId(), "TRAINER");

            System.out.println("📊 Found " + paidSalaries.size() + " paid salary payments");

            Tab paidTab = salaryTabPane.getTabs().get(1);
            ScrollPane scrollPane = (ScrollPane) paidTab.getContent();
            VBox paidContainer = (VBox) scrollPane.getContent();

            paidContainer.getChildren().clear();

            if (paidSalaries.isEmpty()) {
                Label noSalariesLabel = new Label("No salary payment history");
                noSalariesLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");
                VBox emptyBox = new VBox(noSalariesLabel);
                emptyBox.setAlignment(Pos.CENTER);
                emptyBox.setPadding(new Insets(50));
                paidContainer.getChildren().add(emptyBox);
                totalPaidLabel.setText("$0.00");
                return;
            }

            double totalPaid = 0;
            for (Payment salary : paidSalaries) {
                totalPaid += salary.getAmount();
                VBox salaryCard = createSalaryCard(salary, false);
                paidContainer.getChildren().add(salaryCard);
            }

            totalPaidLabel.setText(String.format("$%.2f", totalPaid));

        } catch (Exception e) {
            System.err.println("❌ Error loading paid salaries: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private VBox createSalaryCard(Payment salary, boolean isPending) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 10; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label typeLabel = new Label("💵 " + salary.getPaymentType());
        typeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label statusLabel = new Label(salary.getStatus());
        String statusColor = salary.getStatus().equals("PAID") ? "#27ae60" :
                            salary.getStatus().equals("OVERDUE") ? "#e74c3c" : "#3498db";
        statusLabel.setStyle("-fx-font-size: 12px; -fx-padding: 4 12; -fx-background-radius: 12; " +
                           "-fx-text-fill: white; -fx-background-color: " + statusColor + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(typeLabel, spacer, statusLabel);

        Label amountLabel = new Label(String.format("Amount: $%.2f", salary.getAmount()));
        amountLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        VBox dateBox = new VBox(5);
        Label dueDateLabel = new Label(isPending ? "Expected Date: " : "Due Date: " +
            salary.getDueDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        dueDateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");
        dateBox.getChildren().add(dueDateLabel);

        if (!isPending && salary.getPaidDate() != null) {
            Label paidDateLabel = new Label("Paid on: " +
                salary.getPaidDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
            paidDateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");

            Label methodLabel = new Label("Payment Method: " +
                (salary.getPaymentMethod() != null ? salary.getPaymentMethod() : "N/A"));
            methodLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #95a5a6;");

            dateBox.getChildren().addAll(paidDateLabel, methodLabel);
        }

        Label descLabel = new Label(salary.getDescription());
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #95a5a6; -fx-font-style: italic;");
        descLabel.setWrapText(true);

        if (isPending) {
            Button requestBtn = new Button("Request Payment");
            requestBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; " +
                              "-fx-padding: 10 25; -fx-background-radius: 5; -fx-cursor: hand;");
            requestBtn.setOnAction(e -> handleRequestPayment(salary));

            card.getChildren().addAll(header, amountLabel, dateBox, descLabel, requestBtn);
        } else {
            card.getChildren().addAll(header, amountLabel, dateBox, descLabel);
        }

        return card;
    }

    private void handleRequestPayment(Payment salary) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Request Payment");
        confirmAlert.setHeaderText("Request Salary Payment");
        confirmAlert.setContentText(String.format(
            "Send a payment request to admin for $%.2f?\n\n" +
            "This will notify the admin to process your salary payment.",
            salary.getAmount()));

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
                infoAlert.setTitle("Request Sent");
                infoAlert.setHeaderText("✅ Payment Request Sent");
                infoAlert.setContentText(
                    "Your payment request has been sent to the admin.\n" +
                    "You will be notified once the payment is processed.");
                infoAlert.showAndWait();

                System.out.println("💰 Payment request sent for Trainer ID: " +
                    currentTrainer.getTrainerId() + ", Amount: $" + salary.getAmount());
            }
        });
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
