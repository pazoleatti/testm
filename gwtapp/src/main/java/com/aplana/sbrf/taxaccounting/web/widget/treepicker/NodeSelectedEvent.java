package com.aplana.sbrf.taxaccounting.web.widget.treepicker;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class NodeSelectedEvent extends GwtEvent<NodeSelectedEvent.NodeSelectedEventHandler>{

	public static interface NodeSelectedEventHandler extends EventHandler {
		void onNodeSelected(NodeSelectedEvent event);
	}

	public static Type<NodeSelectedEventHandler> TYPE = new Type<NodeSelectedEventHandler>();

	private final Integer nodeId;

	private final boolean childSelect;

	public NodeSelectedEvent(Integer nodeId, boolean setChildSelected) {
		this.childSelect = setChildSelected;
		this.nodeId = nodeId;
	}

	@Override
	public Type<NodeSelectedEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(NodeSelectedEventHandler handler) {
		handler.onNodeSelected(this);
	}

	public Integer getNodeId() {
		return nodeId;
	}

	public boolean isChildSelect() {
		return childSelect;
	}
}
