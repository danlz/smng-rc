package pl.danlz.remotecontrol.samsung.gui.control;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;

/**
 * A button consisting of an upper and lower {@link RCPictoButton} and a
 * pictogram in the center.
 *
 * @author Leszek
 */
public class RCIntegratedButton extends StackPane {

	@FXML
	private RCPictoButton upperButton;

	@FXML
	private RCPictoButton lowerButton;

	/**
	 * Constructs a new instance.
	 */
	public RCIntegratedButton() {
		String resourcePath = getClass().getSimpleName() + ".fxml";
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(resourcePath));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException("Could not load resource: " + resourcePath, exception);
		}
	}

	@FXML
	private void initialize() {
		upperButton.prefHeightProperty().bind(heightProperty().divide(2));
		lowerButton.prefHeightProperty().bind(heightProperty().divide(2));
	}

	public void setUpperKeyCode(String keyCode) {
		upperButton.setKeyCode(keyCode);
	}

	public String getUpperKeyCode() {
		return upperButton.getKeyCode();
	}

	public void setUpperShortcut(String shortcut) {
		upperButton.setShortcut(shortcut);
	}

	public String getUpperShortcut() {
		return upperButton.getShortcut();
	}

	public void setLowerKeyCode(String keyCode) {
		lowerButton.setKeyCode(keyCode);
	}

	public String getLowerKeyCode() {
		return lowerButton.getKeyCode();
	}

	public void setLowerShortcut(String shortcut) {
		lowerButton.setShortcut(shortcut);
	}

	public String getLowerShortcut() {
		return lowerButton.getShortcut();
	}

	public String getLowerAdditionalShortcuts() {
		return lowerButton.getAdditionalShortcuts();
	}

	public void setLowerAdditionalShortcuts(String additionalShortcuts) {
		lowerButton.setAdditionalShortcuts(additionalShortcuts);
	}

	public String getUpperAdditionalShortcuts() {
		return upperButton.getAdditionalShortcuts();
	}

	public void setUpperAdditionalShortcuts(String additionalShortcuts) {
		upperButton.setAdditionalShortcuts(additionalShortcuts);
	}
}
