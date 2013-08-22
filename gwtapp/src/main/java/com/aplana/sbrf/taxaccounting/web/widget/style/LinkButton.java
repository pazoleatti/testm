package com.aplana.sbrf.taxaccounting.web.widget.style;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.safehtml.client.HasSafeHtml;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.DirectionalTextHelper;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;

public class LinkButton extends FocusWidget implements HasHorizontalAlignment,
		HasHTML, HasSafeHtml {

	interface LocalHtmlTemplates extends SafeHtmlTemplates {
		@Template("<div style=\"cursor: pointer !important; cursor: hand !important\"><img style=\"position:absolute; border: none;\" src=\"{0}\"/><div style=\"text-decoration: underline;"
				+ "color: #004276; font-size: 12px; margin-left: 20px;\">{1}</div></div>")
		SafeHtml render(String url, String text);
	}

	private static final LocalHtmlTemplates templates = GWT
			.create(LocalHtmlTemplates.class);
	private static final String DEFAULT_IMG = "resources/img/starfish-16.png";
	private static final String DEFAULT_TEXT = "";
	private String text;
	private String img;

	public LinkButton() {
		this(null, null, null);
	}

	public LinkButton(String text) {
		this(text, null, null);
	}

	public LinkButton(String text, String img) {
		this(text, img, null);
	}

	public LinkButton(String text, String img, String title) {
		setElement(Document.get().createDivElement());
		setStyleName("gwt-Anchor");
		directionalTextHelper = new DirectionalTextHelper(getDivElement(), true);
		this.text = text == null ? DEFAULT_TEXT : text;
		this.img = img == null ? DEFAULT_IMG : img;
		this.setTitle(title);
		setHTML(templates.render(this.img, this.text));
	}

	@Override
	public void setText(String text) {
		this.text = text;
		setHTML(templates.render(this.img, this.text));
	}

	public void setImg(String url) {
		this.img = url;
		setHTML(templates.render(this.img, this.text));
	}

	private final DirectionalTextHelper directionalTextHelper;

	private HorizontalAlignmentConstant horzAlign;

	@Override
	public HorizontalAlignmentConstant getHorizontalAlignment() {
		return horzAlign;
	}

	@Override
	public String getHTML() {
		return getElement().getInnerHTML();
	}

	@Override
	public int getTabIndex() {
		return getDivElement().getTabIndex();
	}

	@Override
	public void setFocus(boolean focused) {
		if (focused) {
			getDivElement().focus();
		} else {
			getDivElement().blur();
		}
	}

	@Override
	public void setHorizontalAlignment(HorizontalAlignmentConstant align) {
		horzAlign = align;
		getElement().getStyle().setProperty("textAlign",
				align.getTextAlignString());
	}

	@Override
	public void setHTML(SafeHtml html) {
		setHTML(html.asString());
	}

	@Override
	public void setHTML(String html) {
		directionalTextHelper.setTextOrHtml(html, true);
	}

	@Override
	public void setTabIndex(int index) {
		getDivElement().setTabIndex(index);
	}

	private DivElement getDivElement() {
		return DivElement.as(getElement());
	}

	@Override
	public String getText() {
		return text;
	}
	
	public String getImg() {
		return img;
	}

}
