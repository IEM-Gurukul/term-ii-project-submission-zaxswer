package com.course.registration.ui;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class JavaFxWebVideoPanel extends JPanel {
    private static final Color BACKGROUND_COLOR = new Color(240, 248, 255); // AliceBlue
    private final JTextArea fallbackTextArea;
    private Object webEngine;
    private Method webEngineLoadContent;
    private Method platformRunLater;
    private Method platformIsFxThread;
    private boolean javaFxReady;

    public JavaFxWebVideoPanel() {
        super(new BorderLayout());
        setBackground(BACKGROUND_COLOR);

        fallbackTextArea = new JTextArea();
        fallbackTextArea.setEditable(false);
        fallbackTextArea.setLineWrap(true);
        fallbackTextArea.setWrapStyleWord(true);
        fallbackTextArea.setBackground(BACKGROUND_COLOR);

        javaFxReady = initializeJavaFxWebView();
        if (!javaFxReady) {
            add(new JScrollPane(fallbackTextArea), BorderLayout.CENTER);
            showPlaceholder("JavaFX WebView is not available. Install JavaFX runtime to enable embedded video playback.");
        }
    }

    public void showPlaceholder(String message) {
        String safeMessage = message == null ? "" : message;
        String html = "<html><body style='font-family:sans-serif; margin:10px; background-color: #f0f8ff;'>"
                + "<p>" + escapeHtml(safeMessage) + "</p></body></html>";
        loadHtml(html);
    }

    public void loadHtml(String html) {
        String content = html == null ? "" : html;
        if (!javaFxReady || webEngine == null || webEngineLoadContent == null) {
            SwingUtilities.invokeLater(() -> fallbackTextArea.setText(stripHtml(content)));
            return;
        }

        runOnFxThread(() -> {
            try {
                webEngineLoadContent.invoke(webEngine, content);
            } catch (Exception ignored) {
                SwingUtilities.invokeLater(() -> fallbackTextArea.setText(stripHtml(content)));
            }
        });
    }

    private boolean initializeJavaFxWebView() {
        try {
            Class<?> jfxPanelClass = Class.forName("javafx.embed.swing.JFXPanel");
            Class<?> platformClass = Class.forName("javafx.application.Platform");
            Class<?> parentClass = Class.forName("javafx.scene.Parent");
            Class<?> sceneClass = Class.forName("javafx.scene.Scene");
            Class<?> webViewClass = Class.forName("javafx.scene.web.WebView");
            Class<?> webEngineClass = Class.forName("javafx.scene.web.WebEngine");

            platformRunLater = platformClass.getMethod("runLater", Runnable.class);
            platformIsFxThread = platformClass.getMethod("isFxApplicationThread");

            Constructor<?> jfxPanelCtor = jfxPanelClass.getConstructor();
            Object jfxPanel = jfxPanelCtor.newInstance();
            add((java.awt.Component) jfxPanel, BorderLayout.CENTER);
            revalidate();
            repaint();

            Method setSceneMethod = jfxPanelClass.getMethod("setScene", sceneClass);
            Constructor<?> sceneCtor = sceneClass.getConstructor(parentClass);
            Constructor<?> webViewCtor = webViewClass.getConstructor();
            Method getEngineMethod = webViewClass.getMethod("getEngine");
            webEngineLoadContent = webEngineClass.getMethod("loadContent", String.class);

            runOnFxThread(() -> {
                try {
                    Object webView = webViewCtor.newInstance();
                    webEngine = getEngineMethod.invoke(webView);
                    Object scene = sceneCtor.newInstance(webView);
                    setSceneMethod.invoke(jfxPanel, scene);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });

            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private void runOnFxThread(Runnable runnable) {
        if (platformRunLater == null || platformIsFxThread == null) {
            runnable.run();
            return;
        }

        try {
            boolean isFxThread = (Boolean) platformIsFxThread.invoke(null);
            if (isFxThread) {
                runnable.run();
            } else {
                platformRunLater.invoke(null, runnable);
            }
        } catch (Exception ex) {
            runnable.run();
        }
    }

    private String stripHtml(String html) {
        return html
                .replaceAll("<[^>]*>", " ")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .trim();
    }

    private String escapeHtml(String input) {
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
