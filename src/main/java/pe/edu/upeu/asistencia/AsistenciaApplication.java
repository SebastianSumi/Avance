package pe.edu.upeu.asistencia;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import pe.edu.upeu.asistencia.config.StageManager;

@SpringBootApplication
public class AsistenciaApplication extends Application {

    private ConfigurableApplicationContext springContext;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        springContext = SpringApplication.run(AsistenciaApplication.class);
    }

    @Override
    public void start(Stage stage) throws Exception {

        StageManager stageManager = springContext.getBean(StageManager.class);
        stageManager.setPrimaryStage(stage);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        loader.setControllerFactory(springContext::getBean);

        Scene scene = new Scene(loader.load(), 400, 500);
        scene.getStylesheets().add(getClass().getResource("/static/css/styles.css").toExternalForm());

        stage.setTitle("Sistema de Asistencia");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    @Override
    public void stop() {
        springContext.close();
    }
}