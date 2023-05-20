package org.fufeng.tdd;

import java.util.List;
import java.util.function.Function;

class SingleValueOptionParser<T> implements OptionParser<T> {

    private Function<String, T> valueParser;

    private Function<String, T> zeroValue;

    public SingleValueOptionParser(Function<String, T> valueParser, Function<String, T> zeroValue) {
        this.valueParser = valueParser;
        this.zeroValue = zeroValue;
    }

    @Override
    public T parse(List<String> arguments, Option option) {
        int index = arguments.indexOf("-" + option.value());
        if (index == -1) {
            return zeroValue.apply(null);
        }

        // TODO: 这里可以增加注释，更推荐的方式是，通过抽取方法，让方法名成为注释。或者，换一种更容易理解的方法来实现同样的功能.
        if (isReachEndOfList(arguments, index) || isFollowedByOtherFlag(arguments, index)) {
            throw new InsufficientArgmentsException(option.value());
        }

        if (secondArgumentIsNotAFlag(arguments, index)) {
            throw new TooManyArgmentsException(option.value());
        }

        String value = arguments.get(index + 1);
        return valueParser.apply(value);
    }

    private static boolean secondArgumentIsNotAFlag(List<String> arguments, int index) {
        return (index + 2) < arguments.size() &&
                !arguments.get(index + 2).startsWith("-");
    }

    private static boolean isFollowedByOtherFlag(List<String> arguments, int index) {
        return index + 1 < arguments.size() &&
                arguments.get(index + 1).startsWith("-");
    }

    private static boolean isReachEndOfList(List<String> arguments, int index) {
        return index + 1 == arguments.size();
    }

}
