package org.fufeng.tdd;

import org.fufeng.tdd.exceptions.IllegalOptionException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ArgsTest {

    // -l -p 8080 -d /usr/logs
    // [-l], [-p 8080], [-d /usr/logs]
    // {[-l], [-p 8080], [-d /usr/logs]}
    // Single Option:

    // -Bool -l
    @Test
    public void should_set_boolean_option_to_true_if_flag_present() {
        BooleanOption option = Args.parse(BooleanOption.class, "-l");

        assertTrue(option.logging());
    }

    @Test
    public void should_set_boolean_option_to_true_if_flag_not_present() {
        BooleanOption option = Args.parse(BooleanOption.class);

        assertFalse(option.logging());
    }

    static record BooleanOption(@Option("l") boolean logging) {
    }

    // -Integer -p 8080
    @Test
    public void should_set_integer_option_to_true_if_flag_present() {
        IntegerOption option = Args.parse(IntegerOption.class, "-p", "8080");

        assertEquals(8080, option.port());
    }

    @Test
    public void should_set_integer_option_to_true_if_flag_not_present() {
        IntegerOption option = Args.parse(IntegerOption.class);

        assertEquals(0, option.port());
    }

    static record IntegerOption(@Option("p") int port) {
    }

    // -String -d /usr/logs
    //
    @Test
    public void should_set_string_option_to_true_if_flag_present() {
        StringOption option = Args.parse(StringOption.class, "-d", "/usr/logs");

        assertEquals("/usr/logs", option.directory());
    }

    @Test
    public void should_set_string_option_to_true_if_flag_not_present() {
        StringOption option = Args.parse(StringOption.class);

        assertEquals("", option.directory());
    }

    static record StringOption(@Option("d") String directory) {
    }

    // Multiple Option:
    // -l -p 8080 -d /usr/logs
    //

    @Test
    public void should_set_mutiple_option_to_true_if_flag_present() {
        // SUT Args.parse

        // exercise
        MultiOptions options = Args.parse(MultiOptions.class, "-l", "-p", "8080", "-d", "/usr/logs");

        // verify
        assertTrue(options.logging());
        assertEquals(8080, options.port());
        assertEquals("/usr/logs", options.directory());

        // teardown
    }

    // setup
    static record MultiOptions(@Option("l") boolean logging, @Option("p") int port, @Option("d") String directory) {
    }

    @Test
    public void should_throw_illegal_option_exception_if_annotation_not_present() {
        // before
        IllegalOptionException e = /*verify*/assertThrows(IllegalOptionException.class, () -> /* execrise */Args.parse(OptionsWithoutAnnotation.class));

        // verify
        assertEquals("port", e.getOption());

        // teardown
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
    public void should_example_2() {
        ListOptions options = Args.parse(ListOptions.class, "-g", "this", "is", "a", "list", "-d", "1", "2", "-3", "5");

        assertArrayEquals(new String[]{"this", "is", "a", "list"}, options.group());
        assertArrayEquals(new Integer[]{1, 2, -3, 5}, options.decimals);
    }

    static record ListOptions(@Option("g") String[] group, @Option("d") Integer[] decimals) {
    }


    // -l -p 8080 -d /usr/logs
    @Test
    public void should() {
//        Arguments args = Args.parse("l:b,p:d,d:s", "-l", "-p", "8080", "-d", "/usr/logs");
//        args.getBool("l");
//        args.getInt("p");
//
//        Options options = Args.parse(Options.class, "-l", "-p", "8080", "-d", "/usr/logs");
    }

    /**
     * 下面就转换成了单元测试风格
     */
    @Test
    public void should_parse_option_if_option_parser_provided() {
        //SUT Args.parse
        OptionParser boolParser = mock(OptionParser.class);
        OptionParser intParser = mock(OptionParser.class);
        OptionParser stringParser = mock(OptionParser.class);

        when(boolParser.parse(any(), any())).thenReturn(true);
        when(intParser.parse(any(), any())).thenReturn(0);
        when(stringParser.parse(any(),any())).thenReturn("parse");

        // exercise
        Args<MultiOptions> args = new Args<>(MultiOptions.class, Map.of(boolean.class, boolParser, int.class, intParser, String.class, stringParser));
        MultiOptions options = args.parse("-l", "-p", "8080", "-d", "/usr/logs");

        // verify
        assertTrue(options.logging());
        assertEquals(0, options.port());
        assertEquals("parse", options.directory());

        // teardown*/
    }

}
