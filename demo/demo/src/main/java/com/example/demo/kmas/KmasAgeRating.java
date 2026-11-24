package com.example.demo.kmas;

final class KmasAgeRating {
    private KmasAgeRating() {}

    // null means an explicitly recognized non-adult rating.
    static String exclusion(String age) {
        String rating=age==null?"":age.replaceAll("[\\s\\p{Z}]+","");
        if(rating.matches(".*(?:18세|19세|20세|성인|청소년이용불가|청소년관람불가|청불).*"))return "adult";
        if(rating.matches("(?:전체|전체연령|전연령)(?:이용가|관람가)?|(?:[0-9]|1[0-7])세(?:이상|이용가|관람가)"))return null;
        return "unknownAge";
    }
}
