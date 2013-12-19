package com.aplana.gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.*;


/**
 * Модальное окно.
 * @author : vpetrov
 * Date: 16.12.13
 */
public class ModalWindow extends DialogBox {

    Image close;
    HTML title;

    private PopupPanel popup;
    private Image icon = new Image();
    private VerticalPanel rootPanel = new VerticalPanel();
    private SimplePanel mainPanel = new SimplePanel();
    private FlowPanel captionPanel = new FlowPanel();
    private FlowPanel captionTitlePanel = new FlowPanel();
    private HorizontalPanel footerPanel = new HorizontalPanel();
    private FlowPanel defaultButtonsPanel = new FlowPanel();
    private FlowPanel additionalButtonsPanel = new FlowPanel();
    private Button saveButton;
    private Button cancelButton;

    /**
     * @param title - заголовок окна
     * @param iconUrl - url ссылка на иконку в заголовке окна
     */
    public ModalWindow(String title, String iconUrl) {

        super(false, true);
        popup = this;
        this.addStyleName("AplanaModalWindow");


        ModalWindowResources closeImgRes = GWT.create(ModalWindowResources.class);
        close = new Image(closeImgRes.closeImage());

        if (iconUrl.equals("")){
            icon.setUrl(iconUrl);
            icon.setVisible(true);
        }
        else {
            icon.setVisible(false);
        }

        // Переделываем стиль DialogBox
        Element td = getCellElement(0, 1);
        Element r0c0 = getCellElement (0,0);
        r0c0.getParentElement().addClassName("OverrideA");
        Element r0c2 = getCellElement (0,2);
        r0c2.getParentElement().addClassName("OverrideA");
        Element r1c0 = getCellElement (1,0);
        r1c0.getParentElement().addClassName("OverrideB");
        Element r1c2 = getCellElement (1,2);
        r1c2.getParentElement().addClassName("OverrideB");
        Element r2c0 = getCellElement (2,0);
        r2c0.getParentElement().addClassName("OverrideC");
        Element r2c2 = getCellElement (2,2);
        r2c2.getParentElement().addClassName("OverrideC");

        Element td1 = getCellElement(1, 1);

        td.getParentElement().addClassName("OverrideCenter");
        td1.getParentElement().addClassName("OverrideCenter");

        this.title = new HTML();
        this.title.setText(title);
        this.title.addStyleName("captionTitle");
        if (icon.isVisible())
            captionTitlePanel.add(icon);

        icon.addStyleName("icon");
        close.addStyleName("closeButton");
        captionTitlePanel.addStyleName("captionTitlePanel");

        captionTitlePanel.add(this.title);
        captionPanel.add(captionTitlePanel);
        captionPanel.add(close);
        captionPanel.addStyleName("caption");

        saveButton = new Button("Сохранить");
        saveButton.addStyleName("footerButton");
        cancelButton = new Button("Отмена");
        cancelButton.addStyleName("footerButton");

        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                popup.hide();
            }
        });

        additionalButtonsPanel.addStyleName("additionalButtonsPanel");

        defaultButtonsPanel.add(saveButton);
        defaultButtonsPanel.add(cancelButton);
        defaultButtonsPanel.addStyleName("defaultButtonPanel");

        footerPanel.add(additionalButtonsPanel);
        footerPanel.add(defaultButtonsPanel);
        footerPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        footerPanel.addStyleName("footerPanel");

        rootPanel.addStyleName("body");
        rootPanel.add(mainPanel);
        rootPanel.add(footerPanel);

        DOM.removeChild(td, (Element) td.getFirstChildElement());
        DOM.appendChild(td, captionPanel.getElement());

        super.add(rootPanel);

        super.setAnimationEnabled(true);
        center();
    }

    /**
     * @param title - заголовок окна
     */
    public ModalWindow(String title)
    {
        this(title, "");
    }

    public ModalWindow()
    {
        this("");
    }

    /**
     * Добавляет дополнительные кнопки в нижнюю панель слева
     * @param w - добовляемая кнопка
     */
    public  void addAdditionalButton(Widget w){
        additionalButtonsPanel.add(w);
    }

    /**
     * Очищает дополнительные кнопки
     */
    public void clearAdditionalButton(){
        additionalButtonsPanel.clear();
    }

    /**
     * Добавляет handler к кнопке "сохранить"
     * @param handler - хендлер
     */
    public void addSaveButtonClickHandler (ClickHandler handler){
        saveButton.addClickHandler(handler);
    }

    /**
     * Добавляет handler к кнопке "отменить"
     * @param handler - хендлер
     */
    public void addСancelButtonClickHandler(ClickHandler handler){
        cancelButton.addClickHandler(handler);
    }

    @Override
    public void add(Widget w){
        this.mainPanel.add(w);
    }

    @Override
    public void clear(){
        this.mainPanel.clear();
    }

    @Override
    public String getHTML()
    {
        return this.title.getHTML();
    }

    @Override
    public String getText()
    {
        return this.title.getText();
    }

    @Override
    public void setHTML(String html)
    {
        this.title.setHTML(html);
    }

    @Override
    public void setText(String text)
    {
        this.title.setText(text);
    }

    @Override
    protected void onPreviewNativeEvent(Event.NativePreviewEvent event)
    {
        NativeEvent nativeEvent = event.getNativeEvent();

        if ((!event.isCanceled()
                && (event.getTypeInt() == Event.ONCLICK)
                && isCloseEvent(nativeEvent))
                ||(Event.ONKEYUP == event.getTypeInt() && event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE))
        {
            this.hide();
        }
        if (Event.ONKEYUP == event.getTypeInt() && event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER){
            saveButton.fireEvent(new ClickEvent(){});
        }
        super.onPreviewNativeEvent(event);
    }

    private boolean isCloseEvent(NativeEvent event)
    {
        return event.getEventTarget().equals(close.getElement());
    }

    interface ModalWindowResources extends ClientBundle {
        @Source("close.png")
        @ImageResource.ImageOptions(repeatStyle = ImageResource.RepeatStyle.Both)
        ImageResource closeImage();
    }
}
