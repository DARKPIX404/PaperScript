package dev.paperscript.legacy.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal JSON reader/writer (objects, arrays, strings, numbers, booleans,
 * null). Enough for plugin manifests and the per-script storage file; keeps
 * the legacy line dependency-free (no Gson on a bare 1.12.2 classpath).
 */
public final class Json {
    private Json() {
    }

    public static Object parse(String text) {
        return new Reader(text).read();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String text) {
        Object value = parse(text);
        return value instanceof Map ? (Map<String, Object>) value : new LinkedHashMap<String, Object>();
    }

    public static String stringify(Object value) {
        StringBuilder sb = new StringBuilder();
        write(sb, value);
        return sb.toString();
    }

    // ---------------------------------------------------------------- writer

    @SuppressWarnings("unchecked")
    private static void write(StringBuilder sb, Object value) {
        if (value == null) {
            sb.append("null");
        } else if (value instanceof String) {
            writeString(sb, (String) value);
        } else if (value instanceof Number || value instanceof Boolean) {
            sb.append(value.toString());
        } else if (value instanceof Map) {
            sb.append('{');
            boolean first = true;
            for (Map.Entry<Object, Object> e : ((Map<Object, Object>) value).entrySet()) {
                if (!first) sb.append(',');
                first = false;
                writeString(sb, String.valueOf(e.getKey()));
                sb.append(':');
                write(sb, e.getValue());
            }
            sb.append('}');
        } else if (value instanceof Iterable) {
            sb.append('[');
            boolean first = true;
            for (Object item : (Iterable<Object>) value) {
                if (!first) sb.append(',');
                first = false;
                write(sb, item);
            }
            sb.append(']');
        } else if (value.getClass().isArray()) {
            sb.append('[');
            Object[] arr = (Object[]) value;
            for (int i = 0; i < arr.length; i++) {
                if (i > 0) sb.append(',');
                write(sb, arr[i]);
            }
            sb.append(']');
        } else {
            writeString(sb, value.toString());
        }
    }

    private static void writeString(StringBuilder sb, String s) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
    }

    // ---------------------------------------------------------------- reader

    private static final class Reader {
        private final String s;
        private int pos;

        Reader(String s) {
            this.s = s == null ? "" : s;
        }

        Object read() {
            skipWs();
            Object value = readValue();
            skipWs();
            if (pos != s.length()) throw error("trailing characters");
            return value;
        }

        private Object readValue() {
            skipWs();
            if (pos >= s.length()) throw error("unexpected end of input");
            char c = s.charAt(pos);
            switch (c) {
                case '{': return readObject();
                case '[': return readArray();
                case '"': return readString();
                case 't': expect("true"); return Boolean.TRUE;
                case 'f': expect("false"); return Boolean.FALSE;
                case 'n': expect("null"); return null;
                default: return readNumber();
            }
        }

        private Map<String, Object> readObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            pos++; // {
            skipWs();
            if (peek('}')) { pos++; return map; }
            while (true) {
                skipWs();
                if (pos >= s.length() || s.charAt(pos) != '"') throw error("expected object key");
                String key = readString();
                skipWs();
                if (pos >= s.length() || s.charAt(pos) != ':') throw error("expected ':'");
                pos++;
                map.put(key, readValue());
                skipWs();
                if (peek(',')) { pos++; continue; }
                if (peek('}')) { pos++; return map; }
                throw error("expected ',' or '}'");
            }
        }

        private List<Object> readArray() {
            List<Object> list = new ArrayList<>();
            pos++; // [
            skipWs();
            if (peek(']')) { pos++; return list; }
            while (true) {
                list.add(readValue());
                skipWs();
                if (peek(',')) { pos++; continue; }
                if (peek(']')) { pos++; return list; }
                throw error("expected ',' or ']'");
            }
        }

        private String readString() {
            pos++; // opening quote
            StringBuilder sb = new StringBuilder();
            while (pos < s.length()) {
                char c = s.charAt(pos++);
                if (c == '"') return sb.toString();
                if (c == '\\') {
                    if (pos >= s.length()) throw error("bad escape");
                    char e = s.charAt(pos++);
                    switch (e) {
                        case '"': sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        case '/': sb.append('/'); break;
                        case 'n': sb.append('\n'); break;
                        case 'r': sb.append('\r'); break;
                        case 't': sb.append('\t'); break;
                        case 'b': sb.append('\b'); break;
                        case 'f': sb.append('\f'); break;
                        case 'u':
                            if (pos + 4 > s.length()) throw error("bad \\u escape");
                            sb.append((char) Integer.parseInt(s.substring(pos, pos + 4), 16));
                            pos += 4;
                            break;
                        default: throw error("bad escape '\\" + e + "'");
                    }
                } else {
                    sb.append(c);
                }
            }
            throw error("unterminated string");
        }

        private Object readNumber() {
            int start = pos;
            while (pos < s.length()) {
                char c = s.charAt(pos);
                if (c == '-' || c == '+' || c == '.' || c == 'e' || c == 'E' || Character.isDigit(c)) {
                    pos++;
                } else {
                    break;
                }
            }
            if (start == pos) throw error("unexpected character '" + s.charAt(pos) + "'");
            String raw = s.substring(start, pos);
            try {
                if (raw.indexOf('.') < 0 && raw.indexOf('e') < 0 && raw.indexOf('E') < 0) {
                    return Long.parseLong(raw);
                }
                return Double.parseDouble(raw);
            } catch (NumberFormatException ex) {
                throw error("bad number '" + raw + "'");
            }
        }

        private void expect(String literal) {
            if (!s.startsWith(literal, pos)) throw error("expected '" + literal + "'");
            pos += literal.length();
        }

        private boolean peek(char c) {
            return pos < s.length() && s.charAt(pos) == c;
        }

        private void skipWs() {
            while (pos < s.length()) {
                char c = s.charAt(pos);
                if (c == ' ' || c == '\t' || c == '\n' || c == '\r') pos++; else break;
            }
        }

        private IllegalArgumentException error(String message) {
            return new IllegalArgumentException("Invalid JSON at char " + pos + ": " + message);
        }
    }
}
