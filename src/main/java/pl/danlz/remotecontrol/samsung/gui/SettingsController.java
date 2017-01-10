package pl.danlz.remotecontrol.samsung.gui;

import java.util.Collection;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Service;
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
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.StringConverter;
import pl.danlz.remotecontrol.samsung.adapter.TVAdapter;
import pl.danlz.remotecontrol.samsung.config.Configuration;
import pl.danlz.remotecontrol.samsung.config.Configuration.ChannelSorting;
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

	private final UPnPAdapter upnpAdapter = AppCtx.getBean(UPnPAdapter.class);
	private final TVAdapter tvAdapter = AppCtx.getBean(TVAdapter.class);
	private final Configuration config = AppCtx.getBean(Configuration.class);

	private final Service<Collection<UPnPDevice>> upnpSearchService = new Service<Collection<UPnPDevice>>() {

		@Override
		protected Task<Collection<UPnPDevice>> createTask() {
			return new Task<Collection<UPnPDevice>>() {

				@Override
				protected Collection<UPnPDevice> call() throws Exception {
					Collection<UPnPDevice> devices = upnpAdapter.findDevices(SEARCH_TARGET, config.getScanTimeout());
					LOG.debug("Found devices: " + devices);

					return devices;
				}

				@Override
				protected void succeeded() {
					Collection<UPnPDevice> devices = getValue();
					Collection<TVAddressInfo> addresses = devices.stream()
							.map(new Function<UPnPDevice, TVAddressInfo>() {

								@Override
								public TVAddressInfo apply(UPnPDevice t) {
									return new TVAddressInfo(t.getAddress().getHostAddress(), t.getFriendlyName(),
											t.getModelName(), t.getManufacturer());
								}
							}).collect(Collectors.toList());
					TVAddressInfo currentValue = addressComboBox.getValue();
					addressComboBox.getItems().setAll(addresses);
					// if no addresses are found the value gets cleared
					addressComboBox.setValue(currentValue);
					addressComboBox.show();
				}

				@Override
				protected void failed() {
					LOG.error("Device search failed", getException());
				}
			};
		}
	};

	@FXML
	private ResourceBundle resources;

	@FXML
	private ComboBox<TVAddressInfo> addressComboBox;

	@FXML
	private Button findButton;

	@FXML
	private Spinner<Integer> portSpinner;

	@FXML
	private TextField controllerNameField;

	@FXML
	private ComboBox<ChannelSorting> channelSortingComboBox;

	public SettingsController() {
		this.upnpSearchService.setExecutor(Executors.newSingleThreadExecutor(new ThreadFactory() {

			@Override
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(r, "UPnP Search Thread");
				thread.setDaemon(true);
				return thread;
			}
		}));
	}

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
									+ niceNullString(item.getModelName()) + "   "
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
				if (object == null) {
					return null;
				}
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
		portSpinner.getEditor().setTextFormatter(new TextFormatter<String>(TextFormatter.IDENTITY_STRING_CONVERTER,
				null, new UnaryOperator<TextFormatter.Change>() {

					private Pattern portPattern = Pattern.compile("\\d{1,5}");

					@Override
					public Change apply(Change change) {
						if (!portPattern.matcher(change.getControlNewText()).matches()) {
							change.setText("");
						}
						return change;
					}
				}));
		for (ChannelSorting channelSorting : ChannelSorting.values()) {
			channelSortingComboBox.getItems().add(channelSorting);
		}
		channelSortingComboBox.setConverter(new StringConverter<Configuration.ChannelSorting>() {

			@Override
			public String toString(ChannelSorting object) {
				return resources.getString("channel.sorting." + object.name());
			}

			@Override
			public ChannelSorting fromString(String string) {
				// not used, read only combobox
				return null;
			}
		});
		findButton.disableProperty().bind(upnpSearchService.runningProperty());
		findButton.textProperty()
				.bind(Bindings.when(upnpSearchService.runningProperty()) //
						.then(new ReadOnlyStringWrapper(resources.getString("button.searching"))) //
						.otherwise(resources.getString("button.find")));
	}

	@Override
	protected void initStage(Stage stage) {
		stage.initStyle(StageStyle.UTILITY);
		stage.setTitle(resources.getString("settings.title"));
		stage.setResizable(false);
	}

	@Override
	public void show() {
		addressComboBox.setValue(new TVAddressInfo(config.getTvAddress()));
		portSpinner.getValueFactory().setValue(config.getTvPort());
		controllerNameField.setText(config.getControllerName());
		channelSortingComboBox.setValue(config.getChannelSorting());
		super.show();
	}

	@FXML
	public void findButtonAction(ActionEvent event) {
		LOG.debug("'FIND' button pressed");

		addressComboBox.requestFocus();
		upnpSearchService.restart();
	}

	@FXML
	public void defaultPortButtonAction(ActionEvent event) {
		/*
		 * if the editor contains invalid value, the new value must be set in
		 * the editor and the value factory
		 */
		portSpinner.getValueFactory().setValue(Configuration.DEFAULT_PORT);
		portSpinner.getEditor().setText(Integer.toString(Configuration.DEFAULT_PORT));
	}

	@FXML
	public void okButtonAction(ActionEvent event) {
		config.setTvAddress(addressComboBox.getValue().getAddress());
		config.setTvPort(portSpinner.getValue());
		config.setControllerName(controllerNameField.getText());
		config.setChannelSorting(channelSortingComboBox.getValue());
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
		private String modelName;
		private String manufacturer;

		public TVAddressInfo(String address) {
			this(address, null, null, null);
		}

		public TVAddressInfo(String address, String name, String modelName, String manufacturer) {
			this.address = address;
			this.name = name;
			this.modelName = modelName;
			this.manufacturer = manufacturer;
		}

		public String getAddress() {
			return address;
		}

		public String getName() {
			return name;
		}

		public String getModelName() {
			return modelName;
		}

		public String getManufacturer() {
			return manufacturer;
		}

		@Override
		public String toString() {
			return "TVAddressInfo [address=" + address + ", name=" + name + ", modelName=" + modelName
					+ ", manufacturer=" + manufacturer + "]";
		}
	}
}
