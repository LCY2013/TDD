package org.fufeng.tdd;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ArgsTest {

    record IntOption(@Option("p") int port){}

    /**
     * TDD-伦敦学派 做法
     */
    @Test
    public void should_parse_int_option() {
        ValueRetriever valueRetriever = mock(ValueRetriever.class);
        OptionParser optionParser = mock(OptionParser.class);
        OptionClass<IntOption> optionClass = mock(OptionClass.class);

        when(optionClass.getOptionNames()).thenReturn(new String[]{"p"});
        when(optionClass.getOptionType(eq("p"))).thenReturn(int.class);
        when(valueRetriever.getValue(eq("p"), eq(new String[]{"-p", "8080"}))).thenReturn(new String[]{"8080"});
        when(optionParser.parse(eq(int.class), eq(new String[]{"8080"}))).thenReturn(8080);
        when(optionClass.create(eq(new Object[]{8080}))).thenReturn(new IntOption(8080));

        Args<IntOption> args = new Args<>(valueRetriever, optionParser, optionClass);

        IntOption intOption = args.parse("-p", "8080");

        assertEquals(8080, intOption.port);
    }

}