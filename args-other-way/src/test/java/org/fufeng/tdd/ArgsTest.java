package org.fufeng.tdd;

import org.fufeng.tdd.exceptions.InsufficientArgmentsException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import static org.fufeng.tdd.Args.checkSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ArgsTest {

    // -l -p 8080 -d /usr/logs
    // [-l], [-p 8080], [-d /usr/logs]
    // {[-l], [-p 8080], [-d /usr/logs]}
    // Single Option:

    private static boolean parseBool(String[] values) {
        checkSize(0, values);
        return values != null;
    }

    // -Bool -l
    @Test
    public void should_set_boolean_option_to_true_if_flag_present() {
        Function<String[], Map<String, String[]>> optionParser = mock(Function.class);
        when(optionParser.apply(new String[]{"-l"})).thenReturn(Map.of("l", new String[0]));

        //Args<BooleanOption> args = new Args<>(BooleanOption.class, Map.of(boolean.class, ArgsTest::parseBool), Args::toMap);
        Args<BooleanOption> args = new Args<>(BooleanOption.class, Map.of(boolean.class, ArgsTest::parseBool), optionParser);

        BooleanOption option = args.parse("-l");
        assertTrue(option.logging());
    }

    @Test
    public void should_set_boolean_option_to_true_if_flag_not_present() {
        Function<String[], Map<String, String[]>> optionParser = mock(Function.class);
        when(optionParser.apply(new String[]{})).thenReturn(Map.of());

       // Args<BooleanOption> args = new Args<>(BooleanOption.class, Map.of(boolean.class, ArgsTest::parseBool), Args::toMap);
        Args<BooleanOption> args = new Args<>(BooleanOption.class, Map.of(boolean.class, ArgsTest::parseBool), optionParser);
        BooleanOption option = args.parse();

        assertFalse(option.logging());
    }

    static record BooleanOption(@Option("l") boolean logging) {
    }

    private static int parseInt(String[] values) {
        checkSize(1, values);
        return Arrays.stream(values).findFirst().map(Integer::parseInt).orElse(0);
    }

    // -Integer -p 8080
    @Test
    public void should_set_integer_option_to_true_if_flag_present() {
        Function<String[], Map<String, String[]>> optionParser = mock(Function.class);
        when(optionParser.apply(new String[]{"-p", "8080"})).thenReturn(Map.of("p", new String[]{"8080"}));

        //Args<IntegerOption> args = new Args<>(IntegerOption.class, Map.of(int.class, ArgsTest::parseInt), Args::toMap);
        Args<IntegerOption> args = new Args<>(IntegerOption.class, Map.of(int.class, ArgsTest::parseInt), optionParser);
        IntegerOption option = args.parse("-p", "8080");

        assertEquals(8080, option.port());
    }

    @Test
    public void should_set_integer_option_to_true_if_flag_not_present() {
        Function<String[], Map<String, String[]>> optionParser = mock(Function.class);
        when(optionParser.apply(new String[]{})).thenReturn(Map.of());

        //Args<IntegerOption> args = new Args<>(IntegerOption.class, Map.of(int.class, ArgsTest::parseInt), Args::toMap);
        Args<IntegerOption> args = new Args<>(IntegerOption.class, Map.of(int.class, ArgsTest::parseInt), optionParser);

        assertThrowsExactly(InsufficientArgmentsException.class, args::parse);
    }

    static record IntegerOption(@Option("p") int port) {
    }

    private static String parseString(String[] values) {
        checkSize(1, values);
        return Arrays.stream(values).findFirst().orElse("");
    }

    // -String -d /usr/logs
    //
    @Test
    public void should_set_string_option_to_true_if_flag_present() {
        Args<StringOption> args = new Args<>(StringOption.class, Map.of(String.class, ArgsTest::parseString), Args::toMap);
        StringOption option = args.parse("-d", "/usr/logs");

        assertEquals("/usr/logs", option.directory());
    }

    @Test
    public void should_set_string_option_to_true_if_flag_not_present() {
        Args<StringOption> args = new Args<>(StringOption.class, Map.of(String.class, ArgsTest::parseString), Args::toMap);

        assertThrowsExactly(InsufficientArgmentsException.class, args::parse);
    }

    static record StringOption(@Option("d") String directory) {
    }

    // Multiple Option:
    // -l -p 8080 -d /usr/logs
    //

    @Test
    @Disabled
    public void should_set_mutiple_option_to_true_if_flag_present() {
        /*// SUT Args.parse

        // exercise
        MultiOptions options = Args.parse(MultiOptions.class, "-l", "-p", "8080", "-d", "/usr/logs");

        // verify
        assertTrue(options.logging());
        assertEquals(8080, options.port());
        assertEquals("/usr/logs", options.directory());

        // teardown*/
    }

    // setup
    static record MultiOptions(@Option("l") boolean logging, @Option("p") int port, @Option("d") String directory) {
    }

    static record OptionsWithoutAnnotation(@Option("l") boolean logging, int port, @Option("d") String directory) {
    }


    // sad path:
    // -bool -l t / -t t f
    // -int -p/ -p 8080 8081
    // -String -d/ -d /usr/logs /usr/vars
    //
    // default value:
    // -bool: false
    // -int: 0
    // -String: ""

    @Test
    @Disabled
    public void should_example_2() {
       /* ListOptions options = Args.parse(ListOptions.class, "-g", "this", "is", "a", "list", "-d", "1", "2", "-3", "5");

        assertArrayEquals(new String[]{"this", "is", "a", "list"}, options.group());
        assertArrayEquals(new Integer[]{1, 2, -3, 5}, options.decimals);*/
    }

    static record ListOptions(@Option("g") String[] group, @Option("d") Integer[] decimals) {
    }


    // -l -p 8080 -d /usr/logs
    @Test
    @Disabled
    public void should() {
        /*Arguments args = Args.parse("l:b,p:d,d:s", "-l", "-p", "8080", "-d", "/usr/logs");
        args.getBool("l");
        args.getInt("p");

        Options options = Args.parse(Options.class, "-l", "-p", "8080", "-d", "/usr/logs");*/
    }

    /**
     * 下面就转换成了单元测试风格
     */
    @Test
    @Disabled
    public void should_parse_option_if_option_parser_provided() {
        //SUT Args.parse
        /*Args::toMap boolParser = mock(Args::toMap.class);
        Args::toMap intParser = mock(Args::toMap.class);
        Args::toMap stringParser = mock(Args::toMap.class);

        when(boolParser.parse(any(), any())).thenReturn(true);
        when(intParser.parse(any(), any())).thenReturn(0);
        when(stringParser.parse(any(),any())).thenReturn("parse");

        // exercise
        Args<MultiOptions> args = new Args<>(MultiOptions.class, Map.of(boolean.class, boolParser, int.class, intParser, String.class, stringParser));
        MultiOptions options = args.parse("-l", "-p", "8080", "-d", "/usr/logs");

        // verify
        assertTrue(options.logging());
        assertEquals(0, options.port());
        assertEquals("parse", options.directory());*/

        // teardown
    }

}
