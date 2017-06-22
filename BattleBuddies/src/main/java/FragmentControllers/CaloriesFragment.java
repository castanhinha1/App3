package FragmentControllers;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.parse.ParseUser;
import com.parse.starter.R;

import ConfigClasses.BMRCalculator;
import Models.User;

/**
 * Created by Dylan Castanhinha on 4/24/2017.
 */

public class CaloriesFragment extends Fragment {

    EditText weightTF;
    EditText bodyFatTF;
    Button calculateButton;
    RadioGroup radioGroup;
    RadioGroup radioGroup2;
    User currentUser;
    double dailyActivityMultplier;
    double sexFactor;

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            checkFieldsForEmptyValues();
        }
        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.custom_toolbar);
        TextView titleTextView = (TextView) getActivity().findViewById(R.id.toolbar_title);
        titleTextView.setText("Update Caloric Intake");
        View rootView = inflater.inflate(R.layout.fragment_calories, container, false);
        currentUser = (User) ParseUser.getCurrentUser();
        weightTF = (EditText) rootView.findViewById(R.id.weightTextField);
        bodyFatTF = (EditText) rootView.findViewById(R.id.bodyFatTextField);
        calculateButton = (Button) rootView.findViewById(R.id.calculateButton);
        calculateButton.setEnabled(false);
        calculateButton.setOnClickListener(new OnCalculateButtonClick());
        radioGroup = (RadioGroup) rootView.findViewById(R.id.radioGroup);
        radioGroup2 = (RadioGroup) rootView.findViewById(R.id.radioGroup2);
        weightTF.addTextChangedListener(textWatcher);
        bodyFatTF.addTextChangedListener(textWatcher);
        radioGroup.setOnCheckedChangeListener(new OnRadioButtonChangeListener());
        radioGroup2.setOnCheckedChangeListener(new OnRadioButton2ChangeListener());

        return rootView;
    }

    private class OnRadioButtonChangeListener implements RadioGroup.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
            switch (checkedId) {
                case R.id.buttonVeryLight:
                    dailyActivityMultplier = 1.30;
                    break;
                case R.id.buttonLight:
                    dailyActivityMultplier = 1.55;
                    break;
                case R.id.buttonModerate:
                    dailyActivityMultplier = 1.65;
                    break;
                case R.id.buttonHeavy:
                    dailyActivityMultplier = 1.80;
                    break;
                case R.id.buttonVeryHeavy:
                    dailyActivityMultplier = 2.00;
                    break;
            }
        }
    }

    private class OnCalculateButtonClick implements Button.OnClickListener {

        @Override
        public void onClick(View v) {
            double userWeightInLb = Double.parseDouble(weightTF.getText().toString());
            double userBodyFat = Double.parseDouble(bodyFatTF.getText().toString());
            double userSexFactor = sexFactor;
            double userDailyActvityMultiplier = dailyActivityMultplier;
            BMRCalculator theBMRCalculator = new BMRCalculator(userWeightInLb, userBodyFat, userSexFactor, userDailyActvityMultiplier);
            currentUser.setBodyFat(String.valueOf(bodyFatTF.getText()));
            currentUser.setWeight(String.valueOf(weightTF.getText()));
            currentUser.saveInBackground();
            HomeViewFragment homeViewFragment = new HomeViewFragment();
            FragmentManager fm = getFragmentManager();
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, homeViewFragment);
            fragmentTransaction.commit();
        }
    }

    private class OnRadioButton2ChangeListener implements RadioGroup.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
            switch (checkedId) {
                case R.id.male:
                    sexFactor = 1.0;
                    break;
                case R.id.female:
                    sexFactor = 0.9;
                    break;
            }
        }
    }

    private void checkFieldsForEmptyValues(){
        if(weightTF.equals("") && bodyFatTF.equals("")){
            calculateButton.setEnabled(false);
            Log.i("AppInfo", "Here 1");
        } else if(!(weightTF.equals("")) && (bodyFatTF.equals(""))){
            calculateButton.setEnabled(false);
            Log.i("AppInfo", "Here 2");
        } else if(!(bodyFatTF.equals("")) && (weightTF.equals(""))){
            calculateButton.setEnabled(false);
        } else{
            calculateButton.setEnabled(true);
            Log.i("AppInfo", "Here 3");
        }
    }
}
