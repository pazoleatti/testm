package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

import java.util.List;

/**
 * @author Eugene Stetsenko
 */
public class UpdateTable extends
		GwtEvent<UpdateTable.UpdateTableHandler> {

	public interface UpdateTableHandler extends EventHandler {
		/**
		 * @param event
		 */
		void onUpdateTable(UpdateTable event);
	}

	private static final Type<UpdateTableHandler> TYPE = new Type<UpdateTableHandler>();

	public static Type<UpdateTableHandler> getType() {
		return TYPE;
	}

    private List<Integer> departments;

    public List<Integer> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Integer> departments) {
        this.departments = departments;
    }

    public static void fire(HasHandlers source) {
		UpdateTable event = new UpdateTable();
		source.fireEvent(event);
	}

    public static void fire(HasHandlers source, List<Integer> departments) {
        UpdateTable event = new UpdateTable();
        event.setDepartments(departments);
        source.fireEvent(event);
    }

	public UpdateTable() {
	}

	@Override
	protected void dispatch(UpdateTableHandler handler) {
		handler.onUpdateTable(this);
	}

	@Override
	public Type<UpdateTableHandler> getAssociatedType() {
		return getType();
	}
}
