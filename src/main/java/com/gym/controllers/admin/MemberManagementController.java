package com.gym.controllers.admin;

import com.gym.dao.MemberDAO;
import com.gym.models.Member;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MemberManagementController {

    @FXML private TableView<Member> membersTable;
    @FXML private TableColumn<Member, Integer> idColumn;
    @FXML private TableColumn<Member, String> nameColumn;
    @FXML private TableColumn<Member, String> emailColumn;
    @FXML private TableColumn<Member, String> phoneColumn;
    @FXML private TableColumn<Member, String> membershipColumn;
    @FXML private TableColumn<Member, LocalDate> joiningDateColumn;
    @FXML private TableColumn<Member, LocalDate> expiryDateColumn;
    @FXML private TableColumn<Member, String> statusColumn;
    @FXML private TableColumn<Member, String> paymentReceivedColumn;
    @FXML private TableColumn<Member, String> paymentPendingColumn;
    @FXML private TableColumn<Member, Void> actionsColumn;

    @FXML private TextField searchField;
    @FXML private Button addMemberBtn;
    @FXML private Button refreshBtn;
    @FXML private Label totalMembersLabel;

    private MemberDAO memberDAO;
    private ObservableList<Member> memberList;
    private FilteredList<Member> filteredMembers;

    // Store payment status for current month: memberId -> true if RECEIVED, false or null if PENDING
    private Map<Integer, Boolean> paymentStatus;
    private YearMonth currentMonth;

    public MemberManagementController() {
        memberDAO = new MemberDAO();
        memberList = FXCollections.observableArrayList();
        paymentStatus = new HashMap<>();
        currentMonth = YearMonth.now();
    }

    @FXML
    public void initialize() {
        System.out.println("MemberManagementController initialized");
        setupTableColumns();
        loadMembers();
        setupSearchFilter();
        setupEventHandlers();
        checkAndResetMonthlyPayments();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("memberId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        membershipColumn.setCellValueFactory(new PropertyValueFactory<>("membershipType"));
        joiningDateColumn.setCellValueFactory(new PropertyValueFactory<>("membershipStart"));
        expiryDateColumn.setCellValueFactory(new PropertyValueFactory<>("membershipEnd"));

        statusColumn.setCellValueFactory(cellData -> {
            LocalDate expiryDate = cellData.getValue().getMembershipEnd();
            if (expiryDate == null) {
                return new SimpleStringProperty("Inactive");
            }
            return new SimpleStringProperty(
                expiryDate.isAfter(LocalDate.now()) ? "Active" : "Expired"
            );
        });

        setupPaymentColumns();
        setupActionsColumn();
    }

    private void setupPaymentColumns() {
        // Payment Pending Column - Shows ⏳ by default for all members
        paymentPendingColumn.setCellValueFactory(cellData -> {
            int memberId = cellData.getValue().getMemberId();
            Boolean received = paymentStatus.get(memberId);
            // Show pending (⏳) if not received, empty if received
            return new SimpleStringProperty(
                (received == null || !received) ? "⏳" : ""
            );
        });

        paymentPendingColumn.setCellFactory(column -> {
            TableCell<Member, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        setStyle("-fx-alignment: CENTER; -fx-font-size: 20px; -fx-text-fill: orange; -fx-font-weight: bold; -fx-cursor: hand;");
                    }
                }
            };

            // Right-click to mark as received
            ContextMenu contextMenu = new ContextMenu();
            MenuItem markReceived = new MenuItem("✓ Mark Payment Received");

            markReceived.setOnAction(event -> {
                if (!cell.isEmpty()) {
                    Member member = cell.getTableView().getItems().get(cell.getIndex());
                    // Mark as received
                    paymentStatus.put(member.getMemberId(), true);
                    membersTable.refresh();
                    System.out.println("✓ Payment marked as received for: " + member.getFullName());
                }
            });

            contextMenu.getItems().add(markReceived);
            cell.setContextMenu(contextMenu);

            return cell;
        });

        // Payment Received Column - Shows ✓ when payment is marked as received
        paymentReceivedColumn.setCellValueFactory(cellData -> {
            int memberId = cellData.getValue().getMemberId();
            Boolean received = paymentStatus.get(memberId);
            // Show checkmark (✓) only if received is true
            return new SimpleStringProperty(
                (received != null && received) ? "✓" : ""
            );
        });

        paymentReceivedColumn.setCellFactory(column -> {
            TableCell<Member, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        setStyle("-fx-alignment: CENTER; -fx-font-size: 20px; -fx-text-fill: green; -fx-font-weight: bold; -fx-cursor: hand;");
                    }
                }
            };

            // Right-click to clear received status (back to pending)
            ContextMenu contextMenu = new ContextMenu();
            MenuItem clearReceived = new MenuItem("⏳ Mark as Pending");

            clearReceived.setOnAction(event -> {
                if (!cell.isEmpty()) {
                    Member member = cell.getTableView().getItems().get(cell.getIndex());
                    // Mark as pending (not received)
                    paymentStatus.put(member.getMemberId(), false);
                    membersTable.refresh();
                    System.out.println("⏳ Payment marked as pending for: " + member.getFullName());
                }
            });

            contextMenu.getItems().add(clearReceived);
            cell.setContextMenu(contextMenu);

            return cell;
        });
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");
            private final HBox actionBox = new HBox(5, deleteBtn);

            {
                deleteBtn.getStyleClass().add("action-button-delete");

                deleteBtn.setOnAction(event -> {
                    Member member = getTableView().getItems().get(getIndex());
                    deleteMember(member);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionBox);
            }
        });
    }

    private void checkAndResetMonthlyPayments() {
        YearMonth now = YearMonth.now();
        if (!now.equals(currentMonth)) {
            // New month started, reset all payment statuses to pending (false)
            paymentStatus.clear();
            currentMonth = now;
            System.out.println("🔄 New month detected (" + currentMonth + "). All payments reset to PENDING.");
        }
    }

    private void loadMembers() {
        try {
            List<Member> members = memberDAO.getAllMembers();
            memberList.clear();
            memberList.addAll(members);

            filteredMembers = new FilteredList<>(memberList, p -> true);
            membersTable.setItems(filteredMembers);

            updateTotalMembersLabel();
            System.out.println("✅ Loaded " + members.size() + " members");
        } catch (Exception e) {
            System.err.println("❌ Error loading members: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Could not load members: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void setupSearchFilter() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredMembers.setPredicate(member -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (member.getFullName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (member.getEmail().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (member.getPhone().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (member.getMembershipType().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                return false;
            });
            updateTotalMembersLabel();
        });
    }

    private void setupEventHandlers() {
        addMemberBtn.setOnAction(event -> openMemberRegistration());
        refreshBtn.setOnAction(event -> {
            checkAndResetMonthlyPayments();
            loadMembers();
        });
    }

    private void openMemberRegistration() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/member_registration.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add New Member");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadMembers();
        } catch (IOException e) {
            System.err.println("❌ Error opening member registration: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Could not open member registration: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void viewMemberDetails(Member member) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Member Details");
        alert.setHeaderText("Member Information");

        String details = String.format(
            "ID: %d\n" +
            "Name: %s\n" +
            "Email: %s\n" +
            "Phone: %s\n" +
            "Address: %s\n" +
            "Date of Birth: %s\n" +
            "Gender: %s\n" +
            "Membership Type: %s\n" +
            "Joining Date: %s\n" +
            "Expiry Date: %s\n" +
            "Status: %s",
            member.getMemberId(),
            member.getFullName(),
            member.getEmail(),
            member.getPhone(),
            member.getDateOfBirth(),
            member.getGender(),
            member.getMembershipType(),
            member.getMembershipStart(),
            member.getMembershipEnd(),
            member.getMembershipEnd().isAfter(LocalDate.now()) ? "Active" : "Expired"
        );

        alert.setContentText(details);
        alert.showAndWait();
    }

    private void editMember(Member member) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/member_registration.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Edit Member - " + member.getFullName());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadMembers();
        } catch (IOException e) {
            System.err.println("❌ Error opening edit member form: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Could not open edit form: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void deleteMember(Member member) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Member");
        confirmAlert.setContentText("Are you sure you want to delete " + member.getFullName() + "?\nThis action cannot be undone.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean deleted = memberDAO.deleteMember(member.getMemberId());
                if (deleted) {
                    // Remove from payment tracking
                    paymentStatus.remove(member.getMemberId());

                    showAlert("Success", "Member deleted successfully!", Alert.AlertType.INFORMATION);
                    loadMembers();
                } else {
                    showAlert("Error", "Could not delete member.", Alert.AlertType.ERROR);
                }
            } catch (Exception e) {
                System.err.println("❌ Error deleting member: " + e.getMessage());
                e.printStackTrace();
                showAlert("Error", "Error deleting member: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void updateTotalMembersLabel() {
        int total = filteredMembers != null ? filteredMembers.size() : memberList.size();
        totalMembersLabel.setText("Total Members: " + total);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

