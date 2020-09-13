package com.proximyst.ban.utils;

public final class ThrowableUtils {
  private ThrowableUtils() throws IllegalAccessException {
    throw new IllegalAccessException(getClass().getSimpleName() + " cannot be instantiated.");
  }

  /**
   * Sneaky throw the throwable.
   *
   * @param throwable The throwable to rethrow.
   */
  @SuppressWarnings("RedundantTypeArguments")
  public static void sneakyThrow(Throwable throwable) {
    throw ThrowableUtils.<RuntimeException>superSneaky(throwable);
  }

  @SuppressWarnings("unchecked")
  private static <T extends Throwable> T superSneaky(Throwable throwable) throws T {
    throw (T) throwable;
  }
}
