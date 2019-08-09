package com.njupt.passbook.service.impl;

/**
 * @author Andy Qiu
 * rows = 6
 * 1111112
 * 3222222
 * 3333334
 * 5444444
 * 5555556
 * 7666666
 * @date 2019/7/12
 */
public class Test {
    public static void patternPrint(int rows){
        int mem = 0;
        String str = "";
        for(int i= 1 ;i<=rows;i++){
            if((i%2)==1){
                for (int j =0;j<6;j++){
                    str +=i;
                }
                if (mem == 0){
                    mem = i+1;
                }else{
                    mem +=1;
                }
                str+=mem;
                System.out.println(str);
                str = "";
            }

            if ((i%2)==0){
                mem +=1;
                str+=mem;
                for (int k =0;k<6;k++){
                    str+=i;
                }
                System.out.println(str);
                str = "";
            }
        }
    }

    public static void main(String[] args) {
        Test.patternPrint(7);
    }

}
