package com.example.irunaaccountswitcher;

import android.app.*;
import android.os.*;
import android.content.*;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import java.net.URLEncoder;

public class MainActivity extends Activity {

    private final String SLOT1_EMAIL = "clintmark566@gmail.com";
    private final String IRUNA_PACKAGE = "com.asobimo.iruna_en";

    private LinearLayout slots;
    private android.content.SharedPreferences prefs;

    private final String[] defaults = {
        "Main", "Mage", "Tank", "Farm",
        "Alt 1", "Alt 2", "Alt 3", "Alt 4"
    };

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        prefs = getSharedPreferences("slots", 0);
        buildUI();
    }

    private void buildUI() {

        ScrollView scroll = new ScrollView(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24,24,24,24);

        TextView title = new TextView(this);
        title.setText("IRUNA 1-TAP ACCOUNT SWITCHER");
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView info = new TextView(this);
        info.setText(
            "Slot 1: " + SLOT1_EMAIL +
            "\\n\\nPassword Google tidak disimpan oleh aplikasi."
        );
        info.setGravity(Gravity.CENTER);
        info.setPadding(0,10,0,20);
        root.addView(info);

        Button accounts = new Button(this);
        accounts.setText("⚙ Kelola akun Google");
        accounts.setOnClickListener(v ->
            startActivity(
                new Intent(android.provider.Settings.ACTION_SYNC_SETTINGS)
            )
        );
        root.addView(accounts);

        Button delay = new Button(this);
        delay.setText("⏱ Atur jeda buka Iruna");
        delay.setOnClickListener(v -> chooseDelay());
        root.addView(delay);

        slots = new LinearLayout(this);
        slots.setOrientation(LinearLayout.VERTICAL);
        root.addView(slots);

        scroll.addView(root);
        setContentView(scroll);

        renderSlots();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (slots != null)
            renderSlots();
    }

    private void renderSlots() {

        slots.removeAllViews();

        android.accounts.Account[] accounts =
            android.accounts.AccountManager
                .get(this)
                .getAccountsByType("com.google");

        android.accounts.Account main = null;

        for (android.accounts.Account a : accounts) {

            if (a.name.equalsIgnoreCase(SLOT1_EMAIL)) {
                main = a;
                break;
            }
        }

        if (main == null) {

            TextView warning = new TextView(this);

            warning.setText(
                "Slot 1 belum tersedia.\\n\\n" +
                "Tambahkan akun:\\n" +
                SLOT1_EMAIL +
                "\\n\\nmelalui Pengaturan Android."
            );

            warning.setTextSize(16);
            warning.setPadding(0,20,0,20);

            slots.addView(warning);

        } else {

            addSlot(0, main);
        }

        int index = 1;

        for (android.accounts.Account a : accounts) {

            if (a.name.equalsIgnoreCase(SLOT1_EMAIL))
                continue;

            if (index >= 8)
                break;

            addSlot(index, a);
            index++;
        }
    }

    private void addSlot(int index, android.accounts.Account account) {

        String name =
            prefs.getString(
                "name_" + account.name,
                defaults[index]
            );

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0,10,0,10);

        TextView label = new TextView(this);

        label.setText(
            "Slot " + (index + 1) +
            ": " + name +
            "\\n" + account.name
        );

        label.setTextSize(16);

        row.addView(label);

        Button open = new Button(this);

        open.setText(
            "▶ BUKA SLOT " +
            (index + 1)
        );

        open.setOnClickListener(
            v -> openAccountThenIruna(account)
        );

        row.addView(open);

        Button rename = new Button(this);

        rename.setText("✏ Ubah nama");

        rename.setOnClickListener(
            v -> renameSlot(account, name)
        );

        row.addView(rename);

        slots.addView(row);
    }

    private void openAccountThenIruna(
        android.accounts.Account account
    ) {

        try {

            String email =
                URLEncoder.encode(
                    account.name,
                    "UTF-8"
                );

            Intent chooser =
                new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        "https://accounts.google.com/AccountChooser?Email="
                        + email
                    )
                );

            startActivity(chooser);

        } catch (Exception ignored) {}

        long delay =
            prefs.getLong("delay", 3000);

        new Handler(Looper.getMainLooper())
            .postDelayed(
                this::openIruna,
                delay
            );
    }

    private void openIruna() {

        Intent launch =
            getPackageManager()
                .getLaunchIntentForPackage(
                    IRUNA_PACKAGE
                );

        if (launch != null) {

            startActivity(launch);

        } else {

            startActivity(
                new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        "https://play.google.com/store/apps/details?id="
                        + IRUNA_PACKAGE
                    )
                )
            );
        }
    }

    private void chooseDelay() {

        String[] labels = {
            "2 detik",
            "3 detik",
            "5 detik",
            "8 detik",
            "10 detik"
        };

        long[] values = {
            2000,
            3000,
            5000,
            8000,
            10000
        };

        long current =
            prefs.getLong("delay", 3000);

        int checked = 1;

        for (int i=0; i<values.length; i++) {

            if (values[i] == current)
                checked = i;
        }

        new AlertDialog.Builder(this)
            .setTitle("Jeda membuka Iruna")
            .setSingleChoiceItems(
                labels,
                checked,
                (dialog, which) -> {

                    prefs.edit()
                        .putLong(
                            "delay",
                            values[which]
                        )
                        .apply();

                    dialog.dismiss();
                }
            )
            .setNegativeButton("Batal", null)
            .show();
    }

    private void renameSlot(
        android.accounts.Account account,
        String oldName
    ) {

        EditText input = new EditText(this);
        input.setText(oldName);

        new AlertDialog.Builder(this)
            .setTitle("Nama slot")
            .setView(input)
            .setNegativeButton("Batal", null)
            .setPositiveButton(
                "Simpan",
                (dialog, which) -> {

                    prefs.edit()
                        .putString(
                            "name_" + account.name,
                            input.getText().toString()
                        )
                        .apply();

                    renderSlots();
                }
            )
            .show();
    }
}
