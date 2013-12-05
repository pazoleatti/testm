package com.aplana.gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;


/**
 *  Компонент "Картинка-кнопка-ссылка"
 *  @author vpetrov
 */
public class ImageButtonLink extends Composite {
    private static Binder uiBinder = GWT.create(Binder.class);

    interface Binder extends UiBinder<Widget, ImageButtonLink> {
    }


    @UiField
    Label lbl;

    @UiField
    Image img;

    public ImageButtonLink() {
        initWidget(uiBinder.createAndBindUi(this));
        setStyleName("AplanaImageButtonLink");
    }

    /**
     * @param imageUrl - url изображения кнопки
     * @param text - текст ссылки
     */
    @Inject
    @UiConstructor
    public ImageButtonLink(String imageUrl, String text) {
        initWidget(uiBinder.createAndBindUi(this));
        img.setUrl(imageUrl);
        lbl.setText(text);
    }

    public void setImageUrl(String imageUrl){
        img.setUrl(imageUrl);
    }

    public void addClickHandler(ClickHandler clickHandler){
        lbl.addClickHandler(clickHandler);
        img.addClickHandler(clickHandler);
    }


}
