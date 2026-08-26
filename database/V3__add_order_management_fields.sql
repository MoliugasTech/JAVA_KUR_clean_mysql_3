ALTER TABLE food_orders
    ADD COLUMN cancellation_reason VARCHAR(500) NULL AFTER status,
    ADD COLUMN driver_assignment_allowed BOOLEAN NOT NULL DEFAULT TRUE AFTER driver_id;
