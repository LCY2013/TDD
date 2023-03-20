package org.fufeng.tdd;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

import static java.util.Arrays.asList;
import static org.fufeng.tdd.BeforeTest.option;
import static org.junit.jupiter.api.Assertions.*;

public class BooleanParserTest {

    @Test // Sad path
    public void should_not_accept_extra_argument_for_boolean_option() {
        TooManyArgmentsException e = assertThrows(TooManyArgmentsException.class, () -> {
            new BooleanOptionParser().parse(asList("-l", "t"), option("l"));
        });

        assertEquals("l", e.getOption());
    }

    @Test // Sad path
    public void should_not_accept_extra_arguments_for_boolean_option() {
        TooManyArgmentsException e = assertThrows(TooManyArgmentsException.class, () -> {
            new BooleanOptionParser().parse(asList("-l", "t", "f"), option("l"));
        });

        assertEquals("l", e.getOption());
    }

    @Test // Default value
    public void should_set_default_value_to_false_if_option_not_present() {
        assertFalse(new BooleanOptionParser().parse(asList( ), option("l")));
    }

    @Test // Happy path
    public void should_set_boolean_option_to_true_if_flag_present() {
        assertTrue(new BooleanOptionParser().parse(asList("-l"), option("l")));
    }

}
