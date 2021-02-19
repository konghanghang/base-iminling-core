package com.iminling.common.id;

import org.hashids.Hashids;

/**
 * @author yslao@outlook.com
 * @since 2020/11/13
 */
public class IdUtils {

    private IdUtils(){}

    private static Hashids hashids;

    static {
        hashids = new Hashids("this is my salt1", 12);
    }

    /**
     * 加密id
     * @param id
     * @return
     */
    public static String encodeId(long id){
        return hashids.encode(id);
    }

    /**
     * 解密id
     * @param hashId
     * @return
     */
    public static Long decodeId(String hashId){
        long[] decode = hashids.decode(hashId);
        if (decode.length == 0 || decode[0] == 0){
            return null;
        }
        return decode[0];
    }

}
