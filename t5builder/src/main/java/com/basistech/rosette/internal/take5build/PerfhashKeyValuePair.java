/******************************************************************************
 ** This data and information is proprietary to, and a valuable trade secret
 ** of, Basis Technology Corp.  It is given in confidence by Basis Technology
 ** and may only be used as permitted under the license agreement under which
 ** it has been distributed, and in no other way.
 **
 ** Copyright (c) 2014 Basis Technology Corporation All rights reserved.
 **
 ** The technical data and information provided herein are provided with
 ** `limited rights', and the computer software provided herein is provided
 ** with `restricted rights' as those terms are defined in DAR and ASPR
 ** 7-104.9(a).
 ******************************************************************************/

package com.basistech.rosette.internal.take5build;

/**
 * Internal key-value pair (see build.h) used for perfect hashes.
 */
class PerfhashKeyValuePair {
    final Value key;
    final Value value;
    final int keyHash;
    int index = -1;

    PerfhashKeyValuePair(Value key, Value value, int keyHash) {
        this.key = key;
        this.value = value;
        this.keyHash = keyHash;
    }
}