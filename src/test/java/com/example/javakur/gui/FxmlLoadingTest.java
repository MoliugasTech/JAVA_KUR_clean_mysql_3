package com.example.javakur.gui;

import com.example.javakur.model.UserRole;
import com.example.javakur.security.UserSession;
import com.example.javakur.fxControllers.UserFormController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FxmlLoadingTest {
    @BeforeAll
    static void startJavaFx() throws InterruptedException {
        CountDownLatch started = new CountDownLatch(1);
        Platform.startup(started::countDown);
        assertTrue(started.await(5, TimeUnit.SECONDS));
    }

    @AfterAll
    static void stopJavaFx() {
        Platform.exit();
    }

    @Test
    void loginFormLoadsWithItsController() throws Exception {
        Parent root = loadOnJavaFxThread("/gui/login-form.fxml");
        assertNotNull(root);
    }

    @Test
    void mainFormLoadsAfterDesktopSessionIsSet() throws Exception {
        GuiSession.set(new UserSession(1L, "admin", UserRole.ADMIN));
        Parent root = loadOnJavaFxThread("/gui/main-form.fxml");
        assertNotNull(root);
        GuiSession.clear();
    }

    @Test
    void userFormLoadsWithItsTableConfiguration() throws Exception {
        GuiSession.set(new UserSession(1L, "admin", UserRole.ADMIN));
        Parent root = loadUserFormOnJavaFxThread();
        assertNotNull(root);
        GuiSession.clear();
    }

    private Parent loadOnJavaFxThread(String resource) throws Exception {
        CountDownLatch finished = new CountDownLatch(1);
        Parent[] result = new Parent[1];
        Throwable[] failure = new Throwable[1];
        Platform.runLater(() -> {
            try {
                result[0] = FXMLLoader.load(FxmlLoadingTest.class.getResource(resource));
            } catch (Throwable throwable) {
                failure[0] = throwable;
            } finally {
                finished.countDown();
            }
        });
        assertTrue(finished.await(5, TimeUnit.SECONDS));
        if (failure[0] != null) {
            throw new AssertionError("Nepavyko užkrauti " + resource, failure[0]);
        }
        return result[0];
    }

    private Parent loadUserFormOnJavaFxThread() throws Exception {
        CountDownLatch finished = new CountDownLatch(1);
        Parent[] result = new Parent[1];
        Throwable[] failure = new Throwable[1];
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(FxmlLoadingTest.class.getResource("/gui/user-form.fxml"));
                loader.setControllerFactory(type -> new UserFormController() {
                    @Override
                    protected void refreshUsers() {
                        // FXML smoke test does not connect to MySQL.
                    }
                });
                result[0] = loader.load();
            } catch (Throwable throwable) {
                failure[0] = throwable;
            } finally {
                finished.countDown();
            }
        });
        assertTrue(finished.await(5, TimeUnit.SECONDS));
        if (failure[0] != null) {
            throw new AssertionError("Nepavyko užkrauti /gui/user-form.fxml", failure[0]);
        }
        return result[0];
    }
}
