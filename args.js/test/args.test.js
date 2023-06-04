import expect from "expect";

describe('Args parser', () => {
    it('should parse multi options', () => {
        let schema = {
            logging: option('l', bool()),
            port: option('p', int()),
            directory: option('d', string()),
        };

        let options = parse(schema, ['-l', '-p', "8080", '-d', '/usr/logs']);

        expect(options.logging).toBeTruthy();
        expect(options.port).toEqual(8080);
        expect(options.directory).toEqual('/usr/logs');
    });

    describe('parse', () => {
        it('should call parsers in schema to build option', () => {
            let schema = {
                logging: (args) => args,
                port: (args) => args,
            };

            let option = parse(schema, ["args"]);
            expect(option.logging).toEqual(["args"]);
            expect(option.port).toEqual(["args"]);
        });
    });

    describe('option', () => {
        let opt = option('l', (values) => values);

        it('should fetch values followed by flag', () => {
            expect(opt(['-l', 'a', 'b'])).toEqual(['a', 'b']);
        });

        it('should only fetch values util next flag', () => {
            expect(opt(['-l', 'a', 'b', '-p'])).toEqual(['a', 'b']);
        });

        it('should fetch empty array if no value given', () => {
            expect(opt(['-l'])).toEqual([]);
        });

        it('should fetch undefined if no flag match', () => {
            expect(opt(['-p', 'a'])).toEqual(undefined);
        });

        it('should call type to handle values', () => {
            let opt = option('l', (values) => 1);
            expect(opt(['-l', 'a', 'b'])).toEqual(1);
        });
    });

    describe('bool', () => {
        let type = bool();

        it('should return true, if empty array given', () => {
            expect(type([])).toBeTruthy();
        });

        it('should return true, if empty array given', () => {
            expect(type(undefined)).toBeFalsy();
        });
    });

    describe('int', () => {
        let type = int();

        it('should return int value, if array with single value given', () => {
            expect(type(['1'])).toEqual(1);
        });

        it('should return default int value, if undefined given', () => {
            expect(type(undefined)).toEqual(0);
        });
    });

    describe('string', () => {
        let type = string();

        it('should return string value, if array with single value given', () => {
            expect(type(['1'])).toEqual('1');
        });

        it('should return default string value, if undefined given', () => {
            expect(type(undefined)).toEqual('');
        });
    });

});

function option(flag, type) {
    return function (args) {
        let index = args.indexOf(`-${flag}`);
        if (index == -1) return undefined;
        let nextIndex = args.findIndex((v, i) => i > index && /^-[a-zA-Z]+/.test(v));
        if (nextIndex === -1) nextIndex = args.length;
        return type(args.slice(index + 1, nextIndex));
    }
}

function single(defaultValue, parse) {
    return function (args) {
        if (!args) return defaultValue;
        if (args.length > 0) throw 'too many values';
        if (args.length < 0) throw 'too few values';
        return parse(args);
    };
}

function bool(defaultValue = false) {
    return single(defaultValue, (args) => args.length === 0);
}

function int(defaultValue = 0) {
    return unary(defaultValue, parseInt);
}

function string(defaultValue = '') {
    return unary(defaultValue, (args) => args);
}

function unary(defaultValue, parse) {
    return function (args) {
        if (!args) return defaultValue;
        if (args.length > 1) throw 'too many values';
        if (args.length < 1) throw 'too few values';
        return parse(args[0]);
    };
}

function parse(schema, args) {
    let options = {};
    for (let key of Object.keys(schema))
        options[key] = schema[key](args)
    return options;
}