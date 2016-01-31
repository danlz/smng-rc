package pl.danlz.remotecontrol.samsung.gui;

import java.util.List;
import java.util.concurrent.ExecutorService;

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
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import pl.danlz.remotecontrol.samsung.adapter.TVAdapter;
import pl.danlz.remotecontrol.samsung.channellist.Channel;
import pl.danlz.remotecontrol.samsung.channellist.ChannelListProvider;
import pl.danlz.remotecontrol.samsung.config.Configuration;
import pl.danlz.remotecontrol.samsung.context.AppCtx;
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
	private final ExecutorService executor = AppCtx.getBean(ExecutorService.class);
	private final TVAdapter adapter = AppCtx.getBean(TVAdapter.class);
	private final Configuration config = AppCtx.getBean(Configuration.class);

	@FXML
	private TextField queryField;

	@FXML
	private ListView<Channel> resultList;

	private String lastQuery = "";

	@Override
	protected void initStage(Stage stage) {
		stage.initStyle(StageStyle.TRANSPARENT);
		stage.setTitle("Channel list");
		stage.setResizable(false);
		stage.getScene().setFill(Color.TRANSPARENT);
	}

	private void handleEnterKey() {
		if (lastQuery.equals(queryField.getText())) {
			if (resultList.getSelectionModel().getSelectedItem() != null) {
				int channel = resultList.getSelectionModel().getSelectedItem().getNumber();
				LOG.info("Switching to channel: " + channel);

				String channelStr = String.valueOf(channel);
				for (int i = 0; i < channelStr.length(); i++) {
					String keyCode = "KEY_" + channelStr.charAt(i);
					executor.execute(new SendKeyTask(config, adapter, keyCode, Configuration.MACRO_DELAY));
				}
				executor.execute(new SendKeyTask(config, adapter, "KEY_ENTER"));
			}
		} else {
			List<Channel> channels = channelListProvider.find(queryField.getText().toLowerCase());
			LOG.debug("Found channels:");
			for (Channel channel : channels) {
				LOG.debug(channel.toString());
			}
			resultList.getItems().setAll(channels);
			if (!channels.isEmpty()) {
				resultList.getSelectionModel().select(0);
			}
			lastQuery = queryField.getText();
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
		queryField.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				LOG.debug(event.getCode() + " pressed");
				switch (event.getCode()) {
				case ESCAPE:
					hide();
					break;
				case ENTER:
					handleEnterKey();
					break;
				case UP:
					handleUpKey();
					event.consume();
					break;
				case DOWN:
					handleDownKey();
					event.consume();
					break;
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
		queryField.focusedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (!newValue) {
					hide();
				}
			}
		});
	}
}
