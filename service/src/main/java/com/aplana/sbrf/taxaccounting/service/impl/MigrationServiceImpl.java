package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.MigrationDao;
import com.aplana.sbrf.taxaccounting.model.migration.enums.*;
import com.aplana.sbrf.taxaccounting.model.migration.row.AbstractRnuRow;
import com.aplana.sbrf.taxaccounting.model.migration.Exemplar;
import com.aplana.sbrf.taxaccounting.service.MigrationService;
import com.aplana.sbrf.taxaccounting.service.RnuGenerationService;
import com.aplana.sbrf.taxaccounting.service.XmlGenerationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class MigrationServiceImpl implements MigrationService {

    private final Log logger = LogFactory.getLog(getClass());

    @Autowired
    private MigrationDao migrationDao;

    @Autowired
    private XmlGenerationService xmlService;

    @Autowired
    private RnuGenerationService rnuService;

    @Override
    public List<Exemplar> getActualExemplarByRnuType(long rnuTypeId) {
        return migrationDao.getActualExemplarByRnuType(rnuTypeId);
    }

    @Override
    public List<Exemplar> getActualExemplarByRnuType(List<Long> rnuIds) {
        List<Exemplar> rnuList = new ArrayList<Exemplar>();
        for (Long rnu : rnuIds) {
            rnuList.addAll(getActualExemplarByRnuType(rnu));
        }
        return rnuList;
    }

    @Override
    public Map<String, String> startMigrationProcessDebug(List<Long> rnuIds) {
        List<Exemplar> list = getActualExemplarByRnuType(rnuIds);

        List<Integer> rnus = Arrays.asList(25, 26, 27, 31);
        List<Integer> xmls = Arrays.asList(51, 53, 54, 59, 60, 64);

        LinkedHashMap<String, String> hashMap = new LinkedHashMap<String, String>();

        for (Exemplar ex : list) {
            if (rnus.contains(ex.getRnuTypeId())) {
                hashMap.put(rnuService.getRnuFileName(ex), rnuService.generateRnuFileToString(ex));
            } else if (xmls.contains(ex.getRnuTypeId())) {
                hashMap.put(xmlService.getXmlFileName(ex), xmlService.generateXmlFileToString(ex));
            }
        }
        return hashMap;
    }

    @Override
    public Map<String, byte[]> startMigrationProcess(List<Long> rnuIds) {
        List<Exemplar> list = getActualExemplarByRnuType(rnuIds);

        List<Integer> rnus = Arrays.asList(25, 26, 27, 31);
        List<Integer> xmls = Arrays.asList(51, 53, 54, 59, 60, 64);

        LinkedHashMap<String, byte[]> hashMap = new LinkedHashMap<String, byte[]>();

        for (Exemplar ex : list) {
            if (rnus.contains(ex.getRnuTypeId())) {
                hashMap.put(rnuService.getRnuFileName(ex), rnuService.generateRnuFileToBytes(ex));
            } else if (xmls.contains(ex.getRnuTypeId())) {
                hashMap.put(xmlService.getXmlFileName(ex), xmlService.generateXmlFileToBytes(ex));
            }
        }
        return hashMap;
    }

    @Override
    public List<? extends AbstractRnuRow> getRnuList(Exemplar ex) {
        List<? extends AbstractRnuRow> list = new ArrayList<AbstractRnuRow>();
        switch (NalogFormType.getByCode(ex.getRnuTypeId())) {
            case RNU25:
                list = migrationDao.getRnu25RowList(ex);
                break;
            case RNU26:
                list = migrationDao.getRnu26RowList(ex);
                break;
            case RNU27:
                list = migrationDao.getRnu27RowList(ex);
                break;
            case RNU31:
                list = migrationDao.getRnu31RowList(ex);
                break;
            case RNU51:
                break;
            case RNU53:
                break;
            case RNU54:
                break;
            case RNU59:
                break;
            case RNU60:
                list = migrationDao.getRnu60RowList(ex);
                break;
            case RNU64:
                list = migrationDao.getRnu64RowList(ex);
                break;
        }
        return list;
    }
}
