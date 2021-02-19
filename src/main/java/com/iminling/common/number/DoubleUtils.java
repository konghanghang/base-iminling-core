package com.iminling.common.number;

import java.math.BigDecimal;

/**
 * double运算工具
 * @author konghang
 */
public class DoubleUtils {

    /**
     * double 相加
     * @param doubles
     * @return
     */
    public static double sum(Double... doubles){
        BigDecimal rs = new BigDecimal("0");
        for (Double aDouble : doubles) {
            rs = rs.add(new BigDecimal(aDouble.toString()));
        }
        return rs.doubleValue();
    }


    /**
     * double 相减
     * @param doubles
     * @return
     */
    public static double sub(Double... doubles){
        BigDecimal first = null;
        for (int i = 0; i< doubles.length; i++){
            if (i == 0){
                first = new BigDecimal(doubles[i].toString());
            }else {
                first = first.subtract(new BigDecimal(doubles[i].toString()));
            }
        }
        return first.doubleValue();
    }

    /**
     * double 乘法
     * @param doubles
     * @return
     */
    public static double mul(Double... doubles){
        BigDecimal rs = new BigDecimal("1");
        for (Double aDouble : doubles) {
            BigDecimal temp = new BigDecimal(aDouble.toString());
            rs = rs.multiply(temp);
        }
        return rs.doubleValue();
    }

    /**
     * double 除法
     * @param d1
     * @param d2
     * @return
     */
    public static double div(double d1,double d2){
        return DoubleUtils.div(d1,d2,2);
    }


    /**
     * double 除法
     * @param d1
     * @param d2
     * @param scale 四舍五入 小数点位数
     * @return
     */
    public static double div(double d1,double d2,int scale){
        //  当然在此之前，你要判断分母是否为0，
        //  为0你可以根据实际需求做相应的处理
        BigDecimal bd1 = new BigDecimal(Double.toString(d1));
        BigDecimal bd2 = new BigDecimal(Double.toString(d2));
        return bd1.divide(bd2,scale,BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * double 转 string 去掉后面锝0
     * @param i
     * @return
     */
    public static String getString(double i){
        String s = String.valueOf(i);
        if(s.indexOf(".") > 0){
            //去掉后面无用的零
            s = s.replaceAll("0+?$", "");
            //如小数点后面全是零则去掉小数点
            s = s.replaceAll("[.]$", "");
        }
        return s;
    }

    /**
     * 数字转换为千位符
     * @param number
     * @return
     */
    public static String numberToBits(String number){
        String begin="",end="";
        String[] num=number.split("\\.");
        if(num.length>1){
            begin=num[0];
            end=num[1];
        }else{
            begin=number;
        }
        return begin.replaceAll("(?<=\\d)(?=(?:\\d{3})+$)", ",")+"."+end;
    }

}
