package com.aplana.sbrf.taxaccounting.web.widget.cell;

import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.text.shared.SimpleSafeHtmlRenderer;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;

import static com.google.gwt.dom.client.BrowserEvents.CLICK;
import static com.google.gwt.dom.client.BrowserEvents.KEYDOWN;

public class SortingHeaderCell extends AbstractSafeHtmlCell<String> {

	private static final int ESCAPE = 27;
	private static final String ARROW_ASC_IMAGE_SOURCE = "resources/img/sort_arrow_asc_image.png";
	private static final String ARROW_DES_IMAGE_SOURCE = "resources/img/sort_arrow_des_image.png";
	private static final String ARROW_IMAGE_WIDTH = "10px";
	private static final String ARROW_IMAGE_HEIGHT = "10px";
	private static final String HEADER_BACKGROUND_COLOR = "#949494";
	private static final Image arrowAscImage;
	private static final Image arrowDesImage;
	private static boolean isAscSort = true;
	private PopupPanel popupWithArrow;
	private Element lastParent;

	static {
		arrowAscImage = new Image();
		arrowAscImage.setUrl(ARROW_ASC_IMAGE_SOURCE);
		arrowAscImage.setWidth(ARROW_IMAGE_WIDTH);
		arrowAscImage.setHeight(ARROW_IMAGE_HEIGHT);
		arrowAscImage.getElement().getStyle().setVerticalAlign(Style.VerticalAlign.MIDDLE);

		arrowDesImage = new Image();
		arrowDesImage.setUrl(ARROW_DES_IMAGE_SOURCE);
		arrowDesImage.setWidth(ARROW_IMAGE_WIDTH);
		arrowDesImage.setHeight(ARROW_IMAGE_HEIGHT);
		arrowDesImage.getElement().getStyle().setVerticalAlign(Style.VerticalAlign.MIDDLE);
	}

	public SortingHeaderCell() {
		this(SimpleSafeHtmlRenderer.getInstance());
	}

	public SortingHeaderCell(SafeHtmlRenderer<String> renderer) {
		super(renderer, CLICK, KEYDOWN);

		this.popupWithArrow = new PopupPanel(true, false) {
			@Override
			protected void onPreviewNativeEvent(Event.NativePreviewEvent event) {
				if (Event.ONKEYUP == event.getTypeInt()) {
					if (event.getNativeEvent().getKeyCode() == ESCAPE) {
						// Dismiss when escape is pressed
						popupWithArrow.hide();
					}
				}
			}
		};

		popupWithArrow.getElement().getStyle().setBorderStyle(Style.BorderStyle.HIDDEN);
		popupWithArrow.getElement().getStyle().setBackgroundColor(HEADER_BACKGROUND_COLOR);
	}

	@Override
	public void onBrowserEvent(Context context, Element parent, String value,
							   NativeEvent event, ValueUpdater<String> valueUpdater) {
		super.onBrowserEvent(context, parent, value, event, valueUpdater);
		if (CLICK.equals(event.getType())) {
			onEnterKeyDown(context, parent, value, event, valueUpdater);
		}
	}

	@Override
	protected void onEnterKeyDown(Context context, Element parent, String value,
								  NativeEvent event, ValueUpdater<String> valueUpdater) {
		if (valueUpdater != null) {
			valueUpdater.update(value);

			this.lastParent = parent;
			this.popupWithArrow.clear();

			if(isAscSort){
				popupWithArrow.add(arrowAscImage);
				isAscSort = false;
			} else {
				popupWithArrow.add(arrowDesImage);
				isAscSort = true;
			}

			popupWithArrow.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
				public void setPosition(int offsetWidth, int offsetHeight) {
					int exceedOffsetY = 5;
					int exceedOffsetX = 5;

					popupWithArrow.setPopupPosition(
							lastParent.getAbsoluteLeft() + lastParent.getClientWidth() - popupWithArrow.getOffsetWidth() - exceedOffsetX,
							lastParent.getAbsoluteTop() + exceedOffsetY
					);
				}
			});
		}
	}

	@Override
	protected void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {
		if (value != null) {
			sb.append(value);
		}
	}
}
