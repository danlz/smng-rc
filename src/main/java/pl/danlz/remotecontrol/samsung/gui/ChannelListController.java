package pl.danlz.remotecontrol.samsung.gui;

import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import pl.danlz.remotecontrol.samsung.adapter.TVAdapter;
import pl.danlz.remotecontrol.samsung.channellist.Channel;
import pl.danlz.remotecontrol.samsung.channellist.ChannelListProvider;
import pl.danlz.remotecontrol.samsung.config.Configuration;
import pl.danlz.remotecontrol.samsung.config.Configuration.ChannelSorting;
import pl.danlz.remotecontrol.samsung.context.AppCtx;
import pl.danlz.remotecontrol.samsung.executor.DirectExecutorService;
import pl.danlz.remotecontrol.samsung.gui.task.SendKeyTask;
import pl.danlz.remotecontrol.samsung.logger.Logger;

/**
 * Channel list controller.
 *
 * @author Leszek
 */
public class ChannelListController extends AbstractController {

	private static final Logger LOG = Logger.getLogger(ChannelListController.class);

	private final ChannelListProvider channelListProvider = AppCtx.getBean(ChannelListProvider.class);
	private final DirectExecutorService executor = AppCtx.getBean(DirectExecutorService.class);
	private final TVAdapter adapter = AppCtx.getBean(TVAdapter.class);
	private final Configuration config = AppCtx.getBean(Configuration.class);

	@FXML
	private ResourceBundle resources;

	private final static Comparator<Channel> CHANNEL_NAME_COMPARATOR = new Comparator<Channel>() {

		@Override
		public int compare(Channel o1, Channel o2) {
			return o1.getName().compareToIgnoreCase(o2.getName());
		}
	};

	private final static Comparator<Channel> CHANNEL_NUMBER_COMPARATOR = new Comparator<Channel>() {

		@Override
		public int compare(Channel o1, Channel o2) {
			return o1.getNumber() - o2.getNumber();
		}
	};

	@FXML
	private TextField queryField;

	@FXML
	private ListView<Channel> resultList;

	@Override
	protected void initStage(Stage stage) {
		stage.initStyle(StageStyle.TRANSPARENT);
		stage.setTitle(resources.getString("channelList.title"));
		stage.setResizable(false);
		stage.getScene().setFill(Color.TRANSPARENT);
		stage.focusedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (!newValue) {
					hide();
				}
			}
		});
	}

	private void changeChannel() {
		if (resultList.getSelectionModel().getSelectedItem() != null) {
			int channel = resultList.getSelectionModel().getSelectedItem().getNumber();
			LOG.info("Switching to channel: " + channel);

			String channelStr = String.valueOf(channel);
			for (int i = 0; i < channelStr.length(); i++) {
				String keyCode = "KEY_" + channelStr.charAt(i);
				executor.execute(
						new SendKeyTask(resources, config, adapter, keyCode, Configuration.SEND_KEY_QUIET_PERIOD));
			}
			executor.execute(
					new SendKeyTask(resources, config, adapter, "KEY_ENTER", Configuration.SEND_KEY_QUIET_PERIOD));
		}
	}

	private void handleUpKey() {
		if (!resultList.getItems().isEmpty()) {
			int selected = resultList.getSelectionModel().getSelectedIndex();
			int previous = selected - 1;
			if (previous < 0) {
				previous = selected;
			}
			resultList.getSelectionModel().select(previous);
			resultList.scrollTo(previous);
		}
	}

	private void handleDownKey() {
		if (!resultList.getItems().isEmpty()) {
			int selected = resultList.getSelectionModel().getSelectedIndex();
			int next = selected + 1;
			if (next >= resultList.getItems().size()) {
				next = selected;
			}
			resultList.getSelectionModel().select(next);
			resultList.scrollTo(next);
		}
	}

	@FXML
	private void initialize() {
		queryField.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				fillChannelList(newValue);
			}
		});
		queryField.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				LOG.debug(event.getCode() + " pressed");
				switch (event.getCode()) {
				case ESCAPE:
					hide();
					break;
				case ENTER:
					changeChannel();
					hide();
					break;
				case UP:
					handleUpKey();
					event.consume();
					break;
				case DOWN:
					handleDownKey();
					event.consume();
					break;
				default:
					break;
				}
			}
		});
		queryField.focusedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (!newValue) {
					queryField.requestFocus();
				}
			}
		});
		resultList.setCellFactory(new Callback<ListView<Channel>, ListCell<Channel>>() {

			@Override
			public ListCell<Channel> call(ListView<Channel> param) {
				return new ListCell<Channel>() {

					@Override
					protected void updateItem(Channel item, boolean empty) {
						super.updateItem(item, empty);
						if (empty) {
							setText(null);
							setGraphic(null);
						} else {
							setText(item.getName());

							Label numberLabel = new Label("" + item.getNumber());
							numberLabel.setAlignment(Pos.CENTER_RIGHT);
							numberLabel.setMinWidth(30);
							setGraphic(numberLabel);
						}
					}
				};
			}
		});
		resultList.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				changeChannel();
				hide();
			}
		});
	}

	private void fillChannelList(String query) {
		List<Channel> channels = channelListProvider.find(query.toLowerCase());
		if (config.getChannelSorting() == ChannelSorting.NAME) {
			channels.sort(CHANNEL_NAME_COMPARATOR);
		} else {
			channels.sort(CHANNEL_NUMBER_COMPARATOR);
		}
		LOG.debug("Found channels:");
		for (Channel channel : channels) {
			LOG.debug(channel.toString());
		}
		resultList.getItems().setAll(channels);
		if (!channels.isEmpty()) {
			resultList.getSelectionModel().select(0);
			resultList.scrollTo(0);
		}
	}

	/**
	 * Shows stage associated with this controller relative to the main stage.
	 *
	 * @param mainStage
	 *            main stage
	 */
	public void show(Stage mainStage) {
		super.show();
		double mainX = mainStage.getX();
		double mainWidth = mainStage.getWidth();
		double screenWidth = Screen.getPrimary().getBounds().getMaxX();

		double thisWidth = stage.getWidth();

		double thisX = mainX + mainWidth;
		if (mainX + mainWidth + thisWidth >= screenWidth) {
			thisX = mainX - thisWidth;
		}
		stage.setX(thisX);
		stage.setY(mainStage.getY() + 40);

		fillChannelList(queryField.getText());
		queryField.requestFocus();
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// deselect must be executed after focus is set
				queryField.deselect();
			}
		});
	}
}
