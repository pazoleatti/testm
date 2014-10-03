package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.Diff;
import com.aplana.sbrf.taxaccounting.model.DiffType;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class DiffServiceTest {
    DiffServiceImpl diffService = new DiffServiceImpl();

    @Test
    public void computeDiffSimpleTest() throws IOException {
        // Нет изменений
        List<Diff> diffList = diffService.computeDiff(Arrays.asList("AAA", "BBB", "CCC"), Arrays.asList("AAA", "BBB", "CCC"));
        Assert.assertTrue(diffList.isEmpty());
        // Удалена строка
        diffList = diffService.computeDiff(Arrays.asList("AAA", "BBB", "CCC"), Arrays.asList("AAA", "BBB"));
        Assert.assertEquals(1, diffList.size());
        Diff diff = diffList.get(0);
        Assert.assertEquals(DiffType.DELETE, diff.getDiffType());
        Assert.assertNull(diff.getRevisedRowNumber());
        Assert.assertNotNull(diff.getOriginalRowNumber());
        Assert.assertEquals(2, diff.getOriginalRowNumber().intValue());
        // Добавлена строка
        diffList = diffService.computeDiff(Arrays.asList("AAA", "BBB", "CCC"), Arrays.asList("AAA", "BBB", "CCC", "DDD"));
        Assert.assertEquals(1, diffList.size());
        diff = diffList.get(0);
        Assert.assertEquals(DiffType.INSERT, diff.getDiffType());
        Assert.assertNotNull(diff.getRevisedRowNumber());
        Assert.assertNull(diff.getOriginalRowNumber());
        Assert.assertEquals(3, diff.getRevisedRowNumber().intValue());
        // Изменена строка
        diffList = diffService.computeDiff(Arrays.asList("AAA", "BBB", "CCC"), Arrays.asList("AAA", "DDD", "CCC"));
        Assert.assertEquals(1, diffList.size());
        diff = diffList.get(0);
        Assert.assertEquals(DiffType.CHANGE, diff.getDiffType());
        Assert.assertNotNull(diff.getOriginalRowNumber());
        Assert.assertNotNull(diff.getRevisedRowNumber());
        Assert.assertEquals(1, diff.getOriginalRowNumber().intValue());
        Assert.assertEquals(1, diff.getRevisedRowNumber().intValue());
    }

    @Test
    public void computeDiffComplexTest() throws IOException {
        List<Diff> diffList = diffService.computeDiff(streamToLines(getOriginalInputStream()),
                streamToLines(getRevisedInputStream()));
        Assert.assertEquals(8, diffList.size());
        DiffType[] diffTypes = new DiffType[]{DiffType.CHANGE, DiffType.INSERT, DiffType.CHANGE, DiffType.CHANGE,
                DiffType.DELETE, DiffType.DELETE, DiffType.INSERT, DiffType.INSERT};
        for (int i = 0; i < diffList.size(); i++) {
            Assert.assertEquals(diffTypes[i], diffList.get(i).getDiffType());
        }
    }

    @Test
    public void getMergedOrderTest() {
        List<Diff> diffList;
        List<Pair<Integer, Integer>> pairList;
        diffList = Arrays.asList(new Diff(0, 0, DiffType.CHANGE));
        pairList = diffService.getMergedOrder(diffList, 1);
        Assert.assertEquals(1, pairList.size());
        Assert.assertEquals(0, pairList.get(0).first.intValue());
        Assert.assertEquals(0, pairList.get(0).second.intValue());

        diffList = Arrays.asList(new Diff(1, 1, DiffType.CHANGE));
        pairList = diffService.getMergedOrder(diffList, 3);
        Assert.assertEquals(3, pairList.size());
        Assert.assertEquals(0, pairList.get(0).first.intValue());
        Assert.assertEquals(0, pairList.get(0).second.intValue());
        Assert.assertEquals(1, pairList.get(1).first.intValue());
        Assert.assertEquals(1, pairList.get(1).second.intValue());
        Assert.assertEquals(2, pairList.get(2).first.intValue());
        Assert.assertEquals(2, pairList.get(2).second.intValue());

        diffList = Arrays.asList(new Diff(null, 1, DiffType.INSERT));
        pairList = diffService.getMergedOrder(diffList, 3);
        Assert.assertEquals(3, pairList.size());
        Assert.assertEquals(0, pairList.get(0).first.intValue());
        Assert.assertEquals(0, pairList.get(0).second.intValue());
        Assert.assertNull(pairList.get(1).first);
        Assert.assertEquals(1, pairList.get(1).second.intValue());
        Assert.assertEquals(1, pairList.get(2).first.intValue());
        Assert.assertEquals(2, pairList.get(2).second.intValue());

        diffList = Arrays.asList(new Diff(1, 1, DiffType.CHANGE), new Diff(null, 2, DiffType.INSERT),
                new Diff(4, 5, DiffType.CHANGE), new Diff(5, 6, DiffType.CHANGE), new Diff(6, null, DiffType.DELETE),
                new Diff(12, null, DiffType.DELETE), new Diff(null, 13, DiffType.INSERT), new Diff(null, 14, DiffType.INSERT));
        pairList = diffService.getMergedOrder(diffList, 14);
        Assert.assertEquals(17, pairList.size());
        Assert.assertEquals(0, pairList.get(0).first.intValue());
        Assert.assertEquals(0, pairList.get(0).second.intValue());
        Assert.assertEquals(4, pairList.get(5).first.intValue());
        Assert.assertEquals(5, pairList.get(5).second.intValue());
        Assert.assertNull(pairList.get(16).first);
        Assert.assertEquals(14, pairList.get(16).second.intValue());
    }

    private static List<String> streamToLines(InputStream inputStream) throws IOException {
        List<String> lines = new LinkedList<String>();
        String line;
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        while ((line = in.readLine()) != null) {
            lines.add(line);
        }
        return lines;
    }

    private static InputStream getOriginalInputStream() {
        return getInputStream("com/aplana/sbrf/taxaccounting/service/impl/diff_original.csv");
    }

    private static InputStream getRevisedInputStream() {
        return getInputStream("com/aplana/sbrf/taxaccounting/service/impl/diff_revised.csv");
    }

    private static InputStream getInputStream(String path) {
        return DiffServiceTest.class.getClassLoader().getResourceAsStream(path);
    }
}
