package org.fufeng.tdd;

import org.fufeng.tdd.exceptions.IllegalValueException;
import org.fufeng.tdd.exceptions.InsufficientArgmentsException;
import org.fufeng.tdd.exceptions.TooManyArgmentsException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;

import java.util.List;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static org.fufeng.tdd.BeforeTest.option;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class OptionParsersTest {

    @Nested
    class UnaryOptionParser {

        @Test // sad path
        public void should_not_accept_extra_argument_for_single_option() {
            TooManyArgmentsException e = assertThrows(TooManyArgmentsException.class, () -> {
                OptionParsers.unary(Integer::parseInt, v -> 0).parse(asList("-p", "8080", "8081"), option("p"));
            });

            assertEquals("p", e.getOption());
        }

        @ParameterizedTest // sad path
        @ValueSource(strings = {"-p -l", "-p"})
        public void should_not_accept_insufficient_argument_for_single_option(String arguments) {
            InsufficientArgmentsException e = assertThrows(InsufficientArgmentsException.class, () -> {
                OptionParsers.unary(Integer::parseInt, v -> 0).parse(asList(arguments.split(" ")), option("p"));
            });

            assertEquals("p", e.getOption());
        }

        @Test // default value
        public void should_not_set_default_value_to_0_for_int_option() {
            assertEquals(0, OptionParsers.unary(Integer::parseInt, v -> 0).parse(asList(), option("p")));
        }

        @Test // default value
        public void should_not_set_default_value_to_any_for_int_option() {
            Function<String, Object> whatever = any -> null;
            Object defaultValue = new Object();

            assertSame(defaultValue, OptionParsers.unary(whatever, v -> defaultValue).parse(asList(), option("p")));
        }

        @Test // happy path
        public void should_parse_value_if_flag_present() {
            assertEquals(8080, OptionParsers.unary(Integer::parseInt, v -> 0).parse(asList("-p", "8080"), option("p")));
        }

        /**
         * 行为验证
         */
        @Test // happy path
        public void should_parse_value_with_any_if_flag_present() {
            /*Object parsed = new Object();
            Function parser = any -> parsed;
            Object whatever = new Object();

            assertSame(parsed, OptionParsers.unary(parser, v -> whatever).parse(asList("-p", "8080"), option("p")));*/

            Function parser = mock(Function.class);

            OptionParsers.unary(parser, v -> v).parse(asList("-p", "8080"), option("p"));
            verify(parser).apply("8080");
        }

        @Test // sad path
        public void should_not_accept_extra_argument_for_string_single_value_option() {
            TooManyArgmentsException e = assertThrows(TooManyArgmentsException.class, () -> {
                OptionParsers.unary(String::valueOf, v -> "").parse(asList("-d", "/usr/vars", "/usr/logs"), option("d"));
            });

            assertEquals("d", e.getOption());
        }
    }

    @Nested
    class BoolOptionParser {

        @Test // Sad path
        public void should_not_accept_extra_argument_for_boolean_option() {
            TooManyArgmentsException e = assertThrows(TooManyArgmentsException.class, () -> {
                OptionParsers.bool().parse(asList("-l", "t"), option("l"));
            });

            assertEquals("l", e.getOption());
        }

        @Test // Sad path
        public void should_not_accept_extra_arguments_for_boolean_option() {
            TooManyArgmentsException e = assertThrows(TooManyArgmentsException.class, () -> {
                OptionParsers.bool().parse(asList("-l", "t", "f"), option("l"));
            });

            assertEquals("l", e.getOption());
        }

        @Test // Default value
        public void should_set_default_value_to_false_if_option_not_present() {
            assertFalse(OptionParsers.bool().parse(List.of(), option("l")));
        }

        @Test // Happy path
        public void should_set_boolean_option_to_true_if_flag_present() {
            assertTrue(OptionParsers.bool().parse(List.of("-l"), option("l")));
        }

    }

    @Nested
    class ListOptionParser {
        //TODO: -g this is a list -d 1 2 -3 5

        /**
         * 行为验证
         */
        @Test
        public void should_not_treat_negative_int_as_flag() {
            //assertArrayEquals(new String[]{"1", "-12"}, OptionParsers.list(String::valueOf, String[]::new).parse(asList("-g", "1", "-12"), option("g")));

            Function parser = mock(Function.class);

            OptionParsers.list(parser, String[]::new).parse(asList("-g", "1", "-12"), option("g"));

            InOrder inOrder = inOrder(parser, parser);
            inOrder.verify(parser).apply("1");
            inOrder.verify(parser).apply("-12");
        }

        //TODO: -g "this" "is" {"this", is"}
        @Test
        public void should_parse_list_value() {
            assertArrayEquals(new String[]{"this", "is"}, OptionParsers.list(String::valueOf, String[]::new).parse(asList("-g", "this", "is"), option("g")));
        }

        //TODO: default value []
        @Test
        public void should_use_empty_array_as_default_value() {
            assertArrayEquals(new String[]{}, OptionParsers.list(String::valueOf, String[]::new).parse(List.of(), option("g")));
        }

        //TODO: -d a throw exception
        @Test
        public void should_throw_exception_if_parser_can_parser_value() {
            Function<String, String> parser = (it) -> {
                throw new RuntimeException();
            };

            IllegalValueException e = assertThrows(
                    IllegalValueException.class,
                    () -> OptionParsers.list(parser, String[]::new).parse(asList("-g", "this"), option("g"))
            );
            assertEquals(e.getOption(), "g");
            assertEquals(e.getValue(), "this");
        }

    }
}
