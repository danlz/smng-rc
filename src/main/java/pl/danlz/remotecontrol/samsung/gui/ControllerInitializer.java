package pl.danlz.remotecontrol.samsung.gui;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ResourceBundle;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import pl.danlz.remotecontrol.samsung.context.AppCtx;

/**
 * Initializer for controllers.
 *
 * @author Leszek
 */
public class ControllerInitializer {

	private static final String CSS_PATH = "/view/rc.css";
	
	private static final String BUNDLE_BASE = "bundle/rc";

	/**
	 * Initializes the controllers.
	 *
	 * @param primaryStage
	 *            primary stage
	 */
	public static void init(Stage primaryStage) {
		initController("/view/main.fxml", primaryStage, null);
		initController("/view/channelList.fxml", new Stage(), null);
		initController("/view/settings.fxml", new Stage(), primaryStage);
	}

	private static Stage initController(String fxmlPath, Stage stage, Window owner) {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setControllerFactory(new Callback<Class<?>, Object>() {

				@Override
				public Object call(Class<?> param) {
					return AppCtx.getBean(param);
				}
			});
			loader.setLocation(ControllerInitializer.class.getResource(fxmlPath));
			loader.setResources(ResourceBundle.getBundle(BUNDLE_BASE));
			Parent root = loader.load();

			if (owner != null) {
				stage.initOwner(owner);
				stage.initModality(Modality.WINDOW_MODAL);
			}
			stage.getIcons().add(new Image(ControllerInitializer.class.getResourceAsStream("/AppIcon.png")));

			Scene scene = new Scene(root);
			scene.getStylesheets().add(ControllerInitializer.class.getResource(CSS_PATH).toExternalForm());
			stage.setScene(scene);

			AbstractController controller = loader.getController();
			Field field = controller.getClass().getSuperclass().getDeclaredField("stage");
			field.setAccessible(true);
			field.set(controller, stage);
			controller.initStage(stage);

			return stage;
		} catch (IOException | NoSuchFieldException | SecurityException | IllegalArgumentException
				| IllegalAccessException e) {
			throw new RuntimeException("Could not initialize controller for FXML: " + fxmlPath, e);
		}
	}
}
