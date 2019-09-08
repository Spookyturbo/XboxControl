package com.spooky.controller;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import com.studiohartman.jamepad.ControllerAxis;
import com.studiohartman.jamepad.ControllerManager;
import com.studiohartman.jamepad.ControllerState;

/*
* This class has a few different ways of reading buttons, with some odd interactions
* if you don't understand why they work the way they do.
* There are the 3 typical button controls. getRawButton, getRawButtonPressed, getRawButtonReleased. These
* will ALWAYS work. Pressed/Released states are only reevaluated when another update() method is called.
* 
* Next there are the combo and held/duration methods. Held methods will trigger after being held for so long.
* Held pressed will only trigger once, and can't be triggered again until the button is released. It can trigger
* multiple times in the same update call though. Normal held will constantly trigger if held longer then the specified time.
* Duration methods allow you to specify a min and max held time and trigger on release.
* This allows you to create combos that use buttons that have singular push effects. As the time requirement will be reset
* when a combo/hold is detected/released. Its main purpose is basically an interruptable onRelease method so combos can work
* with the same buttons.
*
* tl;dr
* If using a combo for any reason that utilizies buttons that work on their own, have those singular buttons use the duration method
* The 3 typical button controls always work no matter what
* Ensure update() is called once and only once each iteration of your control loop.
*
* To be perfectly honest there are a lot of different ways to do controls, and you may want your own way. I probably
* should have left out most of the held/duration/combo stuff and let someone using a library extend this class and come up
* with their own implementation, but I also am not planning on releasing this to the general public and am actually just
* typing to myself right now so yeah, too bad.
*/

public class GenericController {

    public enum Button {
        A, 
        B, 
        X, 
        Y, 
        DPAD_UP, 
        DPAD_RIGHT, 
        DPAD_DOWN,
        DPAD_LEFT, 
        VIEW, 
        MENU, 
        XBOX, 
        LEFTSTICK, 
        RIGHTSTICK, 
        LEFTBUMPER, 
        RIGHTBUMPER
    }

    //Double[] is so 1st element = time held, 2nd = last time held (This allows a timeframe to check for pressed status)
    //And allows multiple pressed status same method calls to trigger, and not mess up pressed with a dif time
    /* EX: Pressed(combo, 5); Pressed(combo, 5); Pressed(combo, 7);
    *   These can all work off of the same heldState key as the pressed condition is calculated, not set.
    *   prevTime < t < currentTime (Pressed state)
    */
    private Map<List<Button>, double[]> heldState = new HashMap<>();
    //Sometimes when a combo is pressed, releasing it will trigger the heldDuration methods, which is unwanted.
    //Singular buttons in an active combo will be added here once the combo returns true, and can't activate duration methods
    //until removed from this list, which will happen on release of that button
    //This list will only stop the duration method which returns true on release
    private List<Button> excludeTillReleased = new ArrayList<>();
    private long previousTime = 0;
    private double deltaTime = 0;
    
    private ControllerManager controllers = new ControllerManager();
    private ControllerState currentState;

    private float[] axes = new float[ControllerAxis.values().length];

    private int buttons;
    private int previousButtons;
    private int buttonsPressed;
    private int buttonsReleased;

    private int index;

    public GenericController(int index) {
        //Initialize controller and initialize values
        controllers.initSDLGamepad();
        previousTime = System.nanoTime();
        update();

        this.index = index;
    }

    //Check if the controller is currently connected
    public boolean isConnected() {
        return currentState.isConnected;
    }

    //Free the controller
    public void close() {
        controllers.quitSDLGamepad();
    }

    //Returns true if button is pressed
    public boolean getRawButton(Button button) {
        return (buttons & 1 << button.ordinal()) != 0;
    }

    //Returns true if the button was just pressed
    public boolean getRawButtonPressed(Button button) {
        if((buttonsPressed & 1 << button.ordinal()) != 0) {
            return true;
        }
        return false;
    }

    //Returns true if the button was just released
    public boolean getRawButtonReleased(Button button) {
        if((buttonsReleased & 1 << button.ordinal()) != 0) {
            return true;
        }
        return false;
    }

    //Returns true if all buttons are pressed, and atleast one was just pressed to ensure this only calls once
    //Does not care about time between presses, just that all are down, and one was pressed that frame
    public boolean getRawButtonComboPressed(Button[] buttons) {
        boolean buttonPressed = false;
        for(Button button : buttons) {
            //Precheck buttonReleased to reduce processing time once this is met
            if(!buttonPressed && getRawButtonPressed(button)) {
                buttonPressed = true;
            }
            else if(!getRawButton(button)) {
                //If a button isnt pressed return
                return false;
            }
        }

        if(buttonPressed) {
            return true;
        }
        return false;
    }

    //Sets each individual button in the finished combo to require being released before activating singular button activites
    private void excludeFinishedCombo(Button[] buttons) {
        if(buttons.length < 2)
            return;

        for(Button button : buttons) {
            if(!excludeTillReleased.contains(button)) {
                excludeTillReleased.add(button);
            }
        }
    }

    //Returns true as long as all the buttons in the combo are down
    public boolean getRawButtonCombo(Button[] buttons) {
        for(Button button : buttons) {
            if(!getRawButton(button))
                return false;
        }
        excludeFinishedCombo(buttons);
        return true;
    }

    //Ensures the combo is currently being tracked. Called at the beggining of comboHeld methods.
    public void getRawButtonComboHeldSetup(Button[] buttons) {
        List<Button> combo = Arrays.asList(buttons);
        //if not tracking add to the list to track
        if(!heldState.containsKey(combo)) {
            heldState.put(combo, new double[] {0, 0});
        }
    }

    //returns true if all buttons are down for atleast t seconds, will repeatedly return true after t seconds if still held.
    public boolean getRawButtonComboHeld(Button[] buttons, double seconds) {
        //Ensures combo is being tracked
        getRawButtonComboHeldSetup(buttons);
        
        List<Button> combo = Arrays.asList(buttons);
        double[] timeStatus = heldState.get(combo);
        double timePassed = timeStatus[0];
        //timePassed[0] = currentTime timePassed[1] = prevTime
        if(timePassed > seconds) {
            excludeFinishedCombo(buttons);
            return true;
        }

        return false;
    }

    //Returns true only once
    public boolean getRawButtonComboHeldPressed(Button[] buttons, double seconds) {
        //Ensures combo is being tracked
        getRawButtonComboHeldSetup(buttons);

        List<Button> combo = Arrays.asList(buttons);

        double[] timeStatus = heldState.get(combo);
        double timePassed = timeStatus[0];
        double prevTimePassed = timeStatus[1];
        //timePassed[0] = currentTime timePassed[1] = prevTime
        if(seconds < timePassed && seconds > prevTimePassed) {
            excludeFinishedCombo(buttons);
            return true;
        }

        return false;
    }

    //Returns true if held for x seconds, and released before y seconds. Returns true on release. maxHold inclusive
    public boolean getRawButtonComboDuration(Button[] buttons, double minHold, double maxHold) {
        //Ensures combo is being tracked
        getRawButtonComboHeldSetup(buttons);

        List<Button> combo = Arrays.asList(buttons);
        
        double[] timeStatus = heldState.get(combo);
        double timePassed = timeStatus[0];
        double prevTimePassed = timeStatus[1];

        //timePassed == 0 only when initialized and when a button in the combo isn't being held
        if(timePassed == 0 && prevTimePassed > minHold && prevTimePassed <= maxHold) {
            excludeFinishedCombo(buttons);
            return true;
        }
        return false;
    }

    //Wrapper method for 1 button
    public boolean getRawButtonHeld(Button button, double seconds) {
        if(excludeTillReleased.contains(button))
            return false;
        return getRawButtonComboHeld(new Button[] {button}, seconds);
    }

    //Wrapper method for 1 button
    public boolean getRawButtonHeldPressed(Button button, double seconds) {
        if(excludeTillReleased.contains(button))
            return false;
        return getRawButtonComboHeldPressed(new Button[] {button}, seconds);
    }

    public boolean getRawButtonDuration(Button button, double minHold, double maxHold) {
        if(excludeTillReleased.contains(button))
            return false;
        return getRawButtonComboDuration(new Button[] {button}, minHold, maxHold);
    }

    //Returns the last update axis value
    public float getRawAxis(ControllerAxis axis) {
        return axes[axis.ordinal()];
    }

    //Update the button and axis values. This MUST be called only once per main loop to work well
    public void update() {
        calculateDeltaTime();
        currentState = controllers.getState(index);

        //Only update if controller is connected
        if (isConnected()) {
            previousButtons = buttons;

            buttons = getJoystickButtons();
            axes = getAxisValues();

            //These should not be |= because if you aren't constantly spam calling a getButtonPressed/Released function, there will be false positives.
            buttonsPressed = ~previousButtons & buttons;
            buttonsReleased = previousButtons & ~buttons;

            updateHeldCombos();
        }
    }

    //This originally was managed when calling the methods to check for a held combo, however, because of that
    //if you were checking for a held combo in two seperate places in your code, time to hold would be shorter (additional delta time additon)
    //And in the case of multiple HeldPressed calls for the same combo, only 1 would ever go off as it was not centralized what
    //reset the pressed state
    public void updateHeldCombos() {
        for(List<Button> key : heldState.keySet()) {
            double[] heldStatus = heldState.get(key);
            Button[] combo = (Button[]) key.toArray();
            //If all buttons down, add to the held time
            if(getRawButtonCombo(combo)) {
                heldStatus[1] = heldStatus[0]; //Set prev time
                heldStatus[0] += getDeltaTime(); //Set current time
            }
            else { //Else reset held time
                //Setting prev state can help for combo release events. Currently helps with comboDuration method
                heldStatus[1] = heldStatus[0];
                heldStatus[0] = 0;

                //If it is being excluded, set the prev time to 0 as well to ensure duration doesn't trigger
                if(combo.length == 1) {
                    if(excludeTillReleased.contains(combo[0])) {
                        heldStatus[1] = 0;
                    }
                }
            }

        }

        //Check to see if any exclusions need removed
        for(Iterator<Button> buttons = excludeTillReleased.iterator(); buttons.hasNext();) {
            Button button = buttons.next();
            if(!getRawButton(button)) {
                buttons.remove();
            }
        }
    }

    //Retrieves axis from controller
    private float[] getAxisValues() {
        float[] axes = new float[ControllerAxis.values().length];

        axes[ControllerAxis.LEFTX.ordinal()] = currentState.leftStickX;
        axes[ControllerAxis.LEFTY.ordinal()] = currentState.leftStickY;
        axes[ControllerAxis.RIGHTX.ordinal()] = currentState.rightStickX;
        axes[ControllerAxis.RIGHTY.ordinal()] = currentState.rightStickY;
        axes[ControllerAxis.TRIGGERLEFT.ordinal()] = currentState.leftTrigger;
        axes[ControllerAxis.TRIGGERRIGHT.ordinal()] = currentState.rightTrigger;

        return axes;
    }

    //Retrieves buttons from controller
    private int getJoystickButtons() {
        int buttons = 0;
        buttons |= (buttonValue(currentState.a) << Button.A.ordinal());
        buttons |= (buttonValue(currentState.b) << Button.B.ordinal());
        buttons |= (buttonValue(currentState.x) << Button.X.ordinal());
        buttons |= (buttonValue(currentState.y) << Button.Y.ordinal());
        buttons |= (buttonValue(currentState.dpadDown) << Button.DPAD_DOWN.ordinal());
        buttons |= (buttonValue(currentState.dpadLeft) << Button.DPAD_LEFT.ordinal());
        buttons |= (buttonValue(currentState.dpadRight) << Button.DPAD_RIGHT.ordinal());
        buttons |= (buttonValue(currentState.dpadUp) << Button.DPAD_UP.ordinal());
        buttons |= (buttonValue(currentState.leftStickClick) << Button.LEFTSTICK.ordinal());
        buttons |= (buttonValue(currentState.rightStickClick) << Button.RIGHTSTICK.ordinal());
        buttons |= (buttonValue(currentState.lb) << Button.LEFTBUMPER.ordinal());
        buttons |= (buttonValue(currentState.rb) << Button.RIGHTBUMPER.ordinal());
        buttons |= (buttonValue(currentState.back) << Button.VIEW.ordinal());
        buttons |= (buttonValue(currentState.start) << Button.MENU.ordinal());

        return buttons;
    }

    //Converts boolean to int
    private int buttonValue(boolean pressed) {
        return (pressed) ? 1 : 0;
    }

    //Gets time since last update in seconds
    public void calculateDeltaTime() {
        long currentTime = System.nanoTime();
        double deltaMilliseconds = (currentTime - previousTime) / 1000000D;
        previousTime = currentTime;
        deltaTime = deltaMilliseconds / 1000D;
    }

    public double getDeltaTime() {
        return deltaTime;
    }
}