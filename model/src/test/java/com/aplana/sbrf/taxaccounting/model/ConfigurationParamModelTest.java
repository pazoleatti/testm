package com.aplana.sbrf.taxaccounting.model;

import org.junit.Assert;
import org.junit.Test;

import static java.util.Arrays.asList;

/**
 * @author Dmitriy Levykin
 */
public class ConfigurationParamModelTest {

    private static final ConfigurationParam KEY1 = ConfigurationParam.FORM_DATA_KEY_FILE;
    private static final ConfigurationParam KEY2 = ConfigurationParam.FORM_DATA_DIRECTORY;

    @Test
    public void getFullStringValueTest() {
        ConfigurationParamModel model = new ConfigurationParamModel();
        String[] values1 = {"value1"};
        String[] values2 = {"value2", "value3", "value4"};
        model.put(KEY1, asList(values1));
        model.put(KEY2, asList(values2));

        String result1 = model.getFullStringValue(KEY1);
        String result2 = model.getFullStringValue(KEY2);

        Assert.assertEquals(result1, values1[0]);
        Assert.assertEquals(result2, values2[0] + "\n" + values2[1] + "\n" + values2[2]);
        Assert.assertNull(model.getFullStringValue(null));
    }

    @Test
    public void setFullStringValueTest() {
        ConfigurationParamModel model = new ConfigurationParamModel();

        String path1 = "http://google.com";
        String path2 = "C:\\temp";

        String str1 = "\n\n" + path1 + "\n" + path2 + "\n\n";
        String str2 = "<html><body>test</body></html>";

        model.setFullStringValue(KEY1, str1);
        model.setFullStringValue(KEY2, str2);

        Assert.assertEquals(2, model.get(KEY1).size());
        Assert.assertEquals(1, model.get(KEY2).size());

        Assert.assertEquals(model.get(KEY1).get(0), path1);
        Assert.assertEquals(model.get(KEY1).get(1), path2);
        Assert.assertEquals(model.get(KEY2).get(0), str2);

        model.setFullStringValue(null, null);
    }
}
