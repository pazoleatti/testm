package com.aplana.sbrf.taxaccounting.model.raschsv;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Расчет сумм страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности и в связи с материнством
 */
public class RaschsvOssVnm extends IdentityObject<Long> {

    private Long raschsvObyazPlatSvId;
    private String prizVypl;

    // Сумма для расчета сумм страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности и в связи с материнством
    List<RaschsvOssVnmSum> raschsvOssVnmSumList;

    // Количество для расчета сумм страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности и в связи с материнством
    List<RaschsvOssVnmKol> raschsvOssVnmKolList;

    // Сумма страховых взносов, подлежащая к уплате (сумма превышения произведенных расходов над исчисленными страховыми взносами)
    List<RaschsvUplSvPrev> raschsvUplSvPrevList;

    public RaschsvOssVnm() {
        super();
        raschsvOssVnmSumList = new ArrayList<RaschsvOssVnmSum>();
        raschsvOssVnmKolList = new ArrayList<RaschsvOssVnmKol>();
        raschsvUplSvPrevList = new ArrayList<RaschsvUplSvPrev>();
    }

    public static final String SEQ = "seq_raschsv_oss_vnm";
    public static final String TABLE_NAME = "raschsv_oss_vnm";
    public static final String COL_ID = "id";
    public static final String COL_RASCHSV_OBYAZ_PLAT_SV_ID = "raschsv_obyaz_plat_sv_id";
    public static final String COL_PRIZ_VYPL = "priz_vypl";

    public static final String[] COLUMNS = {COL_ID, COL_RASCHSV_OBYAZ_PLAT_SV_ID, COL_PRIZ_VYPL};

    public Long getRaschsvObyazPlatSvId() { return raschsvObyazPlatSvId; }
    public void setRaschsvObyazPlatSvId(Long raschsvObyazPlatSvId) { this.raschsvObyazPlatSvId = raschsvObyazPlatSvId; }

    public String getPrizVypl() {
        return prizVypl;
    }
    public void setPrizVypl(String prizVypl) {
        this.prizVypl = prizVypl;
    }

    public List<RaschsvOssVnmSum> getRaschsvOssVnmSumList() {
        return raschsvOssVnmSumList;
    }
    public void setRaschsvOssVnmSumList(List<RaschsvOssVnmSum> raschsvOssVnmSumList) {
        this.raschsvOssVnmSumList = raschsvOssVnmSumList;
    }

    public List<RaschsvOssVnmKol> getRaschsvOssVnmKolList() {
        return raschsvOssVnmKolList;
    }
    public void setRaschsvOssVnmKolList(List<RaschsvOssVnmKol> raschsvOssVnmKolList) {
        this.raschsvOssVnmKolList = raschsvOssVnmKolList;
    }

    public List<RaschsvUplSvPrev> getRaschsvUplSvPrevList() {
        return raschsvUplSvPrevList;
    }
    public void setRaschsvUplSvPrevList(List<RaschsvUplSvPrev> raschsvUplSvPrevList) {
        this.raschsvUplSvPrevList = raschsvUplSvPrevList;
    }
}
