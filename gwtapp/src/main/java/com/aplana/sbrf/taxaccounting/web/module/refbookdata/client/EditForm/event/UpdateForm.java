package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.event;

import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RecordChanges;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * @author Eugene Stetsenko
 */
public class UpdateForm extends
		GwtEvent<UpdateForm.UpdateFormHandler> {

	public interface UpdateFormHandler extends EventHandler {
		/**
		 * @param event
		 */
		void onUpdateForm(UpdateForm event);
	}

	private static final Type<UpdateFormHandler> TYPE = new Type<UpdateFormHandler>();

	public static Type<UpdateFormHandler> getType() {
		return TYPE;
	}

	public static void fire(HasHandlers source,	boolean success) {
		UpdateForm event = new UpdateForm();
		event.setSuccess(success);
		source.fireEvent(event);
	}

    public static void fire(HasHandlers source,	boolean success, RecordChanges recordChanges) {
        UpdateForm event = new UpdateForm();
        event.setRecordChanges(recordChanges);
        event.setSuccess(success);
        source.fireEvent(event);
    }

    private RecordChanges recordChanges;
	private boolean success;

	public UpdateForm() {
	}

	@Override
	protected void dispatch(UpdateFormHandler handler) {
		handler.onUpdateForm(this);
	}

	@Override
	public Type<UpdateFormHandler> getAssociatedType() {
		return getType();
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

    public RecordChanges getRecordChanges() {
        return recordChanges;
    }

    public void setRecordChanges(RecordChanges recordChanges) {
        this.recordChanges = recordChanges;
    }
}
