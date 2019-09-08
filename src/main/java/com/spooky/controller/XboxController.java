package com.spooky.controller;

import com.studiohartman.jamepad.ControllerAxis;

public class XboxController extends GenericController {
    float deadband = 0.13f;

    public enum Hand {
        LEFT, RIGHT
    }

    public enum DPad {
        UP, RIGHT, DOWN, LEFT
    }

    public XboxController(int index) {
        super(index);
    }

    public float getY(Hand hand) {
        if(hand == Hand.LEFT) {
            return applyDeadband(getRawAxis(ControllerAxis.LEFTY), deadband);
        }
        else {
            return applyDeadband(getRawAxis(ControllerAxis.RIGHTY), deadband);
         }
    }

    public float getX(Hand hand) {
        if(hand == Hand.LEFT) {
            return applyDeadband(getRawAxis(ControllerAxis.LEFTX), deadband);
        }
        else {
            return applyDeadband(getRawAxis(ControllerAxis.RIGHTX), deadband);
        }
    }

    public float getTriggerAxis(Hand hand) {
        if(hand == Hand.LEFT) {
            return getRawAxis(ControllerAxis.TRIGGERLEFT);
        }
        else {
            return getRawAxis(ControllerAxis.TRIGGERRIGHT);
        }
    }

    //Bumper is currently pressed for corresponding hand
    public boolean getBumper(Hand hand) {
        if(hand == Hand.LEFT) {
            return getRawButton(Button.LEFTBUMPER);
        }
        else {
            return getRawButton(Button.RIGHTBUMPER);
        }
    }

    public boolean getBumperPressed(Hand hand) {
        if(hand == Hand.LEFT) {
            return getRawButtonPressed(Button.LEFTBUMPER);
        }
        else {
            return getRawButtonPressed(Button.RIGHTBUMPER);
        }
    }

    public boolean getBumperReleased(Hand hand) {
        if(hand == Hand.LEFT) {
            return getRawButtonReleased(Button.LEFTBUMPER);
        }
        else {
            return getRawButtonReleased(Button.RIGHTBUMPER);
        }
    }

    //Stick button is currently pressed for corresponding hand
    public boolean getStickButton(Hand hand) {
        if(hand == Hand.LEFT) {
            return getRawButton(Button.LEFTSTICK);
        }
        else {
            return getRawButton(Button.RIGHTSTICK);
        }
    }

    public boolean getStickButtonPressed(Hand hand) {
        if(hand == Hand.LEFT) {
            return getRawButtonPressed(Button.LEFTSTICK);
        }
        else {
            return getRawButtonPressed(Button.RIGHTSTICK);
        }
    }

    public boolean getStickButtonReleased(Hand hand) {
        if(hand == Hand.LEFT) {
            return getRawButtonReleased(Button.LEFTSTICK);
        }
        else {
            return getRawButtonReleased(Button.RIGHTSTICK);
        }
    }

    //A button is currently pressed
    public boolean getAButton() {
        return getRawButton(Button.A);
    }

    public boolean getAButtonPressed() {
        return getRawButtonPressed(Button.A);
    }

    public boolean getAButtonReleased() {
        return getRawButtonReleased(Button.A);
    }

    //B button is currently pressed
    public boolean getBButton() {
        return getRawButton(Button.B);
    }

    public boolean getBButtonPressed() {
        return getRawButtonPressed(Button.B);
    }

    public boolean getBButtonReleased() {
        return getRawButtonReleased(Button.B);
    }

    //X button is currently pressed
    public boolean getXButton() {
        return getRawButton(Button.X);
    }

    public boolean getXButtonPressed() {
        return getRawButton(Button.X);
    }

    public boolean getXButtonReleased() {
        return getRawButton(Button.X);
    }

    //Y button is currently pressed
    public boolean getYButton() {
        return getRawButton(Button.Y);
    }

    public boolean getYButtonPressed() {
        return getRawButtonPressed(Button.Y);
    }

    public boolean getYButtonReleased() {
        return getRawButtonReleased(Button.Y);
    }

    //Menu button is currently pressed
    public boolean getMenuButton() {
        return getRawButton(Button.MENU);
    }

    public boolean getMenuButtonPressed() {
        return getRawButtonPressed(Button.MENU);
    }

    public boolean getMenuButtonReleased() {
        return getRawButtonReleased(Button.MENU);
    }

    //View button is currently pressed
    public boolean getViewButton() {
        return getRawButton(Button.VIEW);
    }

    public boolean getViewButtonPressed() {
        return getRawButtonPressed(Button.VIEW);
    }

    public boolean getViewButtonReleased() {
        return getRawButtonReleased(Button.VIEW);
    }

    //Xbox button is currently pressed (These dont do anything btw)
    public boolean getXboxButton() {
        return getRawButton(Button.XBOX);
    }

    public boolean getXboxButtonPressed() {
        return getRawButtonPressed(Button.XBOX);
    }

    public boolean getXboxButtonReleased() {
        return getRawButtonReleased(Button.XBOX);
    }

    //Returns angle of the POV
    public int getPOV() {
        boolean up = getRawButton(Button.DPAD_UP);
        boolean right = getRawButton(Button.DPAD_RIGHT);
        boolean down = getRawButton(Button.DPAD_DOWN);
        boolean left = getRawButton(Button.DPAD_LEFT);

        if (up) {
            if (right) {
                return 45;
            } else if (left) {
                return 315;
            } else {
                return 0;
            }
        } else if (down) {
            if (right) {
                return 135;
            } else if (left) {
                return 225;
            } else {
                return 180;
            }
        } else if (right) {
            return 90;
        } else if (left) {
            return 270;
        }

        return -1;
    }

    //DPad button is currently pressed for given direction
    public boolean getDPadButton(DPad direction) {
        if(direction == DPad.UP) {
            return getRawButton(Button.DPAD_UP);
        }
        else if(direction == DPad.RIGHT) {
            return getRawButton(Button.DPAD_RIGHT);
        }
        else if(direction == DPad.DOWN) {
            return getRawButton(Button.DPAD_DOWN);
        }
        else {
            return getRawButton(Button.DPAD_LEFT);
        }
    } 

    public boolean getDPadButtonPressed(DPad direction) {
        if(direction == DPad.UP) {
            return getRawButtonPressed(Button.DPAD_UP);
        }
        else if(direction == DPad.RIGHT) {
            return getRawButtonPressed(Button.DPAD_RIGHT);
        }
        else if(direction == DPad.DOWN) {
            return getRawButtonPressed(Button.DPAD_DOWN);
        }
        else {
            return getRawButtonPressed(Button.DPAD_LEFT);
        }
    } 

    public boolean getDPadButtonReleased(DPad direction) {
        if(direction == DPad.UP) {
            return getRawButtonReleased(Button.DPAD_UP);
        }
        else if(direction == DPad.RIGHT) {
            return getRawButtonReleased(Button.DPAD_RIGHT);
        }
        else if(direction == DPad.DOWN) {
            return getRawButtonReleased(Button.DPAD_DOWN);
        }
        else {
            return getRawButtonReleased(Button.DPAD_LEFT);
        }
    } 

    private float applyDeadband(float value, float deadband) {
        if(Math.abs(value) > deadband) {
            if(value > 0) {
                return (value - deadband) / (1f - deadband);
            }
            else {
                return (value + deadband) / (1f - deadband);
            }
        }
        else {
            return 0;
        }
    }
}