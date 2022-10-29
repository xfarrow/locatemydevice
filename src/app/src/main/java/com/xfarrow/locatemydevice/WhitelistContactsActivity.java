package com.xfarrow.locatemydevice;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class WhitelistContactsActivity extends AppCompatActivity {

    private ListView contactsListView;
    private ArrayList<String> contactsListView_datasource;
    private ArrayAdapter<String> listviewAdapter;
    private final WhitelistDbHandler whitelistDbHandler = new WhitelistDbHandler(this);

    private static final int CONTACT_PICK_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whitelist_contacts);
        setTitle(R.string.whitelist);
        setViews();
        setListeners();

        contactsListView_datasource = whitelistDbHandler.getAllContacts();
        listviewAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                contactsListView_datasource);
        contactsListView.setAdapter(listviewAdapter);
    }

    private void setViews(){
        contactsListView = findViewById(R.id.ContactsListView);
    }

    private void setListeners(){
        contactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(WhitelistContactsActivity.this, R.string.long_click_to_delete, Toast.LENGTH_SHORT).show();
            }
        });

        contactsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String number = (String)adapterView.getItemAtPosition(i);
                removeNumberFromWhiteList(number);
                return true;
            }
        });

    }

    // Add "add contact" button on the top
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // "Add contact" click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.add_button) {
            AlertDialog.Builder alert = new AlertDialog.Builder(WhitelistContactsActivity.this);
            alert.setTitle(R.string.add_contact);
            alert.setMessage(R.string.type_a_phone_number);

            EditText input = new EditText(WhitelistContactsActivity.this);
            input.setInputType(InputType.TYPE_CLASS_PHONE);
            alert.setView(input);

            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String chosenPhoneNumber = input.getText().toString();
                    addNumberToWhiteList(chosenPhoneNumber);
                }
            });

            alert.setNeutralButton(R.string.choose_from_contacts,new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                    startActivityForResult(pickContact, CONTACT_PICK_CODE);
                }
            });

            alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            alert.show();
        }
        return super.onOptionsItemSelected(item);
    }

    // Gets fired when the user has chosen the contact
    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == CONTACT_PICK_CODE) {
            Uri contactData = data.getData();
            String[] projection = {
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.Contacts.DISPLAY_NAME
            };
            Cursor c = getContentResolver().query(contactData, projection, null, null, null);
            if (c.moveToFirst()) {
                int phoneIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                int contactNameIndex = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                if(phoneIndex < 0){
                    Toast.makeText(this, R.string.unable_to_retrieve_contact_error, Toast.LENGTH_LONG).show();
                    return;
                }
                String number = c.getString(phoneIndex);
                String contactName = (contactNameIndex > 0)?  c.getString(contactNameIndex) : "unknown"; // planned to show contact's name as well
                addNumberToWhiteList(number);
            }
            c.close();
        }
    }

    private void addNumberToWhiteList(String phoneNo){
        phoneNo = Utils.normalizePhoneNumber(phoneNo);
        if(!PhoneNumberUtils.isGlobalPhoneNumber(phoneNo)) {
            Toast.makeText(WhitelistContactsActivity.this,
                    R.string.phone_number_not_valid, Toast.LENGTH_LONG).show();
            return;
        }
        if(contactsListView_datasource.contains(phoneNo)){
            Toast.makeText(this, R.string.contact_already_in_the_list, Toast.LENGTH_SHORT).show();
            return;
        }
        whitelistDbHandler.addContact(phoneNo);
        contactsListView_datasource.add(phoneNo);
        listviewAdapter.notifyDataSetChanged();
    }

    private void removeNumberFromWhiteList(String phoneNo){
        whitelistDbHandler.deleteContact(phoneNo);
        contactsListView_datasource.remove(phoneNo);
        listviewAdapter.notifyDataSetChanged();
    }
}