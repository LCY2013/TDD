package org.fufeng.tdd;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ArgsTest {

    // -l -p 8080 -d /usr/logs
    // [-l], [-p 8080], [-d /usr/logs]
    // {[-l], [-p 8080], [-d /usr/logs]}
    // Single Option:

    // -Bool -l
    /*@Test
    public void should_set_boolean_option_to_true_if_flag_present() {
        BooleanOption option = Args.parse(BooleanOption.class, "-l");

        assertTrue(option.logging());
    }

    @Test
    public void should_set_boolean_option_to_true_if_flag_not_present() {
        BooleanOption option = Args.parse(BooleanOption.class);

        assertFalse(option.logging());
    }

    static record BooleanOption(@Option("l") boolean logging) {}*/

    // -Integer -p 8080
    /*@Test
    public void should_set_integer_option_to_true_if_flag_present() {
        IntegerOption option = Args.parse(IntegerOption.class, "-p", "8080");

        assertEquals(8080, option.port());
    }

    @Test
    public void should_set_integer_option_to_true_if_flag_not_present() {
        IntegerOption option = Args.parse(IntegerOption.class);

        assertEquals(0, option.port());
    }

    static record IntegerOption(@Option("p") int port) {}

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

    static record StringOption(@Option("d") String directory) {}*/

    // Multiple Option:
    // -l -p 8080 -d /usr/logs
    //

    @Test
    public void should_set_mutiple_option_to_true_if_flag_present() {
        MultiOptions options = Args.parse(MultiOptions.class, "-l", "-p", "8080", "-d", "/usr/logs");
        assertTrue(options.logging());
        assertEquals(8080, options.port());
        assertEquals("/usr/logs", options.directory());
    }

    static record MultiOptions(@Option("l") boolean logging, @Option("p") int port, @Option("d") String directory) {}

    @Test
    public void should_throw_illegal_option_exception_if_annotation_not_present() {
        IllegalOptionException e = assertThrows(IllegalOptionException.class, () -> Args.parse(OptionsWithoutAnnotation.class));

        assertEquals("port", e.getOption());
    }

    static record OptionsWithoutAnnotation(@Option("l") boolean logging, int port, @Option("d") String directory) {}


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
        /*ListOptions options = Args.parse(ListOptions.class, "-g", "this", "is", "a", "list", "-d", "1", "2", "-3", "5");

        assertArrayEquals(new String[]{"this", "is", "a", "list"}, options.group());
        assertArrayEquals(new int[]{1, 2, -3, 5}, options.decimals);*/
    }

    static record ListOptions(@Option("g") String[] group, @Option("d") int[] decimals) {}

    // -l -p 8080 -d /usr/logs
    @Test
    public void should() {
//        Arguments args = Args.parse("l:b,p:d,d:s", "-l", "-p", "8080", "-d", "/usr/logs");
//        args.getBool("l");
//        args.getInt("p");
//
//        Options options = Args.parse(Options.class, "-l", "-p", "8080", "-d", "/usr/logs");
    }

}
