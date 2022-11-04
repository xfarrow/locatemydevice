package com.xfarrow.locatemydevice;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private Button buttonEnterPin;
    private EditText editTextLmdCommand;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch whitelistSwitch;
    private Button addContactsButton;
    private Settings settings;
    private LinearLayout infoLinearLayout;
    private LinearLayout permissionsLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        settings = new Settings(this);

        setViews();
        setValues();
        setListeners();
    }

    private void setViews(){
        buttonEnterPin = findViewById(R.id.buttonEnterPassword);
        editTextLmdCommand = findViewById(R.id.editTextLmdCommand);
        whitelistSwitch = findViewById(R.id.SwitchWhitelist);
        addContactsButton = findViewById(R.id.buttonAddContacts);
        infoLinearLayout = findViewById(R.id.info_layout);
        permissionsLinearLayout = findViewById(R.id.permissions_layout);

        addContactsButton.setEnabled(settings.getBoolean(Settings.WHITELIST_ENABLED));
        whitelistSwitch.setChecked(settings.getBoolean(Settings.WHITELIST_ENABLED));
    }

    private void setValues(){
        editTextLmdCommand.setText(settings.getString(Settings.SMS_COMMAND));
    }

    private void setListeners(){
        buttonEnterPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(SettingsActivity.this);
                alert.setTitle(R.string.password);
                alert.setMessage(R.string.choose_a_password);
                EditText input = new EditText(SettingsActivity.this);
                input.setTransformationMethod(new PasswordTransformationMethod());
                alert.setView(input);

                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String text = input.getText().toString();
                        if (!text.isEmpty()) {
                            settings.setString(Settings.PASSWORD, CipherUtils.get256Sha(text));
                        }
                        else{
                            Toast.makeText(SettingsActivity.this, R.string.cannot_use_a_blank_password, Toast.LENGTH_LONG).show();
                        }
                    }
                });

                alert.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

                final AlertDialog dialog = alert.create();
                dialog.show();

                // Disable button "OK" if the PIN contains a space or it's empty.
                input.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(
                                !charSequence.toString().equals("") && !charSequence.toString().contains(" ")
                        );
                    }

                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                    @Override
                    public void afterTextChanged(Editable editable) {}
                });
            }
        });

        editTextLmdCommand.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    Toast.makeText(SettingsActivity.this, R.string.empty_sms_command_not_allowed, Toast.LENGTH_LONG).show();
                    settings.setString(Settings.SMS_COMMAND, (String)settings.defaultValues(Settings.SMS_COMMAND));
                } else {
                    settings.setString(Settings.SMS_COMMAND, s.toString());
                }
            }
        });

        whitelistSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                addContactsButton.setEnabled(isChecked);
                settings.setBoolean(Settings.WHITELIST_ENABLED, isChecked);
            }
        });

        addContactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(SettingsActivity.this, WhitelistContactsActivity.class);
                SettingsActivity.this.startActivity(myIntent);
            }
        });

        infoLinearLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(SettingsActivity.this, AppInfoActivity.class);
                SettingsActivity.this.startActivity(myIntent);
            }
        });

        permissionsLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(SettingsActivity.this, PermissionsActivity.class);
                SettingsActivity.this.startActivity(myIntent);
            }
        });

    }
}
