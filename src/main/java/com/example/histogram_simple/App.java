package com.example.histogram_simple;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.opencv.core.Core;


public class App extends Application {
    public void start(Stage stage){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("structure.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setTitle("Histogram");
            stage.setResizable(false);
            stage.getIcons().add(new Image("music.png"));
            stage.setScene(scene);
            stage.show();
            AppController controller = loader.getController();
            stage.setOnCloseRequest(windowEvent -> controller.setClose());
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    public static void main(String[] args){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);
    }
}
