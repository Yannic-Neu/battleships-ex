package battleships_ex.gdx.data;

/**
 * Generic callback for asynchronous data operations.
 * Used by DataSource implementations to return results from Firebase or other backends.
 */
@FunctionalInterface
public interface DataCallback<T> {
    void onSuccess(T result);
    default void onFailure(String error) {
        // Default implementation to allow functional interface usage
    }
}
