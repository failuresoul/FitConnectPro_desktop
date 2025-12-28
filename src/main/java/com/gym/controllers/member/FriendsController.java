package com.gym.controllers.member;

import com.gym.dao.SocialDAO;
import com.gym.models.FriendRequest;
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
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class FriendsController {

    @FXML private Button backButton;
    @FXML private TabPane tabPane;
    @FXML private Label totalFriendsLabel;

    // My Friends Tab
    @FXML private TableView<Member> friendsTableView;
    @FXML private TableColumn<Member, String> friendPhotoColumn;
    @FXML private TableColumn<Member, String> friendNameColumn;
    @FXML private TableColumn<Member, String> friendEmailColumn;
    @FXML private TableColumn<Member, String> friendGoalColumn;
    @FXML private TableColumn<Member, String> friendSinceColumn;
    @FXML private TableColumn<Member, Void> friendActionColumn;

    // Received Requests Tab
    @FXML private Label receivedCountLabel;
    @FXML private TableView<FriendRequest> receivedRequestsTableView;
    @FXML private TableColumn<FriendRequest, String> receivedPhotoColumn;
    @FXML private TableColumn<FriendRequest, String> receivedNameColumn;
    @FXML private TableColumn<FriendRequest, String> receivedEmailColumn;
    @FXML private TableColumn<FriendRequest, String> receivedDateColumn;
    @FXML private TableColumn<FriendRequest, Void> receivedActionColumn;

    // Sent Requests Tab
    @FXML private Label sentCountLabel;
    @FXML private TableView<FriendRequest> sentRequestsTableView;
    @FXML private TableColumn<FriendRequest, String> sentPhotoColumn;
    @FXML private TableColumn<FriendRequest, String> sentNameColumn;
    @FXML private TableColumn<FriendRequest, String> sentEmailColumn;
    @FXML private TableColumn<FriendRequest, String> sentDateColumn;
    @FXML private TableColumn<FriendRequest, Void> sentActionColumn;

    private SocialDAO socialDAO;
    private Member currentMember;

    @FXML
    public void initialize() {
        socialDAO = new SocialDAO();
        currentMember = (Member) Session.getInstance().getCurrentUser();

        setupFriendsTable();
        setupReceivedRequestsTable();
        setupSentRequestsTable();

        loadAllData();
    }

    private void setupFriendsTable() {
        // Photo column
        friendPhotoColumn.setCellFactory(col -> createPhotoCell());
        friendPhotoColumn.setCellValueFactory(data -> new SimpleStringProperty(""));

        friendNameColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getFullName()));

        friendEmailColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getEmail()));

        friendGoalColumn.setCellValueFactory(data -> {
            String goal = data.getValue().getFitnessGoal();
            return new SimpleStringProperty(goal != null ? goal : "Not Set");
        });

        friendSinceColumn.setCellValueFactory(data ->
            new SimpleStringProperty("Recent")); // You can enhance this with actual date

        // Action column
        friendActionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button viewButton = new Button("View Progress");
            private final Button unfriendButton = new Button("Unfriend");
            private final HBox buttons = new HBox(5, viewButton, unfriendButton);

            {
                viewButton.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");
                unfriendButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");
                buttons.setAlignment(Pos.CENTER);

                viewButton.setOnAction(event -> {
                    Member friend = getTableView().getItems().get(getIndex());
                    viewFriendProgress(friend);
                });

                unfriendButton.setOnAction(event -> {
                    Member friend = getTableView().getItems().get(getIndex());
                    unfriend(friend);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
    }

    private void setupReceivedRequestsTable() {
        // Photo column
        receivedPhotoColumn.setCellFactory(col -> createPhotoCell());
        receivedPhotoColumn.setCellValueFactory(data -> new SimpleStringProperty(""));

        receivedNameColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getSenderName()));

        receivedEmailColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getSenderEmail()));

        receivedDateColumn.setCellValueFactory(data -> {
            if (data.getValue().getRequestDate() != null) {
                return new SimpleStringProperty(data.getValue().getRequestDate()
                    .format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
            }
            return new SimpleStringProperty("N/A");
        });

        // Action column
        receivedActionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button acceptButton = new Button("Accept");
            private final Button rejectButton = new Button("Reject");
            private final HBox buttons = new HBox(5, acceptButton, rejectButton);

            {
                acceptButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");
                rejectButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");
                buttons.setAlignment(Pos.CENTER);

                acceptButton.setOnAction(event -> {
                    FriendRequest request = getTableView().getItems().get(getIndex());
                    acceptRequest(request);
                });

                rejectButton.setOnAction(event -> {
                    FriendRequest request = getTableView().getItems().get(getIndex());
                    rejectRequest(request);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
    }

    private void setupSentRequestsTable() {
        // Photo column
        sentPhotoColumn.setCellFactory(col -> createPhotoCell());
        sentPhotoColumn.setCellValueFactory(data -> new SimpleStringProperty(""));

        sentNameColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getReceiverName()));

        sentEmailColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getReceiverEmail()));

        sentDateColumn.setCellValueFactory(data -> {
            if (data.getValue().getRequestDate() != null) {
                return new SimpleStringProperty(data.getValue().getRequestDate()
                    .format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
            }
            return new SimpleStringProperty("N/A");
        });

        // Action column
        sentActionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button cancelButton = new Button("Cancel Request");

            {
                cancelButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");

                cancelButton.setOnAction(event -> {
                    FriendRequest request = getTableView().getItems().get(getIndex());
                    cancelRequest(request);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : cancelButton);
            }
        });
    }

    private <T> TableCell<T, String> createPhotoCell() {
        return new TableCell<>() {
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
        };
    }

    private void loadAllData() {
        loadMyFriends();
        loadReceivedRequests();
        loadSentRequests();
    }

    @FXML
    private void loadMyFriends() {
        try {
            List<Member> friends = socialDAO.getMyFriends(currentMember.getMemberId());
            ObservableList<Member> friendsList = FXCollections.observableArrayList(friends);
            friendsTableView.setItems(friendsList);
            totalFriendsLabel.setText("Total Friends: " + friends.size());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load friends: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadReceivedRequests() {
        try {
            List<FriendRequest> requests = socialDAO.getReceivedRequests(currentMember.getMemberId());
            ObservableList<FriendRequest> requestsList = FXCollections.observableArrayList(requests);
            receivedRequestsTableView.setItems(requestsList);
            receivedCountLabel.setText(requests.size() + " pending");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load received requests: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadSentRequests() {
        try {
            List<FriendRequest> requests = socialDAO.getSentRequests(currentMember.getMemberId());
            ObservableList<FriendRequest> requestsList = FXCollections.observableArrayList(requests);
            sentRequestsTableView.setItems(requestsList);
            sentCountLabel.setText(requests.size() + " pending");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load sent requests: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void acceptRequest(FriendRequest request) {
        try {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Accept Friend Request");
            confirmation.setHeaderText(null);
            confirmation.setContentText("Accept friend request from " + request.getSenderName() + "?");

            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    boolean success = socialDAO.acceptFriendRequest(request.getRequestId());

                    if (success) {
                        showAlert("Success", "Friend request accepted!", Alert.AlertType.INFORMATION);
                        loadAllData();
                    } else {
                        showAlert("Error", "Failed to accept friend request", Alert.AlertType.ERROR);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to accept request: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void rejectRequest(FriendRequest request) {
        try {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Reject Friend Request");
            confirmation.setHeaderText(null);
            confirmation.setContentText("Reject friend request from " + request.getSenderName() + "?");

            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    boolean success = socialDAO.rejectFriendRequest(request.getRequestId());

                    if (success) {
                        showAlert("Success", "Friend request rejected", Alert.AlertType.INFORMATION);
                        loadAllData();
                    } else {
                        showAlert("Error", "Failed to reject friend request", Alert.AlertType.ERROR);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to reject request: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void cancelRequest(FriendRequest request) {
        try {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Cancel Friend Request");
            confirmation.setHeaderText(null);
            confirmation.setContentText("Cancel friend request to " + request.getReceiverName() + "?");

            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    boolean success = socialDAO.rejectFriendRequest(request.getRequestId());

                    if (success) {
                        showAlert("Success", "Friend request cancelled", Alert.AlertType.INFORMATION);
                        loadAllData();
                    } else {
                        showAlert("Error", "Failed to cancel friend request", Alert.AlertType.ERROR);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to cancel request: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void unfriend(Member friend) {
        try {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Unfriend");
            confirmation.setHeaderText(null);
            confirmation.setContentText("Remove " + friend.getFullName() + " from your friends?");

            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    boolean success = socialDAO.unfriend(currentMember.getMemberId(), friend.getMemberId());

                    if (success) {
                        showAlert("Success", friend.getFullName() + " has been removed from your friends", Alert.AlertType.INFORMATION);
                        loadAllData();
                    } else {
                        showAlert("Error", "Failed to unfriend", Alert.AlertType.ERROR);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to unfriend: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void viewFriendProgress(Member friend) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Friend Progress");
        info.setHeaderText(friend.getFullName() + "'s Progress");
        info.setContentText("Progress tracking feature coming soon!\n\nThis will show:\n" +
                "- Workout statistics\n" +
                "- Goals achieved\n" +
                "- Recent activities");
        info.showAndWait();
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

