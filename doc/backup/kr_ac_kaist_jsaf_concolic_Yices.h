/*******************************************************************************
    Copyright (c) 2012-2013, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

#include <jni.h>

#ifndef _Included_kr_ac_kaist_jsaf_concolic_Yices
#define _Included_kr_ac_kaist_jsaf_concolic_Yices
#ifdef __cplusplus
extern "C" {
#endif
    /*
     * Class: kr_ac_kaist_jsaf_concolic_Yices
     * Method: yicesSolveLinear
     * Signature: (Ljava/lang/String;)Ljava/lang/String;
     */
    JNIEXPORT jstring JNICALL Java_kr_ac_kaist_jsaf_concolic_Yices_yicesSolveLinear (JNIEnv *, jobject, jstring);
# ifdef __cplusplus
}
#endif
#endif

