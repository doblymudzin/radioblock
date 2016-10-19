package eu.rekawek.radioblock.standalone;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.prefs.Preferences;

import static eu.rekawek.radioblock.standalone.Main.SHOW_WINDOW_PROP;

public class PlayerGui extends JPanel implements ActionListener {

    private static final long serialVersionUID = -1162567178177329163L;

    private static final Image NORMAL_ICON = getIcon("icon215x215.png");

    private static final Image MUTE_ICON = getIcon("icon215x215-mute.png");

    private final JButton startButton;

    private final JButton stopButton;

    private final MenuItem startItem;

    private final MenuItem stopItem;

    private final CheckboxMenuItem showWindowItem;

    private final Player player;

    private final TrayIcon icon;

    public PlayerGui(Player player, Preferences prefs) {
        this.player = player;

        startButton = new JButton("Start");
        startButton.setActionCommand("start");
        startButton.setEnabled(true);
        startButton.addActionListener(this);

        stopButton = new JButton("Stop");
        stopButton.setActionCommand("stop");
        stopButton.setEnabled(false);
        stopButton.addActionListener(this);


        add(startButton);
        add(stopButton);

        startItem = new MenuItem("Start");
        startItem.setActionCommand("start");
        startItem.setEnabled(true);
        startItem.addActionListener(this);

        stopItem = new MenuItem("Stop");
        stopItem.setActionCommand("stop");
        stopItem.setEnabled(false);
        stopItem.addActionListener(this);

        showWindowItem = new CheckboxMenuItem("Show window");
        showWindowItem.setState(prefs.getBoolean(SHOW_WINDOW_PROP, true));
        showWindowItem.addItemListener(l -> {
            prefs.putBoolean(SHOW_WINDOW_PROP, showWindowItem.getState());
            toggleWindow();
        });

        icon = new TrayIcon(NORMAL_ICON, "Radioblock");
        this.player.addListener((index, level) -> {
            if (index == 0) {
                icon.setImage(MUTE_ICON);
            } else {
                icon.setImage(NORMAL_ICON);
            }
        });

        setupTray();
    }

    private void setupTray() {
        MenuItem quitItem = new MenuItem("Quit");
        quitItem.setActionCommand("quit");
        quitItem.addActionListener(this);

        final PopupMenu popup = new PopupMenu();
        popup.add(startItem);
        popup.add(stopItem);
        popup.add(showWindowItem);
        popup.add(quitItem);

        icon.setPopupMenu(popup);

        try {
            SystemTray.getSystemTray().add(icon);
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "start":
                player.start();
                startButton.setEnabled(false);
                startItem.setEnabled(false);
                stopButton.setEnabled(true);
                stopItem.setEnabled(true);
                icon.setImage(NORMAL_ICON);
                break;

            case "stop":
                player.stop();
                startButton.setEnabled(true);
                startItem.setEnabled(true);
                stopButton.setEnabled(false);
                stopItem.setEnabled(false);
                icon.setImage(NORMAL_ICON);
                break;

            case "quit":
                System.exit(0);
                break;
        }
    }

    private void toggleWindow() {
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        topFrame.setVisible(showWindowItem.getState());
    }

    private static Image getIcon(String name) {
        try {
            BufferedImage trayIconImage = ImageIO.read(PlayerGui.class.getResource("/" + name));
            int trayIconWidth = new TrayIcon(trayIconImage).getSize().width;
            return trayIconImage.getScaledInstance(trayIconWidth, -1, Image.SCALE_SMOOTH);
        } catch(IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
