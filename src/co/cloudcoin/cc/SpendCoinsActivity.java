package co.cloudcoin.cc;


import android.app.Activity;
import android.os.Bundle;

import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;
import android.content.Context;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.content.Intent;
import android.view.Window;
import android.os.Handler;
import android.graphics.Color;
import android.view.ViewGroup;

import android.graphics.drawable.Drawable;
import android.graphics.Typeface;
import android.util.Log;
import android.view.WindowManager;
import android.view.Display;
import android.util.DisplayMetrics;


import android.widget.FrameLayout;
import android.widget.LinearLayout;

import android.view.Gravity;
import android.widget.Toast;
import android.widget.EditText;

import android.util.TypedValue;
import android.content.res.Resources;

import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class SpendCoinsActivity extends Activity implements NumberPicker.OnValueChangeListener, OnClickListener {


	static String TAG = "CLOUDCOIN";

	Bank bank;

	boolean lastScreen;

	static int IDX_BANK = 0;
	static int IDX_COUNTERFEIT = 1;
	static int IDX_FRACTURED = 2;

	TextView mtv;
	EditText et;

	TextView[][] ids;
	int[][] stats;
	int size;

	NumberPicker[] nps;
	TextView[] tvs;

	RadioGroup rg;
	Button button;

	public void onCreate(Bundle savedInstanceState) {
		int i, resId;
		String idTxt;

		super.onCreate(savedInstanceState);

		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.spendcoins);

		size = Bank.denominations.length;
		nps = new NumberPicker[size];
		tvs = new TextView[size];
		for (i = 0; i < size; i++) {
			idTxt = "np" + Bank.denominations[i];
			resId = getResources().getIdentifier(idTxt, "id", getPackageName());
			nps[i] = (NumberPicker) findViewById(resId);

			idTxt = "bs" + Bank.denominations[i];
			resId = getResources().getIdentifier(idTxt, "id", getPackageName());
			tvs[i] = (TextView) findViewById(resId);
		}
	
		mtv = (TextView) findViewById(R.id.text);
		et = (EditText) findViewById(R.id.exporttag);
		rg = (RadioGroup) findViewById(R.id.radioGroup);

		button = (Button) findViewById(R.id.button);
		button.setOnClickListener(this);		


		
	}


	public void onPause() {
		super.onPause();
	}

	public void onResume() {
		int bankCoins[], frackedCoins[];
		int lTotal;

		super.onResume();

		lastScreen = false;

		bank = new Bank();
		bankCoins = bank.countCoins("bank");
		frackedCoins = bank.countCoins("fracked");

		for (int i = 0; i < size; i++) {
			lTotal = bankCoins[i + 1] + frackedCoins[i + 1];

			nps[i].setMinValue(0);
			nps[i].setMaxValue(lTotal);
			nps[i].setValue(0);
			nps[i].setOnValueChangedListener(this);
			nps[i].setTag(Bank.denominations[i]);
			nps[i].setWrapSelectorWheel(false);

			tvs[i].setText("" + lTotal);
                }

		updateTotal();

	}

	public void updateTotal() {
		String totalStr;

		int total = 0;
		for (int i = 0; i < size; i++) {
			int denomination =  Bank.denominations[i];

			total += denomination * nps[i].getValue();
		}

		totalStr = "Total to export: " + total + " CloudCoins";
		mtv.setText(totalStr);
	}

	public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
		updateTotal();
	}

	private void showError(String msg) {
		Toast toast = Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG);
		toast.show();
        }

	public void doExport(String tag) {
		int selectedId = rg.getCheckedRadioButtonId();
		int[] values;
		int[] failed;
		int totalFailed = 0;

		bank.setContext(this);

		values = new int[size];
		for (int i = 0; i < size; i++)
			values[i] = nps[i].getValue();

		if (selectedId == R.id.rjpg) {
			failed = bank.exportJpeg(values, tag);
		} else if (selectedId == R.id.rjson) {
			failed = bank.exportJson(values, tag);
		} else {
			Log.v(TAG, "We will never be here");
			return;
		}

		lastScreen = true;
		String msg;

		if (failed[0] == -1) {
			msg = "Global error. Coins have not been exported";
		} else {
			for (int i = 0; i < size; i++) {
				totalFailed += failed[i];
			}
			if (totalFailed == 0) {
				msg = "Coins have been exported successfully";
			} else {
				msg = "Failed to export " + totalFailed + " coins";
			}
		}

		hideScreen();
		mtv.setText(msg);
	}

	public void hideScreen() {
		LinearLayout parent = (LinearLayout) findViewById(R.id.blayoutmaininner);

		parent.removeView(findViewById(R.id.blayoutmain));
		parent.removeView(rg);
		parent.removeView(et);
	}

	public void onClick(View v) {
		int id = v.getId();
		String exportTag = "";


		switch (id) {
			case R.id.button:
				if (lastScreen) {
					finish();
					return;
				}

				exportTag = et.getText().toString();

				if (exportTag.length() > 16) {
					showError("Export Tag length should not be longer than 16 chars");
					return;
				}

				doExport(exportTag);
		}
	}

}

