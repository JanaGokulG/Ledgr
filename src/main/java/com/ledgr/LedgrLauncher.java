package com.ledgr;

import com.ledgr.ui.LedgrDesktopApp;
import javafx.application.Application;

/**
 * Main application launcher for Ledgr JavaFX Desktop Application.
 * Standard non-modular launcher preventing JavaFX runtime initialization issues.
 */
public class LedgrLauncher {

    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("sun.jnu.encoding", "UTF-8");
        System.setProperty("stdout.encoding", "UTF-8");
        System.setProperty("stderr.encoding", "UTF-8");
        Application.launch(LedgrDesktopApp.class, args);
    }
}
