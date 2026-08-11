package com.example.demo.constant;

public final class CustomerConstants {
  public static final int ACTIVE_STATUS = 1;
  public static final int INACTIVE_STATUS = 0;
  public static final String NAME_PROPERTY = "name";
  public static final String ID_PROPERTY = "id";
  public static final int MAX_PAGE_SIZE = 100;
  public static final String CUSTOMER_CREATE = "hasAuthority('CUSTOMER_CREATE')";
  public static final String CUSTOMER_VIEW = "hasAuthority('CUSTOMER_VIEW')";
  public static final String CUSTOMER_UPDATE = "hasAuthority('CUSTOMER_UPDATE')";

  private CustomerConstants() {}
}
