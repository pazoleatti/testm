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
 *  Компонент "Картинка-кнопка-ссылка" ч/з Anchor
 *  @author vpetrov
 */
public class ImgBtnLnk  extends Composite {
    private static Binder uiBinder = GWT.create(Binder.class);

    interface Binder extends UiBinder<Widget, ImgBtnLnk> {
    }

   /* @UiField
    Hyperlink link;*/

    @UiField
    Anchor anchor;

    @UiField
    Image img;

    public ImgBtnLnk() {
        initWidget(uiBinder.createAndBindUi(this));
        setStyleName("AplanaImgBtnLnk");
    }

    /**
     * @param imageUrl - url изображения кнопки
     * @param href - ссылка
     */
    @Inject
    @UiConstructor
    public ImgBtnLnk(String imageUrl, String text, String href) {
        initWidget(uiBinder.createAndBindUi(this));
        img.setUrl(imageUrl);
        anchor.setText(text);
        anchor.setHref(href);
    }

    public void setImageUrl(String imageUrl){
        img.setUrl(imageUrl);
    }

    public void addClickHandler(ClickHandler clickHandler){
        anchor.addClickHandler(clickHandler);
        img.addClickHandler(clickHandler);
    }


}