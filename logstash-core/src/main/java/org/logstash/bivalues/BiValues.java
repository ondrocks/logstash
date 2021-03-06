package org.logstash.bivalues;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import org.jruby.RubyBignum;
import org.jruby.RubyInteger;
import org.jruby.RubySymbol;
import org.jruby.ext.bigdecimal.RubyBigDecimal;
import org.jruby.java.proxies.JavaProxy;
import org.jruby.runtime.builtin.IRubyObject;

public enum BiValues {
    JAVA_LANG_INTEGER(BiValueType.INT),
    JAVA_LANG_LONG(BiValueType.LONG),
    JAVA_MATH_BIGDECIMAL(BiValueType.DECIMAL),
    JAVA_MATH_BIGINTEGER(BiValueType.BIGINT),
    ORG_JRUBY_EXT_BIGDECIMAL_RUBYBIGDECIMAL(BiValueType.DECIMAL),
    ORG_JRUBY_JAVA_PROXIES_CONCRETEJAVAPROXY(BiValueType.JAVAPROXY),
    ORG_JRUBY_RUBYBIGNUM(BiValueType.BIGINT),
    ORG_JRUBY_RUBYFIXNUM(BiValueType.LONG),
    ORG_JRUBY_RUBYINTEGER(BiValueType.LONG),
    ORG_JRUBY_RUBYNIL(BiValueType.NULL),
    ORG_JRUBY_RUBYSYMBOL(BiValueType.SYMBOL), // one way conversion, a Java string will use STRING
    NULL(BiValueType.NULL);

    private static HashMap<String, String> initCache() {
        HashMap<String, String> hm = new HashMap<>();
        hm.put("java.lang.Integer", "JAVA_LANG_INTEGER");
        hm.put("java.lang.Long", "JAVA_LANG_LONG");
        hm.put("java.math.BigDecimal", "JAVA_MATH_BIGDECIMAL");
        hm.put("java.math.BigInteger", "JAVA_MATH_BIGINTEGER");
        hm.put("org.jruby.RubyBignum", "ORG_JRUBY_RUBYBIGNUM");
        hm.put("org.jruby.RubyFixnum", "ORG_JRUBY_RUBYFIXNUM");
        hm.put("org.jruby.RubyInteger", "ORG_JRUBY_RUBYINTEGER");
        hm.put("org.jruby.RubyNil", "ORG_JRUBY_RUBYNIL");
        hm.put("org.jruby.RubySymbol", "ORG_JRUBY_RUBYSYMBOL");
        hm.put("org.jruby.ext.bigdecimal.RubyBigDecimal", "ORG_JRUBY_EXT_BIGDECIMAL_RUBYBIGDECIMAL");
        hm.put("org.jruby.java.proxies.ConcreteJavaProxy", "ORG_JRUBY_JAVA_PROXIES_CONCRETEJAVAPROXY");
        return hm;
    }

    public static final NullBiValue NULL_BI_VALUE = NullBiValue.newNullBiValue();

    private final BiValueType biValueType;

    BiValues(BiValueType biValueType) {
        this.biValueType = biValueType;
    }

    private static final HashMap<String, String> NAME_CACHE = initCache();

    private BiValue build(Object value) {
        return biValueType.build(value);
    }

    public static BiValue newBiValue(Object o) {
        if (o == null) {
            return NULL_BI_VALUE;
        }
        return valueOf(fetchName(o)).build(o);
    }

    private static String fetchName(Object o) {
        final String cls = o.getClass().getName();
        final String name = NAME_CACHE.get(cls);
        if (name != null) {
            return name;
        }
        return cacheName(cls);
    }
    
    private static String cacheName(final String cls) {
        final String toCache = cls.toUpperCase().replace('.', '_');
        // TODO[Guy] log warn that we are seeing a uncached value
        NAME_CACHE.put(cls, toCache);
        return toCache;
    }

    private enum BiValueType {
        SYMBOL {
            BiValue build(Object value) {
                if (value instanceof IRubyObject) {
                    return new SymbolBiValue((RubySymbol) value);
                }
                return new SymbolBiValue((String) value);
            }
        },
        LONG {
            BiValue build(Object value) {
                if (value instanceof IRubyObject) {
                    return new LongBiValue((RubyInteger) value);
                }
                return new LongBiValue((Long) value);
            }
        },
        INT {
            BiValue build(Object value) {
                if (value instanceof IRubyObject) {
                    return new IntegerBiValue((RubyInteger) value);
                }
                return new IntegerBiValue((Integer) value);
            }
        },
        DECIMAL {
            BiValue build(Object value) {
                if (value instanceof IRubyObject) {
                    return new BigDecimalBiValue((RubyBigDecimal) value);
                }
                return new BigDecimalBiValue((BigDecimal) value);
            }
        },
        NULL {
            NullBiValue build(Object value) {
                return NULL_BI_VALUE;
            }
        },
        BIGINT {
            BiValue build(Object value) {
                if (value instanceof IRubyObject) {
                    return new BigIntegerBiValue((RubyBignum) value);
                }
                return new BigIntegerBiValue((BigInteger) value);
            }
        },
        JAVAPROXY {
            BiValue build(Object value) {
                if (value instanceof IRubyObject) {
                    return new JavaProxyBiValue((JavaProxy) value);
                }
                return new JavaProxyBiValue(value);
            }
        };
        abstract BiValue build(Object value);
    }

}
