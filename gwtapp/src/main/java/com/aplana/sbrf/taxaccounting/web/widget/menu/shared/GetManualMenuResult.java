package com.aplana.sbrf.taxaccounting.web.widget.menu.shared;

/**
 * @author Fail Mukhametdinov
 */
public class GetManualMenuResult extends AbstractMenuResult {

    private boolean canShowNotification;

    public boolean canShowNotification() {
        return canShowNotification;
    }

    public void setCanShowNotification(boolean canShowNotification) {
        this.canShowNotification = canShowNotification;
    }
}
