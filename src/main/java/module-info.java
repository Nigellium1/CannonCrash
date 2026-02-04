module com.example.cannoncrash {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.cannoncrash to javafx.fxml;
    exports com.example.cannoncrash;
}