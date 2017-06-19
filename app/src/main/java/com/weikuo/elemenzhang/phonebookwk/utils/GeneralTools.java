package com.weikuo.elemenzhang.phonebookwk.utils;

import java.io.File;

/**
 * Created by elemenzhang on 2017/6/19.
 */

public class GeneralTools {
    static int i = 1;

    public static File generateFileName(File file, String date) {
        File fileFinal = new File(file + "/" + date + "_" + i + ".vcf");
        i++;
        if (fileFinal.exists()) {
            return generateFileName(file, date);
        } else
            return fileFinal;
    }
}