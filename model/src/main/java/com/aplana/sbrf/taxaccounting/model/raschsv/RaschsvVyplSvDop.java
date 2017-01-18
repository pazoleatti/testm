package com.aplana.sbrf.taxaccounting.model.raschsv;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица, на которые исчислены страховые взносы по дополнительному тарифу (ВыплСВДоп)
 */
public class RaschsvVyplSvDop extends IdentityObject<Long> {

    private Long raschsvPersSvStrahLicId;
    private Double vyplSvVs3;
    private Double nachislSvVs3;

    // ВыплСВДопМТ
    private List<RaschsvVyplSvDopMt> raschsvVyplSvDopMtList;

    public RaschsvVyplSvDop() {
        super();
        raschsvVyplSvDopMtList = new ArrayList<RaschsvVyplSvDopMt>();
    }

    public static final String SEQ = "seq_raschsv_vypl_sv_dop";
    public static final String TABLE_NAME = "raschsv_vypl_sv_dop";
    public static final String COL_ID = "id";
    public static final String COL_RASCHSV_PERS_SV_STRAH_LIC_ID = "raschsv_pers_sv_strah_lic_id";
    public static final String COL_VYPL_SV_VS3 = "vypl_sv_vs3";
    public static final String COL_NACHISL_SV_VS3 = "nachisl_sv_vs3";

    public static final String[] COLUMNS = {COL_ID, COL_RASCHSV_PERS_SV_STRAH_LIC_ID, COL_VYPL_SV_VS3, COL_NACHISL_SV_VS3};

    public Long getRaschsvPersSvStrahLicId() { return raschsvPersSvStrahLicId; }
    public void setRaschsvPersSvStrahLicId(Long raschsvPersSvStrahLicId) { this.raschsvPersSvStrahLicId = raschsvPersSvStrahLicId; }

    public Double getVyplSvVs3() { return vyplSvVs3; }
    public void setVyplSvVs3(Double vyplSvVs3) { this.vyplSvVs3 = vyplSvVs3; }

    public Double getNachislSvVs3() { return nachislSvVs3; }
    public void setNachislSvVs3(Double nachislSvVs3) { this.nachislSvVs3 = nachislSvVs3; }

    public List<RaschsvVyplSvDopMt> getRaschsvVyplSvDopMtList() {
        return raschsvVyplSvDopMtList != null ? raschsvVyplSvDopMtList : Collections.<RaschsvVyplSvDopMt>emptyList();
    }
    public void setRaschsvVyplSvDopMtList(List<RaschsvVyplSvDopMt> raschsvVyplSvDopMtList) { this.raschsvVyplSvDopMtList = raschsvVyplSvDopMtList; }
    public void addRaschsvVyplSvDopMt(RaschsvVyplSvDopMt raschsvVyplSvDopMt) {
        raschsvVyplSvDopMtList.add(raschsvVyplSvDopMt);
    }
}
