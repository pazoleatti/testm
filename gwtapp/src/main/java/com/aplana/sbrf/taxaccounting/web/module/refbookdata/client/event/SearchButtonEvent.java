package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

import java.util.Date;

/**
 * User: avanteev
 * Нажатие на кнопку поиск
 */
public class SearchButtonEvent extends GwtEvent<SearchButtonEvent.SearchHandler> {

    private Date relevanceDate;
    private String searchPattern;

    public Date getRelevanceDate() {
        return relevanceDate;
    }

    public String getSearchPattern() {
        return searchPattern;
    }

    public SearchButtonEvent(Date relevanceDate, String searchPattern) {
        this.relevanceDate = relevanceDate;
        this.searchPattern = searchPattern;
    }

    private static final GwtEvent.Type<SearchHandler> TYPE = new GwtEvent.Type<SearchHandler>();

    public static GwtEvent.Type<SearchHandler> getType() {
        return TYPE;
    }

    @Override
    public GwtEvent.Type<SearchHandler> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(SearchHandler handler) {
        handler.onSearch(this);
    }

    public interface SearchHandler extends EventHandler {
        void onSearch(SearchButtonEvent event);
    }

    public static void fire(HasHandlers source, Date date, String pattern) {
        source.fireEvent(new SearchButtonEvent(date, pattern));
    }
}
