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
  public static void sneakyThrow(Throwable throwable) {
    //noinspection RedundantTypeArguments
    throw ThrowableUtils.<RuntimeException>superSneaky(throwable);
  }

  private static <T extends Throwable> T superSneaky(Throwable throwable) throws T {
    //noinspection unchecked
    throw (T) throwable;
  }
}
