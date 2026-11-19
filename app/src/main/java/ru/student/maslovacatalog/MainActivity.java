package ru.student.maslovacatalog;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {
    private CatalogDatabase store;
    private SQLiteDatabase connection;
    private LinearLayout categoryMenu;
    private LinearLayout goodsContainer;
    private TextView heading;
    private final ArrayList<Button> menuButtons = new ArrayList<>();
    private long currentCategory = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View root = findViewById(R.id.root);
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets.consumeSystemWindowInsets();
        });
        categoryMenu = findViewById(R.id.categories);
        goodsContainer = findViewById(R.id.products);
        heading = findViewById(R.id.section_title);
        store = new CatalogDatabase(this);
        try {
            store.prepareDatabase();
            connection = store.getReadableDatabase();
            if (savedInstanceState != null) {
                currentCategory = savedInstanceState.getLong("category", -1);
            }
            buildCategoryMenu();
        } catch (IOException error) {
            heading.setText("Не удалось открыть каталог");
        }
    }

    private void buildCategoryMenu() {
        try (Cursor rows = connection.rawQuery("SELECT _id, name FROM categories ORDER BY _id", null)) {
            while (rows.moveToNext()) {
                long id = rows.getLong(0);
                String name = rows.getString(1);
                Button button = new Button(this);
                button.setText(name);
                button.setTextSize(15);
                button.setAllCaps(false);
                button.setMinHeight(dp(48));
                button.setPadding(dp(20), 0, dp(20), 0);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp(52));
                params.bottomMargin = dp(8);
                categoryMenu.addView(button, params);
                menuButtons.add(button);
                button.setTag(id);
                button.setOnClickListener(view -> showCategory(id, name));
                if (currentCategory == -1) {
                    currentCategory = id;
                }
            }
        }
        for (Button button : menuButtons) {
            if ((long) button.getTag() == currentCategory) {
                showCategory(currentCategory, button.getText().toString());
                return;
            }
        }
        if (!menuButtons.isEmpty()) {
            Button first = menuButtons.get(0);
            showCategory((long) first.getTag(), first.getText().toString());
        } else {
            heading.setText("Категорий пока нет");
        }
    }

    private void showCategory(long id, String name) {
        currentCategory = id;
        heading.setText(name);
        for (Button button : menuButtons) {
            boolean selected = (long) button.getTag() == id;
            button.setBackgroundResource(selected ? R.drawable.button_active : R.drawable.button_idle);
            button.setTextColor(Color.parseColor(selected ? "#FFFFFF" : "#593A55"));
        }
        goodsContainer.removeAllViews();
        try (Cursor rows = connection.rawQuery(
                "SELECT name, description, price FROM products WHERE category_id = ? ORDER BY _id",
                new String[]{String.valueOf(id)})) {
            while (rows.moveToNext()) {
                displayItem(rows.getString(0), rows.getString(1), rows.getInt(2));
            }
        }
        if (goodsContainer.getChildCount() == 0) {
            TextView empty = new TextView(this);
            empty.setText("В этой категории пока нет товаров");
            empty.setTextColor(Color.parseColor("#765F73"));
            goodsContainer.addView(empty);
        }
    }

    private void displayItem(String name, String description, int price) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setPadding(dp(18), dp(16), dp(18), dp(16));
        item.setBackgroundResource(R.drawable.product_bg);
        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        itemParams.topMargin = dp(10);
        goodsContainer.addView(item, itemParams);

        TextView title = new TextView(this);
        title.setText(name);
        title.setTextColor(Color.parseColor("#432744"));
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        item.addView(title);

        TextView details = new TextView(this);
        details.setText(description);
        details.setTextColor(Color.parseColor("#765F73"));
        details.setTextSize(14);
        LinearLayout.LayoutParams detailParams = new LinearLayout.LayoutParams(-1, -2);
        detailParams.topMargin = dp(5);
        item.addView(details, detailParams);

        TextView cost = new TextView(this);
        cost.setText(String.format(new Locale("ru", "RU"), "%d ₽", price));
        cost.setTextColor(Color.parseColor("#854C76"));
        cost.setTextSize(16);
        cost.setTypeface(null, android.graphics.Typeface.BOLD);
        cost.setGravity(Gravity.END);
        LinearLayout.LayoutParams costParams = new LinearLayout.LayoutParams(-1, -2);
        costParams.topMargin = dp(8);
        item.addView(cost, costParams);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        state.putLong("category", currentCategory);
        super.onSaveInstanceState(state);
    }

    @Override
    protected void onDestroy() {
        if (store != null) {
            store.close();
        }
        super.onDestroy();
    }
}
