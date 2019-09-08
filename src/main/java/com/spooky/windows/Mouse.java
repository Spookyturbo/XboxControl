package com.spooky.windows;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.geom.Point2D;

public class Mouse {
    Robot robot;

    //Screen size changes based off current montior being used.
    //(Closing my laptop changes screensize to my external monitor, this lets it still work)
    private final int MIN_X = 0;
    private int MAX_X = 1920;
    
    private final int MIN_Y = 0;
    private int MAX_Y = 1080;

    Point currentPosition;
    Point2D.Double deltaPosition;

    private double scrollValue;

    public enum MouseButton {
        LEFT(InputEvent.BUTTON1_DOWN_MASK), MIDDLE(InputEvent.BUTTON2_DOWN_MASK), RIGHT(InputEvent.BUTTON3_DOWN_MASK);

        private final int value;
        public boolean pressed = false;

        MouseButton(int value) {
            this.value = value;
        }
    }

    //Start cursor off at current position
    public Mouse() {
        this(MouseInfo.getPointerInfo().getLocation());
    }

    public Mouse(Robot robot) {
        this(robot, MouseInfo.getPointerInfo().getLocation());
    }

    public Mouse(int startX, int startY) {
        this(new Point(startX, startY));
    }

    public Mouse(Robot robot, int startX, int startY) {
        this(robot, new Point(startX, startY));
    }

    public Mouse(Point start) {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }

        robot.setAutoDelay(1);

        currentPosition = start;
        deltaPosition = new Point2D.Double(0, 0);

        setPosition(currentPosition);
    }

    public Mouse(Robot robot, Point start) {
        this.robot = robot;
        robot.setAutoDelay(1);
        
        currentPosition = start;
        deltaPosition = new Point2D.Double(0, 0);

        setPosition(currentPosition);
    }

    //Moves the cursor relative to its current position
    public void move(double x, double y) {
        deltaPosition.x += x;
        deltaPosition.y += y;

        //Can move atleast one pixel, do so and subtract the whole number movement
        if(Math.abs(deltaPosition.x) >= 1 || Math.abs(deltaPosition.y) > 1) {
            currentPosition = MouseInfo.getPointerInfo().getLocation();
            currentPosition.x += (int) deltaPosition.x;
            currentPosition.y += (int) deltaPosition.y;

            deltaPosition.x -= (int) deltaPosition.x;
            deltaPosition.y -= (int) deltaPosition.y;

            updatePosition();
        }
    }

    //Set the cursors position
    public void setPosition(double x, double y) {
        currentPosition.setLocation(x, y);

        updatePosition();
    }

    public void setPosition(Point point) {
        currentPosition = point;

        updatePosition();
    }

    //Actually updates the position of the cursor and clamps it
    private void updatePosition() {
        //Lets it still work when monitor gets swapped, like when I close my laptop while using my external
        Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
        MAX_X = (int) screenDimension.getWidth();
        MAX_Y = (int) screenDimension.getHeight();

        currentPosition.x = clamp(currentPosition.x, MIN_X, MAX_X);
        currentPosition.y = clamp(currentPosition.y, MIN_Y, MAX_Y);

        robot.mouseMove(currentPosition.x, currentPosition.y);
    }

    //Updates the scroll value to scroll so far
    public void scroll(double ticks) {
        scrollValue += ticks;
        if(Math.abs(scrollValue) >= 1) {
            robot.mouseWheel((int) scrollValue);
            //Get rid of those scroll ticks because we scrolled that far
            scrollValue -= (int) scrollValue;
        }
    }
    
    //Press indicated mouse button
    public void press(MouseButton button) {
        button.pressed = true;
        robot.mousePress(button.value);
    }

    //Release indicated mouse button
    public void release(MouseButton button) {
        button.pressed = false;
        robot.mouseRelease(button.value);
    }

    public boolean isPressed(MouseButton button) {
        return button.pressed;
    }

    private int clamp(int n, int min, int max) {
        return Math.max(min, Math.min(n, max));
    }

}