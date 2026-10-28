
package com.example.smartpantrymanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    PantryDB db;
    LinearLayout page;
    String profileName = "My Pantry";

    static class Recipe {
        String name, description;
        String[] ingredients;
        double[] amounts;

        Recipe(String n, String d, String[] i, double[] a) {
            name = n;
            description = d;
            ingredients = i;
            amounts = a;
        }
    }

    final Recipe[] recipes = {
            new Recipe("Cheese Omelette", "A quick cheesy egg breakfast.",
                    new String[]{"Eggs","Cheese","Salt"}, new double[]{2,1,1}),
            new Recipe("Tomato Sandwich", "A simple fresh sandwich.",
                    new String[]{"Bread","Tomato"}, new double[]{2,1}),
            new Recipe("Chicken and Rice", "Chicken served with rice.",
                    new String[]{"Chicken","Rice","Oil"}, new double[]{1,1,1}),
            new Recipe("Tomato Pasta", "Pasta with a tomato sauce.",
                    new String[]{"Pasta","Tomato","Onion","Oil"}, new double[]{1,2,1,1}),
            new Recipe("Banana Oats", "A simple breakfast bowl.",
                    new String[]{"Oats","Banana","Milk"}, new double[]{1,1,1}),
            new Recipe("Mashed Potatoes", "Soft mashed potatoes.",
                    new String[]{"Potato","Butter","Milk"}, new double[]{2,1,1}),
            new Recipe("Tuna Sandwich", "A quick tuna sandwich.",
                    new String[]{"Bread","Tuna"}, new double[]{2,1}),
            new Recipe("Garlic Chicken", "Garlic-flavoured chicken.",
                    new String[]{"Chicken","Garlic","Oil"}, new double[]{1,1,1}),
            new Recipe("Vegetable Rice", "Rice with vegetables.",
                    new String[]{"Rice","Carrot","Onion","Oil"}, new double[]{1,1,1,1}),
            new Recipe("Spinach Omelette", "Eggs with spinach.",
                    new String[]{"Eggs","Spinach","Salt"}, new double[]{2,1,1}),
            new Recipe("Pancakes", "Basic homemade pancakes.",
                    new String[]{"Flour","Eggs","Milk","Sugar"}, new double[]{1,1,1,1}),
            new Recipe("Cheese Toast", "Toasted bread with cheese.",
                    new String[]{"Bread","Cheese","Butter"}, new double[]{2,1,1}),
            new Recipe("Bean Rice Bowl", "Beans served over rice.",
                    new String[]{"Beans","Rice","Onion"}, new double[]{1,1,1}),
            new Recipe("Lemon Chicken", "Chicken with lemon flavour.",
                    new String[]{"Chicken","Lemon","Oil"}, new double[]{1,1,1}),
            new Recipe("Carrot Salad", "A simple carrot salad.",
                    new String[]{"Carrot","Lemon","Salt"}, new double[]{1,1,1}),
            new Recipe("Banana Pancakes", "Sweet banana pancakes.",
                    new String[]{"Banana","Flour","Eggs"}, new double[]{1,1,1}),
            new Recipe("Butter Pasta", "Pasta tossed with butter.",
                    new String[]{"Pasta","Butter","Salt"}, new double[]{1,1,1}),
            new Recipe("Chicken Sandwich", "Chicken between slices of bread.",
                    new String[]{"Chicken","Bread"}, new double[]{1,2}),
            new Recipe("Spinach Rice", "Rice with spinach.",
                    new String[]{"Rice","Spinach","Garlic"}, new double[]{1,1,1}),
            new Recipe("Tuna Rice Bowl", "A quick tuna and rice meal.",
                    new String[]{"Tuna","Rice","Lemon"}, new double[]{1,1,1})
    };

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        db = new PantryDB(this);
        showHome();
    }

    TextView text(String value, int size) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setPadding(12, 12, 12, 12);
        return t;
    }

    Button button(String label, Runnable action) {
        Button b = new Button(this);
        b.setText(label);
        b.setOnClickListener(v -> action.run());
        return b;
    }

    void startPage(String title) {
        ScrollView scroll = new ScrollView(this);
        page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(16, 12, 16, 20);

        TextView heading = text(title, 25);
        heading.setTypeface(null, Typeface.BOLD);
        page.addView(heading);

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        String[] labels = {"Home", "Pantry", "Recipes", "Profile"};
        Runnable[] actions = {
                this::showHome, this::showPantry,
                this::showRecipes, this::showProfile
        };

        for (int i = 0; i < labels.length; i++) {
            Button b = new Button(this);
            b.setText(labels[i]);
            b.setTextSize(10);
            final int index = i;
            b.setOnClickListener(v -> actions[index].run());
            nav.addView(b, new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        }

        page.addView(nav);
        scroll.addView(page);
        setContentView(scroll);
    }

    void showHome() {
        startPage("Smart Pantry Manager");
        page.addView(text("Welcome, " + profileName + "!", 20));
        page.addView(text("Manage your food, track quantities and discover recipes using ingredients you have.", 16));
        page.addView(button("Manage Pantry", this::showPantry));
        page.addView(button("Find Available Recipes", this::showRecipes));
        page.addView(text("Recipes are suggested only when every required ingredient is available in the required quantity.", 14));
        page.addView(text("Total pantry items: " + db.countItems(), 16));
    }

    void showPantry() {
        startPage("My Pantry");
        page.addView(text("Tap an item to edit its quantity. Long-press to delete it.", 14));
        page.addView(button("+ Add Pantry Item", () -> itemDialog(null, 0)));

        Cursor c = db.getItems();
        if (!c.moveToFirst()) {
            page.addView(text("Your pantry is empty. Add an item to get started.", 16));
        } else {
            do {
                String name = c.getString(0);
                double quantity = c.getDouble(1);
                Button item = button(name + " — " + quantity,
                        () -> itemDialog(name, quantity));
                item.setOnLongClickListener(v -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Delete " + name + "?")
                            .setMessage("Remove this item from your pantry?")
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Delete", (d, w) -> {
                                db.deleteItem(name);
                                showPantry();
                            }).show();
                    return true;
                });
                page.addView(item);
            } while (c.moveToNext());
        }
        c.close();
    }

    void itemDialog(String oldName, double oldQuantity) {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(20, 5, 20, 5);

        EditText name = new EditText(this);
        name.setHint("Ingredient name (e.g. Eggs)");
        name.setSingleLine(true);
        if (oldName != null) name.setText(oldName);
        form.addView(name);

        EditText quantity = new EditText(this);
        quantity.setHint("Quantity available");
        quantity.setInputType(8194);
        if (oldName != null) quantity.setText(String.valueOf(oldQuantity));
        form.addView(quantity);

        new AlertDialog.Builder(this)
                .setTitle(oldName == null ? "Add Ingredient" : "Edit Ingredient")
                .setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> {
                    String n = name.getText().toString().trim();
                    try {
                        double q = Double.parseDouble(quantity.getText().toString().trim());
                        if (n.isEmpty() || q < 0 || !Double.isFinite(q)) {
                            Toast.makeText(this, "Enter a name and a valid quantity.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        db.saveItem(n, q, oldName);
                        showPantry();
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Enter a valid quantity.", Toast.LENGTH_LONG).show();
                    }
                }).show();
    }

    boolean canMake(Recipe r) {
        for (int i = 0; i < r.ingredients.length; i++) {
            if (db.getQuantity(r.ingredients[i]) < r.amounts[i]) return false;
        }
        return true;
    }

    void showRecipes() {
        startPage("Available Recipes");
        page.addView(text("Only recipes with every required ingredient in sufficient quantity are shown.", 14));
        int count = 0;
        for (Recipe r : recipes) {
            if (canMake(r)) {
                count++;
                page.addView(button(r.name, () -> showRecipeDetails(r)));
            }
        }
        if (count == 0) {
            page.addView(text("No matching recipes yet. Add more ingredients or increase their quantities in your pantry.", 17));
        } else {
            page.addView(text(count + " matching recipes found.", 14));
        }
    }

    void showRecipeDetails(Recipe r) {
        startPage(r.name);
        page.addView(text(r.description, 17));
        page.addView(text("Required ingredients:", 18));
        for (int i = 0; i < r.ingredients.length; i++) {
            page.addView(text("• " + r.ingredients[i] + ": " + r.amounts[i], 16));
        }
        page.addView(text("Preparation: Gather the listed ingredients, prepare them safely, cook or combine as appropriate, and check that the food is fully cooked before serving.", 16));
        page.addView(button("Back to Recipes", this::showRecipes));
    }

    void showProfile() {
        startPage("Profile and Settings");
        EditText name = new EditText(this);
        name.setHint("Your name");
        name.setSingleLine(true);
        name.setText(profileName);
        page.addView(text("Profile name", 16));
        page.addView(name);
        page.addView(button("Save Profile", () -> {
            String n = name.getText().toString().trim();
            if (!n.isEmpty()) {
                profileName = n;
                Toast.makeText(this, "Profile saved.", Toast.LENGTH_SHORT).show();
                showProfile();
            }
        }));
        page.addView(text("Storage: Local SQLite database", 14));
        page.addView(text("Recipe matching requires every listed ingredient and quantity to be available.", 14));
    }

    static class PantryDB extends SQLiteOpenHelper {
        PantryDB(Context context) {
            super(context, "smart_pantry.db", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE pantry (name TEXT PRIMARY KEY COLLATE NOCASE, quantity REAL NOT NULL)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }

        void saveItem(String name, double quantity, String oldName) {
            SQLiteDatabase d = getWritableDatabase();
            if (oldName != null && !oldName.equalsIgnoreCase(name)) {
                d.delete("pantry", "name=?", new String[]{oldName});
            }
            ContentValues values = new ContentValues();
            values.put("name", name);
            values.put("quantity", quantity);
            d.insertWithOnConflict("pantry", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }

        Cursor getItems() {
            return getReadableDatabase().rawQuery(
                    "SELECT name, quantity FROM pantry ORDER BY name", null);
        }

        double getQuantity(String name) {
            Cursor c = getReadableDatabase().rawQuery(
                    "SELECT quantity FROM pantry WHERE name=? COLLATE NOCASE",
                    new String[]{name});
            double q = 0;
            if (c.moveToFirst()) q = c.getDouble(0);
            c.close();
            return q;
        }

        void deleteItem(String name) {
            getWritableDatabase().delete("pantry", "name=?",
                    new String[]{name});
        }

        int countItems() {
            Cursor c = getReadableDatabase().rawQuery(
                    "SELECT COUNT(*) FROM pantry", null);
            int count = 0;
            if (c.moveToFirst()) count = c.getInt(0);
            c.close();
            return count;
        }
    }
}
