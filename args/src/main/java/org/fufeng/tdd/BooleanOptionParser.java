package org.fufeng.tdd;

import java.util.List;

class BooleanOptionParser implements OptionParser<Boolean> {

    public static final BooleanOptionParser parser = new BooleanOptionParser();

    @Override
    public Boolean parse(List<String> arguments, Option option) {
        int index = arguments.indexOf("-" + option.value());
        if ((index + 1) < arguments.size() &&
                !arguments.get(index +1).startsWith("-")) {
            throw new TooManyArgmentsException(option.value());
        }
        return index != -1;
    }

}
