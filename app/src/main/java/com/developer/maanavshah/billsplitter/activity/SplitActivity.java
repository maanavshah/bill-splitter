package com.developer.maanavshah.billsplitter.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.developer.maanavshah.billsplitter.R;
import com.developer.maanavshah.billsplitter.adapter.MemberAdapter;
import com.developer.maanavshah.billsplitter.model.Member;

import java.util.ArrayList;

public class SplitActivity extends AppCompatActivity {

    TextView tv;
    private MemberAdapter memberAdapter;
    private String tripId;
    private ArrayList<Member> listMember;
    private int noPerson;
    private String text = "";

    static double minOfAmount(double amount, double amount2) {
        return (amount < amount2) ? amount : amount2;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_split);

        initViews();

        Cursor cursor = memberAdapter.getMember(tripId);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            listMember.add(memberAdapter.cursorToMember(cursor));
            cursor.moveToNext();
        }
        noPerson = listMember.size();
        double[] amount = new double[noPerson];
        for (int i = 0; i < noPerson; i++) {
            amount[i] = listMember.get(i).getBalance();
        }
        minCashFlowRec(amount);
        tv = (TextView) findViewById(R.id.tvSplit);
        tv.setText(text);
    }

    private void initViews() {
        tripId = getIntent().getStringExtra("tripId");
        this.memberAdapter = new MemberAdapter(this);
        this.listMember = new ArrayList<>();
    }

    int getMin(double[] amount) {
        int minInd = 0;
        for (int i = 1; i < noPerson; i++)
            if (amount[i] < amount[minInd])
                minInd = i;
        return minInd;
    }

    int getMax(double arr[]) {
        int maxInd = 0;
        for (int i = 1; i < noPerson; i++)
            if (arr[i] > arr[maxInd])
                maxInd = i;
        return maxInd;
    }

    // Greedy Algorithm
    void minCashFlowRec(double[] amount) {
        int mxCredit = getMax(amount), mxDebit = getMin(amount);
        // If both amounts are 0, then all amounts are settled
        if (amount[mxCredit] == 0 || amount[mxDebit] == 0)
            return;
        // Find the minimum of two amounts
        double min = minOfAmount((-amount[mxDebit]), amount[mxCredit]);
        amount[mxCredit] -= min;
        amount[mxDebit] += min;
        text += "- " + listMember.get(mxDebit).getName() + " pays Rs." + min + " to "
                + listMember.get(mxCredit).getName() + "\n";
        minCashFlowRec(amount);
    }

    @Override
    public void onPause() {
        super.onPause();
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
