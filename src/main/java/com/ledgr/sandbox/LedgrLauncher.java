package com.ledgr.sandbox;

import com.ledgr.sandbox.ui.LedgrDesktopApp;
import javafx.application.Application;

/**
 * Entry point launcher for Ledgr Desktop Application.
 * This class bypasses the JavaFX modular runtime check when running as a standard jar.
 */
public class LedgrLauncher {

    public static void main(String[] args) {
        Application.launch(LedgrDesktopApp.class, args);
    }
}
