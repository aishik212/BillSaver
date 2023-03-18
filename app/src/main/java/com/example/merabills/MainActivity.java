package com.example.merabills;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    AlertDialog alertDialog;
    TextView totalAmountTextView;
    ChipGroup chipGroup;
    Button addPaymentBtn;
    Button saveBtn;

    AlertDialog.Builder adb;

    EditText amountEt;

    HashMap<String, DataModel> costmap = new HashMap<>();
    Spinner spinner;

    ArrayList<String> arraySpinner;

    ArrayAdapter<String> adapter;
    EditText bankNameEt;
    EditText referenceEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        showData();
    }

    private void showData() {
        File file = new File(getFilesDir() + "/LastPayment.txt");
        if (file.exists()) {
            getOldData();
        } else {
            showEmptyData();
        }
    }

    private void initViews() {
        String[] stringArray = getResources().getStringArray(R.array.paymentType);
        totalAmountTextView = findViewById(R.id.total_amt_tv);
        chipGroup = findViewById(R.id.payment_chip_group);
        addPaymentBtn = findViewById(R.id.add_payment_btn);
        saveBtn = findViewById(R.id.save_btn);
        adb = new AlertDialog.Builder(this);
        View inflate = getLayoutInflater().inflate(R.layout.add_payment_layout, null);
        View cancelBtn = inflate.findViewById(R.id.cancelBtn);
        View acceptBtn = inflate.findViewById(R.id.acceptBtn);
        amountEt = inflate.findViewById(R.id.amountEt);
        bankNameEt = inflate.findViewById(R.id.bankNameEt);
        referenceEt = inflate.findViewById(R.id.referenceEt);
        spinner = inflate.findViewById(R.id.spinner);
        arraySpinner = new ArrayList<>(Arrays.asList(stringArray));
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arraySpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateSpinnerData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        cancelBtn.setOnClickListener(v -> alertDialog.dismiss());
        acceptBtn.setOnClickListener(v -> {
            if (amountEt.getText() != null && !amountEt.getText().toString().trim().isEmpty()) {
                int amt = Integer.parseInt(amountEt.getText().toString());
                String type = spinner.getSelectedItem().toString();
                Log.d("texts", "onCreate: " + amt + " " + type);
                costmap.put(type, new DataModel(amt, bankNameEt.getText().toString(), referenceEt.getText().toString()));
                amountEt.setText("0");
                bankNameEt.setText("");
                referenceEt.setText("");
                alertDialog.dismiss();
                updateData();
            } else {
                Toast.makeText(this, "Empty Amount Value", Toast.LENGTH_SHORT).show();
            }
        });
        addPaymentBtn.setOnClickListener(v -> {
            updateSpinner();
            alertDialog.show();
        });
        saveBtn.setOnClickListener(v -> {
            saveData();
        });
        adb.setView(inflate);
        alertDialog = adb.create();
    }

    private void updateSpinnerData() {
        try {
            Object selectedItem = spinner.getSelectedItem();
            if (selectedItem != null) {
                String itemAtPosition = selectedItem.toString();
                if (itemAtPosition != null && !itemAtPosition.isEmpty()) {
                    if (itemAtPosition.toLowerCase().trim().equals("cash")) {
                        bankNameEt.setVisibility(View.GONE);
                        referenceEt.setVisibility(View.GONE);
                        bankNameEt.setText("");
                        referenceEt.setText("");
                    } else {
                        bankNameEt.setVisibility(View.VISIBLE);
                        referenceEt.setVisibility(View.VISIBLE);
                    }
                }
                Log.d("texts", "onItemSelected: " + itemAtPosition);
            } else {
                bankNameEt.setVisibility(View.GONE);
                referenceEt.setVisibility(View.GONE);
                bankNameEt.setText("");
                referenceEt.setText("");
            }
        } catch (Exception e) {
            bankNameEt.setVisibility(View.GONE);
            referenceEt.setVisibility(View.GONE);
            bankNameEt.setText("");
            referenceEt.setText("");

        }
    }

    private void updateData() {
        Set<Map.Entry<String, DataModel>> entries = costmap.entrySet();
        int total = 0;
        chipGroup.removeAllViews();
        for (Map.Entry<String, DataModel> s : entries) {
            total += s.getValue().getCost();
            Chip chip = new Chip(MainActivity.this);
            chip.setText(s.getKey() + " = Rs." + s.getValue().getCost());
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> {
                costmap.remove(s.getKey());
                arraySpinner.add(s.getKey());
                chipGroup.removeView(chip);
                updateData();
//                updateSpinner();
            });
            chipGroup.addView(chip);
            Log.d("texts", "updateData: " + s.getKey() + " " + s.getValue());
            updateSpinner();
            updateSpinnerData();
        }
        totalAmountTextView.setText("Total Amount = ₹" + total);
    }

    private void showEmptyData() {
        totalAmountTextView.setText("Total Amount = ₹0");
        chipGroup.removeAllViews();

    }

    private void saveData() {
        File file = new File(getFilesDir() + "/LastPayment.txt");
        if (costmap.size() > 0) {
            JSONObject data = new JSONObject();
            try {
                for (Map.Entry<String, DataModel> s : costmap.entrySet()) {
                    JSONObject jsonObject = new JSONObject();
                    DataModel value = s.getValue();
                    jsonObject.put("cost", value.cost);
                    jsonObject.put("bankName", value.bankName);
                    jsonObject.put("refrence", value.refrence);
                    data.put(s.getKey(), jsonObject);
                }
                file.createNewFile();
                FileWriter writer = new FileWriter(file);
                writer.append(data.toString());
                writer.flush();
                writer.close();
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.d("texts", "saveData: " + e.getLocalizedMessage());
            }
        } else {
            file.delete();
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateSpinner() {
        List<String> tempArr = new ArrayList<>(arraySpinner);
        for (String s : tempArr) {
            if (costmap.containsKey(s)) {
                arraySpinner.remove(s);
            }
        }
        adapter.notifyDataSetChanged();
        if (costmap.size() >= 3) {
            addPaymentBtn.setVisibility(View.GONE);
        } else {
            addPaymentBtn.setVisibility(View.VISIBLE);
        }
    }

    private void getOldData() {
        File file = new File(getFilesDir() + "/LastPayment.txt");
        if (file.exists() && file.length() > 0) {
            try {
                StringBuilder text = new StringBuilder();
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;

                while ((line = br.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
                br.close();
                if (!text.toString().isEmpty()) {
                    JSONObject jsonObject = new JSONObject(text.toString());
                    for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
                        String s = it.next();
                        JSONObject object = new JSONObject(jsonObject.get(s).toString());
                        DataModel dataModel = new DataModel(object.getInt("cost"), object.getString("bankName"), object.getString("refrence"));
                        costmap.put(s, dataModel);
                    }
                }
                updateData();
            } catch (Exception ignored) {
            }
        }
    }
}