package com.example.first;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {
    private List<Contact> contacts;
    private static final int PERMISSIONS_REQUEST_CALL_PHONE = 101;
    private final ContactsCollectorActivity activity;

    public ContactAdapter(List<Contact> contacts, ContactsCollectorActivity activity) {
        this.contacts = contacts;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contacts.get(position);
        holder.text1.setText(contact.getName());
        holder.text2.setText(contact.getPhoneNumber());

        // Click to call
        holder.itemView.setOnClickListener(v -> {
            String phoneNumber = contact.getPhoneNumber();
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            v.getContext().startActivity(intent);
        });

        // Long press to edit
        holder.itemView.setOnLongClickListener(v -> {
            activity.showEditContactDialog(contact, position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public void removeItem(int position) {
        contacts.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Contact contact, int position) {
        contacts.add(position, contact);
        notifyItemInserted(position);
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView text1;
        TextView text2;

        ContactViewHolder(View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
} 