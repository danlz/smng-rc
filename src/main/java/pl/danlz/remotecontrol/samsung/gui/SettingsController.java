package pl.danlz.remotecontrol.samsung.gui;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.StringConverter;
import pl.danlz.remotecontrol.samsung.adapter.TVAdapter;
import pl.danlz.remotecontrol.samsung.config.Configuration;
import pl.danlz.remotecontrol.samsung.context.AppCtx;
import pl.danlz.remotecontrol.samsung.logger.Logger;
import pl.danlz.remotecontrol.samsung.upnp.UPnPAdapter;
import pl.danlz.remotecontrol.samsung.upnp.UPnPDevice;

/**
 * Settings screen controller.
 *
 * @author Leszek
 */
public class SettingsController extends AbstractController {

	private static final Logger LOG = Logger.getLogger(MainController.class);

	private static final String SEARCH_TARGET = "urn:samsung.com:device:RemoteControlReceiver:1";

	private final ExecutorService executor = AppCtx.getBean(ExecutorService.class);
	private final UPnPAdapter upnpAdapter = AppCtx.getBean(UPnPAdapter.class);
	private final TVAdapter tvAdapter = AppCtx.getBean(TVAdapter.class);
	private final Configuration config = AppCtx.getBean(Configuration.class);

	@FXML
	private ComboBox<TVAddressInfo> addressComboBox;

	@FXML
	private Spinner<Integer> portSpinner;

	@FXML
	private TextField controllerNameField;

	@FXML
	private void initialize() {
		addressComboBox.setCellFactory(new Callback<ListView<TVAddressInfo>, ListCell<TVAddressInfo>>() {

			@Override
			public ListCell<TVAddressInfo> call(ListView<TVAddressInfo> param) {
				return new ListCell<TVAddressInfo>() {

					@Override
					protected void updateItem(TVAddressInfo item, boolean empty) {
						super.updateItem(item, empty);
						if (empty) {
							setText("");
						} else {
							setText(niceNullString(item.getAddress()) + "   " + niceNullString(item.getName()) + "   "
									+ niceNullString(item.getManufacturer()));
						}
					}

					private String niceNullString(String str) {
						return str == null ? "" : str;
					}
				};
			}
		});
		addressComboBox.setConverter(new StringConverter<SettingsController.TVAddressInfo>() {

			@Override
			public String toString(TVAddressInfo object) {
				return object.getAddress();
			}

			@Override
			public TVAddressInfo fromString(String string) {
				Optional<TVAddressInfo> address = addressComboBox.getItems().stream()
						.filter(p -> p.getAddress().equals(string)).findFirst();
				if (address.isPresent()) {
					return address.get();
				}
				return new TVAddressInfo(string);
			}
		});
		portSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 65535));
		// portSpinner.addEventFilter(KeyEvent.KEY_PRESSED, new
		// EventHandler<KeyEvent>() {
		//
		// @Override
		// public void handle(KeyEvent event) {
		// LOG.debug(event.getCode() + " " + event.getCode().isDigitKey());
		// if (!event.getCode().isDigitKey()) {
		// event.consume();
		// }
		// }
		// });
	}

	@Override
	protected void initStage(Stage stage) {
		stage.initStyle(StageStyle.UTILITY);
		stage.setTitle("Settings");
		stage.setResizable(false);
	}

	@Override
	public void show() {
		addressComboBox.setValue(new TVAddressInfo(config.getTvAddress()));
		portSpinner.getValueFactory().setValue(config.getTvPort());
		controllerNameField.setText(config.getControllerName());
		super.show();
	}

	@FXML
	public void findButtonAction(ActionEvent event) {
		LOG.debug("'FIND' button pressed");

		addressComboBox.requestFocus();
		Button button = (Button) event.getSource();
		button.setDisable(true);
		button.setText("Searching...");
		executor.execute(new Task<Collection<UPnPDevice>>() {

			@Override
			protected Collection<UPnPDevice> call() throws Exception {
				Collection<UPnPDevice> devices = upnpAdapter.findDevices(SEARCH_TARGET, config.getScanTimeout());
				LOG.debug("Found devices: " + devices);

				return devices;
			}

			@Override
			protected void succeeded() {
				Collection<UPnPDevice> devices = getValue();
				Collection<TVAddressInfo> addresses = devices.stream().map(new Function<UPnPDevice, TVAddressInfo>() {

					@Override
					public TVAddressInfo apply(UPnPDevice t) {
						return new TVAddressInfo(t.getAddress().getHostAddress(), t.getFriendlyName(),
								t.getManufacturer());
					}
				}).collect(Collectors.toList());
				addressComboBox.getItems().setAll(addresses);
				addressComboBox.show();
				button.setDisable(false);
				button.setText("Find");
			}

			@Override
			protected void failed() {
				LOG.error("Device search failed", getException());
				button.setDisable(false);
			}
		});
	}

	@FXML
	public void defaultPortButtonAction(ActionEvent event) {
		portSpinner.getValueFactory().setValue(Configuration.DEFAULT_PORT);
	}

	@FXML
	public void okButtonAction(ActionEvent event) {
		config.setTvAddress(addressComboBox.getValue().getAddress());
		config.setTvPort(portSpinner.getValue());
		config.setControllerName(controllerNameField.getText());
		LOG.debug("Current configuration: " + config);
		config.save();
		tvAdapter.close();
		hide();
	}

	@FXML
	public void cancelButtonAction(ActionEvent event) {
		hide();
	}

	private class TVAddressInfo {
		private String address;
		private String name;
		private String manufacturer;

		public TVAddressInfo(String address) {
			this(address, null, null);
		}

		public TVAddressInfo(String address, String name, String manufacturer) {
			this.address = address;
			this.name = name;
			this.manufacturer = manufacturer;
		}

		public String getAddress() {
			return address;
		}

		public String getName() {
			return name;
		}

		public String getManufacturer() {
			return manufacturer;
		}

		@Override
		public String toString() {
			return "TVAddressInfo [address=" + address + ", name=" + name + ", manufacturer=" + manufacturer + "]";
		}
	}
}
