package FragmentControllers;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.starter.R;

import Models.Relation;
import Models.User;

/**
 * Created by Dylan Castanhinha on 4/13/2017.
 */

public class ChangeDetailsFragment extends DialogFragment {

    int position;
    TextView cancelButton;
    TextView label;
    TextView saveButton;
    TextView numberPickerLabel;
    User currentUser;
    RelativeLayout photoRR;
    RelativeLayout textViewRR;
    RelativeLayout numberPickerRR;
    RelativeLayout dualNumberPickerRR;
    NumberPicker singleNumberPicker;
    NumberPicker firstNumberPicker;
    NumberPicker secondNumberPicker;
    EditText changeText;
    DismissEditDialogListener activityCallback;
    int updatedValue;
    String[] valuesSex = {"Male", "Female", "Other"};
    String[] valuesHeightFt = {"3 ft", "4 ft", "5 ft", "6 ft", "7ft"};
    String[] valuesHeightInches = {"0 in", "1 in", "2 in", "3 in", "4in", "5 in", "6 in", "7 in", "8 in", "9in", "10 in", "11 in"};

    public ChangeDetailsFragment(){
    }

    public static ChangeDetailsFragment newInstance(int position){
        ChangeDetailsFragment frag = new ChangeDetailsFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        frag.setArguments(args);
        return frag;
    }

    public interface DismissEditDialogListener{
        void onEditDialogDismissal();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        activityCallback.onEditDialogDismissal();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_change_details, container, false);
        position = getArguments().getInt("position");
        currentUser = (User) ParseUser.getCurrentUser();
        cancelButton = (TextView) rootView.findViewById(R.id.change_details_cancelButtton);
        label = (TextView) rootView.findViewById(R.id.change_details_label);
        saveButton = (TextView) rootView.findViewById(R.id.change_details_save_button);
        photoRR = (RelativeLayout) rootView.findViewById(R.id.change_details_photo_relative_layout);
        textViewRR = (RelativeLayout) rootView.findViewById(R.id.change_details_text_view_relative_layout);
        numberPickerRR = (RelativeLayout) rootView.findViewById(R.id.change_details_number_picker_relative_layout);
        dualNumberPickerRR = (RelativeLayout) rootView.findViewById(R.id.change_details_dual_number_picker_relative_layout);
        singleNumberPicker = (NumberPicker) rootView.findViewById(R.id.singleNumberPicker);
        firstNumberPicker = (NumberPicker) rootView.findViewById(R.id.firstNumberPicker);
        secondNumberPicker = (NumberPicker) rootView.findViewById(R.id.secondNumberPicker);
        numberPickerLabel = (TextView) rootView.findViewById(R.id.change_details_single_number_picker_label);
        numberPickerLabel.setText("");
        photoRR.setVisibility(View.INVISIBLE);
        textViewRR.setVisibility(View.INVISIBLE);
        numberPickerRR.setVisibility(View.INVISIBLE);
        dualNumberPickerRR.setVisibility(View.INVISIBLE);
        changeText = (EditText) rootView.findViewById(R.id.change_details_edit_text);
        chooseCorrectRelativeLayout();
        cancelButton.setOnClickListener(new CancelClickListener());
        saveButton.setOnClickListener(new SaveButtonClickListener());
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activityCallback = (DismissEditDialogListener) context;
    }

    public void chooseCorrectRelativeLayout() {
        switch (position) {
            case 0: {
                photoRR.setVisibility(View.VISIBLE);
                label.setText("Change Photo");

                break;
            }
            case 1: {
                textViewRR.setVisibility(View.VISIBLE);
                label.setText("Edit Name");
                break;
            }
            case 2: {
                numberPickerRR.setVisibility(View.VISIBLE);
                singleNumberPicker.setMinValue(0);
                singleNumberPicker.setMaxValue(valuesSex.length-1);
                singleNumberPicker.setDisplayedValues(valuesSex);
                singleNumberPicker.setWrapSelectorWheel(true);
                singleNumberPicker.setOnValueChangedListener(new NumberPickerListener());
                label.setText("Edit Sex");
                break;
            }
            case 3: {
                label.setText("Edit Age");
                numberPickerLabel.setText("Years");
                numberPickerRR.setVisibility(View.VISIBLE);
                singleNumberPicker.setMinValue(0);
                singleNumberPicker.setMaxValue(120);
                singleNumberPicker.setWrapSelectorWheel(true);
                singleNumberPicker.setOnValueChangedListener(new NumberPickerListener());
                int defaultValue;
                try {
                    defaultValue = Integer.parseInt(currentUser.getAge());
                    singleNumberPicker.setValue(defaultValue);
                } catch(NumberFormatException nfe) {
                    Log.i("AppInfo", nfe.getMessage());
                }
                break;
            }
            case 4: {
                label.setText("Edit Weight");
                dualNumberPickerRR.setVisibility(View.VISIBLE);
                break;
            }
            case 5: {
                label.setText("Edit Height");
                dualNumberPickerRR.setVisibility(View.VISIBLE);
                firstNumberPicker.setMinValue(0);
                firstNumberPicker.setMaxValue(valuesHeightFt.length-1);
                firstNumberPicker.setDisplayedValues(valuesHeightFt);
                firstNumberPicker.setWrapSelectorWheel(true);
                secondNumberPicker.setMinValue(0);
                secondNumberPicker.setMaxValue(valuesHeightInches.length-1);
                secondNumberPicker.setDisplayedValues(valuesHeightInches);
                secondNumberPicker.setWrapSelectorWheel(true);
                firstNumberPicker.setOnValueChangedListener(new NumberPickerListener());
                secondNumberPicker.setOnValueChangedListener(new NumberPickerListener());
                break;
            }
            case 6: {
                dualNumberPickerRR.setVisibility(View.VISIBLE);
                label.setText("Edit Body Fat%");
                break;
            }
            case 7: {
                break;
            }
            case 8: {
                //Show trainer view
                ParseQuery<Relation> query = ParseQuery.getQuery(Relation.class);
                query.whereEqualTo("trainer", currentUser);
                query.getFirstInBackground(new GetCallback<Relation>() {
                    @Override
                    public void done(Relation object, ParseException e) {
                        if (e == null) {
                            object.deleteInBackground(new DeleteCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null){
                                        Log.i("AppInfo", "Deleted");
                                    } else {
                                        Log.i("AppInfo", e.getMessage());
                                    }
                                }
                            });
                        } else {
                            Log.i("AppInfo", e.getMessage());
                        }
                    }
                });
                break;
            }
        }
    }

    private class NumberPickerListener implements NumberPicker.OnValueChangeListener{

        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            updatedValue = newVal;
        }
    }
    private class SaveButtonClickListener implements TextView.OnClickListener{
        @Override
        public void onClick(View v) {
            switch (position) {
                case 0: {

                    break;
                }
                case 1: {
                    currentUser.setFullName(String.valueOf(changeText.getText()));
                    break;
                }
                case 2: {
                    if (updatedValue != -1) {
                        currentUser.setSex(valuesSex[updatedValue]);
                    } else {
                        currentUser.setSex(valuesSex[singleNumberPicker.getValue()]);
                    }
                    break;
                }
                case 3: {
                    if (updatedValue != -1) {
                        currentUser.setAge(String.valueOf(updatedValue));
                    } else {
                        currentUser.setAge(String.valueOf(singleNumberPicker.getValue()));
                    }
                    break;
                }
                case 4: {
                    currentUser.setWeight(String.valueOf(updatedValue));
                    break;
                }
                case 5: {
                    String height = valuesHeightFt[firstNumberPicker.getValue()]+" "+valuesHeightInches[secondNumberPicker.getValue()];
                    currentUser.setHeight(height);
                    break;
                }
                case 6: {
                    currentUser.setBodyFat(String.valueOf(updatedValue));
                    break;
                }
                case 7: {

                    break;
                }
                case 8: {

                    break;
                }
            }
            currentUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null){
                        Log.i("AppInfo", "Saved");
                        dismiss();
                    } else {
                        Log.i("AppInfo", e.getMessage());
                    }
                }
            });
        }
    }

    private class CancelClickListener implements TextView.OnClickListener{
        @Override
        public void onClick(View v) {
            dismiss();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }
}
