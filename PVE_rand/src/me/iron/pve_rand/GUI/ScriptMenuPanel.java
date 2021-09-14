package me.iron.pve_rand.GUI;

import api.ModPlayground;
import api.utils.gui.GUIMenuPanel;
import me.iron.pve_rand.CodeElements.CustomAction;
import me.iron.pve_rand.CodeElements.CustomCode;
import me.iron.pve_rand.CodeElements.CustomScript;
import me.iron.pve_rand.Managers.ActionController;
import me.iron.pve_rand.CodeElements.CustomTrigger;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.input.InputState;

import javax.vecmath.Vector4f;
import java.util.ArrayList;
import java.util.Collection;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 03.08.2021
 * TIME: 15:01
 */
public class ScriptMenuPanel extends GUIMenuPanel {
    GUIAncor triggerElement;
    GUIContentPane triggers;
    private GUITextOverlay triggerText;

    static int width = 400;
    static int height = 400;
    FontLibrary.FontSize fontSize =  FontLibrary.FontSize.MEDIUM;

    protected static CustomTrigger selectedTrigger;
    protected static CustomScript selectedScript;
    protected static CustomAction selectedAction;

    public ScriptMenuPanel(InputState state) {
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
    }

    @Override
    public void draw() {
        update(); //TODO super unperformant, find better update way
        super.draw();
    }

    @Override
    public void recreateTabs() {
        guiWindow.clearTabs();
        allTriggerTab();
        tableTab();
    }

    private void allTriggerTab() {
        GUIContentPane tab = guiWindow.addTab("ALL");
        triggers = tab;
        tab.setTextBoxHeightLast(500);

        //background colored instantiation, so it can be used as a parent for auto resizing
        final GUIColoredRectangle background = new GUIColoredRectangle(getState(),height,width, tab.getContent(0),new Vector4f(0.3f,0.3f,0.3f,1));
        background.onInit();
        tab.getContent(0).attach(background);
        final GUIScrollabeElementList list = new GUIScrollabeElementList(width,height,tab,getState());
        list.setCallBackActivation(new GUICallback() {
            @Override
            public void callback(GUIElement guiElement, MouseEvent mouseEvent) {

            }

            @Override
            public boolean isOccluded() {
                return false;
            }
        });
        Collection<CustomCode> c = new ArrayList<>();
        c.addAll(ActionController.getAllTriggers());
        list.setCodes(c);
        list.onInit();
        background.attach(list);

        //static accessor
        triggerElement = background;

    }

    private void tableTab() {
        GUIContentPane tab = guiWindow.addTab("TABLE");

        //create scrollable table of elements
        GUIElementTable table = new GUIElementTable(getState(),width,height,tab.getContent(0));
        table.onInit();
        tab.getContent(0).attach(table);
    }
}
