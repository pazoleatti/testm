package com.aplana.sbrf.taxaccounting.web.widget.style;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.*;

import static com.google.gwt.user.client.ui.HasVerticalAlignment.*;

public class LeftBar extends Composite implements HasWidgets {

    private static Binder uiBinder = GWT.create(Binder.class);

    interface Binder extends UiBinder<Widget, LeftBar> {
    }

    interface ButtonStyle extends CssResource {
        String buttonMarginLeft();
    }

    private int marginRight = 5;

    List<Widget> widgetList = new LinkedList<Widget>();

    @UiField
    Panel placeHolder;

    @UiField
    ButtonStyle style;

    public LeftBar() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void setChildValign(String childVerticalAligment) {
        VerticalAlignmentConstant childVerticalAligment1 = parseValignString(childVerticalAligment);
        for (Widget widget : widgetList) {
            setCellValign(widget, childVerticalAligment1);
        }
    }

    /**
     * Установка значения правого отступа
     *
     * @param marginRight в пикселях
     */
    public void setMarginRight(int marginRight) {
        this.marginRight = marginRight;
        // добавляем правый отступ у остальных виджетов кроме добавляемого, потому что он последний
        for (int i = 0; i < widgetList.size() - 1; i++) {
            Widget widget = widgetList.get(i);
            setWidgetMagrinRight(widget);
        }
    }

    @Override
    public void add(Widget w) {
        setWidgetMagrinRight(w);
        placeHolder.add(w);
        widgetList.add(w);

    }

    @Override
    public void clear() {
        placeHolder.clear();
        widgetList.clear();
    }

    @Override
    public Iterator<Widget> iterator() {
        return placeHolder.iterator();
    }

    @Override
    public boolean remove(Widget w) {
        widgetList.remove(w);
        return placeHolder.remove(w);
    }

    private void setWidgetMagrinRight(Widget widget) {
        widget.getElement().getStyle().setPropertyPx("marginRight", marginRight);
    }

    private void setCellValign(Widget widget, VerticalAlignmentConstant constant) {
        Element td = DOM.getParent(widget.getElement());
        DOM.setStyleAttribute(td, "verticalAlign", constant.getVerticalAlignString());
    }

    private VerticalAlignmentConstant parseValignString(String valign) {
        if (ALIGN_TOP.getVerticalAlignString().equals(valign)) return ALIGN_TOP;
        else {
            if (ALIGN_MIDDLE.getVerticalAlignString().equals(valign)) return ALIGN_MIDDLE;
            else {
                if (ALIGN_BOTTOM.getVerticalAlignString().equals(valign)) return ALIGN_BOTTOM;
                else {
                    throw new IllegalArgumentException("Для вертикального выравнивания не существует значения " + valign);
                }
            }
        }
    }

}
