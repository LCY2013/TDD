package org.fufeng.tdd;

import java.lang.annotation.Annotation;

public class BeforeTest {

    static Option option(String option) {
        return new Option(){

            @Override
            public Class<? extends Annotation> annotationType() {
                return Option.class;
            }

            @Override
            public String value() {
                return option;
            }
        };
    }

}
