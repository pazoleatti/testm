package com.aplana.gwt.client.dialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

import java.util.*;

/**
 * User: vpetrov
 * Date: 14.01.14
 * Time: 16:59
 */
public class DialogPanel extends Composite {
    private static Binder uiBinder = GWT.create(Binder.class);

    private List<Dialog.PredefinedButton> buttons = new ArrayList<Dialog.PredefinedButton>();

    interface Binder extends UiBinder<Widget, DialogPanel> {
    }

    @UiField
    FlowPanel imagePanel;

    @UiField
    Label message;

    @UiField
    Button yesButton;
    @UiField
    Button noButton;
    @UiField
    Button okButton;
    @UiField
    Button cancelButton;
    @UiField
    Button closeButton;

    private EnumMap<Dialog.PredefinedButton, Button> buttonsMap = new EnumMap<Dialog.PredefinedButton, Button>(
            Dialog.PredefinedButton.class);

    private List<HandlerRegistration> registrationsList = new ArrayList<HandlerRegistration>();


    public DialogPanel(){
        initWidget(uiBinder.createAndBindUi(this));
        buttonsMap.put(Dialog.PredefinedButton.YES, yesButton);
        buttonsMap.put(Dialog.PredefinedButton.NO, noButton);
        buttonsMap.put(Dialog.PredefinedButton.OK, okButton);
        buttonsMap.put(Dialog.PredefinedButton.CANCEL, cancelButton);
        buttonsMap.put(Dialog.PredefinedButton.CLOSE, closeButton);

        yesButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                closeAfterCkick();
            }
        });

        noButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                closeAfterCkick();
            }
        });
        okButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                closeAfterCkick();
            }
        });
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                closeAfterCkick();
            }
        });
        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                closeAfterCkick();
            }
        });
        setWidth("450px");
    }

    private void closeAfterCkick(){
        Dialog.hideMessage();
    }

    /**
     *  Удаляет все обработчики
     */
    public void removeDialogHandlers(){
        for (HandlerRegistration r : registrationsList){
            r.removeHandler();
        }
        registrationsList.clear();
    }

    /**
     *   Устанавливает обработчик диалогового окна для всех зарегестированных
     * источников событий.
     *
     * @param handler
     *            обработчик события
     */
    public void setDialogHandler(final DialogHandler handler){
        if (handler==null)
            return;
        removeDialogHandlers();
        for (Map.Entry<Dialog.PredefinedButton, Button> entry : buttonsMap.entrySet()){
            switch (entry.getKey()){
                case YES:
                    registrationsList.add(entry.getValue().addClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            handler.yes();
                        }
                    }));
                    break;
                case NO:
                    registrationsList.add(entry.getValue().addClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            handler.no();
                        }
                    }));
                    break;
                case OK:
                    registrationsList.add(entry.getValue().addClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            handler.ok();
                        }
                    }));
                    break;
                case CANCEL:
                    registrationsList.add(entry.getValue().addClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            handler.cancel();
                        }
                    }));
                    break;
                case CLOSE:
                    registrationsList.add(entry.getValue().addClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            handler.close();
                        }
                    }));
                    break;
            }

        }
    }

    /**
     * Сообщение выводимое в окне
     * @param text - текст сообщения
     */
    public void setText(String text){
        message.setText(text);
    }

    /**
     * Устанавивает отображаемые кнопки
     * @param buttons - список кнопок
     */
    public void setPredefinedButtons(Dialog.PredefinedButton... buttons){
        if (this.buttons == null) {
            return;
        }
        this.buttons.clear();
        Collections.addAll(this.buttons, buttons);
        showButtons();
    }

    /**
     * Устанавливает видимость кнопок в соответствии с задаными
     */
    private void showButtons(){
        for(Dialog.PredefinedButton e: buttonsMap.keySet()){
            buttonsMap.get(e).setVisible(buttons.contains(e));
        }

    }

    public void setImageVisible(Boolean visible){
        imagePanel.setVisible(visible);
    }

    public void setImage(Image img){
        imagePanel.clear();
        imagePanel.add(img);
    }


}
