package com.example.fooddeliverymobile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MenuItemCartAdapter extends BaseAdapter {
    public interface OnQuantityChanged {
        void onQuantityChanged();
    }

    private final Context context;
    private final List<MenuItemInfo> items;
    private final Map<Long, Integer> quantities = new LinkedHashMap<>();
    private final OnQuantityChanged listener;

    public MenuItemCartAdapter(Context context, List<MenuItemInfo> items,
                               OnQuantityChanged listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public MenuItemInfo getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View recycled, ViewGroup parent) {
        ViewHolder holder;
        if (recycled == null) {
            recycled = LayoutInflater.from(context).inflate(R.layout.item_menu_cart, parent, false);
            holder = new ViewHolder(recycled);
            recycled.setTag(holder);
        } else {
            holder = (ViewHolder) recycled.getTag();
        }

        MenuItemInfo item = getItem(position);
        int quantity = getQuantity(item.getId());
        holder.name.setText(item.getName());
        holder.description.setText(item.getDescription().isBlank()
                ? context.getString(R.string.no_description)
                : item.getDescription());
        holder.price.setText(context.getString(
                R.string.menu_item_price, item.getPrice().setScale(2, RoundingMode.HALF_UP)));
        holder.quantity.setText(String.valueOf(quantity));
        holder.minus.setEnabled(quantity > 0);
        holder.minus.setOnClickListener(view -> changeQuantity(item.getId(), -1));
        holder.plus.setOnClickListener(view -> changeQuantity(item.getId(), 1));
        return recycled;
    }

    public Map<Long, Integer> getQuantities() {
        return new LinkedHashMap<>(quantities);
    }

    public int getUnitCount() {
        return quantities.values().stream().mapToInt(Integer::intValue).sum();
    }

    public BigDecimal getTotal() {
        return items.stream()
                .map(item -> item.getPrice().multiply(
                        BigDecimal.valueOf(getQuantity(item.getId()))))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public void clearCart() {
        quantities.clear();
        notifyDataSetChanged();
        listener.onQuantityChanged();
    }

    private int getQuantity(long menuItemId) {
        return quantities.getOrDefault(menuItemId, 0);
    }

    private void changeQuantity(long menuItemId, int difference) {
        int updated = Math.max(0, getQuantity(menuItemId) + difference);
        if (updated == 0) {
            quantities.remove(menuItemId);
        } else {
            quantities.put(menuItemId, updated);
        }
        notifyDataSetChanged();
        listener.onQuantityChanged();
    }

    private static final class ViewHolder {
        private final TextView name;
        private final TextView description;
        private final TextView price;
        private final TextView quantity;
        private final Button minus;
        private final Button plus;

        private ViewHolder(View view) {
            name = view.findViewById(R.id.menuItemName);
            description = view.findViewById(R.id.menuItemDescription);
            price = view.findViewById(R.id.menuItemPrice);
            quantity = view.findViewById(R.id.menuItemQuantity);
            minus = view.findViewById(R.id.menuItemMinus);
            plus = view.findViewById(R.id.menuItemPlus);
        }
    }
}
