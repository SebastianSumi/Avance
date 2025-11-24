package pe.edu.upeu.asistencia.config;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class StageManager {
    //Cambia de pantallas la pp Javafxx

    private final ConfigurableApplicationContext springContext;
    private Stage primaryStage;

    public StageManager(ConfigurableApplicationContext springContext) {
        this.springContext = springContext;
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public void cambiarEscena(String fxmlPath, String titulo, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(springContext::getBean); // Spring inyecta el controller

            Scene scene = new Scene(loader.load(), width, height);
            scene.getStylesheets().add(getClass().getResource("/static/css/styles.css").toExternalForm());

            primaryStage.setTitle(titulo);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al cargar la vista: " + fxmlPath, e);
        }
    }

}