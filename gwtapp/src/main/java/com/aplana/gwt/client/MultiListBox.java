package com.aplana.gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
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

public class MultiListBox<T> extends Composite {
    private static Binder uiBinder = GWT.create(Binder.class);

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


    interface Binder extends UiBinder<Widget, MultiListBox> {
    }

    private Renderer<T> renderer;

    private int countSelect = 0;

    private final boolean multiselect;

    private List<SavedData> dataList;

    // Сохраненые данные
    private class SavedData {
        private boolean chk;
        private String name;
        private CheckBox linkedElement;

        public SavedData(String name, boolean chk, CheckBox linkedElement){
            this.setLinkedElement(linkedElement);
            setName(name);
            setChk(chk);
        }

        public boolean isChk() {
            return chk;
        }

        public void setChk(boolean chk) {
            this.chk = chk;
            getLinkedElement().setValue(chk);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
            getLinkedElement().setText(name);
        }

        public CheckBox getLinkedElement() {
            return linkedElement;
        }

        public void setLinkedElement(CheckBox linkedElement) {
            this.linkedElement = linkedElement;
        }
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

    @Inject
    public MultiListBox(){
        initWidget(uiBinder.createAndBindUi(this));
        addStyleName("AplanaMultiListBox");
        this.multiselect = true;
    }

    @UiConstructor
    public MultiListBox(boolean multiselect){
        initWidget(uiBinder.createAndBindUi(this));
        addStyleName("AplanaMultiListBox");
        this.multiselect = multiselect;
        footerPanel.setVisible(isMultiselect());
    }

    public void setRenderer(Renderer<T> renderer){
        this.renderer = renderer;
    }

    /**
     * Добавляет элементы выпадающего списка
     * @param valueList - список элеметов
     */
    public void setAvailableValues(List<T> valueList){
        checkBoxPanel.clear();
        setCountSelect(0);
        dataList = new ArrayList<SavedData>();
        for (T temp : valueList) {
            CheckBox chk = new CheckBox();
            dataList.add(new SavedData(renderer.render(temp),false,chk));
            chk.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                   chkValueChange(event);
                }
            });
            checkBoxPanel.add(chk);
        }
        updateTxtValue();
    }

    /**
     * Обработчик изменений значения у элеметов списка
     * @param event
     */
    private void chkValueChange(ValueChangeEvent<Boolean> event){
        if(isMultiselect()){
            if (event.getValue())
                setCountSelect(getCountSelect() + 1);
            else
                setCountSelect(getCountSelect() - 1);
        } else {
            for(SavedData temp : dataList)
                temp.getLinkedElement().setValue(false);
            ((CheckBox)event.getSource()).setValue(true);
            popupPanel.hide();
            for(SavedData temp : dataList){
                temp.setChk(temp.getLinkedElement().getValue());
            }
            updateTxtValue();

        }


    }

    /**
     *  Устанавливает значения у элеметов выпадающего списка
     * @param selectedList - значения элеметов
     */
    public void setSelectedValues(List<Boolean> selectedList){
        int i = 0;
        for(SavedData temp : dataList){
            temp.setChk(selectedList.get(i));

            i++;
        }
        updateTxtValue();
        countSelectedElements();
    }

    /**
     * Обработчик нажатия на кнопку отображения списка
     * @param event
     */
    @UiHandler("showButton")
    public void onClick(ClickEvent event){
        for(SavedData temp : dataList){
            temp.getLinkedElement().setValue(temp.isChk());
        }
        countSelectedElements();
        popupPanel.setPopupPosition(panel.getAbsoluteLeft(),
                panel.getAbsoluteTop() + panel.getOffsetHeight());
        popupPanel.setWidth(String.valueOf(panel.getOffsetWidth()) + "px");
        popupPanel.show();
    }

    @UiHandler("cancelButton")
    public void onCancelButtonClick(ClickEvent event){
        popupPanel.hide();
    }

    /**
     * Обновляет отображение выбраных элементов в поле TextBox
     */
    private void updateTxtValue() {
        String showInTxt = "";
        for(SavedData temp : dataList){
            if (temp.isChk()){
                showInTxt = showInTxt + temp.getName() + "; ";
            }
        }
        txt.setText(showInTxt);
    }

    /**
     * Подсчитывает колличество выбранных элементов
     */
    private void countSelectedElements(){
        int i = 0;
        for(SavedData temp : dataList){
            if (temp.isChk()){
                i ++;
            }
        }
        setCountSelect(i);
    }

    @UiHandler("selectButton")
    public void onOkButtonClick(ClickEvent event){
        popupPanel.hide();
        for(SavedData temp : dataList){
            temp.setChk(temp.getLinkedElement().getValue());
        }
        updateTxtValue();
    }

}
