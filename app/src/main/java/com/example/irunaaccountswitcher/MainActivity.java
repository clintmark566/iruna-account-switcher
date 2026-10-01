package com.example.irunaaccountswitcher;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final int REQUEST_ACCOUNT = 1001;

    private static final String IRUNA_PACKAGE = "com.asobimo.iruna_en";
    private static final String DEFAULT_EMAIL = "clintmark566@gmail.com";

    private android.content.SharedPreferences prefs;
    private LinearLayout slots;
    private TextView selectedAccountText;

    private final String[] defaultNames = {
            "Main",
            "Mage",
            "Tank",
            "Farm",
            "Alt 1",
            "Alt 2",
            "Alt 3",
            "Alt 4"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("iruna_switcher", MODE_PRIVATE);

        buildUI();
    }

    private void buildUI() {

        ScrollView scroll = new ScrollView(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 24, 24, 24);

        TextView title = new TextView(this);
        title.setText("IRUNA 1-TAP ACCOUNT SWITCHER");
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        selectedAccountText = new TextView(this);
        selectedAccountText.setText(
                "Akun utama:\n" + DEFAULT_EMAIL +
                "\n\nPassword Google tidak disimpan oleh aplikasi."
        );
        selectedAccountText.setGravity(Gravity.CENTER);
        selectedAccountText.setPadding(0, 16, 0, 20);
        root.addView(selectedAccountText);

        Button choose = new Button(this);
        choose.setText("👤 PILIH AKUN GOOGLE");
        choose.setOnClickListener(v -> chooseGoogleAccount());
        root.addView(choose);

        Button settings = new Button(this);
        settings.setText("⚙ KELOLA AKUN GOOGLE");
        settings.setOnClickListener(v -> {
            try {
                startActivity(
                        new Intent(Settings.ACTION_SYNC_SETTINGS)
                );
            } catch (Exception e) {
                startActivity(
                        new Intent(Settings.ACTION_SETTINGS)
                );
            }
        });
        root.addView(settings);

        Button delay = new Button(this);
        delay.setText("⏱ ATUR JEDA BUKA IRUNA");
        delay.setOnClickListener(v -> chooseDelay());
        root.addView(delay);

        slots = new LinearLayout(this);
        slots.setOrientation(LinearLayout.VERTICAL);
        slots.setPadding(0, 20, 0, 0);
        root.addView(slots);

        TextView info = new TextView(this);
        info.setText(
                "Cara kerja:\n\n" +
                "1. Pilih akun Google yang sudah ada di HP.\n" +
                "2. Setelah memilih akun, aplikasi membuka Iruna.\n" +
                "3. Login/sesi akun tetap mengikuti mekanisme resmi Iruna.\n\n" +
                "Aplikasi tidak menyimpan password Google."
        );
        info.setPadding(0, 20, 0, 20);
        root.addView(info);

        scroll.addView(root);
        setContentView(scroll);

        renderSavedAccount();
    }

    private void renderSavedAccount() {

        String email = prefs.getString(
                "selected_account",
                DEFAULT_EMAIL
        );

        selectedAccountText.setText(
                "Akun terpilih:\n" +
                email +
                "\n\nPassword Google tidak disimpan oleh aplikasi."
        );
    }

    private void chooseGoogleAccount() {

        try {

            Intent intent =
                    AccountManager
                            .get(this)
                            .newChooseAccountIntent(
                                    null,
                                    null,
                                    new String[]{"com.google"},
                                    false,
                                    "Pilih akun Google untuk Iruna",
                                    null,
                                    null,
                                    null
                            );

            startActivityForResult(
                    intent,
                    REQUEST_ACCOUNT
            );

        } catch (Exception e) {

            // Fallback ke pengaturan akun Android.
            try {
                startActivity(
                        new Intent(Settings.ACTION_SYNC_SETTINGS)
                );
            } catch (Exception ignored) {
                startActivity(
                        new Intent(Settings.ACTION_SETTINGS)
                );
            }
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode != REQUEST_ACCOUNT)
            return;

        if (resultCode != RESULT_OK || data == null)
            return;

        String email =
                data.getStringExtra(
                        AccountManager.KEY_ACCOUNT_NAME
                );

        if (email == null || email.trim().isEmpty())
            return;

        prefs.edit()
                .putString(
                        "selected_account",
                        email
                )
                .apply();

        renderSavedAccount();

        openIrunaAfterDelay();
    }

    private void openIrunaAfterDelay() {

        long delay =
                prefs.getLong(
                        "delay",
                        3000
                );

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

            try {

                startActivity(
                        new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(
                                        "https://play.google.com/store/apps/details?id="
                                                + IRUNA_PACKAGE
                                )
                        )
                );

            } catch (Exception ignored) {
            }
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
                prefs.getLong(
                        "delay",
                        3000
                );

        int checked = 1;

        for (int i = 0; i < values.length; i++) {

            if (values[i] == current) {
                checked = i;
                break;
            }
        }

        new android.app.AlertDialog.Builder(this)
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
                .setNegativeButton(
                        "Batal",
                        null
                )
                .show();
    }

    private void renderSlots() {
        // Slot tambahan dapat dikembangkan pada versi berikutnya.
    }
}
