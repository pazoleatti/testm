package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.move_to_create;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import java.util.List;

public class CommentEvent extends GwtEvent<CommentEvent.CommentEventHandler>{

    public interface CommentEventHandler extends EventHandler{
        void update(CommentEvent event);
    }

    private String comment;

    private List<Long> declarationDataIdList;

    public static Type<CommentEvent.CommentEventHandler> TYPE = new Type<CommentEvent.CommentEventHandler>();

    public CommentEvent(String comment, List<Long> declarationDataIdList) {
        this.comment = comment;
        this.declarationDataIdList = declarationDataIdList;
    }

    @Override
    public Type<CommentEvent.CommentEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(CommentEventHandler handler) {
        handler.update(this);
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<Long> getDeclarationDataIdList() {
        return declarationDataIdList;
    }

    public void setDeclarationDataIdList(List<Long> declarationDataIdList) {
        this.declarationDataIdList = declarationDataIdList;
    }
}
