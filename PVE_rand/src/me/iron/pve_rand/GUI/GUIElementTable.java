package me.iron.pve_rand.GUI;

import api.ModPlayground;
import me.iron.pve_rand.CodeElements.CustomAction;
import me.iron.pve_rand.CodeElements.CustomCode;
import me.iron.pve_rand.Managers.ActionController;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;

import java.util.*;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 05.08.2021
 * TIME: 15:44
 */
public class GUIElementTable extends ScrollableTableList<CustomCode> {
    public GUIElementTable(InputState inputState, float width, float height, GUIElement guiElement) {
        super(inputState, width, height, guiElement);
        columnsHeight = 60;
    }
    private final String[] columnNames = new String[]{"type","name","description","argument","param 01","param 02","param 03","param 04","param 05","param 06","param 07"};
    @Override
    public void initColumns() {
        for (String s: columnNames) {
            addColumn(Lng.str(s), 1, new Comparator() {
                @Override
                public int compare(Object o1, Object o2) {
                    return 0;
                }
            });
        }
    }

    @Override
    protected Collection<CustomCode> getElementList() {
        Collection<CustomCode> actions = new HashSet<>();
        actions.addAll(ActionController.id_to_action.values());
        return actions;
    }

    @Override
    public void updateListEntries(GUIElementList mainList, Set<CustomCode> set) {
        mainList.clear();
        mainList.deleteObservers();
        mainList.addObserver(this);
        for (final CustomCode cc: set) {
            if (!(cc instanceof CustomAction))
                return;
            CustomAction c =(CustomAction) cc;
            GUIElement[] rowElementArr = new GUIElement[columnNames.length];
           // columnsHeight = 10;
            HashMap<String,Object> params = c.getParams();
            Iterator<Map.Entry<String,Object>> itt = params.entrySet().iterator();
            for (int i = 0; i < columnNames.length; i++) {
                String txt = "x";
                switch (i) {
                    case 0: txt = c.getClass().getSimpleName(); break;
                    default: {
                       if (!itt.hasNext()) break;
                       Map.Entry<String,Object> param= itt.next();
                       txt = param.getKey() + ":\n" + param.getValue().toString();
                    }
                }
                GUIClippedRow rowElement = new GUIClippedRow(getState());

                GUITextOverlayTable textElement = new GUITextOverlayTable(10, 10, getState());
                textElement.onInit();
                textElement.setTextSimple(txt);
                textElement.autoWrapOn = rowElement;
                textElement.updateTextSize();
                textElement.setHeight(textElement.getTextHeight());
                columnsHeight = Math.max(columnsHeight, textElement.getTextHeight());

                rowElement.attach(textElement);
                rowElementArr[i] = rowElement;
            }
            ElementRow row = new ElementRow(getState(),c,rowElementArr);
            row.onInit();
            mainList.add(row);
        }
        mainList.updateDim();
    }

    private class ElementRow extends Row {
        public CustomCode c;
        public ElementRow(InputState inputState, CustomCode c, GUIElement... guiElements) {
            super(inputState, c, guiElements);
            this.c = c;
        }

        @Override
        public void clickedOnRow() {
            super.clickedOnRow();
            ModPlayground.broadcastMessage("i was clicked!");
        //    strings.add("IM A NEW ROW");
        //    flagDirty();
        }
    }
}
