package com.aplana.sbrf.taxaccounting.web.widget.style;

import com.google.gwt.core.client.GWT;
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
		@Template("<div style=\"text-decoration: underline;"+
				"color: #004276; font-size: 12px; margin-left: 20px;\">{0}</div>")
		SafeHtml text(String text);

		@Template("<img style=\"position:absolute; border: none;\" src=\"{0}\">")
		SafeHtml img(String url);
	}

	private static final LocalHtmlTemplates templates = GWT.create(LocalHtmlTemplates.class);
	private static final String DEFAULT_URL = "resources/img/starfish-16.png";
	private String text = "";

	public LinkAnchor() {
		super(templates.img(DEFAULT_URL));
	}

	public void setText(String text){
		this.text = text;
		setHTML(getHTML() +	templates.text(text).asString());
	}

	public void setImg(String url) {
		setHTML(templates.img(url).asString() + (!text.isEmpty() ? templates.text(text).asString() : ""));
	}
}
