module com.gym {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires jbcrypt;

    opens com.gym to javafx.fxml;
    opens com.gym.controllers to javafx.fxml;
    opens com.gym.controllers.admin to javafx.fxml;

    exports com.gym;
    exports com.gym.controllers;
    exports com.gym.controllers.admin;
}