/******************************************************************************
 ** This data and information is proprietary to, and a valuable trade secret
 ** of, Basis Technology Corp.  It is given in confidence by Basis Technology
 ** and may only be used as permitted under the license agreement under which
 ** it has been distributed, and in no other way.
 **
 ** Copyright (c) 2000-2008 Basis Technology Corporation All rights reserved.
 **
 ** The technical data and information provided herein are provided with
 ** `limited rights', and the computer software provided herein is provided
 ** with `restricted rights' as those terms are defined in DAR and ASPR
 ** 7-104.9(a).
 ******************************************************************************/
//CHECKSTYLE:OFF
package com.basistech.rosette.internal.take5;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A single match generated by a dictionary lookup. 
 * This object can be queried for the value data associated
 * with the match.
 */
public class Take5Match {
    Take5Dictionary dict; /* Reference back to the dictionary we can from. */
    int length; /* Length of the matched prefix. */
    public int index; /* Index associated with the matched prefix. */
    public int state;
    public char c;

    /**
     * Initializes a new match object. The object should not be used until it internal values have been filled
     * in.
     */
    public Take5Match() {
        this.dict = null;
        this.length = 0;
        this.index = 0;
    }
    
    void reset() {
        dict = null;
        length = 0;
        index = 0;
    }

    /**
     * Get the length of the matching prefix string.
     */
    public int getLength() {
        return length;
    }

    /**
     * Get the index of the matched word.
     */
    public int getIndex() {
        return index;
    }
    
    /**
     * Return the state of the matched word.
     * @return the state.
     */
    public int getState() {
        return state;
    }

    /**
     * Retrieve a ByteBuffer containing a complex Take5 value. It is up to the caller to disassemble it.
     * 
     * @param dataLength
     * @return
     * @throws Take5Exception
     * @deprecated 1.3.7: use getPayloadOffset.
     */
    @Deprecated
    public ByteBuffer getComplexData(int dataLength) throws Take5Exception {
        int ptr = getOffsetValue();
        dict.data.position(ptr);
        dict.data.limit(ptr + dataLength);
        ByteBuffer rbb = dict.data.slice();
        rbb.order(ByteOrder.nativeOrder());
        dict.data.position(0);
        dict.data.limit(dict.data.capacity());
        return rbb;
    }

    /**
     * Get the single-precision float value associated with the matched prefix.
     */
    public float getFloatValue() throws Take5Exception {
        if (dict.valueSize != 4) {
            throw new Take5Exception(Take5Exception.VALUE_SIZE_MISMATCH);   
        }
        int ptr = getOffsetValue();
        return dict.data.getFloat(ptr);
    }
    
    public int getIntValue() throws Take5Exception {
        if (dict.valueSize != 4) {
            throw new Take5Exception(Take5Exception.VALUE_SIZE_MISMATCH);   
        }
        int ptr = getOffsetValue();
        return dict.data.getInt(ptr);
    }

    /**
     * Get the double-precision float value associated with the matched prefix.
     */
    public double getDoubleValue() throws Take5Exception {
        if (dict.valueSize != 8) {
            throw new Take5Exception(Take5Exception.VALUE_SIZE_MISMATCH);   
        }
        int ptr = getOffsetValue();
        return dict.data.getDouble(ptr);
    }

    /**
     * Convenience accessor for the most common type of payload in a take5: Strings. For all other
     * payloads, where the size is unknown in advance, the caller should use 
     * {@link #getPayloadOffset}.
     * 
     * Get the String value associated with the matched prefix. Warning, this assumes that the value is in a
     * particular format: null-terminated! Use getOffsetValue for more complex cases.
     * 
     * @return string value assuming either UTF-8 (for data size 1) or UTF-16 (for data size 2).
     */
    public String getStringValue() throws Take5Exception {
        int ptr = dict.data.getInt(dict.valueData + index * 4);
        int inc = dict.valueSize;
        int len = 0;
        int count = -1;

        if (ptr == 0) {
            return null;
        }
        if ((dict.valueFormat & 0xFF000000) != Take5Dictionary.VALUE_FORMAT_INDIRECT) {
            throw new Take5Exception(Take5Exception.NO_POINTERS_HERE);
        }

        while (true) {
            switch (dict.valueSize) {
            default:
                throw new Take5Exception(Take5Exception.VALUE_SIZE_MISMATCH);
            case 1:
                count = dict.data.get(ptr);
                break;
            case 2:
                count = dict.data.getShort(ptr);
                break;
            }
            if (count == 0) {
                break;
            }
            len++;
            ptr += inc;
        }

        ptr = dict.data.getInt(dict.valueData + index * 4);
        if (inc == 1) {
            byte[] buffer = new byte[len];
            for (int i = 0; i < len; i++) {
                buffer[i] = dict.data.get(ptr + i);
            }
            try {
                return new String(buffer, "UTF-8");
            } catch (java.io.UnsupportedEncodingException e) {
                return null;
            }
        } else if (inc == 2) {
            char[] buffer = new char[len];
            for (int i = 0; i < len; i++) {
                buffer[i] = dict.data.getChar(ptr + i * inc);
            }
            return new String(buffer);
        }

        return null;
    }

    /**
     * Get the offset into the data file at which the result can be found. 
     * The caller must use their own access to the underlying ByteBuffer to 
     * retrieve the actual values.
     * @return the offset of the payload data in the take5;
     */
    public int getOffsetValue() throws Take5Exception {
        switch (dict.valueFormat & 0xFF000000) {
        default:
            throw new Take5Exception(Take5Exception.UNSUPPORTED_VALUE_FORMAT);
        case Take5Dictionary.VALUE_FORMAT_NONE:
        case Take5Dictionary.VALUE_FORMAT_INDEX:
            return index;
        case Take5Dictionary.VALUE_FORMAT_FIXED:
            return dict.valueData + index * dict.valueSize;
        case Take5Dictionary.VALUE_FORMAT_INDIRECT:
            return dict.data.getInt(dict.valueData + index * 4);
        }
    }

    /**
     * String representation of a match.
     */
    public String toString() {
        return "[Match: index = " + index + " length = " + length + "]";
    }
}
