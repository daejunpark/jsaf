/*******************************************************************************
    Copyright (c) 2012-2013, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

#include <jni.h>
#include <sys/types.h>
#include <sys/ipc.h>
#include <sys/shm.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>

#include <iostream>
#include <string>
#include <cstring>
#include <list>

#include "kr_ac_kaist_jsaf_concolic_Yices.h"
#include "YicesSolver.h"

using namespace std;

JNIEXPORT jstring JNICALL Java_kr_ac_kaist_jsaf_concolic_Yices_yicesSolveLinear (JNIEnv *env, jobject jobj, jstring str)
{
    const char *utf_string;
    jboolean isCopy;
    utf_string = env->GetStringUTFChars (str, &isCopy);

    YicesSolver solver;
    string constraint;
    string result;

    constraint = utf_string;
    solver.solve(constraint);

    result = solver.getSolution();

    printf("%s\n", constraint.c_str());

    if (isCopy == JNI_TRUE)
        env->ReleaseStringUTFChars (str, utf_string);
    
    return env->NewStringUTF(result.c_str());
    /*
    return env->NewStringUTF("Returning...");
    */
}
