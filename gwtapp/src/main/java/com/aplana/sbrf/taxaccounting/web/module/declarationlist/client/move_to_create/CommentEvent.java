package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.move_to_create;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class CommentEvent extends GwtEvent<CommentEvent.CommentEventHandler>{

    public interface CommentEventHandler extends EventHandler{
        void update(CommentEvent event);
    }

    private Long declarationDataId;

    private String comment;

    public static Type<CommentEvent.CommentEventHandler> TYPE = new Type<CommentEvent.CommentEventHandler>();

    public CommentEvent(Long declarationDataId, String comment) {
        this.declarationDataId = declarationDataId;
        this.comment = comment;
    }

    @Override
    public Type<CommentEvent.CommentEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(CommentEventHandler handler) {
        handler.update(this);
    }

    public Long getDeclarationDataId() {
        return declarationDataId;
    }

    public void setDeclarationDataId(Long declarationDataId) {
        this.declarationDataId = declarationDataId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
