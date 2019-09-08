/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.spooky.xboxcontrol;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.IOException;

import com.spooky.controller.XboxController;
import com.spooky.controller.GenericController.Button;
import com.spooky.controller.XboxController.DPad;
import com.spooky.controller.XboxController.Hand;
import com.spooky.windows.Keyboard;
import com.spooky.windows.Mouse;
import com.spooky.windows.Mouse.MouseButton;

public class App {

    String sysroot = System.getenv("SystemRoot");
    Process proc;

    Robot robot;
    XboxController controller = new XboxController(0);
    Mouse mouse;
    Keyboard keyboard;

    final int MAX_PIXELS_PER_SECOND = 960;
    final int MAX_SCROLL_SPEED = 40;

    long previousTime = 0;
    double deltaTime = 0;

    boolean pause = true;

    public static void main(String[] args) {
        new App();
    }

    public App() {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }

        mouse = new Mouse(robot);
        keyboard = new Keyboard(robot);

        run();
    }

    private void run() {
        while(!disconnect()) {
            controller.update();
            deltaTime = getDeltaTime();
            pause();

            if(!pause) {
                arrowKeys();
    
                leftClick();
                middleClick();
                rightClick();
    
                //Currently using these two buttons
                startMenu();
                tabWindow();
                openKeyboard();
    
                forwardButton();
                backButton();
    
                mouseMove();
                scroll();
                //middleScroll();
            }
        }

        controller.close();
    }

    //If this happens, nothing will run until unpaused through a button sequence
    //This allows opening apps that use the controller, and not controlling the mouse and the app at the same time
    public void pause() {
        if(controller.getRawButtonComboPressed(new Button[] {Button.MENU, Button.VIEW})) {
            pause = !pause;
        }

        if(!controller.isConnected())
            pause = true;
    }

    //If this happens, the service will shut down
    public boolean disconnect() {
        return controller.getRawButtonComboHeld(new Button[] {Button.MENU, Button.VIEW}, 3);
    }

    public void mouseMove() {
        float leftX = controller.getX(Hand.LEFT);
        float leftY = -controller.getY(Hand.LEFT);

        // Square the inputs
        leftX = Math.copySign(leftX * leftX, leftX);
        leftY = Math.copySign(leftY * leftY, leftY);

        mouse.move(leftX * MAX_PIXELS_PER_SECOND * deltaTime, leftY * MAX_PIXELS_PER_SECOND * deltaTime);
    }

    public void leftClick() {
        if (controller.getAButtonPressed()) {
            mouse.press(MouseButton.LEFT);
        }
        else if(controller.getAButtonReleased()) {
            mouse.release(MouseButton.LEFT);
        }
    }

    public void middleClick() {
        if(controller.getStickButtonPressed(Hand.RIGHT)) {
            mouse.press(MouseButton.MIDDLE);
        }
        else if(controller.getStickButtonReleased(Hand.RIGHT)) {
            mouse.release(MouseButton.MIDDLE);
        }
    }

    public void rightClick() {
        if(controller.getBButtonPressed()) {
            mouse.press(MouseButton.RIGHT);
        }
        else if(controller.getBButtonReleased()) {
            mouse.release(MouseButton.RIGHT);
        }
    }

    public void startMenu() {
        if(controller.getRawButtonDuration(Button.MENU, 0, 0.5)) {
            keyboard.tapKey(KeyEvent.VK_WINDOWS);
        }
    }

    public void tabWindow() {
        //Windows+Tab
        if(controller.getRawButtonDuration(Button.VIEW, 0, 0.5)) {
            keyboard.pressKey(KeyEvent.VK_WINDOWS);
            keyboard.pressKey(KeyEvent.VK_TAB);
            keyboard.releaseKey(KeyEvent.VK_WINDOWS);
            keyboard.releaseKey(KeyEvent.VK_TAB);
        }
    }

    public void openKeyboard() {
        if (controller.getStickButtonPressed(Hand.LEFT)) {
            if (proc == null) {
                try {
                    proc = Runtime.getRuntime().exec("cmd /c osk");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                proc.destroy();
                proc = null;
            }
        }
    }

    public void backButton() {
        if(controller.getBumperPressed(Hand.LEFT)) {
            keyboard.pressKey(KeyEvent.VK_ALT);
            keyboard.pressKey(KeyEvent.VK_LEFT);
            keyboard.releaseKey(KeyEvent.VK_ALT);
            keyboard.releaseKey(KeyEvent.VK_LEFT);
        }
    }

    public void forwardButton() {
        if(controller.getBumperPressed(Hand.RIGHT)) {
            keyboard.pressKey(KeyEvent.VK_ALT);
            keyboard.pressKey(KeyEvent.VK_RIGHT);
            keyboard.releaseKey(KeyEvent.VK_ALT);
            keyboard.releaseKey(KeyEvent.VK_RIGHT);
        }
    }

    public void middleScroll() {
        float rightY = -controller.getY(Hand.RIGHT);
        if (rightY != 0) {
            // Start scrolling
            if (!mouse.isPressed(MouseButton.MIDDLE)) {
                mouse.press(MouseButton.MIDDLE);
            }
            mouse.move(0, rightY * MAX_PIXELS_PER_SECOND * deltaTime);
        }
        else if(mouse.isPressed(MouseButton.MIDDLE)) {
            mouse.release(MouseButton.MIDDLE);
        }
    }

    public void scroll() {
        float rightY = -controller.getY(Hand.RIGHT);

        //Scroll with right stick
        if (rightY != 0) {
            mouse.scroll(rightY * MAX_SCROLL_SPEED * deltaTime);
        }
    }

    //DPad controls arrow keys
    public void arrowKeys() {
        
        if(controller.getDPadButton(DPad.UP)) {
            keyboard.holdKey(KeyEvent.VK_UP);
        }
        else if(keyboard.isKeyPressed(KeyEvent.VK_UP)){
            keyboard.releaseKey(KeyEvent.VK_UP);
        }

        if(controller.getDPadButton(DPad.RIGHT)) {
            keyboard.holdKey(KeyEvent.VK_RIGHT);
        }
        else if(keyboard.isKeyPressed(KeyEvent.VK_RIGHT)) {
            keyboard.releaseKey(KeyEvent.VK_RIGHT);
        }

        if(controller.getDPadButton(DPad.DOWN)) {
            keyboard.holdKey(KeyEvent.VK_DOWN);
        }
        else if(keyboard.isKeyPressed(KeyEvent.VK_DOWN)){
            keyboard.releaseKey(KeyEvent.VK_DOWN);
        }

        if(controller.getDPadButton(DPad.LEFT)) {
            keyboard.holdKey(KeyEvent.VK_LEFT);
        }
        else if(keyboard.isKeyPressed(KeyEvent.VK_LEFT)) {
            keyboard.releaseKey(KeyEvent.VK_LEFT);
        }
    }

    public double getDeltaTime() {
        long currentTime = System.nanoTime();
        double deltaMilliseconds = (currentTime - previousTime) / 1000000D;
        previousTime = currentTime;
        return deltaMilliseconds / 1000D;
    }
}
