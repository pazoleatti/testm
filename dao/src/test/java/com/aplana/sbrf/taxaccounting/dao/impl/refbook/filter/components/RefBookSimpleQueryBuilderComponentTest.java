package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components;

import com.aplana.sbrf.taxaccounting.model.PreparedStatementData;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.junit.Test;

import java.util.*;

public class RefBookSimpleQueryBuilderComponentTest {

    @Test
    public void getMatchedRecordsByUniqueAttributes() throws Exception {
        RefBookSimpleQueryBuilderComponent builder = new RefBookSimpleQueryBuilderComponent();
        PreparedStatementData ps = builder.psGetMatchedRecordsByUniqueAttributes(createRefBook(), 2L, createRecord(),
                createUniqueValues());
        System.out.println(ps.getQueryString());
    }

    private RefBook createRefBook(){
        RefBook rb = new RefBook();
        rb.setVersioned(true);
        rb.setAttributes(createAttributes());
        rb.setTableName("REF_BOOK_PERSON");
        rb.setId(904L);
        return rb;
    }

    private List<RefBookAttribute> createAttributes(){
        RefBookAttribute[] attributes = new RefBookAttribute[3];
        attributes[0] = new RefBookAttribute();
        attributes[0].setName("ИНН в РФ");
        attributes[0].setAlias("INN");
        attributes[0].setUnique(1);
        attributes[0].setRequired(false);
        attributes[0].setRefBookId(904L);
        attributes[0].setId(9045L);

        attributes[1] = new RefBookAttribute();
        attributes[1].setName("ИНН в стране гражданства");
        attributes[1].setAlias("INN_FOREIGN");
        attributes[1].setUnique(1);
        attributes[1].setRequired(false);
        attributes[1].setRefBookId(904L);
        attributes[1].setId(9046L);

        attributes[2] = new RefBookAttribute();
        attributes[2].setName("СНИЛС");
        attributes[2].setAlias("SNILS");
        attributes[2].setUnique(1);
        attributes[2].setRequired(false);
        attributes[2].setRefBookId(904L);
        attributes[2].setId(9047L);

        return new ArrayList<RefBookAttribute>(Arrays.asList(attributes));
    }

    private RefBookRecord createRecord(){
        RefBookValue valueINN = new RefBookValue(RefBookAttributeType.STRING, "012345678912");
        RefBookValue valueINNFOR = new RefBookValue(RefBookAttributeType.STRING, "abc54321");
        RefBookValue valueSNILS = new RefBookValue(RefBookAttributeType.STRING, "123-456-789 01");

        Map<String, RefBookValue> values = new HashMap<String, RefBookValue>();
        values.put("INN", valueINN);
        values.put("INN_FOREIGN", valueINNFOR);
        values.put("SNILS", valueSNILS);

        RefBookRecord record = new RefBookRecord();
        record.setValues(values);
        return record;
    }

    private Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>> createUniqueValues(){


        RefBookAttribute aINN = new RefBookAttribute();
        aINN.setName("ИНН в РФ");
        aINN.setAlias("INN");
        aINN.setUnique(1);
        aINN.setRequired(false);
        aINN.setRefBookId(904L);
        aINN.setId(9045L);

        RefBookAttribute aINNFOR = new RefBookAttribute();
        aINNFOR.setName("ИНН в стране гражданства");
        aINNFOR.setAlias("INN_FOREIGN");
        aINNFOR.setUnique(1);
        aINNFOR.setRequired(false);
        aINNFOR.setRefBookId(904L);
        aINNFOR.setId(9046L);

        RefBookAttribute aSNILS = new RefBookAttribute();
        aSNILS.setName("СНИЛС");
        aSNILS.setAlias("SNILS");
        aSNILS.setUnique(1);
        aSNILS.setRequired(false);
        aSNILS.setRefBookId(904L);
        aSNILS.setId(9047L);

        RefBookValue valueINN = new RefBookValue(RefBookAttributeType.STRING, "012345678912");
        RefBookValue valueINNFOR = new RefBookValue(RefBookAttributeType.STRING, "abc54321");
        RefBookValue valueSNILS = new RefBookValue(RefBookAttributeType.STRING, "123-456-789 01");

        Pair pINN = new Pair(aINN, valueINN);
        Pair pINNFOR = new Pair(aINNFOR, valueINNFOR);
        Pair pSNILS = new Pair(aSNILS, valueSNILS);

        List<Pair<RefBookAttribute, RefBookValue>> list = new ArrayList<Pair<RefBookAttribute, RefBookValue>>();
        list.add(pINN);
        list.add(pINNFOR);
        list.add(pSNILS);

        Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>> values
                = new HashMap<Integer, List<Pair<RefBookAttribute, RefBookValue>>>();

        values.put(1, list);
        return values;
    }
}