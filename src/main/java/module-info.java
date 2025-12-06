module com.gym {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;

    requires com.almasb.fxgl.all;

    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires jbcrypt;

    opens com.gym to javafx.fxml;
    opens com.gym.controllers to javafx.fxml;
    opens com.gym.controllers.admin to javafx.fxml;
    opens com.gym.controllers.trainer to javafx.fxml;
    opens com.gym.controllers.member to javafx.fxml;
    opens com.gym.models to javafx.base;

    exports com.gym;
    exports com.gym.controllers;
    exports com.gym.controllers.admin;
    exports com.gym.controllers.trainer;
    exports com.gym.controllers.member;
    exports com.gym.models;
    exports com.gym.dao;
    exports com.gym.services;
    exports com.gym.utils;
}