package com.example.demo.kmas;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class KmasAgeRatingTest {
    @Test void acceptsKnownNonAdultRatings(){
        for(String value:new String[]{"전체연령","전체 연령","전체 이용가","전체관람가","전연령","12세 이상","15세 이상","17세 이상"})
            assertNull(KmasAgeRating.exclusion(value),value);
    }
    @Test void excludesAdults(){
        for(String value:new String[]{"18세 이상","19세 이상","성인","청소년 이용 불가","전체연령 / 18세 이상"})
            assertEquals("adult",KmasAgeRating.exclusion(value),value);
    }
    @Test void excludesUnknownRatings(){
        for(String value:new String[]{null,"","확인필요","확인 필요","미정","전체연령 확인필요"})
            assertEquals("unknownAge",KmasAgeRating.exclusion(value));
    }
}
