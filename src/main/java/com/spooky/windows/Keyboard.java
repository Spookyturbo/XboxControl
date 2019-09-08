package com.spooky.windows;

import java.awt.Robot;
import java.util.HashMap;

public class Keyboard {
    HashMap<Integer, Long> heldKeys = new HashMap<>(); //Key = keycode, Value = timePressed in ms
    Robot robot;

    public Keyboard(Robot robot) {
        this.robot = robot;
    }

    public void tapKey(int keycode) {
        robot.keyPress(keycode);
        robot.keyRelease(keycode);
    }

    public void pressKey(int keycode) {
        if(!heldKeys.containsKey(keycode)) {
            robot.keyPress(keycode);
            heldKeys.put(keycode, System.currentTimeMillis());
        }
    }

    public void releaseKey(int keycode) {
        robot.keyRelease(keycode);
        if(heldKeys.containsKey(keycode)) {
            heldKeys.remove(keycode);
        }
    }

    public void holdKey(int keycode) {
        long currentTime = System.currentTimeMillis();
        if(!heldKeys.containsKey(keycode)) {
            heldKeys.put(keycode, currentTime);
        }

        long timePressed = heldKeys.get(keycode);

        if(currentTime - timePressed == 0) {
            tapKey(keycode);
        }
        else if(currentTime - timePressed >= 500) {
            // Initial repetitous press
            long timeDifference = currentTime - Math.abs(timePressed);
            if (timeDifference >= 30) {
                tapKey(keycode);
                // Replace with negative last time updated
                // Negatives mean it button holding has gotten to the repetitious part
                heldKeys.replace(keycode, -System.currentTimeMillis());
            }
        }
    }

    public boolean isKeyPressed(int keycode) {
        return heldKeys.containsKey(keycode);
    }

}