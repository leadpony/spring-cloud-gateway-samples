package example;

import java.util.stream.Stream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public interface FlatMapper<T, R> {

    @SuppressWarnings("unchecked")
    default Class<T> getInClass() {
        return (Class<T>) getTypeArgument(getClass(), 0);
    }

    @SuppressWarnings("unchecked")
    default Class<R> getOutClass() {
        return (Class<R>) getTypeArgument(getClass(), 1);
    }

    Stream<R> map(T body);

    private static Type getTypeArgument(Class<?> clazz, int index) {
        for (var type : clazz.getGenericInterfaces()) {
            if (type instanceof ParameterizedType parameterized) {
                if (parameterized.getRawType() == FlatMapper.class) {
                    return parameterized.getActualTypeArguments()[index];
                }
            }
        }
        throw new IllegalStateException();
    }
}
