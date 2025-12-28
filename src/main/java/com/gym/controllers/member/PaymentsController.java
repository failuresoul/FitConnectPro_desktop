package com.gym.controllers.member;

import com.gym.dao.PaymentDAO;
import com.gym.models.Member;
import com.gym.models.Payment;
import com.gym.services.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class PaymentsController {

    @FXML private Button backButton;
    @FXML private VBox paymentsContainer;
    @FXML private Label totalDueLabel;

    private PaymentDAO paymentDAO;
    private Member currentMember;

    @FXML
    public void initialize() {
        paymentDAO = new PaymentDAO();
        currentMember = (Member) Session.getInstance().getCurrentUser();

        if (currentMember == null) {
            System.err.println("❌ No member session found!");
            return;
        }

        System.out.println("💰 Loading payments for Member ID: " + currentMember.getMemberId());
        loadPayments();
    }

    private void loadPayments() {
        try {
            paymentDAO.updateOverduePayments();
            List<Payment> payments = paymentDAO.getPendingPayments(
                currentMember.getMemberId(), "MEMBER");

            System.out.println("📊 Found " + payments.size() + " pending payments");

            paymentsContainer.getChildren().clear();

            if (payments.isEmpty()) {
                Label noPaymentsLabel = new Label("✅ No pending payments");
                noPaymentsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
                VBox emptyBox = new VBox(noPaymentsLabel);
                emptyBox.setAlignment(Pos.CENTER);
                emptyBox.setPadding(new Insets(50));
                paymentsContainer.getChildren().add(emptyBox);
                totalDueLabel.setText("$0.00");
                return;
            }

            double totalDue = 0;
            for (Payment payment : payments) {
                totalDue += payment.getAmount();
                VBox paymentCard = createPaymentCard(payment);
                paymentsContainer.getChildren().add(paymentCard);
            }

            totalDueLabel.setText(String.format("$%.2f", totalDue));

        } catch (Exception e) {
            System.err.println("❌ Error loading payments: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private VBox createPaymentCard(Payment payment) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 10; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label typeLabel = new Label("💳 " + payment.getPaymentType());
        typeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label statusLabel = new Label(payment.getStatus());
        statusLabel.setStyle("-fx-font-size: 12px; -fx-padding: 4 12; -fx-background-radius: 12; " +
                           "-fx-text-fill: white; -fx-background-color: " +
                           (payment.getStatus().equals("OVERDUE") ? "#e74c3c" : "#f39c12") + ";");

        header.getChildren().addAll(typeLabel, statusLabel);

        Label amountLabel = new Label(String.format("Amount: $%.2f", payment.getAmount()));
        amountLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");

        Label dueDateLabel = new Label("Due: " +
            payment.getDueDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        dueDateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        Label descLabel = new Label(payment.getDescription());
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #95a5a6; -fx-font-style: italic;");
        descLabel.setWrapText(true);

        Button payButton = new Button("Pay Now");
        payButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 14px; " +
                          "-fx-padding: 10 30; -fx-background-radius: 5; -fx-cursor: hand;");
        payButton.setOnAction(e -> handlePayment(payment));

        card.getChildren().addAll(header, amountLabel, dueDateLabel, descLabel, payButton);
        return card;
    }

    private void handlePayment(Payment payment) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Payment");
        confirmAlert.setHeaderText("Payment Confirmation");
        confirmAlert.setContentText(String.format(
            "Are you sure you want to pay $%.2f for %s?",
            payment.getAmount(), payment.getDescription()));

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                showPaymentMethodDialog(payment);
            }
        });
    }

    private void showPaymentMethodDialog(Payment payment) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Credit Card",
            "Credit Card", "Debit Card", "Cash", "Bank Transfer");
        dialog.setTitle("Payment Method");
        dialog.setHeaderText("Select Payment Method");
        dialog.setContentText("Choose payment method:");

        dialog.showAndWait().ifPresent(method -> {
            boolean success = paymentDAO.markPaymentAsPaid(payment.getPaymentId(), method);

            if (success) {
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Payment Successful");
                successAlert.setHeaderText("✅ Payment Completed");
                successAlert.setContentText(String.format(
                    "Your payment of $%.2f has been processed successfully via %s.",
                    payment.getAmount(), method));
                successAlert.showAndWait();

                loadPayments();
            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Payment Failed");
                errorAlert.setHeaderText("❌ Payment Error");
                errorAlert.setContentText("Failed to process payment. Please try again.");
                errorAlert.showAndWait();
            }
        });
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/member_dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene currentScene = stage.getScene();
            currentScene.setRoot(root);
            stage.setTitle("FitConnectPro - Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
