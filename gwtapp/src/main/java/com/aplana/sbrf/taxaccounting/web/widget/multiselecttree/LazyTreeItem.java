package com.aplana.sbrf.taxaccounting.web.widget.multiselecttree;

import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.*;
import com.google.inject.internal.cglib.proxy.$Dispatcher;

/**
 * Элемент дерева множественного выбора.
 *
 * @author aivanov
 */
public class LazyTreeItem extends TreeItem {

    protected Long itemId;
    /* Тип узла дерева: true - с чекбоксом, false - с радиокнопкой, null - только с текстом*/
    protected Boolean multiSelection;

    protected Label label;
    protected CheckBox checkBox;
    protected RadioButton radioButton;

    protected static final String RADIO_BUTTON_GROUP = "LTI_GROUP";

    private boolean isChildLoaded = false;

    /**
     * Элемент дерева множественного выбора. По-умолчанию создается узел с чекбоксом.
     */
    public LazyTreeItem(Long itemId, String name) {
        this(itemId, name, true);
    }

    public LazyTreeItem(Long itemId, String name, Boolean multiSelection) {
        this.itemId = itemId;
        this.multiSelection = multiSelection;

        label = new Label(name);

        checkBox = new CheckBox(name);
        radioButton = new RadioButton(RADIO_BUTTON_GROUP, name);
        radioButton.getElement().getFirstChildElement().getStyle().setDisplay(Style.Display.NONE);

        label.getElement().getStyle().setCursor(Style.Cursor.POINTER);
        DOM.getChild(checkBox.getElement(), 0).getStyle().setCursor(Style.Cursor.POINTER);
        DOM.getChild(checkBox.getElement(), 1).getStyle().setCursor(Style.Cursor.POINTER);
        DOM.getChild(radioButton.getElement(), 0).getStyle().setCursor(Style.Cursor.POINTER);
        DOM.getChild(radioButton.getElement(), 1).getStyle().setCursor(Style.Cursor.POINTER);

        setWidget(multiSelection == null ? label : multiSelection ? checkBox : radioButton);

        getWidget().getElement().getStyle().setVerticalAlign(Style.VerticalAlign.MIDDLE);
        //gwt-TreeItem
        getWidget().getElement().getParentElement().getStyle().setDisplay(Style.Display.BLOCK);
        getWidget().getElement().getStyle().setProperty("width", "100%");
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public void setName(String name) {
        label.setText(name);
        checkBox.setText(name);
        radioButton.setText(name);
    }

    public String getName() {
        return multiSelection == null ? label.getText() : ((ButtonBase) getWidget()).getText();
    }

    public Boolean isMultiSelection() {
        return multiSelection;
    }

    /**
     * Сброс значения виджета итема
     * @param aBoolean может быть нулл
     */
    public void setItemState(Boolean aBoolean){
        if (getWidget() instanceof CheckBox) {
            ((CheckBox) getWidget()).setValue(aBoolean, false);
            setColorOnSelect(aBoolean);
        }
    }

    private void setColorOnSelect(Boolean select) {
        if(select == null){
            select = false;
        }
        setStyleName(getWidget().getElement().getParentElement(), "gwt-TreeItem-selected", select);
    }

    /**
     *
     * @param selected
     * @deprecated
     * @see LazyTreeItem#setItemState(java.lang.Boolean)
     */
    @Override
    @Deprecated
    public void setSelected(boolean selected) {
        // специально переопределил метод
        // обычное дерево не поддерживает множественность выбор и поэтому при выборе одного значения
        // другое развыделяется. В итоге отказываемся от дефолтных селектов и делаем свои селекты
    }

    @Override
    public boolean isSelected() {
        return multiSelection == null ? false : ((CheckBox) getWidget()).getValue();
    }

    public void addItem(LazyTreeItem item) {
        item.addItem("Загрузка...");
        super.addItem(item);
        // выравнивание смещения узлов дерева
//        String lm = item.getElement().getStyle().getMarginLeft();
//        if (lm != null && !"".equals(lm)) {
//            lm = lm.replaceAll("px", "");
//            double tmp = Double.valueOf(lm);
//            if (tmp > 0.0) {
//                item.getElement().getStyle().setMarginLeft(tmp + 7.0, Style.Unit.PX);
//            }
//        }
    }

    public boolean isChildLoaded() {
        return isChildLoaded;
    }

    public void setChildLoaded(boolean isChildLoaded) {
        this.isChildLoaded = isChildLoaded;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("LazyTreeItem{");
        sb.append("itemId=").append(itemId);
        sb.append(", multiSelect=").append(multiSelection);
        sb.append(", isChildLoaded=").append(isChildLoaded);
        sb.append('}');
        return sb.toString();
    }
}