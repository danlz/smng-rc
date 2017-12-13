package pl.danlz.remotecontrol.samsung.gui.control;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.scene.control.Button;

/**
 * Remote control button.
 *
 * @author Leszek
 */
public class RCButton extends Button {

	private String keyCode;
	private String shortcut;
	private String additionalShortcuts;

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

	/**
	 * Sets a shortcut key. See {@link javafx.scene.input.KeyCode} for key codes.
	 * 
	 * @param shortcut shortcut key code
	 */
	public void setShortcut(String shortcut) {
		this.shortcut = shortcut;
	}

	public String getAdditionalShortcuts() {
		return additionalShortcuts;
	}
	
	/**
	 * Sets additional shortcut keys. See {@link javafx.scene.input.KeyCode} for key codes. They must be separated with comma.
	 * 
	 * @param additionalShortcuts additional shortcuts
	 */
	public void setAdditionalShortcuts(String additionalShortcuts) {
		this.additionalShortcuts = additionalShortcuts;
	}

	public Set<String> getParsedAdditionalShortcuts() {
		if (additionalShortcuts == null) {
			return null;
		}
		return Stream.of(additionalShortcuts.split(",")).map(s -> s.trim()).collect(Collectors.toSet());
	}

}
