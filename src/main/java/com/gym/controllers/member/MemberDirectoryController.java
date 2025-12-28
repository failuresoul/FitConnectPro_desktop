package com.gym.controllers.member;

import com.gym.dao.SocialDAO;
import com.gym.models.Member;
import com.gym.services.Session;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class MemberDirectoryController {

    @FXML private Button backButton;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> goalFilterComboBox;
    @FXML private ComboBox<String> membershipFilterComboBox;
    @FXML private TableView<Member> memberTableView;
    @FXML private TableColumn<Member, String> photoColumn;
    @FXML private TableColumn<Member, String> nameColumn;
    @FXML private TableColumn<Member, String> emailColumn;
    @FXML private TableColumn<Member, String> goalColumn;
    @FXML private TableColumn<Member, String> membershipColumn;
    @FXML private TableColumn<Member, String> memberSinceColumn;
    @FXML private TableColumn<Member, Void> actionColumn;
    @FXML private Label totalMembersLabel;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Label pageInfoLabel;
    @FXML private ComboBox<Integer> pageSizeComboBox;

    private SocialDAO socialDAO;
    private Member currentMember;
    private ObservableList<Member> allMembers;
    private ObservableList<Member> filteredMembers;

    // Pagination
    private int currentPage = 1;
    private int pageSize = 10;
    private int totalPages = 1;

    @FXML
    public void initialize() {
        socialDAO = new SocialDAO();
        currentMember = (Member) Session.getInstance().getCurrentUser();

        setupComboBoxes();
        setupTableColumns();
        setupPagination();
        loadMembers();
    }

    private void setupComboBoxes() {
        goalFilterComboBox.setItems(FXCollections.observableArrayList(
            "All Goals",
            "Weight Loss",
            "Muscle Gain",
            "General Fitness",
            "Endurance",
            "Flexibility"
        ));
        goalFilterComboBox.setValue("All Goals");

        membershipFilterComboBox.setItems(FXCollections.observableArrayList(
            "All Types",
            "Monthly",
            "Quarterly",
            "Annual",
            "Premium"
        ));
        membershipFilterComboBox.setValue("All Types");

        pageSizeComboBox.setItems(FXCollections.observableArrayList(10, 25, 50, 100));
        pageSizeComboBox.setValue(10);
    }

    private void setupTableColumns() {
        // Photo column
        photoColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Circle circle = new Circle(20);
                    circle.setFill(Color.rgb(
                        (int) (Math.random() * 156 + 100),
                        (int) (Math.random() * 156 + 100),
                        (int) (Math.random() * 156 + 100)
                    ));
                    setGraphic(circle);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        photoColumn.setCellValueFactory(data -> new SimpleStringProperty(""));

        nameColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getFullName()));

        emailColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getEmail()));

        goalColumn.setCellValueFactory(data -> {
            String goal = data.getValue().getFitnessGoal();
            return new SimpleStringProperty(goal != null ? goal : "Not Set");
        });

        membershipColumn.setCellValueFactory(data -> {
            String type = data.getValue().getMembershipType();
            return new SimpleStringProperty(type != null ? type : "N/A");
        });

        memberSinceColumn.setCellValueFactory(data -> {
            if (data.getValue().getMembershipStart() != null) {
                String date = data.getValue().getMembershipStart()
                    .format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
                return new SimpleStringProperty(date);
            }
            return new SimpleStringProperty("N/A");
        });

        // Action column
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button viewButton = new Button("View Profile");
            private final Button addFriendButton = new Button("Add Friend");
            private final HBox buttons = new HBox(5, viewButton, addFriendButton);

            {
                viewButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");
                addFriendButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");
                buttons.setAlignment(Pos.CENTER);

                viewButton.setOnAction(event -> {
                    Member member = getTableView().getItems().get(getIndex());
                    viewMemberProfile(member);
                });

                addFriendButton.setOnAction(event -> {
                    Member member = getTableView().getItems().get(getIndex());
                    sendFriendRequest(member);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
    }

    private void setupPagination() {
        updatePaginationControls();
    }

    private void loadMembers() {
        try {
            int excludeMemberId = currentMember != null ? currentMember.getMemberId() : 0;
            List<Member> members = socialDAO.getAllActiveMembers(excludeMemberId);
            allMembers = FXCollections.observableArrayList(members);
            filteredMembers = FXCollections.observableArrayList(members);

            updateDisplay();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load members: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();

        if (keyword.isEmpty()) {
            filteredMembers = FXCollections.observableArrayList(allMembers);
        } else {
            try {
                int excludeMemberId = currentMember != null ? currentMember.getMemberId() : 0;
                List<Member> searchResults = socialDAO.searchMembers(keyword, excludeMemberId);
                filteredMembers = FXCollections.observableArrayList(searchResults);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Search failed: " + e.getMessage(), Alert.AlertType.ERROR);
                return;
            }
        }

        applyFilters();
        currentPage = 1;
        updateDisplay();
    }

    @FXML
    private void handleClearSearch() {
        searchField.clear();
        filteredMembers = FXCollections.observableArrayList(allMembers);
        applyFilters();
        currentPage = 1;
        updateDisplay();
    }

    @FXML
    private void handleFilter() {
        applyFilters();
        currentPage = 1;
        updateDisplay();
    }

    @FXML
    private void handleResetFilters() {
        goalFilterComboBox.setValue("All Goals");
        membershipFilterComboBox.setValue("All Types");
        searchField.clear();
        filteredMembers = FXCollections.observableArrayList(allMembers);
        currentPage = 1;
        updateDisplay();
    }

    private void applyFilters() {
        ObservableList<Member> filtered = FXCollections.observableArrayList(filteredMembers);

        String goalFilter = goalFilterComboBox.getValue();
        String membershipFilter = membershipFilterComboBox.getValue();

        if (goalFilter != null && !goalFilter.equals("All Goals")) {
            filtered.removeIf(m -> !goalFilter.equals(m.getFitnessGoal()));
        }

        if (membershipFilter != null && !membershipFilter.equals("All Types")) {
            filtered.removeIf(m -> !membershipFilter.equals(m.getMembershipType()));
        }

        filteredMembers = filtered;
    }

    @FXML
    private void handlePreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            updateDisplay();
        }
    }

    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            updateDisplay();
        }
    }

    @FXML
    private void handlePageSizeChange() {
        pageSize = pageSizeComboBox.getValue();
        currentPage = 1;
        updateDisplay();
    }

    private void updateDisplay() {
        int totalMembers = filteredMembers.size();
        totalPages = (int) Math.ceil((double) totalMembers / pageSize);

        if (totalPages == 0) totalPages = 1;
        if (currentPage > totalPages) currentPage = totalPages;

        int fromIndex = (currentPage - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalMembers);

        ObservableList<Member> pageItems = FXCollections.observableArrayList(
            filteredMembers.subList(fromIndex, toIndex)
        );

        memberTableView.setItems(pageItems);
        totalMembersLabel.setText("Total Members: " + totalMembers);

        updatePaginationControls();
    }

    private void updatePaginationControls() {
        pageInfoLabel.setText("Page " + currentPage + " of " + totalPages);
        prevPageButton.setDisable(currentPage <= 1);
        nextPageButton.setDisable(currentPage >= totalPages);
    }

    private void viewMemberProfile(Member member) {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Member Profile");
            dialog.setHeaderText(member.getFullName() + "'s Profile");

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new javafx.geometry.Insets(20));

            int row = 0;
            grid.add(new Label("Member ID:"), 0, row);
            grid.add(new Label(String.valueOf(member.getMemberId())), 1, row++);

            grid.add(new Label("Email:"), 0, row);
            grid.add(new Label(member.getEmail()), 1, row++);

            if (member.getPhone() != null && !member.getPhone().isEmpty()) {
                grid.add(new Label("Phone:"), 0, row);
                grid.add(new Label(member.getPhone()), 1, row++);
            }

            if (member.getGender() != null && !member.getGender().isEmpty()) {
                grid.add(new Label("Gender:"), 0, row);
                grid.add(new Label(member.getGender()), 1, row++);
            }

            if (member.getFitnessGoal() != null) {
                grid.add(new Label("Fitness Goal:"), 0, row);
                grid.add(new Label(member.getFitnessGoal()), 1, row++);
            }

            grid.add(new Label("Membership Type:"), 0, row);
            grid.add(new Label(member.getMembershipType() != null ? member.getMembershipType() : "N/A"), 1, row++);

            if (member.getMembershipStart() != null) {
                grid.add(new Label("Member Since:"), 0, row);
                grid.add(new Label(member.getMembershipStart().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))), 1, row++);
            }

            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load profile: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void sendFriendRequest(Member member) {
        try {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Send Friend Request");
            confirmation.setHeaderText(null);
            confirmation.setContentText("Send friend request to " + member.getFullName() + "?");

            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    boolean success = socialDAO.sendFriendRequest(
                        currentMember.getMemberId(),
                        member.getMemberId()
                    );

                    if (success) {
                        showAlert("Success", "Friend request sent to " + member.getFullName(), Alert.AlertType.INFORMATION);
                    } else {
                        showAlert("Info", "Friend request already exists or couldn't be sent", Alert.AlertType.INFORMATION);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to send friend request: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/member_dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Member Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

