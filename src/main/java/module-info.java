module com.example.histogram_simple {
    requires javafx.controls;
    requires javafx.fxml;
    requires opencv;
    requires com.jfoenix;
    requires javafx.swing;


    opens com.example.histogram_simple to javafx.fxml;
    exports com.example.histogram_simple;
    exports com.example.histogram_simple.utils;
    opens com.example.histogram_simple.utils to javafx.fxml;
}