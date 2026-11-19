package com.wall.timer25;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/** Popup de configuração (segurar o ícone > "Configurar"). */
public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        int pad = Math.round(20 * getResources().getDisplayMetrics().density);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(pad, pad / 2, pad, 0);

        TextView l1 = new TextView(this);
        l1.setText("Tempo (minutos)");
        box.addView(l1);

        NumberPicker minutes = new NumberPicker(this);
        minutes.setMinValue(1);
        minutes.setMaxValue(180);
        minutes.setValue(Timer.minutes(this));
        minutes.setWrapSelectorWheel(false);
        box.addView(minutes);

        TextView l2 = new TextView(this);
        l2.setText("App que abre ao parar o alarme");
        l2.setPadding(0, pad, 0, 0);
        box.addView(l2);

        // Lista de apps instalados que aparecem na gaveta
        PackageManager pm = getPackageManager();
        Intent launcher = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> found = pm.queryIntentActivities(launcher, 0);
        found.sort((a, c) -> a.loadLabel(pm).toString()
                .compareToIgnoreCase(c.loadLabel(pm).toString()));
        List<String> labels = new ArrayList<>();
        List<String> pkgs = new ArrayList<>();
        for (ResolveInfo r : found) {
            String p = r.activityInfo.packageName;
            if (p.equals(getPackageName()) || pkgs.contains(p)) continue;
            pkgs.add(p);
            labels.add(r.loadLabel(pm).toString());
        }

        Spinner app = new Spinner(this);
        app.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, labels));
        int sel = pkgs.indexOf(Timer.appPackage(this));
        if (sel >= 0) app.setSelection(sel);
        box.addView(app);

        new AlertDialog.Builder(this)
                .setTitle("Configurar timer")
                .setView(box)
                .setPositiveButton("Salvar", (d, w) -> {
                    minutes.clearFocus();
                    int i = app.getSelectedItemPosition();
                    String pkg = i >= 0 ? pkgs.get(i) : Timer.DEFAULT_APP;
                    Timer.saveSettings(this, minutes.getValue(), pkg);
                    Toast.makeText(getApplicationContext(), "Salvo: " + minutes.getValue()
                            + " min → " + (i >= 0 ? labels.get(i) : "WhatsApp Business"),
                            Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Cancelar", null)
                .setOnDismissListener(d -> finish())
                .show();
    }
}
