/*******************************************************************************
    Copyright (c) 2013, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.webapp_converter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

public class ConvertingResultStorage implements ConvertingResult{
    File directoryPath = null;
    ArrayList<ConversionInfo> conversionInfoList = new ArrayList<ConversionInfo>();
    public void setDirectoryPath(File path){
        directoryPath = path;
    }
    public File getConvertedDirectory() {
        return directoryPath;
    }
    public ArrayList<ConversionInfo> getConversionInfoList() {
      return conversionInfoList;
    }
    public void appendConversionInfoList(Collection<? extends ConversionInfo> l){
        conversionInfoList.addAll(l);
    }
}
