package com.aplana.sbrf.taxaccounting.model.raschsv;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

import java.util.Date;

/**
 * Сведения из реестра молодежных и детских объединений, пользующихся государственной поддержкой (СвРеестрМДО)
 */
public class RaschsvSvReestrMdo extends IdentityObject<Long> {

    private Long raschsvSvedObuchId;
    private String naimMdo;
    private Date dataZapis;
    private String nomerZapis;

    public static final String SEQ = "seq_raschsv_sv_reestr_mdo";
    public static final String TABLE_NAME = "raschsv_sv_reestr_mdo";
    public static final String COL_ID = "id";
    public static final String COL_RASCHSV_SVED_OBUCH_ID = "raschsv_sved_obuch_id";
    public static final String COL_NAIM_MDO = "naim_mdo";
    public static final String COL_DATA_ZAPIS = "data_zapis";
    public static final String COL_NOMER_ZAPIS = "nomer_zapis";

    public static final String[] COLUMNS = {COL_ID, COL_RASCHSV_SVED_OBUCH_ID, COL_NAIM_MDO, COL_DATA_ZAPIS, COL_NOMER_ZAPIS};

    public Long getRaschsvSvedObuchId() {
        return raschsvSvedObuchId;
    }
    public void setRaschsvSvedObuchId(Long raschsvSvedObuchId) {
        this.raschsvSvedObuchId = raschsvSvedObuchId;
    }

    public String getNaimMdo() {
        return naimMdo;
    }
    public void setNaimMdo(String naimMdo) {
        this.naimMdo = naimMdo;
    }

    public Date getDataZapis() {
        return dataZapis;
    }
    public void setDataZapis(Date dataZapis) {
        this.dataZapis = dataZapis;
    }

    public String getNomerZapis() {
        return nomerZapis;
    }
    public void setNomerZapis(String nomerZapis) {
        this.nomerZapis = nomerZapis;
    }
}
