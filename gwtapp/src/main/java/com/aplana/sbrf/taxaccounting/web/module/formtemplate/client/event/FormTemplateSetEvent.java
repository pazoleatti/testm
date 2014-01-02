package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.event;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

import java.util.List;

public class FormTemplateSetEvent extends GwtEvent<FormTemplateSetEvent.MyHandler> {

	public static interface MyHandler extends EventHandler {
		void onSet(FormTemplateSetEvent event);
	}

	private static final Type<MyHandler> TYPE = new Type<MyHandler>();

	public static Type<MyHandler> getType() {
		return TYPE;
	}

	private final FormTemplate formTemplate;
    private final List<RefBook> refBookList;

	public FormTemplateSetEvent(FormTemplate formTemplate, List<RefBook> refBookList) {
		this.formTemplate = formTemplate;
        this.refBookList = refBookList;
	}

	public static void fire(HasHandlers source, FormTemplate formTemplate, List<RefBook> refBookList) {
		source.fireEvent(new FormTemplateSetEvent(formTemplate, refBookList));
	}

	@Override
	protected void dispatch(MyHandler handler) {
		handler.onSet(this);
	}

	@Override
	public Type<MyHandler> getAssociatedType() {
		return getType();
	}

	public FormTemplate getFormTemplate() {
		return formTemplate;
	}

    public List<RefBook> getRefBookList() {
        return refBookList;
    }
}
