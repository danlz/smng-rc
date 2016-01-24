package pl.danlz.remotecontrol.samsung.gui.control;

import javafx.scene.control.Button;

/**
 * Remote control button.
 *
 * @author Leszek
 */
public class RCButton extends Button {

	private String keyCode;
	private String shortcut;

	/**
	 * Creates a new instance.
	 */
	public RCButton() {
		getStyleClass().add("rcButton");
		setPickOnBounds(false);
		setFocusTraversable(false);
	}

	public String getKeyCode() {
		return keyCode;
	}

	public void setKeyCode(String keyCode) {
		this.keyCode = keyCode;
	}

	public String getShortcut() {
		return shortcut;
	}

	public void setShortcut(String shortcut) {
		this.shortcut = shortcut;
	}
}
