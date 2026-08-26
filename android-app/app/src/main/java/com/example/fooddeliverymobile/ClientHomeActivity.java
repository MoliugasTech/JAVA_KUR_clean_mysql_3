package com.example.fooddeliverymobile;

public class ClientHomeActivity extends BaseHomeActivity {
    @Override
    protected MobileRole requiredRole() {
        return MobileRole.CLIENT;
    }

    @Override
    protected int titleResource() {
        return R.string.client_space;
    }

    @Override
    protected int descriptionResource() {
        return R.string.client_home_description;
    }
}
