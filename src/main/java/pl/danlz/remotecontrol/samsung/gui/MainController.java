package pl.danlz.remotecontrol.samsung.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import pl.danlz.remotecontrol.samsung.adapter.TVAdapter;
import pl.danlz.remotecontrol.samsung.config.Configuration;
import pl.danlz.remotecontrol.samsung.context.AppCtx;
import pl.danlz.remotecontrol.samsung.executor.DirectExecutorService;
import pl.danlz.remotecontrol.samsung.gui.control.RCButton;
import pl.danlz.remotecontrol.samsung.gui.task.SendKeyTask;
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

	private static final String WINDOW_FOCUSED_CLASS = "windowFocused";

	private static final KeyCombination CHANNEL_LIST_OPEN_KEY = KeyCombination.valueOf("ALT+ENTER");

	private final DirectExecutorService executor = AppCtx.getBean(DirectExecutorService.class);
	private final TVAdapter adapter = AppCtx.getBean(TVAdapter.class);
	private final Configuration config = AppCtx.getBean(Configuration.class);
	private final ChannelListController channelListController = AppCtx.getBean(ChannelListController.class);
	private final SettingsController settingsController = AppCtx.getBean(SettingsController.class);

	@FXML
	private ResourceBundle resources;

	@FXML
	private Pane root;

	@FXML
	private VBox regionArea;

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
		stage.setTitle(resources.getString("title"));
		stage.getScene().setFill(Color.TRANSPARENT);
		stage.setOnCloseRequest(v -> {
			close();
		});
		stage.focusedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue) {
					root.getStyleClass().add(WINDOW_FOCUSED_CLASS);
				} else {
					root.getStyleClass().remove(WINDOW_FOCUSED_CLASS);
				}
			}
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
				String localizedShortcut = shortcut;
				try {
					localizedShortcut = resources.getString("shortcut.key." + shortcut);
				} catch (MissingResourceException e) {
					// nothing
				}
				button.setTooltip(new Tooltip(resources.getString("shortcut.key") + " " + localizedShortcut));
				scene.getAccelerators().put(KeyCombination.valueOf(shortcut), () -> button.fire());
			}
			Set<String> additionalShortcuts = button.getParsedAdditionalShortcuts();
			if (additionalShortcuts != null) {
				for (String additionalShortcut : additionalShortcuts) {
					KeyCombination key = KeyCombination.valueOf(additionalShortcut);
					scene.getAccelerators().put(KeyCombination.valueOf(additionalShortcut), () -> button.fire());
				}
			}
		}
		scene.getAccelerators().put(CHANNEL_LIST_OPEN_KEY, new Runnable() {

			@Override
			public void run() {
				channelListController.show(stage);
			}
		});
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
		for (Configuration.Region region : config.getRegions()) {
			regionList.add(regionArea.lookup("." + region.getName() + "Region"));
		}
		regionArea.getChildren().setAll(regionList);
		Set<Node> regions = regionArea.lookupAll(".region");
		for (Node node : regions) {
			Region region = (Region) node;
			region.setOnDragDetected(e -> {
				LOG.debug("DragDetected: " + region.getStyleClass());
				region.getStyleClass().add(MOVING_CLASS);

				int sourceIndex = regionArea.getChildren().indexOf(region);

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
				int targetIndex = regionArea.getChildren().indexOf(region);
				Node movedRegion = regionArea.getChildren().remove(sourceIndex);
				LOG.debug("Moving region [" + region.getStyleClass() + "] from index " + sourceIndex + " to index "
						+ targetIndex);
				regionArea.getChildren().add(targetIndex, movedRegion);

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

				executor.execute(new SendKeyTask(resources, config, adapter, button.getKeyCode(),
						Configuration.SEND_KEY_QUIET_PERIOD));
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
		for (Node region : regionArea.getChildren()) {
			if (region.getStyleClass().contains("region")) {
				regions.add(new Configuration.Region(region.getStyleClass().get(1).replace("Region", "")));
			}
		}
		config.setRegions(regions);
		config.save();
	}
}
