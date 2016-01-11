package com.aplana.sbrf.taxaccounting.web.widget.menu.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Проверка состояния отчета
 * @author lhaziev
 */
public class CheckReportNotificationAction extends UnsecuredActionImpl<CheckReportNotificationResult> {
    /** id оповещения*/
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}

