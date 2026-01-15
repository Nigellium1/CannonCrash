module com.example.furiousflingers {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.furiousflingers to javafx.fxml;
    exports com.example.furiousflingers;
}