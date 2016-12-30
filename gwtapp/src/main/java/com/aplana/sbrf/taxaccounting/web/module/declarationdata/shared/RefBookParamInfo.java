package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import java.io.Serializable;

/**
 * Created by lhaziev on 29.12.2016.
 */
public class RefBookParamInfo implements Serializable {
    private static final long serialVersionUID = -1641653187913249641L;

    private String refBookName;
    private boolean isVersioned;
    private boolean isHierarchic;

    public RefBookParamInfo(String refBookName, boolean isVersioned, boolean isHierarchic) {
        this.refBookName = refBookName;
        this.isVersioned = isVersioned;
        this.isHierarchic = isHierarchic;
    }

    public RefBookParamInfo() {
    }

    public String getRefBookName() {
        return refBookName;
    }

    public boolean isVersioned() {
        return isVersioned;
    }

    public boolean isHierarchic() {
        return isHierarchic;
    }
}
