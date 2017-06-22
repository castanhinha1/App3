package ConfigClasses;

import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import Models.User;

/**
 * Created by Dylan Castanhinha on 3/30/2017.
 */

public class BMRCalculator {
    User currentUser;
    private int dailyCalories;
    private double userBodyFat;
    private double userWeightInLb;
    private double userSexFactor;
    private double userDailyActvityMultiplier;
    private double weightInKg;

    public BMRCalculator(double userWeightInLb, double userBodyFat, double userSexFactor, double userDailyActvityMultiplier){
        this.userWeightInLb = userWeightInLb;
        this.userBodyFat = userBodyFat;
        this.userSexFactor = userSexFactor;
        this.userDailyActvityMultiplier = userDailyActvityMultiplier;
        step1();
        dailyCalories = (int) dailyCaloricExpenditure();
        currentUser = (User) ParseUser.getCurrentUser();
        currentUser.setCalories(Integer.toString(dailyCalories));
        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null){
                    Log.i("AppInfo", "Daily caloric intake updated");
                } else {
                    Log.i("AppInfo", e.getMessage());
                }
            }
        });

    }

    //Multiply BMR by lean factor multipler to obtain daily caloric expenditure
    private double dailyCaloricExpenditure(){
        return step4() * step5();
    }
    //Step 1: Converts bodyweight from pounds to kilograms
    private double step1(){
        weightInKg = userWeightInLb / 2.2;
        return weightInKg;
    }
    /*Step 2: Calculate the appropriate equation below using bodyweight in kilograms.
    Men 1.0 x Body Weight(kg) x 24
    Women 0.9 x Body Weight(kg) x 24*/
    private double step2(){
        return Math.round(userSexFactor * weightInKg * 24);
    }
    //Step 3: Determine lean factor multiplier
    private double step3(){
        double leanFactorMultiplier;
        if (userSexFactor == 1.0){
            if (userBodyFat <= 14){
                leanFactorMultiplier = 1.0;
            } else if (userBodyFat > 14 && userBodyFat <= 20){
                leanFactorMultiplier = .95;
            } else if (userBodyFat > 20 && userBodyFat <= 28){
                leanFactorMultiplier = .90;
            } else {
                leanFactorMultiplier = .85;
            }
        } else{
            if (userBodyFat <= 18){
                leanFactorMultiplier = 1.0;
            } else if (userBodyFat > 18 && userBodyFat <= 28){
                leanFactorMultiplier = .95;
            } else if (userBodyFat > 28 && userBodyFat <= 38){
                leanFactorMultiplier = .90;
            } else {
                leanFactorMultiplier = .85;
            }
        }
        return leanFactorMultiplier;
    }
    //Step 4: Multiply the number obtained in Step 2 by the lean factor multiplier to determine BMR
    //Number from Step 2 x lean factor multiplier = BMR
    private double step4(){
        return step2() * step3();
    }
    //Step 5: Determine daily activity multipler
    private double step5(){
        return userDailyActvityMultiplier;
    }
}
