package me.iron.pve_rand.GUI;

import me.iron.pve_rand.Action.ActionController;
import me.iron.pve_rand.Event.CustomTrigger;
import obfuscated.E;
import org.schema.game.common.controller.observer.DrawerObservable;
import org.schema.game.common.controller.observer.DrawerObserver;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;

import javax.swing.*;
import java.util.*;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 03.08.2021
 * TIME: 22:37
 */
public class GUITriggerScrollableList extends ScrollableTableList<CustomTrigger> implements DrawerObserver {
    public GUITriggerScrollableList(InputState inputState, float width, float height, GUIElement guiElement) {
        super(inputState, width, height, guiElement);
        ActionController.instance.addObserver(this);
    }

    @Override
    public void initColumns() {
        addColumn(Lng.str("All Trigger"),1, new Comparator<CustomTrigger>() {
            @Override
            public int compare(CustomTrigger o1, CustomTrigger o2) {
                return 0;
            }
        });
    }

    @Override
    protected Collection<CustomTrigger> getElementList() {
        return ActionController.getAllTriggers();
    }

    @Override
    public void updateListEntries(GUIElementList elementList, Set<CustomTrigger> triggers) {
        elementList.deleteObservers();
        elementList.addObserver(this);

        for (CustomTrigger t: triggers) {
            //make a text object for the trigger, give it its own row, add to list
            GUITextOverlayTable text = new GUITextOverlayTable(10,10,getState());
            text.onInit();
            text.setTextSimple(t.getOverview());
            text.updateTextSize();
            text.setHeight(text.getTextHeight());

            //clipped row, then slaved to new row
            GUIClippedRow clippedRow;
            (clippedRow = new GUIClippedRow(this.getState())).attach(text);
            //create new row with text slaved to it

            CustomTriggerListRow newRow = new CustomTriggerListRow(getState(),t, clippedRow);
            newRow.onInit();
            //add row to list
            elementList.addWithoutUpdate(newRow);
        }
        elementList.updateDim();
    }

    @Override
    public void update(DrawerObservable drawerObservable, Object o, Object o1) {
        flagDirty(); //idk what that does, but the mail list had it
    }

    public class CustomTriggerListRow extends ScrollableTableList<CustomTrigger>.Row {

        public CustomTriggerListRow(InputState inputState, CustomTrigger trigger, GUIElement... guiElements) {
            super(inputState, trigger, guiElements);
            this.highlightSelect = true;
            this.highlightSelectSimple = true;
            this.setAllwaysOneSelected(true);
        }

        @Override
        public void clickedOnRow() {
            super.clickedOnRow();
            GUITriggerScrollableList.this.setSelectedRow(this);
            setChanged();
            notifyObservers();
        }
    }
}
