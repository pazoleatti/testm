package com.aplana.sbrf.taxaccounting.model.raschsv;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица (СвВыпл)
 */
public class RaschsvSvVypl extends IdentityObject<Long> {

    private Long raschsvPersSvStrahLicId;
    private Double sumVyplVs3;
    private Double vyplOpsVs3;
    private Double vyplOpsDogVs3;
    private Double nachislSvVs3;

    // СвВыплМК
    private List<RaschsvSvVyplMt> raschsvSvVyplMtList;

    public RaschsvSvVypl() {
        super();
        raschsvSvVyplMtList = new ArrayList<RaschsvSvVyplMt>();
    }

    public static final String SEQ = "seq_raschsv_sv_vypl";
    public static final String TABLE_NAME = "raschsv_sv_vypl";
    public static final String COL_ID = "id";
    public static final String COL_RASCHSV_PERS_SV_STRAH_LIC_ID = "raschsv_pers_sv_strah_lic_id";
    public static final String COL_SUM_VYPL_VS3 = "sum_vypl_vs3";
    public static final String COL_VYPL_OPS_VS3 = "vypl_ops_vs3";
    public static final String COL_VYPL_OPS_DOG_VS3 = "vypl_ops_dog_vs3";
    public static final String COL_NACHISL_SV_VS3 = "nachisl_sv_vs3";

    public static final String[] COLUMNS = {COL_ID, COL_RASCHSV_PERS_SV_STRAH_LIC_ID, COL_SUM_VYPL_VS3, COL_VYPL_OPS_VS3,
            COL_VYPL_OPS_DOG_VS3, COL_NACHISL_SV_VS3};

    public Long getRaschsvPersSvStrahLicId() { return raschsvPersSvStrahLicId; }
    public void setRaschsvPersSvStrahLicId(Long raschsvPersSvStrahLicId) { this.raschsvPersSvStrahLicId = raschsvPersSvStrahLicId; }

    public Double getSumVyplVs3() { return sumVyplVs3; }
    public void setSumVyplVs3(Double sumVyplVs3) { this.sumVyplVs3 = sumVyplVs3; }

    public Double getVyplOpsVs3() { return vyplOpsVs3; }
    public void setVyplOpsVs3(Double vyplOpsVs3) { this.vyplOpsVs3 = vyplOpsVs3; }

    public Double getVyplOpsDogVs3() { return vyplOpsDogVs3; }
    public void setVyplOpsDogVs3(Double vyplOpsDogVs3) { this.vyplOpsDogVs3 = vyplOpsDogVs3; }

    public Double getNachislSvVs3() { return nachislSvVs3; }
    public void setNachislSvVs3(Double nachislSvVs3) { this.nachislSvVs3 = nachislSvVs3; }

    public List<RaschsvSvVyplMt> getRaschsvSvVyplMtList() {
        return raschsvSvVyplMtList != null ? raschsvSvVyplMtList : Collections.<RaschsvSvVyplMt>emptyList();
    }
    public void setRaschsvSvVyplMtList(List<RaschsvSvVyplMt> raschsvSvVyplMtList) { this.raschsvSvVyplMtList = raschsvSvVyplMtList; }
    public void addRaschsvSvVyplMt(RaschsvSvVyplMt raschsvSvVyplMt) {
        raschsvSvVyplMtList.add(raschsvSvVyplMt);
    }
}
