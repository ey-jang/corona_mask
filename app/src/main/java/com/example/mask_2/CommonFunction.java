package com.example.mask_2;

public class CommonFunction {
    private static CommonFunction instance = new CommonFunction();

    public static CommonFunction getInstance(){
        if(instance == null){
            instance = new CommonFunction();
        }
        return instance;
    }
    public static StringBuffer getPath(double startx, double starty, double endx, double endy){
        StringBuffer buffer = new StringBuffer();
        buffer.append("https://m.map.naver.com/route.nhn?menu=route&sx=" + starty + "&sy=" + startx +
                "&pathType=0&showMap=true#/drive/detail/%25EB");
        buffer.append("%25AA%2585%25EC%25B9%25AD%2520%25EC%2597%2586%25EC%259D%258C," + starty + "," + startx + ",,,false,/%25EB%25AA%2585%25EC%25B9%25AD%2520%25EC");
        buffer.append("%2597%2586%25EC%259D%258C," + endy + "," + endx + ",,,false,/2/0/map/0");
        return buffer;





    }
}
