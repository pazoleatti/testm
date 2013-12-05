package com.aplana.gwt.client;

import com.google.gwt.event.dom.client.ClickHandler;

/**
 * Данные кнокпи в компоненте выбора из справочника
 * @author vpetrov
 */

public class RefBookButtonData {

    /** Ссылка на картинку для кнопки */
    private String imageUrl;

    /** Обработчик нажатия на кнопку */
    private ClickHandler clickHandler;

    public RefBookButtonData(String imageUrl, ClickHandler clickHandler){
        setImageUrl(imageUrl);
        setClickHandler(clickHandler);
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public ClickHandler getClickHandler() {
        return clickHandler;
    }

    public void setClickHandler(ClickHandler clickHandler) {
        this.clickHandler = clickHandler;
    }
}
