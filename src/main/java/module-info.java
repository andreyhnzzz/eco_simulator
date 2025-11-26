module com.ecosimulator {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens com.ecosimulator to javafx.fxml;
    opens com.ecosimulator.ui to javafx.fxml;
    opens com.ecosimulator.model to javafx.fxml;

    exports com.ecosimulator;
    exports com.ecosimulator.ui;
    exports com.ecosimulator.model;
    exports com.ecosimulator.simulation;
    exports com.ecosimulator.service;
}
