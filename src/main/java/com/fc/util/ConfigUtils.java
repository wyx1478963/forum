package com.fc.util;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigUtils {
    private static Logger logger = LoggerFactory.getLogger(ConfigUtils.class);
    static Configuration configuration = null;

    static {
        try {
            configuration = new PropertiesConfiguration("df.properties");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getString(String configName) {
        if (configuration != null) {
            return configuration.getString(configName);
        } else {
            return null;
        }
    }

    public static Integer getInteger(String configName) {
        if (configuration != null) {
            return configuration.getInt(configName);
        } else {
            return -1;
        }
    }

    public static List<String> getList(String configName) {
        List<String> resultList = new ArrayList<String>();
        if (configuration != null) {
            String value = configuration.getString(configName);
            if( value != null) {
                resultList = Arrays.asList(value.split(","));
                return resultList;
            }
        }
        return resultList;
    }
}
