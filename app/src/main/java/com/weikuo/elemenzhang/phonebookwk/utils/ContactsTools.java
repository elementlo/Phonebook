package com.weikuo.elemenzhang.phonebookwk.utils;

import com.github.tamir7.contacts.Contact;

import java.io.IOException;

import ezvcard.VCard;
import ezvcard.property.Address;
import ezvcard.property.Organization;
import ezvcard.property.StructuredName;

/**
 * Created by elemenzhang on 2017/6/23.
 */

public class ContactsTools {
    public static VCard createVCard(Contact contact) throws IOException {
        VCard vcard = new VCard();
        StructuredName n = new StructuredName();
        if (contact.getFamilyName()!=null){
            n.setFamily(contact.getFamilyName());
        }
        n.setGiven(contact.getGivenName());
        n.getPrefixes().add(contact.getDisplayName());
        vcard.setStructuredName(n);
        vcard.setOrganization(contact.getCompanyName());

        if (contact.getAddresses() != null && contact.getAddresses().size() != 0) {
            Address adr = new Address();
            adr.setExtendedAddress(contact.getAddresses().get(0).getFormattedAddress());
            vcard.addAddress(adr);
        }

        if (contact.getEmails() != null && contact.getEmails().size() != 0) {
            for (int i = 0; i < contact.getEmails().size(); i++) {
                vcard.addEmail(contact.getEmails().get(i).getAddress());
            }
        }

        if (contact.getPhoneNumbers() != null && contact.getPhoneNumbers().size() != 0) {
            for (int i = 0; i < contact.getPhoneNumbers().size(); i++) {
                vcard.addTelephoneNumber(contact.getPhoneNumbers().get(i).getNumber());
            }
        }
        if (contact.getCompanyName() != null) {
            Organization organization = new Organization();
            organization.setAltId(contact.getCompanyName());
            vcard.addOrganization(organization);
        }

        if (contact.getNote() != null) {
            vcard.addNote(contact.getNote());
        }
        return vcard;
    }
}
