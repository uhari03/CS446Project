package com.synchronicity.APBdev.connectivity;

import java.util.Map;

/**
 * Created by Andrew on 2018-03-17.
 */

public class WifiNsdInfo implements NsdInfo {

    private Map<String,String> record;

    WifiNsdInfo(final Map<String,String> record) {
        this.record = record;
    }

    public NsdInfo.Converter<Map<String,String>> getConverter() {
        return new NsdInfo.Converter<Map<String,String>>() {
            public Map<String,String> convert(NsdInfo info) {
                return record;
            }
        };
    }

}
