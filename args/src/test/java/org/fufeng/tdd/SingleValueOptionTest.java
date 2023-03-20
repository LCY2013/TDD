package org.fufeng.tdd;

import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.fufeng.tdd.BeforeTest.option;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.function.Function;

public class SingleValueOptionTest {

    @Test // sad path
    public void should_not_accept_extra_argument_for_single_option() {
        TooManyArgmentsException e = assertThrows(TooManyArgmentsException.class, () -> {
            new SingleValueOptionParser<>(Integer::parseInt, v -> 0).parse(asList("-p", "8080", "8081"), option("p"));
        });

        assertEquals("p", e.getOption());
    }

    @ParameterizedTest // sad path
    @ValueSource(strings = {"-p -l", "-p"})
    public void should_not_accept_insufficient_argument_for_single_option(String arguments) {
        InsufficientArgmentsException e = assertThrows(InsufficientArgmentsException.class, () -> {
            new SingleValueOptionParser<>(Integer::parseInt, v -> 0).parse(asList(arguments.split(" ")), option("p"));
        });

        assertEquals("p", e.getOption());
    }

    @Test // default value
    public void should_not_set_default_value_to_0_for_int_option() {
        assertEquals(0,  new SingleValueOptionParser<>(Integer::parseInt, v -> 0).parse(asList(), option("p")));
    }

    @Test // default value
    public void should_not_set_default_value_to_any_for_int_option() {
        Function<String, Object> whatever = any -> null;
        Object defaultValue = new Object();

        assertSame(defaultValue,  new SingleValueOptionParser<>(whatever, v -> defaultValue).parse(asList(), option("p")));
    }

    @Test // happy path
    public void should_parse_value_if_flag_present() {
        assertEquals(8080,  new SingleValueOptionParser<>(Integer::parseInt, v -> 0).parse(asList("-p", "8080"), option("p")));
    }

    @Test // happy path
    public void should_parse_value_with_any_if_flag_present() {
        Object parsed = new Object();
        Function parser = any -> parsed;
        Object whatever = new Object();

        assertSame(parsed,  new SingleValueOptionParser<>(parser, v -> whatever).parse(asList("-p", "8080"), option("p")));
    }

    @Test // sad path
    public void should_not_accept_extra_argument_for_string_single_value_option() {
        TooManyArgmentsException e = assertThrows(TooManyArgmentsException.class, () -> {
            new SingleValueOptionParser<>(String::valueOf, v -> "").parse(asList("-d", "/usr/vars", "/usr/logs"), option("d"));
        });

        assertEquals("d", e.getOption());
    }
}
