package wuxian.me.rxqrcodescanner.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static wuxian.me.rxqrcodescanner.util.Preconditions.checkArgument;
import static wuxian.me.rxqrcodescanner.util.Preconditions.checkNotNull;

/**
 * Created by wuxian on 10/11/2016.
 * <p>
 * copy from @google Agera
 */

public final class Result<T> {
    @NonNull
    private static final Result<Object> ABSENT;
    @NonNull
    private static final Result<Object> FAILURE;
    @NonNull
    private static final Throwable ABSENT_THROWABLE;

    static {
        final Throwable failureThrowable = new Throwable("Attempt failed");
        failureThrowable.setStackTrace(new StackTraceElement[0]);
        FAILURE = new Result<>(null, failureThrowable);
        ABSENT_THROWABLE = new NullPointerException("Value is absent");
        ABSENT_THROWABLE.setStackTrace(new StackTraceElement[0]);
        ABSENT = new Result<>(null, ABSENT_THROWABLE);
    }

    @Nullable
    private final T value;
    @Nullable
    private final Throwable failure;

    Result(@Nullable final T value, @Nullable final Throwable failure) {
        checkArgument(value != null ^ failure != null, "Illegal Result arguments");
        this.value = value;
        this.failure = failure;
    }

    /**
     * Creates a {@link Result} of a successful attempt that produced the given {@code value}.
     */
    @NonNull
    public static <T> Result<T> success(@NonNull final T value) {
        return new Result<>(checkNotNull(value), null);
    }

    /**
     * Creates a {@link Result} of a failed attempt that encountered the given {@code failure}.
     */
    @NonNull
    public static <T> Result<T> failure(@NonNull final Throwable failure) {
        return failure == ABSENT_THROWABLE
                ? Result.<T>absent() : new Result<T>(null, checkNotNull(failure));
    }

    /**
     * Returns the singleton {@link Result} denoting a failed attempt that has a generic failure.
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public static <T> Result<T> failure() {
        return (Result<T>) FAILURE;
    }

    /**
     * Creates a {@link Result} denoting a non-null value. This is an alias of {@link #success}.
     */
    @NonNull
    public static <T> Result<T> present(@NonNull final T value) {
        return success(value);
    }

    /**
     * Returns the singleton {@link Result} denoting an absent value, with a failure of
     * {@link NullPointerException}.
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public static <T> Result<T> absent() {
        return (Result<T>) ABSENT;
    }

    /**
     * Creates a {@code Result} denoting the {@code value} if it is non-null, or returns the singleton
     * {@link #absent} result.
     */
    @NonNull
    public static <T> Result<T> absentIfNull(@Nullable final T value) {
        return value == null ? Result.<T>absent() : present(value);
    }

    /**
     * Returns whether this is the result of a successful attempt.
     */
    public boolean succeeded() {
        return value != null;
    }

    /**
     * Returns whether this is the result of a failed attempt.
     */
    public boolean failed() {
        return value == null;
    }

    /**
     * Returns whether the output value is present. This is an alias of {@link #succeeded()}.
     */
    public boolean isPresent() {
        return succeeded();
    }

    /**
     * Returns whether this is a result denoting an absent value. This is <i>not</i> an alias of
     * {@link #failed()}; this checks whether this instance is obtained from {@link #absent()}.
     */
    public boolean isAbsent() {
        return this == ABSENT;
    }

    @NonNull
    public T get() throws FailedResultException {
        if (value != null) {
            return value;
        }
        throw new FailedResultException(failure);
    }

    /**
     * Returns the output value of the successful attempt, or null if the attempt has {@link #failed}.
     */
    @Nullable
    public T orNull() {
        return value;
    }

    /**
     * Returns the failure encountered in the attempt that produced this result, or null if the
     * attempt has {@link #succeeded}.
     */
    @Nullable
    public Throwable failureOrNull() {
        return failure;
    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Result<?> result = (Result<?>) o;

        if (value != null ? !value.equals(result.value) : result.value != null) {
            return false;
        }
        if (failure != null ? !failure.equals(result.failure) : result.failure != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (failure != null ? failure.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        if (this == ABSENT) {
            return "Result{Absent}";
        }
        if (this == FAILURE) {
            return "Result{Failure}";
        }
        if (value != null) {
            return "Result{Success; value=" + value + "}";
        }
        return "Result{Failure; failure=" + failure + "}";
    }

    public static final class FailedResultException extends IllegalStateException {

        FailedResultException(@Nullable final Throwable cause) {
            super("Cannot get() from a failed result", cause);
        }
    }
}

