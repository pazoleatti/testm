package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.io.Serializable;

/**
 * @author aivanov
 */
public class GetHierarchyPathAction extends UnsecuredActionImpl<GetHierarchyPathResult> implements Serializable, ActionName {
    private static final long serialVersionUID = -5419457754608198048L;

    private long refBookAttrId;
    private long uniqueRecordId;

    public long getRefBookAttrId() {
        return refBookAttrId;
    }

    public void setRefBookAttrId(long refBookAttrId) {
        this.refBookAttrId = refBookAttrId;
    }

    public long getUniqueRecordId() {
        return uniqueRecordId;
    }

    public void setUniqueRecordId(long uniqueRecordId) {
        this.uniqueRecordId = uniqueRecordId;
    }

    @Override
    public String getName() {
        return "Получение иерархичного списка родителей справочника";
    }
}