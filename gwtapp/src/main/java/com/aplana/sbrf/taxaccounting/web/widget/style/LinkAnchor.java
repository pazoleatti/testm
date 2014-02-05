package com.aplana.sbrf.taxaccounting.web.widget.style;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.Anchor;

/**
 * Ссылка с картинкой
 *
 * НЕ ИСПОЛЬЗУЙТЕ ЭТОТ ВИДЖЕТ ЕСЛИ
 * НУЖНА РЕАЛЬНО КНОПКА В ВИДЕ ССЫЛКИ
 * ДЛЯ ЭТОГО ИСПОЛЬЗУЙТЕ LinkButton
 *
 * http://jira.aplana.com/browse/SBRFACCTAX-3402
 *
 * @author sgoryachkin
 *
 */
public class LinkAnchor extends Anchor{

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

	private static final LocalHtmlTemplates templates = GWT.create(LocalHtmlTemplates.class);
	private static final String DEFAULT_URL = "resources/img/starfish-16.png";

	private String text = "";
	private String url = DEFAULT_URL;

	public LinkAnchor() {
		super(templates.img(DEFAULT_URL));
        getElement().getStyle().setTextDecoration(Style.TextDecoration.NONE);
        getElement().getStyle().setColor("#004276");
        getElement().getStyle().setCursor(Style.Cursor.POINTER);
	}

	public void setText(String text){
		this.text = text;
        setupHtml();
	}

	public void setImg(String imgUrl) {
        url = imgUrl;
        setupHtml();
	}

    public void setDisableImage(Boolean disable) {
        if (disable) {
            url = "";
        }
        setupHtml();
    }

    private void setupHtml(){
        String image = !url.isEmpty() ? templates.img(url).asString() : "";
        String textDiv = !text.isEmpty() ? templates.text(text).asString() : "";
        setHTML(image + textDiv);
    }

    @Override
    public void fireEvent(GwtEvent<?> event){
        if (this.isEnabled())
            super.fireEvent(event);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled){
            getElement().getStyle().setColor("#004276");
            getElement().getStyle().setCursor(Style.Cursor.POINTER);
        }
        else{
            getElement().getStyle().setColor("#ADADAD");
            getElement().getStyle().setCursor(Style.Cursor.DEFAULT);
        }
    }

}
