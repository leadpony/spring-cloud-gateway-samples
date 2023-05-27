package example;

import java.lang.reflect.RecordComponent;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Function;

import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.AbstractEncoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.MimeType;

import reactor.core.publisher.Flux;

/**
 * An encoder for mime type 'text/csv'.
 */
public class CsvEncoder extends AbstractEncoder<Record> {

    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private static final MimeType[] SUPPORTED_MIME_TYPE = {
            new MimeType("text", "csv")
    };

    public CsvEncoder() {
        super(SUPPORTED_MIME_TYPE);
    }

    @Override
    public boolean canEncode(ResolvableType elementType, @Nullable MimeType mimeType) {
        Class<?> clazz = elementType.toClass();
        return clazz.isRecord() && super.canEncode(elementType, mimeType);
    }

    @Override
    public Flux<DataBuffer> encode(
            Publisher<? extends Record> inputStream,
            DataBufferFactory bufferFactory,
            ResolvableType elementType,
            MimeType mimeType,
            Map<String, Object> hints) {

        var rowMapper = new RowMapper(bufferFactory, elementType.toClass(), getCharset(mimeType));
        return Flux.from(inputStream).map(rowMapper);
    }

    @Override
    public DataBuffer encodeValue(
            Record value,
            DataBufferFactory bufferFactory,
            ResolvableType valueType,
            MimeType mimeType,
            Map<String, Object> hints) {

        var rowMapper = new RowMapper(bufferFactory, valueType.toClass(), getCharset(mimeType));
        return rowMapper.apply(value);
    }

    private static Charset getCharset(MimeType mimeType) {
        if (mimeType != null && mimeType.getCharset() != null) {
            return mimeType.getCharset();
        } else {
            return DEFAULT_CHARSET;
        }
    }

    private static class RowMapper implements Function<Record, DataBuffer> {

        private final DataBufferFactory bufferFactory;
        private final RecordComponent[] components;
        private final Charset charset;
        private int initialCapacity;

        RowMapper(DataBufferFactory bufferFactory, Class<?> rowType, Charset charset) {
            this.bufferFactory = bufferFactory;
            this.components = rowType.getRecordComponents();
            this.charset = charset;
            this.initialCapacity = components.length * 8;
        }

        @Override
        public DataBuffer apply(Record row) {
            DataBuffer buffer = encodeRow(row, bufferFactory, components, charset, initialCapacity);
            int capacity = buffer.capacity();
            if (initialCapacity < capacity) {
                initialCapacity = capacity;
            }
            return buffer;
        }

        private static DataBuffer encodeRow(
                Record row,
                DataBufferFactory bufferFactory,
                RecordComponent[] components,
                Charset charset,
                int initialCapacity) {

            DataBuffer dataBuffer = bufferFactory.allocateBuffer(initialCapacity);
            boolean release = true;
            try {
                for (int i = 0; i < components.length; i++) {
                    var component = components[i];
                    var value = component .getAccessor().invoke(row);
                    if (i > 0) {
                        dataBuffer.write(",", charset);
                    }
                    dataBuffer.write(value.toString(), charset);
                }
                dataBuffer.write("\n", charset);
                release = false;
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            } finally {
                if (release) {
                    DataBufferUtils.release(dataBuffer);
                }
            }
            return dataBuffer;
        }
    }
}
