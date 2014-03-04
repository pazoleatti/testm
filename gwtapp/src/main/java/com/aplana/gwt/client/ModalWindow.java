package com.aplana.gwt.client;

import com.aplana.gwt.client.modal.CanHide;
import com.aplana.gwt.client.modal.OnHideHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.*;

/**
 * Модальное окно.
 *
 * @author : vpetrov
 * @since : 16.12.13
 */
public class ModalWindow extends DialogBox implements CanHide {

    interface LocalHtmlTemplates extends SafeHtmlTemplates {
        @Template("<div style=\"" +
                "text-decoration: underline;" +
                "font-size: 12px; " +
                "margin-left: 3px; " +
                "display: inline; " +
                "vertical-align: middle;\">{0}" +
                "</div>")
        SafeHtml text(String text);

        @Template("<img style=\"display: inline; border: none; vertical-align: middle;\" src=\"{0}\">")
        SafeHtml img(String url);
    }

    interface ModalWindowResources extends ClientBundle {
        @Source("close.png")
        @ImageResource.ImageOptions(repeatStyle = ImageResource.RepeatStyle.Both)
        ImageResource closeImage();

        @Source("icon.png")
        @ImageResource.ImageOptions(repeatStyle = ImageResource.RepeatStyle.Both)
        ImageResource iconImage();
    }
    private ModalWindowResources mwRes = GWT.create(ModalWindowResources.class);

    private Image icon;
    private Image close;

    private Label title;

    private OnHideHandler<CanHide> hideHandler = new OnHideHandler<CanHide>() {
        @Override
        public void OnHide(CanHide modalWindow) {
            modalWindow.hide();
        }
    };

    public ModalWindow() {
        super(false, true);
        this.setGlassEnabled(true);

        title = new Label();
        close = new Image(mwRes.closeImage());
        icon = new Image(mwRes.iconImage());

        title.getElement().getStyle().setFloat(Style.Float.LEFT);
        title.getElement().getStyle().setLineHeight(26, Style.Unit.PX);
        close.getElement().getStyle().setCursor(Style.Cursor.POINTER);
        close.getElement().getStyle().setFloat(Style.Float.RIGHT);
        close.getElement().getStyle().setMargin(4, Style.Unit.PX);
        icon.getElement().getStyle().setFloat(Style.Float.LEFT);
        icon.getElement().getStyle().setMargin(4, Style.Unit.PX);

        HTMLPanel caption = new HTMLPanel("");
        caption.getElement().getStyle().setDisplay(Style.Display.INLINE_BLOCK);
        caption.setWidth("100%");
        caption.addStyleName("Caption");

        caption.add(icon);
        caption.add(title);
        caption.add(close);

        Element td = getCellElement(0, 1);
        td.getFirstChildElement().getParentElement().getStyle().setHeight(27, Style.Unit.PX);
        DOM.removeChild(td, (Element) td.getFirstChildElement());
        DOM.appendChild(td, caption.getElement());
    }

    /**
     * @param title - заголовок окна
     */
    public ModalWindow(String title) {
        this();
        setTitle(title);
    }

    public ModalWindow(String title, String iconUrl) {
        this(title);
        setIconUrl(iconUrl);
    }

    public ModalWindow(OnHideHandler<CanHide> hideHandler) {
        this();
        this.hideHandler = hideHandler;
    }

    public ModalWindow(String title, OnHideHandler<CanHide> hideHandler) {
        this(title);
        this.hideHandler = hideHandler;
    }

    public void setIconUrl(String iconUrl){
        if (iconUrl != null && !iconUrl.trim().isEmpty()) {
            icon.setUrl(iconUrl);
            icon.getElement().getStyle().setFloat(Style.Float.LEFT);
            icon.getElement().getStyle().setMargin(4, Style.Unit.PX);
        }
    }

    public String getIconUrl() {
        return icon.getUrl();
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
    public String getText() {
        return this.title.getText();
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
        if (hideHandler != null) {
            this.hideHandler = hideHandler;
        }
    }

}
