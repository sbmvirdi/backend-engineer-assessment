package com.midas.app.enums;

import java.util.Arrays;

public enum ProviderType {
  /** Enum to maintain payment providers */
  STRIPE("stripe");

  public final String value;

  ProviderType(String value) {
    this.value = value;
  }

  public static ProviderType getProviderType(String value) {
    return Arrays.stream(ProviderType.values())
        .filter(p -> p.value.equals(value))
        .findAny()
        .orElse(null);
  }
}
