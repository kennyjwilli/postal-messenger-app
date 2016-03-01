package com.postalmessanger.messenger.data_representation;

import android.provider.ContactsContract;

/**
 * Created by kenny on 2/19/16.
 */
public class PhoneNumber {
    public String type;
    public String number;

    public PhoneNumber(String type, String phoneNumber) {
        this.type = type;
        this.number = phoneNumber;
    }

    public PhoneNumber(int type, String phoneNumber) {
        this(getType(type), phoneNumber);
    }

    public PhoneNumber(String phoneNumber) {
        this(null, phoneNumber);
    }

    public PhoneNumber() {
    }

    public static String getType(int type) {
        String sType = "";
        switch (type) {
            case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                sType = "Home";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                sType = "Mobile";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                sType = "Work";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME:
                sType = "Home Fax";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK:
                sType = "Work Fax";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_MAIN:
                sType = "Main";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER:
                sType = "Other";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM:
                sType = "Custom";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_PAGER:
                sType = "Pager";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_ASSISTANT:
                sType = "Assistant";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_CALLBACK:
                sType = "Callback";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_CAR:
                sType = "Car";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_COMPANY_MAIN:
                sType = "Company Main";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_ISDN:
                sType = "ISDN";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_MMS:
                sType = "MMS";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER_FAX:
                sType = "Other Fax";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_RADIO:
                sType = "Radio";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_TELEX:
                sType = "Telex";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_TTY_TDD:
                sType = "TTY TDD";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE:
                sType = "Work Mobile";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_PAGER:
                sType = "Work Pager";
                break;
        }
        return sType;
    }

    public String toString() {
        return "{:type " + type + " :phoneNumber " + number + "}";
    }
}
