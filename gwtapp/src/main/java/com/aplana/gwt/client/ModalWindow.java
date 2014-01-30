package com.aplana.gwt.client;

import com.aplana.gwt.client.modal.CanHide;
import com.aplana.gwt.client.modal.OnHideHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;

/**
 * Модальное окно.
 *
 * @author : vpetrov
 * @since : 16.12.13
 */
public class ModalWindow extends DialogBox implements CanHide {

    Image close;
    HTML title;

    private ModalWindowResources mwRes = GWT.create(ModalWindowResources.class);
    private Image icon = new Image(mwRes.iconImage());
    private FlowPanel captionPanel = new FlowPanel();
    private FlowPanel captionTitlePanel = new FlowPanel();

    private OnHideHandler<CanHide> hideHandler = new OnHideHandler<CanHide>() {
        @Override
        public void OnHide(CanHide modalWindow) {
            modalWindow.hide();
        }
    };

    /**
     * @param title   - заголовок окна
     * @param iconUrl - url ссылка на иконку в заголовке окна
     */
    @UiConstructor
    public ModalWindow(String title, String iconUrl) {

        super(false, true);
        this.addStyleName("AplanaModalWindow");

        close = new Image(mwRes.closeImage());

        if (!iconUrl.equals("")) {
            icon.setUrl(iconUrl);
        }

        // Переделываем стиль DialogBox
        Element td = getCellElement(0, 1);
        Element r0c0 = getCellElement(0, 0);
        Element r0c2 = getCellElement(0, 2);
        Element r1c0 = getCellElement(1, 0);
        Element r1c2 = getCellElement(1, 2);
        Element r2c0 = getCellElement(2, 0);
        Element r2c2 = getCellElement(2, 2);

        // Удаляем дивы у DialigBox что бы не было зазоров
        DOM.removeChild(((Element) r0c0.getParentElement()), (Element) r0c0.getParentElement().getFirstChildElement());
        DOM.removeChild(((Element) r0c2.getParentElement()), (Element) r0c2.getParentElement().getFirstChildElement());
        DOM.removeChild(((Element) r1c0.getParentElement()), (Element) r1c0.getParentElement().getFirstChildElement());
        DOM.removeChild(((Element) r1c2.getParentElement()), (Element) r1c2.getParentElement().getFirstChildElement());
        DOM.removeChild(((Element) r2c0.getParentElement()), (Element) r2c0.getParentElement().getFirstChildElement());
        DOM.removeChild(((Element) r2c2.getParentElement()), (Element) r2c2.getParentElement().getFirstChildElement());

        Element td1 = getCellElement(1, 1);
        Element td2 = getCellElement(2, 1);

        // Убираем паддинги у DialigBox
        td.getParentElement().addClassName("OverrideCenter");
        td1.getParentElement().addClassName("OverrideCenter");
        td2.getParentElement().addClassName("OverrideCenter");

        this.title = new HTML();
        this.title.setText(title);
        this.title.addStyleName("captionTitle");

        icon.addStyleName("icon");
        captionTitlePanel.add(icon);

        close.addStyleName("closeButton");
        captionTitlePanel.addStyleName("captionTitlePanel");

        captionTitlePanel.add(this.title);
        captionPanel.add(captionTitlePanel);
        captionPanel.add(close);
        captionPanel.addStyleName("caption");

        this.setGlassEnabled(true);
        DOM.removeChild(td, (Element) td.getFirstChildElement());
        DOM.appendChild(td, captionPanel.getElement());
    }

    public ModalWindow(String title, OnHideHandler<CanHide> hideHandler) {
        this(title, "");
        this.hideHandler = hideHandler;
    }

    /**
     * @param title - заголовок окна
     */
    public ModalWindow(String title) {
        this(title, "");
    }

    public ModalWindow(OnHideHandler<CanHide> hideHandler) {
        this("");
        this.hideHandler = hideHandler;
    }

    public ModalWindow() {
        this("");
    }

    @Override
    public void setTitle(String title) {
        this.title.setText(title);
    }

    @Override
    public String getTitle() {
        return this.title.getText();
    }

    @Override
    public String getHTML() {
        return this.title.getHTML();
    }

    @Override
    public String getText() {
        return this.title.getText();
    }

    @Override
    public void setHTML(String html) {
        this.title.setHTML(html);
    }

    @Override
    public void setText(String text) {
        this.title.setText(text);
    }

    @Override
    protected void onPreviewNativeEvent(Event.NativePreviewEvent event) {
        NativeEvent nativeEvent = event.getNativeEvent();
        if ((!event.isCanceled()
                && (event.getTypeInt() == Event.ONCLICK)
                && isCloseEvent(nativeEvent))
                || (Event.ONKEYUP == event.getTypeInt() && event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE)) {
            hideHandler.OnHide(this);
        }
        super.onPreviewNativeEvent(event);
    }

    private boolean isCloseEvent(NativeEvent event) {
        return event.getEventTarget().equals(close.getElement());
    }

    public void setOnHideHandler(OnHideHandler<CanHide> hideHandler) {
        if (hideHandler != null){
            this.hideHandler = hideHandler;
        }
    }

    interface ModalWindowResources extends ClientBundle {
        @Source("close.png")
        @ImageResource.ImageOptions(repeatStyle = ImageResource.RepeatStyle.Both)
        ImageResource closeImage();

        @Source("icon.png")
        @ImageResource.ImageOptions(repeatStyle = ImageResource.RepeatStyle.Both)
        ImageResource iconImage();

    }

}
