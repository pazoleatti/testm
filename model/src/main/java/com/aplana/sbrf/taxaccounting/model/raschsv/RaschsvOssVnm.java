package com.aplana.sbrf.taxaccounting.model.raschsv;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Расчет сумм страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности и в связи с материнством (РасчСВ_ОСС.ВНМ)
 */
public class RaschsvOssVnm extends IdentityObject<Long> {

    private Long raschsvObyazPlatSvId;
    private String prizVypl;

    // Связь РасчСВ_ОСС.ВНМ и СвСум1Тип
    List<RaschsvOssVnmSum> raschsvOssVnmSumList;

    // Связь РасчСВ_ОСС.ВНМ и КолЛицТип
    List<RaschsvOssVnmKol> raschsvOssVnmKolList;

    // УплСВПрев
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
    public void addRaschsvOssVnmSum(RaschsvOssVnmSum raschsvOssVnmSum) {raschsvOssVnmSumList.add(raschsvOssVnmSum);}

    public List<RaschsvOssVnmKol> getRaschsvOssVnmKolList() {
        return raschsvOssVnmKolList;
    }
    public void setRaschsvOssVnmKolList(List<RaschsvOssVnmKol> raschsvOssVnmKolList) {
        this.raschsvOssVnmKolList = raschsvOssVnmKolList;
    }
    public void addRaschsvOssVnmKol(RaschsvOssVnmKol raschsvOssVnmKol) {raschsvOssVnmKolList.add(raschsvOssVnmKol);}

    public List<RaschsvUplSvPrev> getRaschsvUplSvPrevList() {
        return raschsvUplSvPrevList;
    }
    public void setRaschsvUplSvPrevList(List<RaschsvUplSvPrev> raschsvUplSvPrevList) {
        this.raschsvUplSvPrevList = raschsvUplSvPrevList;
    }
}
