module com.ecosimulator {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    
    // PDF generation
    requires org.apache.pdfbox;
    
    // Chart generation
    requires org.jfree.jfreechart;
    requires java.desktop;
    
    // Email - using Angus Mail (Eclipse Jakarta Mail implementation)
    requires jakarta.mail;
    requires java.logging;
    
    // BCrypt for password hashing
    requires bcrypt;
    
    // GSON for JSON processing
    requires com.google.gson;

    opens com.ecosimulator to javafx.fxml;
    opens com.ecosimulator.ui to javafx.fxml;
    opens com.ecosimulator.model to javafx.fxml;
    opens com.ecosimulator.auth to javafx.fxml;

    exports com.ecosimulator;
    exports com.ecosimulator.ui;
    exports com.ecosimulator.model;
    exports com.ecosimulator.simulation;
    exports com.ecosimulator.service;
    exports com.ecosimulator.auth;
    exports com.ecosimulator.persistence;
    exports com.ecosimulator.report;
    exports com.ecosimulator.core;
    exports com.ecosimulator.util;
}
