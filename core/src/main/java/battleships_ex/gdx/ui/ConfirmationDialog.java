package battleships_ex.gdx.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import battleships_ex.gdx.config.ButtonConfig;

public class ConfirmationDialog extends Dialog {

    public ConfirmationDialog(String title, String message, String confirmText, String cancelText, Runnable onConfirm) {
        super(title, new WindowStyle(Theme.fontMedium, Theme.WHITE, Theme.darkBluePanel));

        Table content = getContentTable();
        content.pad(20);
        Label messageLabel = new Label(message, new Label.LabelStyle(Theme.fontSmall, Theme.WHITE));
        messageLabel.setWrap(true);
        content.add(messageLabel).width(300f).center();

        Table buttonTable = getButtonTable();
        buttonTable.pad(20);

        GameButton btnCancel = new GameButton(cancelText, ButtonConfig.secondary(120f, 44f), this::hide);

        GameButton btnConfirm = new GameButton(confirmText, ButtonConfig.primary(120f, 44f), () -> {
            hide();
            if (onConfirm != null) {
                onConfirm.run();
            }
        });

        buttonTable.add(btnCancel).padRight(10);
        buttonTable.add(btnConfirm);
    }

    @Override
    public Dialog show(Stage stage) {
        return super.show(stage);
    }
}
