package pl.danlz.remotecontrol.samsung.gui.control;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 * Remote control button with pictogram.
 *
 * @author Leszek
 */
public class RCPictoButton extends StackPane {

	private RCButton button = new RCButton();
	private Label picto = new Label();

	/**
	 * Creates a new instance.
	 */
	public RCPictoButton() {
		getStyleClass().add("rcPictoButton");

		button.prefWidthProperty().bind(this.widthProperty());
		button.prefHeightProperty().bind(this.heightProperty());
		button.maxWidthProperty().bind(this.maxWidthProperty());
		button.maxHeightProperty().bind(this.maxHeightProperty());
		button.minWidthProperty().bind(this.minWidthProperty());
		button.minHeightProperty().bind(this.minHeightProperty());

		// picto.prefWidthProperty().bind(this.widthProperty());
		// picto.prefHeightProperty().bind(this.heightProperty());
		// picto.maxWidthProperty().bind(this.maxWidthProperty());
		// picto.maxHeightProperty().bind(this.maxHeightProperty());
		// picto.minWidthProperty().bind(this.minWidthProperty());
		// picto.minHeightProperty().bind(this.minHeightProperty());

		picto.getStyleClass().add("picto");
		picto.setMouseTransparent(true);

		setPickOnBounds(false);
		picto.setPickOnBounds(false);
		button.setPickOnBounds(false);

		getChildren().addAll(button, picto);
	}

	public String getKeyCode() {
		return button.getKeyCode();
	}

	public void setKeyCode(String keyCode) {
		button.setKeyCode(keyCode);
	}

	public String getShortcut() {
		return button.getShortcut();
	}

	public void setShortcut(String shortcut) {
		button.setShortcut(shortcut);
	}

	public String getText() {
		return button.getText();
	}

	public void setText(String text) {
		button.setText(text);
	}

	public String getAdditionalShortcut() {
		return button.getAdditionalShortcut();
	}

	public void setAdditionalShortcut(String additionalShortcut) {
		button.setAdditionalShortcut(additionalShortcut);
	}
}
