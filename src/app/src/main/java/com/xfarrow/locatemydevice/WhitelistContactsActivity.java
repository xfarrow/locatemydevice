package com.xfarrow.locatemydevice;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class WhitelistContactsActivity extends AppCompatActivity {

    private ListView contactsListView;
    private ArrayList<String> contacts;
    private ArrayAdapter<String> listviewAdapter;
    private final WhitelistDbHandler whitelistDbHandler = new WhitelistDbHandler(this);

    private static final int CONTACT_PICK_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whitelist_contacts);
        setTitle("Whitelist");
        setViews();
        setListeners();

        contacts = whitelistDbHandler.getAllContacts();
        listviewAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                contacts);
        contactsListView.setAdapter(listviewAdapter);
    }

    private void setViews(){
        contactsListView = findViewById(R.id.ContactsListView);
    }

    private void setListeners(){
        contactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(WhitelistContactsActivity.this, "Long click to delete", Toast.LENGTH_SHORT).show();
            }
        });

        contactsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String number = (String)adapterView.getItemAtPosition(i);
                contacts.remove(number);
                listviewAdapter.notifyDataSetChanged();
                whitelistDbHandler.deleteContact(number);
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
            Intent pickContact = new Intent(Intent.ACTION_PICK);
            pickContact.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
            startActivityForResult(pickContact, CONTACT_PICK_CODE);
        }
        return super.onOptionsItemSelected(item);
    }

    // Gets fired when the user has chosen the contact
    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == CONTACT_PICK_CODE) {
            Uri contactData = data.getData();
            Cursor c = getContentResolver().query(contactData, null, null, null, null);
            if (c.moveToFirst()) {
                int phoneIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                int contactNameIndex = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                String number = c.getString(phoneIndex);
                String contactName = c.getString(contactNameIndex);
                contactSelected(number);
            }
            c.close();
        }
    }

    private void contactSelected(String phoneNo){
        // We'll replace parenthesis, dashes and whitespaces to obtain a valid phone number
        phoneNo = phoneNo.replaceAll("[-()\\s]", "");

        if(contacts.contains(phoneNo)){
            Toast.makeText(this, "Contact already in the list", Toast.LENGTH_SHORT).show();
            return;
        }
        whitelistDbHandler.addContact(phoneNo);
        contacts.add(phoneNo);
        listviewAdapter.notifyDataSetChanged();
    }
}