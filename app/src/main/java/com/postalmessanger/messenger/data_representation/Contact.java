package com.postalmessanger.messenger.data_representation;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import com.postalmessanger.messenger.util.Util;

import java.util.Collections;
import java.util.List;

/**
 * Created by kenny on 2/18/16.
 */
public class Contact {
    public static final String CONTACT_ID = ContactsContract.Contacts._ID;
    public static final String CONTACT_NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
    public static final String CONTACT_NAME = ContactsContract.Contacts.DISPLAY_NAME;

    public String id;
    public String name;
    public List<PhoneNumber> phoneNumbers;

    public Contact(String id, String name, List<PhoneNumber> phoneNumbers) {
        this.id = id;
        this.name = name;
        this.phoneNumbers = phoneNumbers;
    }

    public Contact(List<PhoneNumber> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    public Contact() {
    }

    public String toString() {
        return "{:id " + id + " :name " + name + " :phoneNumbers " + phoneNumbers + "}";
    }

    public static Contact contactFrom(Context ctx, String phoneNumber) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cur = ctx.getContentResolver().query(
                uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null);
        if (cur != null) {
            if (cur.moveToFirst()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                List<PhoneNumber> phoneNumbers = Util.phoneNumbersFor(ctx, id);
                return new Contact(id, name, phoneNumbers);
            }
            cur.close();
        }
        return new Contact(Collections.singletonList(new PhoneNumber(phoneNumber)));
    }
}
