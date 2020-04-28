package com.owambo.jvamcas.drivemehome.utils;


import com.google.gson.Gson;

import java.text.DecimalFormat;
import java.util.Map;

public class ParseUtil {

    public static <K> String toJson(K object ){
        return new Gson().toJson(object);
    }

    public static <T> T fromJson(String json, Class<T> tClass){
        return new Gson().fromJson(json,tClass);
    }
    public static<V> Map toMap(V object){
        String jsonString  = toJson(object);
        return fromJson(jsonString,Map.class);
    }
    public static <K,V,Y> Y fromMap(Map<K,V> map, Class<Y> tClass){
        String toJson = toJson(map);
        return fromJson(toJson,tClass);
    }
    public static String path(String... param){
        StringBuilder path = new StringBuilder();
        for (String p:param)
            path.append(p).append("/");
        return path.toString();
    }

    /**
     * Format a double as money in the form <CURRENCY VALUE>
     * @param currency the currency for the money
     * @param value the double to be formatted e.g 6.90
     * @return the formatted value NAD 6.90
     */
    public static String moneyString(String currency,double value){
        return currency+" "+new DecimalFormat("0.00").format(value);
    }

    /***
     * Extract the double value from a string representation of money
     * @param moneyString string e.g NAD 34.90
     * @return double value of the string representation e.g 34.90 if non-null else 0.00
     */
    public static double moneyDigit(String moneyString){
        if(moneyString != null){
            String digit = moneyString.replaceAll("[^.0123456789]","");
            return Double.valueOf(digit);
        }return 0.00;
    }

    /***
     * Compute relative path for the view's icon
     * @param viewId id of the view
     * @return the relative path
     */
    public static String iconPath(String viewId){
        return Const.IMAGE_ROOT_PATH+"/_"+viewId+"_.jpg";
    }

    /***
     * Generate a random ID
     * @return randomUID number
     */
    public static String randomUID(){
        return String.valueOf(System.currentTimeMillis());
    }

    /***
     * Convert object instance from type K to T to via JSON
     * @param object to be converted
     * @param tClass destination class T
     * @param <K> source type
     * @param <T> destination type
     * @return instance of type T
     */
    public static<K,T> T parseUser(K object, Class<T> tClass) {
        String jsonString = toJson(object);
        return fromJson(jsonString,tClass);
    }
}
