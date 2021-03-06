package mobilecomputing.hsalbsig.de.mylocation.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.nio.channels.spi.AbstractSelectionKey;

import mobilecomputing.hsalbsig.de.mylocation.R;
import mobilecomputing.hsalbsig.de.mylocation.dao.Track;

public class DialogSaveFragment extends DialogFragment {

    //listener to deliver actions
    private DialogSaveListener dialoglistener;

    //callback interface for interaction.
    public interface DialogSaveListener {
        void onDialogSaveSaveClick(String title, String tag);
    }


    @Override
    public Dialog onCreateDialog(Bundle saveInstanceState) {

        //creating new builder, setting its view.
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_save, null);

        if (getArguments().getString("title") != null) {
            builder.setTitle(getArguments().getString("title"));
        }

        builder.setView(view);

        // getting the Views components.
        final Button dialogCancelButton = (Button) view.findViewById(R.id.dialogCancel);
        final Button dialogSaveButton = (Button) view.findViewById(R.id.dialogSave);
        final EditText title = (EditText) view.findViewById(R.id.dialogTitle);

        // following lines create button press logic and notifys listener if its called
        dialogCancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        dialogSaveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (title == null || title.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), "Please fill out Title", Toast.LENGTH_SHORT).show();
                } else {
//                    if (note == null || note.toString().isEmpty()) {
//                        note.setText(" ");
//                    }
                    dismiss();
                    dialoglistener.onDialogSaveSaveClick(title.getText().toString(), getTag());
                }
            }
        });
        return builder.create();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            dialoglistener = (DialogSaveListener) activity;
        } catch (Exception e) {
            // The activity doesn't implement the interface, throw exception
            Log.d("Debug", activity.toString() + " must implement NoticeDialogListener");
        }
    }
}


