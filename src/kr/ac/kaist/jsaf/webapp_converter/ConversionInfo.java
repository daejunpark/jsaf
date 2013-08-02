/*******************************************************************************
    Copyright (c) 2013, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/
package kr.ac.kaist.jsaf.webapp_converter;

public class ConversionInfo {
    static final String LEVEL_INFO  = "INFO";
    static final String LEVEL_ERROR = "WARNING";

    // Category in the excel shhet
    static final int TYPE_API_NAME_SPACE  = 1;
    static final int TYPE_PLATFORM_VALUES = 2;
    static final int TYPE_PLATFORM_API 	  = 3;
    static final int TYPE_PLATFORM_HTML	  = 4;

    public String level;
    public int type;

    public String message;

    public String fillePath;        // File path
    public int lineNum;	            // Line Number
    public int blockNum;            // Block Number

    public String originalSECAPI;   // SEC API to be converted
    public String convetedTizenAPI; // Tizen API (converting result)
}
