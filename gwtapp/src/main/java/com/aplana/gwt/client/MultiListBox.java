package com.aplana.gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.List;

/**
 * Выподающий список с возможностью выбора нескольких элементов
 * User: vpetrov
 * Date: 27.11.13
 */

public class MultiListBox<T> extends Composite implements HasValue<List<T>>, HasEnabled {

    private static Binder uiBinder = GWT.create(Binder.class);

    interface Binder extends UiBinder<Widget, MultiListBox> {
    }

    @UiField
    PopupPanel popupPanel;

    @UiField
    Button showButton;

    @UiField
    HorizontalPanel panel;

    @UiField
    HorizontalPanel footerPanel;

    @UiField
    VerticalPanel checkBoxPanel;

    @UiField
    Label cnt;

    @UiField
    Button cancelButton;

    @UiField
    Button selectButton;

    @UiField
    TextBox txt;

    @UiField
    ScrollPanel scrollPanel;

    /**
     * Renderer для отображаемого поля
     */
    private Renderer<T> renderer;

    /**
     * Количество выделенных элементов
     */
    private int countSelect = 0;

    private boolean enabled;

    /**
     * Признак возьможности выбора нескольких элементов
     */
    private final boolean multiselect;

    public static final String CHECKBOX_GROUP = "MAIN_GROUP";

    private List<SavedData> dataList;

    private List<T> value = new ArrayList<T>();

    @Inject
    public MultiListBox() {
        initWidget(uiBinder.createAndBindUi(this));
        addStyleName("AplanaMultiListBox");
        this.multiselect = true;
        setEnabled(true);
    }

    public MultiListBox(Renderer<T> renderer, boolean multiselect, boolean enable) {
        initWidget(uiBinder.createAndBindUi(this));
        addStyleName("AplanaMultiListBox");
        this.multiselect = multiselect;
        footerPanel.setVisible(isMultiselect());
        setEnabled(enable);
        this.renderer = renderer;
    }

    /**
     * Добавляет элементы выпадающего списка
     *
     * @param valueList - список элеметов
     */
    public void setAvailableValues(List<T> valueList) {
        checkBoxPanel.clear();
        setCountSelect(0);
        dataList = new ArrayList<SavedData>();

        for (T temp : valueList) {
            HorizontalPanel itemChkPanel = new HorizontalPanel();
            CheckBox chk;
            MyLabel lbl = new MyLabel();

            if (isMultiselect())
                chk = new CheckBox();
            else
                chk = new RadioButton(CHECKBOX_GROUP);

            itemChkPanel.add(chk);
            itemChkPanel.add(lbl);

            dataList.add(new SavedData(temp, false, chk, lbl));
            chk.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    chkValueChange(event);

                }
            });

            lbl.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    CheckBox chk = ((MyLabel) event.getSource()).getLinkToData().getLinkedElement();
                    chk.setValue(!chk.getValue(), true);
                }
            });

            itemChkPanel.addStyleName("AplanaMultiListBoxItem");
            checkBoxPanel.add(itemChkPanel);
        }
        updateTxtValue();
    }

    /**
     * Обработчик изменений значения у элеметов списка
     */
    private void chkValueChange(ValueChangeEvent<Boolean> event) {
        if (isMultiselect()) {
            if (event.getValue())
                setCountSelect(getCountSelect() + 1);
            else
                setCountSelect(getCountSelect() - 1);
        } else {
            save();
            popupPanel.hide();
        }
    }

    /**
     * Обработчик нажатия на кнопку отображения списка
     */
    @UiHandler("showButton")
    public void onClick(ClickEvent event) {
        for (SavedData temp : dataList) {
            temp.getLinkedElement().setValue(temp.isChk());
        }
        countSelectedElements();
        popupPanel.setPopupPosition(panel.getAbsoluteLeft(),
                panel.getAbsoluteTop() + panel.getOffsetHeight());
        popupPanel.setWidth(String.valueOf(panel.getOffsetWidth() - 1) + "px");
        scrollPanel.setSize(String.valueOf(panel.getOffsetWidth() - 1) + "px", "100%");

        popupPanel.show();
    }

    @UiHandler("selectButton")
    public void onOkButtonClick(ClickEvent event) {
        popupPanel.hide();
        save();
    }

    @UiHandler("cancelButton")
    public void onCancelButtonClick(ClickEvent event) {
        popupPanel.hide();
    }

    /**
     * Сохранение значений всех элементов
     */
    private void save() {
        List<T> tmpList = new ArrayList<T>();
        for (SavedData dataItem : dataList) {
            dataItem.save();  // Сохраняем состояние конкретного элемента
            if (dataItem.isChk())
                tmpList.add(dataItem.getValue()); // Добавляем выбранные
        }
        updateTxtValue();
        this.value.clear();                             // очищаем список значений
        this.value.addAll(tmpList);                     // копируем новые значения в наш список
        ValueChangeEvent.fire(this, this.value);        // Генерируем событие изменения
    }

    /**
     * Обновляет отображение выбраных элементов в поле TextBox
     */
    private void updateTxtValue() {
        String showInTxt = "";
        for (SavedData temp : dataList) {
            if (temp.isChk()) {
                showInTxt = showInTxt + temp.getName() + "; ";
            }
        }
        txt.setText(showInTxt);
    }

    /**
     * Подсчитывает количество выбранных элементов
     */
    private void countSelectedElements() {
        int i = 0;
        for (SavedData temp : dataList) {
            if (temp.isChk()) {
                i++;
            }
        }
        setCountSelect(i);
    }

    public int getCountSelect() {
        return countSelect;
    }

    private void setCountSelect(int countSelect) {
        this.countSelect = countSelect;
        cnt.setText(Integer.toString(this.countSelect));
    }

    public boolean isMultiselect() {
        return multiselect;
    }

    @Override
    public List<T> getValue() {
        return value;
    }

    @Override
    public void setValue(List<T> value) {
        for (SavedData itemData : dataList)
            itemData.setChk(value.contains(itemData.getValue()));
        this.value = value;
        updateTxtValue();
    }

    @Override
    public void setValue(List<T> value, boolean fireEvents) {
        setValue(value);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<T>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        selectButton.setEnabled(enabled);
    }

    /**
     * Данные виджета, здесь храняться ссылки на элементы и сохранное значение
     */
    private class SavedData {
        private boolean chk;
        private T value;
        private CheckBox linkedElement;
        private MyLabel linkedLabel;

        public SavedData(T value, boolean chk, CheckBox linkedElement, MyLabel linkedLabel) {
            this.setValue(value);
            this.setLinkedElement(linkedElement);
            setChk(chk);
            this.setLinkedLabel(linkedLabel);
            getLinkedLabel().setLinkToData(this);
            getLinkedLabel().setText(getName());
        }

        public void save() {
            setChk(getLinkedElement().getValue());
        }

        public boolean isChk() {
            return chk;
        }

        public void setChk(boolean chk) {
            this.chk = chk;
            getLinkedElement().setValue(chk);
        }

        public String getName() {
            return renderer.render(value);
        }

        public CheckBox getLinkedElement() {
            return linkedElement;
        }

        public void setLinkedElement(CheckBox linkedElement) {
            this.linkedElement = linkedElement;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }

        public MyLabel getLinkedLabel() {
            return linkedLabel;
        }

        public void setLinkedLabel(MyLabel linkedLabel) {
            this.linkedLabel = linkedLabel;
        }

    }

    /**
     * Label с ссылкой на объект данных виджета
     */
    class MyLabel extends Label {
        private SavedData linkToData;

        public SavedData getLinkToData() {
            return linkToData;
        }

        public void setLinkToData(SavedData linkToChk) {
            this.linkToData = linkToChk;
        }
    }

}
