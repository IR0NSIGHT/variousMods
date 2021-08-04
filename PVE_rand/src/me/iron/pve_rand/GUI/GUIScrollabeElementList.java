package me.iron.pve_rand.GUI;

import api.ModPlayground;
import me.iron.pve_rand.CodeElements.CustomCode;
import me.iron.pve_rand.CodeElements.CustomTrigger;
import me.iron.pve_rand.Managers.ActionController;
import org.schema.game.common.controller.observer.DrawerObservable;
import org.schema.game.common.controller.observer.DrawerObserver;
import org.schema.game.mod.Mod;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.input.InputState;

import javax.vecmath.Vector4f;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 04.08.2021
 * TIME: 16:42
 * a list which is scrollable and displays GUI elements, like text / buttons
 */
public class GUIScrollabeElementList extends GUIScrollablePanel implements DrawerObserver {
    private GUIElementList list;
    private GUICallback callbackActivate;
    private GUICallback callbackAdd;
    private GUICallback callbackRemove;
    private GUICallback callbackSelect;

    private Collection<CustomCode> codes;
    //button params
    int bWidth = 200;
    int bHeight = 20;
    public GUIScrollabeElementList(float width, float height, GUIElement dependent, InputState inputState) {
        super(width, height, dependent, inputState);
    }

    public void setCodes(Collection<CustomCode> codes) {
        this.codes = codes;
    }
    public void setCallBackActivation (GUICallback callBack) {
        this.callbackActivate = callBack;
    }

    @Override
    public void onInit() {
        //create a list
        list = new GUIElementList(getState());
        list.setScrollPane(this);
        this.setContent(list);
        if (codes == null || callbackActivate == null)
            return;
        for (CustomCode c: codes) {
            addCustomCode(c);
        }
        list.updateDim();
        //TODO "add code" button
        super.onInit();
    }


    /**
     * add entry to list: shows code overview + select button
     * @param code
     */
    public void addCustomCode(final CustomCode code) {
        code.addObserver(this);

        //create a text element
        GUITextOverlay textElement = new GUITextOverlay(10,10, FontLibrary.FontSize.MEDIUM, getState());
        textElement.onInit();
        code_to_textElement.put(code,textElement);
        updateCode(code);

        GUIListElement listElement = new GUIListElement(textElement,getState());
        list.add(listElement);

        addButtonRow(list, code, "TOGGLE ACTIVATION", new GUICallback() {
            @Override
            public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                if (mouseEvent.pressedLeftMouse())
                    code.setActive(!code.isActive());
            }

            @Override
            public boolean isOccluded() {
                return false;
            }
        });
        addButtonRow(list, code,"SELECT",callbackActivate);
        addButtonRow(list, code,"REMOVE",callbackActivate);
        addButtonRow(list, code,"ADD",callbackActivate);
    }

    private HashMap<CustomCode,GUITextOverlay> code_to_textElement = new HashMap<>();
    /**
     * updates the text element resonsible for this code.
     * @param code
     */
    private void updateCode(CustomCode code) {
        GUITextOverlay textElement = code_to_textElement.get(code);
        if (textElement == null)
            return;
        //set text.text to current code's values
        ArrayList<Object> textList = new ArrayList<>();
        textList.add(code.getName());
        textList.add("---"+code.getID() + " active: " + code.isActive());
        textList.add("---"+code.getOverview());
        textList.add("---"+code.getChildText());
        textElement.setText(textList);
        textElement.updateTextSize();
        textElement.setHeight(textElement.getTextHeight());
        textElement.setLimitTextDraw(100);
    }

    private void addButtonRow(GUIElementList list, final CustomCode code, String name, GUICallback callback) {
        final GUITextButton button = new GUITextButton(getState(), bWidth, bHeight, name, new GUICallback() {
            @Override
            public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                if (!mouseEvent.pressedLeftMouse())
                    return;
                code.setActive(!code.isActive());

                ((GUITextButton) guiElement).setColorPalette(code.isActive()? GUITextButton.ColorPalette.FRIENDLY: GUITextButton.ColorPalette.NEUTRAL);
            }

            @Override
            public boolean isOccluded() {
                return false;
            }
        });
        button.setColorPalette(GUITextButton.ColorPalette.NEUTRAL);
        button.onInit();
        list.add(new GUIListElement(button,getState()));
    }

    /**
     * updates a single list entry, fired by the entry getting edited.
     * @param drawerObservable
     * @param object
     * @param message
     */
    @Override
    public void update(DrawerObservable drawerObservable, Object object, Object message) {
        if (!(object instanceof CustomCode))
            return;
        updateCode((CustomCode) object);
    }
}
