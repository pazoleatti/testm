package com.aplana.generators.data;

import java.util.Random;

import static com.aplana.generators.Dictionary.*;
import static com.aplana.generators.Utils.*;

public class FL {
    public static Random random;

    public String inp;
    public String snils;
    public String lastname;
    public String firstname;
    public String middlename;
    public String birthday;
    public String inn;
    public String dul;

    public static FL generate() {
        FL fl = new FL();
        String inp = String.valueOf(1000000000 + random.nextInt(2000000000));
        fl.inp = inp.length() == 10 ? inp : inp.substring(inp.length() - 10, inp.length());
        fl.snils = generateSnils(random);
        fl.lastname = lastnameDictionary.get(random.nextInt(lastnameDictionary.size()));
        fl.firstname = firstnameDictionary.get(random.nextInt(firstnameDictionary.size()));
        fl.middlename = middlenameDictionary.get(random.nextInt(middlenameDictionary.size()));
        fl.birthday = generateDate(random);
        fl.inn = generateInn(random);
        fl.dul = generateNumberDul(random);
        return fl;
    }
}
