package com.example.addressbook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.example.addressbook.data.DatabaseDescription.Contact;

public class AddEditFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // defines callback method implemented by MainActivity
    public interface AddEditFragmentListener {
        // called when contact is saved
        void onAddEditCompleted(Uri contactUri);
    }

    // constant used to identify the Loader
    private static final int CONTACT_LOADER = 0;

    private AddEditFragmentListener listener; // MainActivity
    private Uri contactUri; // Uri of selected contact
    private boolean addingNewContact = true; // adding (true) or editing

    // EditTexts for contact information
    private TextInputLayout nameTextInputLayout;
    private TextInputLayout phoneTextInputLayout;
    private TextInputLayout emailTextInputLayout;
    private TextInputLayout streetTextInputLayout;
    private TextInputLayout cityTextInputLayout;
    private TextInputLayout stateTextInputLayout;
    private TextInputLayout zipTextInputLayout;
    private FloatingActionButton saveContactFAB;
    private CoordinatorLayout coordinatorLayout; // used with SnackBars

    // set AddEditFragmentListener when Fragment attached
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (AddEditFragmentListener) context;
    }

    // remove AddEditFragmentListener when Fragment detached
    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    // called when Fragment's view needs to be created
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true); // fragment has menu items to display

        // inflate GUI and get references to EditTexts
        View view = inflater.inflate(R.layout.fragment_add_edit, container, false);
        nameTextInputLayout = view.findViewById(R.id.nameTextInputLayout);
        nameTextInputLayout.getEditText().addTextChangedListener(nameChangedListener);
        phoneTextInputLayout = view.findViewById(R.id.phoneTextInputLayoutEdit);
        emailTextInputLayout = view.findViewById(R.id.emailTextInputLayoutEdit);
        streetTextInputLayout = view.findViewById(R.id.streetTextInputLayoutEdit);
        cityTextInputLayout = view.findViewById(R.id.cityTextInputLayoutEdit);
        stateTextInputLayout = view.findViewById(R.id.stateTextInputLayoutEdit);
        zipTextInputLayout = view.findViewById(R.id.zipTextInputLayoutEdit);

        // set FloatingActionButton's event listener
        saveContactFAB = view.findViewById(R.id.saveFloatingActionButton);
        saveContactFAB.setOnClickListener(saveContactButtonClicked);
        updateSaveButtonFAB();

        // used to display SnackBars with brief messages
        coordinatorLayout = getActivity().findViewById(R.id.coordinatorLayout);

        Bundle arguments = getArguments(); // null if creating new contact

        if (arguments != null) {
            addingNewContact = false;
            contactUri = arguments.getParcelable(MainActivity.CONTACT_URI);
        }

        // if editing an existing contact, create Loader to get the contact
        if (contactUri != null)
            getLoaderManager().initLoader(CONTACT_LOADER, null, this);

        return view;
    }

    // detects when the text in the nameTextInputLayout's EditText changes
    // to hide or show saveButtonFAB
    private final TextWatcher nameChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        // called when the text in nameTextInputLayout changes
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            updateSaveButtonFAB();
        }
        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    // shows saveButtonFAB only if the name is not empty
    private void updateSaveButtonFAB() {
        String input = nameTextInputLayout.getEditText().getText().toString();

        // if there is a name for the contact, show the FloatingActionButton
        if (input.trim().length() != 0)
            saveContactFAB.show();
        else
            saveContactFAB.hide();
    }

    // responds to event generated when user saves a contact
    private final View.OnClickListener saveContactButtonClicked =
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // hide the virtual keyboard
                    ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getView().getWindowToken(), 0);
                    saveContact(); // save contact to the database
                }
            };

    // saves contact information to the database
    private void saveContact() {
        // create ContentValues object containing contact's key-value pairs
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contact.COLUMN_NAME, nameTextInputLayout.getEditText().getText().toString());
        contentValues.put(Contact.COLUMN_PHONE, phoneTextInputLayout.getEditText().getText().toString());
        contentValues.put(Contact.COLUMN_EMAIL, emailTextInputLayout.getEditText().getText().toString());
        contentValues.put(Contact.COLUMN_STREET, streetTextInputLayout.getEditText().getText().toString());
        contentValues.put(Contact.COLUMN_CITY, cityTextInputLayout.getEditText().getText().toString());
        contentValues.put(Contact.COLUMN_STATE, stateTextInputLayout.getEditText().getText().toString());
        contentValues.put(Contact.COLUMN_ZIP, zipTextInputLayout.getEditText().getText().toString());

        if (addingNewContact) {
            Uri newContactUri = getActivity().getContentResolver().insert(Contact.CONTENT_URI, contentValues);
            if (newContactUri != null) {
                Snackbar.make(coordinatorLayout, R.string.contact_added, Snackbar.LENGTH_LONG).show();
                listener.onAddEditCompleted(newContactUri);
            } else {
                Snackbar.make(coordinatorLayout, R.string.contact_not_added, Snackbar.LENGTH_LONG).show();
            }
        } else {
            int updatedRows = getActivity().getContentResolver().update(contactUri, contentValues, null, null);

            if (updatedRows > 0) {
                listener.onAddEditCompleted(contactUri);
                Snackbar.make(coordinatorLayout, R.string.contact_updated, Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar.make(coordinatorLayout, R.string.contact_not_updated, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    // called by LoaderManager to create a Loader
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // create an appropriate CursorLoader based on the id argument;
        // only one Loader in this fragment, so the switch is unnecessary
        switch (id) {
            case CONTACT_LOADER:
                return new CursorLoader(getActivity(), contactUri, null, null, null, null);
            default:
                return null;
        }
    }

    // called by LoaderManager when loading completes
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // if the contact exists in the database, display its data
        if (data != null && data.moveToFirst()) {
            // get the column index for each data item
            int nameIndex = data.getColumnIndex(Contact.COLUMN_NAME);
            int phoneIndex = data.getColumnIndex(Contact.COLUMN_PHONE);
            int emailIndex = data.getColumnIndex(Contact.COLUMN_EMAIL);
            int streetIndex = data.getColumnIndex(Contact.COLUMN_STREET);
            int cityIndex = data.getColumnIndex(Contact.COLUMN_CITY);
            int stateIndex = data.getColumnIndex(Contact.COLUMN_STATE);
            int zipIndex = data.getColumnIndex(Contact.COLUMN_ZIP);

            // fill EditTexts with the retrieved data
            nameTextInputLayout.getEditText().setText(data.getString(nameIndex));
            phoneTextInputLayout.getEditText().setText(data.getString(phoneIndex));
            emailTextInputLayout.getEditText().setText(data.getString(emailIndex));
            streetTextInputLayout.getEditText().setText(data.getString(streetIndex));
            cityTextInputLayout.getEditText().setText(data.getString(cityIndex));
            stateTextInputLayout.getEditText().setText(data.getString(stateIndex));
            zipTextInputLayout.getEditText().setText(data.getString(zipIndex));

            updateSaveButtonFAB();
        }
    }

    // called by LoaderManager when the Loader is being reset
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}