package pl.pomykalskimateusz.recruitmenttask.utils;

import java.util.Optional;

@FunctionalInterface
public interface ThrowableSupplier<T> {
  T get() throws Exception;

  static <T> Optional<T> of(ThrowableSupplier<Optional<T>> supplier) {
    try {
      return supplier.get();
    } catch (Exception e) {
      return Optional.empty();
    }
  }
}
