package adapter.common;

/**
 * Marker interface exposing the underlying delegate held by a Jakarta-to-Javax
 * Servlet adapter instance.
 *
 * <p>Implemented by every adapter class in the conversion layer so that callers
 * who already hold a Jakarta-side reference can recover it from a Javax-typed
 * wrapper without resorting to reflection or instance-of cascades. Used by
 * the central {@code ServletAdapter} to short-circuit double-wrapping: if the
 * input value is already an {@code AdapterUnwrap}, the wrapped delegate is
 * returned directly rather than constructing a second adapter on top.
 *
 * <p>SRS mapping: FR-CONV-003 (AdapterUnwrap marker interface — interface
 * exists / unwrap method present / generic T correct / common module compile).
 *
 * @param <T> the type of the underlying delegate that {@link #unwrap()} returns
 */
// @req FR-CONV-003
public interface AdapterUnwrap<T> {

    /**
     * Returns the underlying delegate held by this adapter.
     *
     * @return the wrapped delegate of type {@code T}
     */
    T unwrap();
}
