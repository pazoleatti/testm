package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 *
 * @author auldanov
 * Created on 28.03.2014.
 */
public class SearchAction extends UnsecuredActionImpl<SearchResult> implements ActionName {

    private String key;
    private Long formDataId;
    private int from;
    private int to;
    private boolean caseSensitive;
    private boolean manual;
    private Integer formTemplateId;
    private boolean correctionDiff;
    private int sessionId;
    private boolean isJustDelete;

    public Integer getFormTemplateId() {
        return formTemplateId;
    }

    public void setFormTemplateId(Integer formTemplateId) {
        this.formTemplateId = formTemplateId;
    }

    public Long getFormDataId() {
        return formDataId;
    }

    public void setFormDataId(Long formDataId) {
        this.formDataId = formDataId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    @Override
    public String getName() {
        return "SearchAction";
    }

    public boolean isManual() {
        return manual;
    }

    public void setManual(boolean manual) {
        this.manual = manual;
    }

    public boolean isCorrectionDiff() {
        return correctionDiff;
    }

    public void setCorrectionDiff(boolean correctionDiff) {
        this.correctionDiff = correctionDiff;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getSessionId() {
        return sessionId;
    }

    public boolean isJustDelete() {
        return isJustDelete;
    }

    public void setJustDelete(boolean isJustDelete) {
        this.isJustDelete = isJustDelete;
    }
}
