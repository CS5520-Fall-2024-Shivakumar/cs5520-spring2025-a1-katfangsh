package com.example.first;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.List;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.graphics.Color;

public class ContactsCollectorActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private static final int PERMISSIONS_REQUEST_CALL_PHONE = 101;
    private RecyclerView recyclerView;
    private ContactAdapter adapter;
    private List<Contact> contactsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts_collector);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fab = findViewById(R.id.fabAddContact);
        fab.setOnClickListener(v -> showAddContactDialog());

        // Setup swipe to delete
        ItemTouchHelper.SimpleCallback swipeToDeleteCallback = 
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder,
                                @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                final Contact deletedContact = contactsList.get(position);
                
                adapter.removeItem(position);

                Snackbar.make(recyclerView, "Contact deleted", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", view -> {
                            adapter.restoreItem(deletedContact, position);
                            recyclerView.scrollToPosition(position);
                        })
                        .setActionTextColor(Color.YELLOW)
                        .show();
            }
        };

        new ItemTouchHelper(swipeToDeleteCallback).attachToRecyclerView(recyclerView);

        // Check for permissions and request if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            loadContacts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadContacts();
            } else {
                Toast.makeText(this, "Permission denied to read contacts", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PERMISSIONS_REQUEST_CALL_PHONE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Retry the call if permission is granted
                // Note: The user will need to tap the contact again
                Toast.makeText(this, "Permission granted, please tap the contact again to call", 
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied to make phone calls", 
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showAddContactDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_contact, null);
        
        TextInputEditText nameInput = dialogView.findViewById(R.id.editTextName);
        TextInputEditText phoneInput = dialogView.findViewById(R.id.editTextPhone);

        builder.setView(dialogView)
                .setTitle("Add New Contact")
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String phone = phoneInput.getText().toString().trim();
                    
                    if (!name.isEmpty() && !phone.isEmpty()) {
                        Contact newContact = new Contact(name, phone);
                        int position = contactsList.size();
                        contactsList.add(newContact);
                        adapter.notifyItemInserted(position);
                        
                        // Show success Snackbar with undo action
                        Snackbar.make(recyclerView, 
                                "Contact " + name + " added successfully", 
                                Snackbar.LENGTH_LONG)
                                .setAction("UNDO", v -> {
                                    contactsList.remove(position);
                                    adapter.notifyItemRemoved(position);
                                    showUndoSuccessSnackbar();
                                })
                                .show();
                    } else {
                        // Show error Snackbar with retry action
                        Snackbar.make(recyclerView, 
                                "Failed to add contact - Empty fields", 
                                Snackbar.LENGTH_LONG)
                                .setAction("RETRY", v -> showAddContactDialog())
                                .show();
                    }
                })
                .setNegativeButton("Cancel", null);

        builder.create().show();
    }

    private void showUndoSuccessSnackbar() {
        Snackbar.make(recyclerView, 
                "Contact removed", 
                Snackbar.LENGTH_SHORT)
                .show();
    }

    public void showEditContactDialog(Contact contact, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_contact, null);
        
        TextInputEditText nameInput = dialogView.findViewById(R.id.editTextName);
        TextInputEditText phoneInput = dialogView.findViewById(R.id.editTextPhone);

        // Pre-fill existing contact info
        nameInput.setText(contact.getName());
        phoneInput.setText(contact.getPhoneNumber());

        builder.setView(dialogView)
                .setTitle("Edit Contact")
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String phone = phoneInput.getText().toString().trim();
                    
                    if (!name.isEmpty() && !phone.isEmpty()) {
                        Contact updatedContact = new Contact(name, phone);
                        contactsList.set(position, updatedContact);
                        adapter.notifyItemChanged(position);
                        
                        Snackbar.make(recyclerView, 
                                "Contact updated successfully", 
                                Snackbar.LENGTH_SHORT)
                                .show();
                    } else {
                        Snackbar.make(recyclerView, 
                                "Failed to update - Empty fields", 
                                Snackbar.LENGTH_LONG)
                                .show();
                    }
                })
                .setNegativeButton("Cancel", null);

        builder.create().show();
    }

    private void loadContacts() {
        contactsList = new ArrayList<>();
        
        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                },
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );

        if (cursor != null) {
            try {
                int nameIndex = cursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int phoneIndex = cursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NUMBER);

                while (cursor.moveToNext()) {
                    String name = nameIndex != -1 ? cursor.getString(nameIndex) : "";
                    String phoneNumber = phoneIndex != -1 ? cursor.getString(phoneIndex) : "";
                    
                    if (!name.isEmpty() || !phoneNumber.isEmpty()) {
                        contactsList.add(new Contact(name, phoneNumber));
                    }
                }
            } finally {
                cursor.close();
            }
        }

        adapter = new ContactAdapter(contactsList, this);
        recyclerView.setAdapter(adapter);
    }
}