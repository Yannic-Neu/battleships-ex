package battleships_ex.gdx.data;

/**
 * Generic callback for asynchronous data operations.
 * Used by DataSource implementations to return results from Firebase or other backends.
 */
public interface DataCallback<T> {
    void onSuccess(T result);
    void onFailure(String error);
}
