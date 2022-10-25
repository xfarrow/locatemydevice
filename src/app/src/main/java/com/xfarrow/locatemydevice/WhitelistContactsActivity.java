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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class WhitelistContactsActivity extends AppCompatActivity {

    ListView contactsListView;
    ArrayList<String> contacts;
    ArrayAdapter<String> listviewAdapter;
    WhitelistDbHandler whitelistDbHandler = new WhitelistDbHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whitelist_contacts);
        setTitle("Whitelist");
        setViews();

        contacts = whitelistDbHandler.getAllContacts();
        listviewAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                contacts);
        contactsListView.setAdapter(listviewAdapter);
    }

    private void setViews(){
        contactsListView = findViewById(R.id.ContactsListView);
    }

    // Add "add" button on the top
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
            Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            pickContact.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
            startActivityForResult(pickContact, 123);
        }
        return super.onOptionsItemSelected(item);
    }

    // Gets fired when the user has chosen the contact
    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 123) {
            Uri contactData = data.getData();
            Cursor c = getContentResolver().query(contactData, null, null, null, null);
            if (c.moveToFirst()) {
                int phoneIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String number = c.getString(phoneIndex).trim().replace("\\s+", "");
                contactSelected(number);
            }
            c.close();
        }
    }

    private void contactSelected(String number){
        if(whitelistDbHandler.isContactPresent(number)){
            Toast.makeText(this, "Contact already in the list", Toast.LENGTH_SHORT).show();
            return;
        }
        whitelistDbHandler.addContact(number);
        contacts.add(number);
        listviewAdapter.notifyDataSetChanged();
    }
}