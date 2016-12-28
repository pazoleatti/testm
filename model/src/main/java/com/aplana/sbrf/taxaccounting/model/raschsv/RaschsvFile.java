package com.aplana.sbrf.taxaccounting.model.raschsv;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

/**
 * Файл обмена
 */
public class RaschsvFile extends IdentityObject<Long> {

    private String idFile;

    public static final String TABLE_NAME = "raschsv_file";
    public static final String SEQ = "seq_raschsv_file";
    public static final String COL_ID = "id";
    public static final String COL_ID_FILE = "id_file";

    public String getIdFile() { return idFile; }
    public void setIdFile(String idFile) { this.idFile = idFile; }
}
