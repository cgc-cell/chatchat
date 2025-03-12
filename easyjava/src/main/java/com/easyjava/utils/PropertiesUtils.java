package com.easyjava.utils;

import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class PropertiesUtils {
    private static Properties props = new Properties();

    private static Map<String, String> propertiesMap = new ConcurrentHashMap();

    static {
        InputStream is = null;
        Reader bf = null;
        try {
            String osType = System.getProperty("os.name").toUpperCase();
            String path = PropertiesUtils.class.getClassLoader().getResource("").getPath();
            //jar包读取外部配置文件
            if (path.contains(".jar")) {
                Integer subIndex = 0;
                if (osType.contains("WINDOWS")) {
                    subIndex = 6;
                } else {
                    subIndex = 5;
                }
                path = path.substring(subIndex, path.indexOf(".jar") + 4);
                is = new FileInputStream(new File(path).getParent() + "/application.properties");
            } else {
                is = PropertiesUtils.class.getClassLoader().getResourceAsStream("application.properties");
            }

            bf = new BufferedReader(new InputStreamReader(is, "utf8"));
            props.load(bf);
            Iterator<Object> iterator = props.keySet().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                propertiesMap.put(key, props.getProperty(key));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getString(String key) {
        return propertiesMap.get(key);
    }
}
