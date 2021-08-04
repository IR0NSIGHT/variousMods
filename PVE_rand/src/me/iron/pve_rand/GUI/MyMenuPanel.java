package me.iron.pve_rand.GUI;

import api.ModPlayground;
import api.utils.gui.GUIMenuPanel;
import me.iron.pve_rand.Action.ActionController;
import me.iron.pve_rand.Event.CustomTrigger;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButton;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.input.InputState;
import org.schema.schine.input.Keyboard;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 03.08.2021
 * TIME: 15:01
 */
public class MyMenuPanel extends GUIMenuPanel {
    GUIAncor triggerElement;
    GUIContentPane triggers;
    private GUITextOverlay triggerText;

    static int width = 400;
    static int height = 400;
    FontLibrary.FontSize fontSize =  FontLibrary.FontSize.MEDIUM;
    public MyMenuPanel(InputState state) {
        super(state,"MyMenuPanel",width,height);
    }

    @Override
    public void onInit() {
        super.onInit();
        update();
    }

    /**
     * updates content: triggers, scripts, etc
     */
    public void update() {
   //     triggerText.getText().clear();
   //     for (int i = 0; i < 20; i++) {
   //         for (CustomTrigger t: ActionController.getAllTriggers()) {
   //             triggerText.getText().add(t.getOverview());
   //         }
   //     }
   //     triggerText.updateTextSize();

    }

    @Override
    public void draw() {
        update(); //TODO super unperformant, find better update way
        super.draw();
    }

    @Override
    public void recreateTabs() {
        guiWindow.clearTabs();
        createTriggerTab();

       GUIContentPane scnTab = guiWindow.addTab("TRIGGER");
  //     scnTab.getContent(0).attach(background);
    }

    private void createTriggerTab() {
        GUIContentPane tab = guiWindow.addTab("ALL");
        triggers = tab;
        tab.setTextBoxHeightLast(500);
    //    tab.getContent(0).attach(triggerText);
        HashSet<CustomTrigger> triggers = ActionController.getAllTriggers();

        // button pane for trigger selection
        /*
        GUIHorizontalButtonTablePane buttonPane = new GUIHorizontalButtonTablePane(getState(),1,triggers.size()*2, tab.getContent(0));
        buttonPane.onInit();
        Iterator<CustomTrigger> t = triggers.iterator();
        int i = 0;
        while (t.hasNext()) {
            CustomTrigger trigger = t.next();
            buttonPane.addText(0,i,trigger.getOverview(), FontLibrary.FontSize.MEDIUM, GUIElement.ORIENTATION_LEFT);
        //    buttonPane.setButtonSpacing(0,i,trigger.getActions().size()+trigger.getConditions().size()*2);
            GUIHorizontalButton b = (GUIHorizontalButton) buttonPane.addButton(0, ++i,"select", GUIHorizontalArea.HButtonType.BUTTON_NEUTRAL_NORMAL, new GUICallback() {
                @Override
                public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                    //TODO select this trigger in script tab
                }

                @Override
                public boolean isOccluded() {
                    return false;
                }
            }, new GUIActivationCallback() {
                @Override
                public boolean isVisible(InputState inputState) {
                    return true;
                }

                @Override
                public boolean isActive(InputState inputState) {
                    return true;
                }
            });

            i++;
        }
        tab.getContent(0).attach(buttonPane);
        */
        //background colored instantiation, so it can be used as a parent for auto resizing
        GUIColoredRectangle background = new GUIColoredRectangle(getState(),height,width, tab.getContent(0),new Vector4f(1,0,1,1));
        background.onInit();
        tab.getContent(0).attach(background);

        //scrollable text field (text gets attached to it)
        GUIScrollableTextPanel scrollPanel = new GUIScrollableTextPanel(width,height, background,getState());
        scrollPanel.setScrollable(GUIScrollableTextPanel.SCROLLABLE_VERTICAL);

    //    //text with all triggers
    //    triggerText = new GUITextOverlay(width, height*10, fontSize, getState());
    //    triggerText.onInit();
    //    triggerText.setLimitTextDraw(100);
    //    triggerText.setText(new ArrayList());

   //    GUITriggerScrollableList list = new GUITriggerScrollableList(getState(),width,height,background);
   //    list.onInit();
   //    list.flagDirty();
   //    background.attach(list);


    //   //slave trigger text to scroll panel
    //   scrollPanel.setContent(triggerText);
    //   scrollPanel.onInit();
    //   triggerText.autoWrapOn = scrollPanel;

    //   //attach scroll pane to background
    //   background.attach(scrollPanel);


        triggerElement = background;

    }
}
