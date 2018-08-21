package com.aplana.sbrf.taxaccounting.model.ndflPerson;


import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class NdflPersonTest {

    @Test
    public void testNdflPersonComporatorByLastName(){
        NdflPerson ndflPerson1 = new NdflPerson();
        ndflPerson1.setLastName("A");

        NdflPerson ndflPerson2 = new NdflPerson();
        ndflPerson2. setLastName("C");

        NdflPerson ndflPerson3 = new NdflPerson();
        ndflPerson3.setLastName("B");

        ArrayList<NdflPerson> ndflPeople = new ArrayList<>(Arrays.asList(ndflPerson1, ndflPerson2, ndflPerson3));
        Collections.sort(ndflPeople, NdflPerson.getComparator());
        assertEquals("A", ndflPeople.get(0).getLastName());
        assertEquals("B", ndflPeople.get(1).getLastName());
        assertEquals("C", ndflPeople.get(2).getLastName());
    }

    @Test
    public void testNdflPersonComporatorByFirstName(){
        NdflPerson ndflPerson1 = new NdflPerson();
        ndflPerson1.setFirstName("A");

        NdflPerson ndflPerson2 = new NdflPerson();
        ndflPerson2. setFirstName("C");

        NdflPerson ndflPerson3 = new NdflPerson();
        ndflPerson3.setFirstName("B");

        ArrayList<NdflPerson> ndflPeople = new ArrayList<>(Arrays.asList(ndflPerson1, ndflPerson2, ndflPerson3));
        Collections.sort(ndflPeople, NdflPerson.getComparator());
        assertEquals("A", ndflPeople.get(0).getFirstName());
        assertEquals("B", ndflPeople.get(1).getFirstName());
        assertEquals("C", ndflPeople.get(2).getFirstName());
    }

    @Test
    public void testNdflPersonComporatorByMiddleName(){
        NdflPerson ndflPerson1 = new NdflPerson();
        ndflPerson1.setMiddleName("A");

        NdflPerson ndflPerson2 = new NdflPerson();
        ndflPerson2. setMiddleName("C");

        NdflPerson ndflPerson3 = new NdflPerson();
        ndflPerson3.setMiddleName("B");

        ArrayList<NdflPerson> ndflPeople = new ArrayList<>(Arrays.asList(ndflPerson1, ndflPerson2, ndflPerson3));
        Collections.sort(ndflPeople, NdflPerson.getComparator());
        assertEquals("A", ndflPeople.get(0).getMiddleName());
        assertEquals("B", ndflPeople.get(1).getMiddleName());
        assertEquals("C", ndflPeople.get(2).getMiddleName());
    }

    @Test
    public void testNdflPersonComporatorByInnNp(){
        NdflPerson ndflPerson1 = new NdflPerson();
        ndflPerson1.setInnNp("A");

        NdflPerson ndflPerson2 = new NdflPerson();
        ndflPerson2.setInnNp("C");

        NdflPerson ndflPerson3 = new NdflPerson();
        ndflPerson3.setInnNp("B");

        ArrayList<NdflPerson> ndflPeople = new ArrayList<>(Arrays.asList(ndflPerson1, ndflPerson2, ndflPerson3));
        Collections.sort(ndflPeople, NdflPerson.getComparator());
        assertEquals("A", ndflPeople.get(0).getInnNp());
        assertEquals("B", ndflPeople.get(1).getInnNp());
        assertEquals("C", ndflPeople.get(2).getInnNp());
    }

    @Test
    public void testNdflPersonComporatorByInnForeign(){
        NdflPerson ndflPerson1 = new NdflPerson();
        ndflPerson1.setInnForeign("A");

        NdflPerson ndflPerson2 = new NdflPerson();
        ndflPerson2.setInnForeign("C");

        NdflPerson ndflPerson3 = new NdflPerson();
        ndflPerson3.setInnForeign("B");

        ArrayList<NdflPerson> ndflPeople = new ArrayList<>(Arrays.asList(ndflPerson1, ndflPerson2, ndflPerson3));
        Collections.sort(ndflPeople, NdflPerson.getComparator());
        assertEquals("A", ndflPeople.get(0).getInnForeign());
        assertEquals("B", ndflPeople.get(1).getInnForeign());
        assertEquals("C", ndflPeople.get(2).getInnForeign());
    }

    @Test
    public void testNdflPersonComporatorByBirthday(){
        Calendar calendar = Calendar.getInstance();
        NdflPerson ndflPerson1 = new NdflPerson();
        calendar.set(2000, Calendar.JANUARY, 1);
        ndflPerson1.setId(1L);
        ndflPerson1.setBirthDay(calendar.getTime());

        NdflPerson ndflPerson2 = new NdflPerson();
        calendar.set(2000, Calendar.MARCH, 1);
        ndflPerson2.setId(2L);
        ndflPerson2.setBirthDay(calendar.getTime());

        NdflPerson ndflPerson3 = new NdflPerson();
        calendar.set(2000, Calendar.FEBRUARY, 1);
        ndflPerson3.setId(3L);
        ndflPerson3.setBirthDay(calendar.getTime());

        ArrayList<NdflPerson> ndflPeople = new ArrayList<>(Arrays.asList(ndflPerson1, ndflPerson2, ndflPerson3));
        Collections.sort(ndflPeople, NdflPerson.getComparator());
        assertEquals(new Long(1), ndflPeople.get(0).getId());
        assertEquals(new Long(3), ndflPeople.get(1).getId());
        assertEquals(new Long(2), ndflPeople.get(2).getId());
    }

    @Test
    public void testNdflPersonComporatorByDocNumber(){
        NdflPerson ndflPerson1 = new NdflPerson();
        ndflPerson1.setIdDocNumber("A");

        NdflPerson ndflPerson2 = new NdflPerson();
        ndflPerson2.setIdDocNumber("C");

        NdflPerson ndflPerson3 = new NdflPerson();
        ndflPerson3.setIdDocNumber("B");

        ArrayList<NdflPerson> ndflPeople = new ArrayList<>(Arrays.asList(ndflPerson1, ndflPerson2, ndflPerson3));
        Collections.sort(ndflPeople, NdflPerson.getComparator());
        assertEquals("A", ndflPeople.get(0).getIdDocNumber());
        assertEquals("B", ndflPeople.get(1).getIdDocNumber());
        assertEquals("C", ndflPeople.get(2).getIdDocNumber());
    }

}
