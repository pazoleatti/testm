package com.aplana.sbrf.taxaccounting.web.main.api.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Если вызвать это событие то, обновятся заголовки страницы. Если событие не
 * вызывать, то для получения заголовка будет использоваться механизм GWTP
 * 
 * @author sgoryachkin
 * 
 */
public class TitleUpdateEvent extends GwtEvent<TitleUpdateEvent.MyHandler> {

	public static interface MyHandler extends EventHandler {
		/**
		 * @param event
		 */
		void onTitleUpdate(TitleUpdateEvent event);
	}

	private static final Type<MyHandler> TYPE = new Type<MyHandler>();

	public static Type<MyHandler> getType() {
		return TYPE;
	}

	public static void fire(HasHandlers source, String title, String desc) {
		TitleUpdateEvent errorEvent = new TitleUpdateEvent();
		errorEvent.setTitle(title);
		errorEvent.setDesc(desc);
		source.fireEvent(errorEvent);
	}

	public static void fire(HasHandlers source, String title) {
		TitleUpdateEvent errorEvent = new TitleUpdateEvent();
		errorEvent.setTitle(title);
		source.fireEvent(errorEvent);
	}

	private String title;

	private String desc;

	public TitleUpdateEvent() {
	}

	@Override
	protected void dispatch(MyHandler handler) {
		handler.onTitleUpdate(this);
	}

	@Override
	public Type<MyHandler> getAssociatedType() {
		return getType();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

}
