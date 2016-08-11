package com.aplana.sbrf.taxaccounting.web.widget.multiselecttree;

import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.RefBookRecordDereferenceValue;
import com.aplana.sbrf.taxaccounting.web.widget.ui.HasHighlighting;
import com.aplana.sbrf.taxaccounting.web.widget.utils.TextUtils;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Element;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Элемент дерева множественного выбора.
 *
 * @author aivanov
 */
public class LazyTreeItem extends TreeItem implements HasHighlighting {

    protected Long itemId;
    /* Тип узла дерева: true - с чекбоксом, false - с радиокнопкой, null - только с текстом*/
    protected Boolean multiSelection;

    protected Anchor label;
    protected CheckBox checkBox;
    protected RadioButton radioButton;

    protected static final String RADIO_BUTTON_GROUP = "LTI_GROUP";

    protected Boolean isChildLoaded;

    class CustomLabel extends Anchor {
        public CustomLabel() {
            super();
            getElement().getStyle().setTextDecoration(Style.TextDecoration.NONE);
            getElement().getStyle().setColor("#000000");
            getElement().getStyle().setCursor(Style.Cursor.POINTER);
            getElement().getStyle().setWhiteSpace(Style.WhiteSpace.NOWRAP);
        }
    }

    /**
     * Элемент дерева множественного выбора. По-умолчанию создается узел с чекбоксом.
     */
    public LazyTreeItem(Long itemId, String name) {
        this(itemId, name, true);
    }

    public LazyTreeItem(Long itemId, String name, Boolean multiSelection) {
        this.itemId = itemId;
        this.multiSelection = multiSelection;
        this.isChildLoaded = false;

        label = new CustomLabel();

        label.getElement().getStyle().setCursor(Style.Cursor.POINTER);

        if (multiSelection != null){
            checkBox = new CheckBox(name);
            radioButton = new RadioButton(RADIO_BUTTON_GROUP, name) {
                // http://jira.aplana.com/browse/SBRFACCTAX-10159
                // в IE8 происходит клиентская ошибка, не может установить фокус на невидимый элемент
                // поэтому пренудительно изменяем фокус только у Label
                @Override
                public void setFocus(boolean focused) {
                    Element el = super.getElement().getElementsByTagName("label").getItem(0);
                    if (focused) {
                        el.focus();
                    } else {
                        el.blur();
                    }
                }
            };

            radioButton.getElement().getFirstChildElement().getStyle().setDisplay(Style.Display.NONE);
            DOM.getChild(checkBox.getElement(), 0).getStyle().setCursor(Style.Cursor.POINTER);
            DOM.getChild(checkBox.getElement(), 1).getStyle().setCursor(Style.Cursor.POINTER);
            DOM.getChild(radioButton.getElement(), 0).getStyle().setCursor(Style.Cursor.POINTER);
            DOM.getChild(radioButton.getElement(), 1).getStyle().setCursor(Style.Cursor.POINTER);
            setWidget(multiSelection ? checkBox : radioButton);
        } else {
            label.setText(name);
            setWidget(label);
        }

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
        if (multiSelection != null){
            checkBox.setText(name);
            radioButton.setText(name);
        }
    }

    public void setInnerHtml(String name) {
        label.getElement().setInnerHTML(name);
        if (multiSelection != null){
            checkBox.getElement().setInnerHTML(name);
            radioButton.getElement().setInnerHTML(name);
        }
    }

    public String getName() {
        return multiSelection == null ? label.getText() : ((ButtonBase) getWidget()).getText();
    }

    public Boolean isMultiSelection() {
        return multiSelection;
    }

    /**
     * Сброс значения виджета итема
     * @param select может быть нулл
     */
    public void setItemState(Boolean select){
        if (getWidget() instanceof CheckBox) {
            ((CheckBox) getWidget()).setValue(select, false);
        }
        setColorOnSelect(select);
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
        if (item.getChildCount() == 0 && !item.isChildLoaded) {
            item.addItem("Загрузка...");
        }
        super.addItem(item);
    }

    public Boolean isChildLoaded() {
        return isChildLoaded;
    }

    public void setChildLoaded(Boolean isChildLoaded) {
        this.isChildLoaded = isChildLoaded;
    }

    @Override
    public void highLightText(String textToHighLight) {
        if(textToHighLight!= null && !textToHighLight.isEmpty()) {
            String highLightedString = RegExp.compile(TextUtils.quote(textToHighLight), "gi").replace(getName(),
                    "<span style=\"color: #ff0000;\">$&</span>");
            //http://conf.aplana.com/pages/viewpage.action?pageId=9597422#id-Формавыбораизсправочника-Элементыформы.1
            if (!getAdditionalAttributeMatches().isEmpty()){
                StringBuilder sb = new StringBuilder("(");
                for (RefBookRecordDereferenceValue value : getAdditionalAttributeMatches()){
                    if (value.getValueAttrAlias().equals("PARENT_ID") || value.getDereferenceValue()==null || !value.getDereferenceValue().contains(textToHighLight))
                        continue;
                    sb.append(value.getAttrName()).append(": ").append(RegExp.compile(TextUtils.quote(textToHighLight), "gi").replace(value.getDereferenceValue(),
                            "<span style=\"color: #ff0000;\">$&</span>")).append("; ");
                }
                if (sb.length()>3) sb.delete(sb.length() - 2, sb.length());
                sb.append(")");
                if (sb.toString().equals("()")) sb.delete(0, sb.length());
                highLightedString = highLightedString + sb.toString();
            }
            String newInnerHTML = getWidget().getElement().getInnerHTML().replace(getName(), highLightedString);
            getWidget().getElement().setInnerHTML(newInnerHTML);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LazyTreeItem{");
        sb.append("itemId=").append(itemId);
        sb.append(", multiSelect=").append(multiSelection);
        sb.append(", isChildLoaded=").append(isChildLoaded);
        sb.append('}');
        return sb.toString();
    }

    public List<RefBookRecordDereferenceValue> getAdditionalAttributeMatches(){
        return new ArrayList<RefBookRecordDereferenceValue>(0);
    }
}