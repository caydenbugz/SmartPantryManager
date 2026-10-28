package com.example.smartpantrymanager;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.text.InputType;
import android.view.*;
import android.widget.*;

public class MainActivity extends Activity {
    PantryDB db;
    LinearLayout layout;
    SharedPreferences prefs;
    String profile = "My Pantry";

    String[][] recipes = {
            {"Cheese Omelette","Eggs:2,Cheese:1,Salt:1"},
            {"Tomato Sandwich","Bread:2,Tomato:1"},
            {"Chicken and Rice","Chicken:1,Rice:1,Oil:1"},
            {"Tomato Pasta","Pasta:1,Tomato:2,Onion:1,Oil:1"},
            {"Banana Oats","Oats:1,Banana:1,Milk:1"},
            {"Mashed Potatoes","Potato:2,Butter:1,Milk:1"},
            {"Tuna Sandwich","Bread:2,Tuna:1"},
            {"Garlic Chicken","Chicken:1,Garlic:1,Oil:1"},
            {"Vegetable Rice","Rice:1,Carrot:1,Onion:1,Oil:1"},
            {"Spinach Omelette","Eggs:2,Spinach:1,Salt:1"},
            {"Pancakes","Flour:1,Eggs:1,Milk:1,Sugar:1"},
            {"Cheese Toast","Bread:2,Cheese:1,Butter:1"},
            {"Bean Rice Bowl","Beans:1,Rice:1,Onion:1"},
            {"Lemon Chicken","Chicken:1,Lemon:1,Oil:1"},
            {"Carrot Salad","Carrot:1,Lemon:1,Salt:1"},
            {"Banana Pancakes","Banana:1,Flour:1,Eggs:1"},
            {"Butter Pasta","Pasta:1,Butter:1,Salt:1"},
            {"Chicken Sandwich","Chicken:1,Bread:2"},
            {"Spinach Rice","Rice:1,Spinach:1,Garlic:1"},
            {"Tuna Rice Bowl","Tuna:1,Rice:1,Lemon:1"}
    };

    public void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("profile", MODE_PRIVATE);
        profile = prefs.getString("name", "My Pantry");
        db = new PantryDB(this);
        home();
    }

    void start(String title) {
        ScrollView s = new ScrollView(this);
        layout = new LinearLayout(this);
        layout.setOrientation(1);
        layout.setPadding(16, 16, 16, 16);
        s.addView(layout);
        setContentView(s);
        add(title, 24);
        String[] nav = {"Home","Pantry","Recipes","Profile"};
        Runnable[] go = {this::home,this::pantry,this::recipes,this::profilePage};
        LinearLayout bar = new LinearLayout(this);
        for (int i=0;i<nav.length;i++) {
            Button x = new Button(this);
            x.setText(nav[i]);
            final int n=i;
            x.setOnClickListener(v -> go[n].run());
            bar.addView(x,new LinearLayout.LayoutParams(0,-2,1));
        }
        layout.addView(bar);
    }

    void add(String t,int size) {
        TextView x = new TextView(this);
        x.setText(t);
        x.setTextSize(size);
        x.setPadding(8,10,8,10);
        layout.addView(x);
    }

    void btn(String t,Runnable r) {
        Button b = new Button(this);
        b.setText(t);
        b.setOnClickListener(v -> r.run());
        layout.addView(b);
    }

    void home() {
        start("Smart Pantry Manager");
        add("Welcome, "+profile,20);
        add("Pantry items: "+db.count(),16);
        btn("Manage Pantry",this::pantry);
        btn("Find Recipes",this::recipes);
    }

    void pantry() {
        start("My Pantry");
        btn("Add Ingredient",()->edit(null,0,"item",""));
        if(db.count()==0) add("Your pantry is empty. Add an ingredient to get started.",16);
        Cursor c=db.items();
        while(c.moveToNext()) {
            String n=c.getString(0),u=c.getString(2),e=c.getString(3);
            double q=c.getDouble(1);
            String label=n+" - "+q+" "+u+(e==null?"":" | Expiry: "+e);
            Button b=new Button(this);
            b.setText(label);
            b.setOnClickListener(v->edit(n,q,u,e));
            b.setOnLongClickListener(v->{
                new AlertDialog.Builder(this).setMessage("Delete "+n+"?")
                        .setNegativeButton("Cancel",null)
                        .setPositiveButton("Delete",(d,w)->{db.delete(n);pantry();})
                        .show();
                return true;
            });
            layout.addView(b);
        }
        c.close();
    }

    void edit(String old,double qty,String unit,String expiry) {
        LinearLayout f=new LinearLayout(this);
        f.setOrientation(1);
        EditText n=field("Ingredient name",old);
        EditText q=field("Quantity",old==null?"":""+qty);
        q.setInputType(8194);
        EditText u=field("Unit",unit);
        EditText e=field("Expiry YYYY-MM-DD",expiry);
        f.addView(n);f.addView(q);f.addView(u);f.addView(e);
        new AlertDialog.Builder(this).setTitle("Pantry Item").setView(f)
                .setNegativeButton("Cancel",null)
                .setPositiveButton("Save",(d,w)->{
                    try {
                        String name=n.getText().toString().trim();
                        String un=u.getText().toString().trim();
                        String ex=e.getText().toString().trim();
                        double amount=Double.parseDouble(q.getText().toString());
                        if(name.isEmpty()||un.isEmpty()||amount<0||
                                (!ex.isEmpty()&&!ex.matches("\\d{4}-\\d{2}-\\d{2}")))
                            throw new Exception();
                        db.save(name,amount,un,ex,old);
                        pantry();
                    } catch(Exception x) {
                        Toast.makeText(this,"Check the name, quantity, unit and date.",0).show();
                    }
                }).show();
    }

    EditText field(String hint,String value) {
        EditText e=new EditText(this);
        e.setHint(hint);
        e.setText(value==null?"":value);
        return e;
    }

    boolean available(String list) {
        for(String item:list.split(",")) {
            String[] a=item.split(":");
            if(db.quantity(a[0])<Double.parseDouble(a[1])) return false;
        }
        return true;
    }

    void recipes() {
        start("Available Recipes");
        int count=0;
        add("Recipes you can make from your pantry:",16);
        for(String[] r:recipes) if(available(r[1])) {
            count++;
            btn(r[0],()->details(r));
        }
        if(count==0) {
            add("No matching recipes. Add ingredients or increase quantities.",16);
            btn("Open Pantry",this::pantry);
        }
    }

    void details(String[] r) {
        start(r[0]);
        add("Required ingredients:",18);
        for(String item:r[1].split(",")) {
            String[] a=item.split(":");
            add(a[0]+": "+a[1],16);
        }
        add("Preparation Instructions",18);
        add("Prepare the ingredients, cook or combine them appropriately, and check that the food is fully cooked before serving.",16);
        btn("Back to Recipes",this::recipes);
    }

    void profilePage() {
        start("Profile and Settings");
        EditText n=field("Your name",profile);
        layout.addView(n);
        btn("Save Profile",()->{
            String name=n.getText().toString().trim();
            if(name.isEmpty()) {
                Toast.makeText(this,"Enter a name",0).show();
                return;
            }
            profile=name;
            prefs.edit().putString("name",profile).apply();
            Toast.makeText(this,"Profile saved",0).show();
            profilePage();
        });
    }

    public void onDestroy() {
        if(db!=null) db.close();
        super.onDestroy();
    }

    static class PantryDB extends SQLiteOpenHelper {
        PantryDB(Context c) { super(c,"pantry.db",null,2); }

        public void onCreate(SQLiteDatabase d) {
            d.execSQL("CREATE TABLE pantry(name TEXT PRIMARY KEY COLLATE NOCASE,quantity REAL NOT NULL,unit TEXT NOT NULL DEFAULT 'item',expiry TEXT)");
        }

        public void onUpgrade(SQLiteDatabase d,int old,int now) {
            if(old<2) {
                d.execSQL("ALTER TABLE pantry ADD COLUMN unit TEXT NOT NULL DEFAULT 'item'");
                d.execSQL("ALTER TABLE pantry ADD COLUMN expiry TEXT");
            }
        }

        void save(String n,double q,String u,String e,String old) {
            SQLiteDatabase d=getWritableDatabase();
            if(old!=null&&!old.equalsIgnoreCase(n))
                d.delete("pantry","name=?",new String[]{old});
            ContentValues v=new ContentValues();
            v.put("name",n);v.put("quantity",q);v.put("unit",u);
            if(e.isEmpty())v.putNull("expiry");else v.put("expiry",e);
            d.insertWithOnConflict("pantry",null,v,5);
        }

        Cursor items() {
            return getReadableDatabase().rawQuery("SELECT name,quantity,unit,expiry FROM pantry ORDER BY name",null);
        }

        double quantity(String n) {
            Cursor c=getReadableDatabase().rawQuery("SELECT quantity FROM pantry WHERE name=? COLLATE NOCASE",new String[]{n});
            double q=c.moveToFirst()?c.getDouble(0):0;
            c.close();
            return q;
        }

        void delete(String n) {
            getWritableDatabase().delete("pantry","name=?",new String[]{n});
        }

        int count() {
            Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM pantry",null);
            c.moveToFirst();int n=c.getInt(0);c.close();return n;
        }
    }
}