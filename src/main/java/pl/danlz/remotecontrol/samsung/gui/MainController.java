package pl.danlz.remotecontrol.samsung.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import pl.danlz.remotecontrol.samsung.adapter.TVAdapter;
import pl.danlz.remotecontrol.samsung.config.Configuration;
import pl.danlz.remotecontrol.samsung.context.AppCtx;
import pl.danlz.remotecontrol.samsung.gui.control.RCButton;
import pl.danlz.remotecontrol.samsung.logger.Logger;

/**
 * Main screen controller.
 *
 * @author Leszek
 */
public class MainController extends AbstractController {

	private static final Logger LOG = Logger.getLogger(MainController.class);

	private static final String OVER_DRAG_TARGET_CLASS = "regionOverDragTarget";

	private static final String MOVING_CLASS = "regionMoving";

	private final ExecutorService executor = AppCtx.getBean(ExecutorService.class);
	private final TVAdapter adapter = AppCtx.getBean(TVAdapter.class);
	private final Configuration config = AppCtx.getBean(Configuration.class);
	private final SettingsController settingsController = AppCtx.getBean(SettingsController.class);

	@FXML
	private Pane root;

	@FXML
	private Pane titleBar;

	@FXML
	private Button minimizeButton;

	@FXML
	private Button closeButton;

	@FXML
	private Button settingsButton;

	// Vector from the top left window corner to the current cursor position
	private double dragStartRelX = 0;
	private double dragStartRelY = 0;

	@FXML
	private void initialize() {
		initializeTitleBar();

		initializeRegions();

		initializeRCButtons();
	}

	@Override
	protected void initStage(Stage stage) {
		stage.initStyle(StageStyle.TRANSPARENT);
		stage.setResizable(false);
		stage.setTitle("Samsung Remote Control");
		stage.getScene().setFill(Color.TRANSPARENT);
		stage.setOnCloseRequest(v -> {
			close();
		});
		initializeShortcuts(stage.getScene());
		if (config.getPosition() != null) {
			if (config.getPosition().getX() != null) {
				stage.setX(config.getPosition().getX());
			}
			if (config.getPosition().getY() != null) {
				stage.setY(config.getPosition().getY());
			}
		}
	}

	private void initializeShortcuts(Scene scene) {
		Set<Node> buttons = root.lookupAll(".rcButton");
		for (Node node : buttons) {
			RCButton button = (RCButton) node;
			String shortcut = button.getShortcut();
			if (shortcut != null) {
				button.setTooltip(new Tooltip("Shortcut key: " + shortcut));
				scene.getAccelerators().put(KeyCombination.valueOf(shortcut), new Runnable() {

					@Override
					public void run() {
						button.fire();
					}
				});
			}
		}
	}

	private void initializeTitleBar() {
		titleBar.setOnMousePressed(e -> {
			Scene scene = getScene(e);
			dragStartRelX = e.getSceneX() - scene.getX();
			dragStartRelY = e.getSceneY() - scene.getY();
		});
		titleBar.setOnMouseDragged(e -> {
			Scene scene = getScene(e);
			Window window = scene.getWindow();

			// move vector when dragging the title bar
			double moveX = e.getScreenX() - window.getX();
			double moveY = e.getScreenY() - window.getY();

			window.setX(window.getX() + moveX - dragStartRelX);
			window.setY(window.getY() + moveY - dragStartRelY);

			LOG.debug("Move window [" + window.getX() + "," + window.getY() + "]");
		});
		minimizeButton.setOnAction(e -> {
			LOG.debug("'MINIMIZE' button pressed");
			stage.setIconified(true);
		});
		closeButton.setOnAction(e -> {
			LOG.debug("'CLOSE' button pressed");
			close();
			Platform.exit();
		});
		settingsButton.setOnAction(e -> {
			LOG.debug("'SETTINGS' button pressed");
			settingsController.show();
		});
	}

	private void initializeRegions() {
		List<Node> regionList = new ArrayList<>();
		regionList.add(root.lookup(".titleBar"));
		for (Configuration.Region region : config.getRegions()) {
			regionList.add(root.lookup("." + region.getName() + "Region"));
		}
		root.getChildren().setAll(regionList);
		Set<Node> regions = root.lookupAll(".region");
		for (Node node : regions) {
			Region region = (Region) node;
			region.setOnDragDetected(e -> {
				LOG.debug("DragDetected: " + region.getStyleClass());
				region.getStyleClass().add(MOVING_CLASS);

				int sourceIndex = root.getChildren().indexOf(region);

				Dragboard db = region.startDragAndDrop(TransferMode.ANY);
				ClipboardContent cc = new ClipboardContent();
				cc.put(DataFormat.PLAIN_TEXT, Integer.toString(sourceIndex));
				db.setContent(cc);

				e.consume();
			});
			region.setOnDragOver(e -> {
				if (e.getGestureSource() != region) {
					e.acceptTransferModes(TransferMode.MOVE);
				}

				e.consume();
			});
			region.setOnDragEntered(e -> {
				if (e.getGestureSource() != region) {
					LOG.debug("DragEntered: " + region.getStyleClass());
					region.getStyleClass().add(OVER_DRAG_TARGET_CLASS);
				}

				e.consume();
			});
			region.setOnDragExited(e -> {
				if (e.getGestureSource() != region) {
					region.getStyleClass().remove(OVER_DRAG_TARGET_CLASS);
					LOG.debug("DragExited: " + region.getStyleClass());
				}

				e.consume();
			});
			region.setOnDragDropped(e -> {
				LOG.debug("DragDropped: " + region.getStyleClass());
				Dragboard db = e.getDragboard();
				int sourceIndex = Integer.valueOf(db.getString());
				int targetIndex = root.getChildren().indexOf(region);
				Node movedRegion = root.getChildren().remove(sourceIndex);
				LOG.debug("Moving region [" + region.getStyleClass() + "] from index " + sourceIndex + " to index "
						+ targetIndex);
				root.getChildren().add(targetIndex, movedRegion);

				e.setDropCompleted(true);

				e.consume();
			});
			region.setOnDragDone(e -> {
				region.getStyleClass().remove(MOVING_CLASS);

				LOG.debug("DragDone: " + region.getStyleClass());

				e.consume();
			});
		}
	}

	private void initializeRCButtons() {
		Set<Node> buttons = root.lookupAll(".rcButton");
		for (Node node : buttons) {
			RCButton button = (RCButton) node;
			button.setOnAction(e -> {
				LOG.debug("'" + button.getText() + "' button pressed");

				executor.execute(new CommunicationTask() {

					@Override
					protected Void call() throws Exception {
						if (!adapter.isConnected()) {
							adapter.close();
							adapter.connect(config.getTvAddress(), config.getTvPort(), config.getControllerName());
						}
						adapter.sendKey(button.getKeyCode());
						return null;
					}
				});
			});
		}
	}

	private Scene getScene(MouseEvent event) {
		Node source = (Node) event.getSource();
		Scene scene = source.getScene();

		return scene;
	}

	private void close() {
		adapter.close();
		executor.shutdown();

		Window window = root.getScene().getWindow();
		Configuration.Position position = new Configuration.Position((int) window.getX(), (int) window.getY());
		config.setPosition(position);
		List<Configuration.Region> regions = new ArrayList<>();
		for (Node region : root.getChildren()) {
			if (region.getStyleClass().contains("region")) {
				regions.add(new Configuration.Region(region.getStyleClass().get(1).replace("Region", "")));
			}
		}
		config.setRegions(regions);
		config.save();
	}

	private abstract class CommunicationTask extends Task<Void> {

		@Override
		protected void failed() {
			LOG.error("Communication failed", getException());
			Alert alert = new Alert(AlertType.ERROR, getException().getMessage());
			alert.setTitle("Samsung Remote Control");
			alert.setHeaderText("Communication error");
			alert.showAndWait();
		}
	};

}