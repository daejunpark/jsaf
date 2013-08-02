/*******************************************************************************
    Copyright (c) 2013, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/
package kr.ac.kaist.jsaf.webapp_converter;

import java.io.File;
import java.util.ArrayList;

public interface ConvertingResult {
    File directoryPath = null;
    ArrayList<ConversionInfo> conversionInfoList = null;
    public File getConvertedDirectory();
    public ArrayList<ConversionInfo> getConversionInfoList();
}