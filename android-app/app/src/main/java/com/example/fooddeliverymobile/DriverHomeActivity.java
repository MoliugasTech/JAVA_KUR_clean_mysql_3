package com.example.fooddeliverymobile;

public class DriverHomeActivity extends BaseHomeActivity {
    @Override
    protected MobileRole requiredRole() {
        return MobileRole.DRIVER;
    }

    @Override
    protected int titleResource() {
        return R.string.driver_space;
    }

    @Override
    protected int descriptionResource() {
        return R.string.driver_home_description;
    }
}
