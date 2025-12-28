package com.gym.utils;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {

    /**
     * Initialize the Friend_Requests table if it doesn't exist
     */
    public static void initializeFriendRequestsTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS Friend_Requests (" +
                "request_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "sender_id INTEGER NOT NULL, " +
                "receiver_id INTEGER NOT NULL, " +
                "status TEXT DEFAULT 'PENDING' CHECK(status IN ('PENDING', 'ACCEPTED', 'REJECTED')), " +
                "request_date TEXT NOT NULL, " +
                "response_date TEXT, " +
                "FOREIGN KEY (sender_id) REFERENCES Members(member_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (receiver_id) REFERENCES Members(member_id) ON DELETE CASCADE, " +
                "UNIQUE(sender_id, receiver_id)" +
                ")";

        String createIndexReceiver = "CREATE INDEX IF NOT EXISTS idx_friend_requests_receiver ON Friend_Requests(receiver_id)";
        String createIndexSender = "CREATE INDEX IF NOT EXISTS idx_friend_requests_sender ON Friend_Requests(sender_id)";
        String createIndexStatus = "CREATE INDEX IF NOT EXISTS idx_friend_requests_status ON Friend_Requests(status)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSQL);
            stmt.execute(createIndexReceiver);
            stmt.execute(createIndexSender);
            stmt.execute(createIndexStatus);

            System.out.println("✅ Friend_Requests table initialized successfully");

        } catch (Exception e) {
            System.err.println("❌ Error initializing Friend_Requests table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Initialize all social-related tables
     */
    public static void initializeAllSocialTables() {
        initializeFriendRequestsTable();
    }
}

