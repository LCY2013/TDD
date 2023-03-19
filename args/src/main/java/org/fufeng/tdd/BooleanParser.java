package org.fufeng.tdd;

import java.util.List;

class BooleanParser implements OptionParser {

    public static final BooleanParser parser = new BooleanParser();

    @Override
    public Object parse(List<String> arguments, Option option) {
        return arguments.contains("-" + option.value());
    }

}
