package com.developer.maanavshah.billsplitter.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.developer.maanavshah.billsplitter.R;
import com.developer.maanavshah.billsplitter.adapter.BillAdapter;
import com.developer.maanavshah.billsplitter.adapter.Helper;
import com.developer.maanavshah.billsplitter.adapter.MemberAdapter;
import com.developer.maanavshah.billsplitter.model.Member;

import java.util.ArrayList;

public class AddBillActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static boolean addedBill = false;
    SparseBooleanArray array;
    int noPerson;
    private String tripId;
    private EditText etName, etAmount;
    private ListView lvList;
    private MemberAdapter memberAdapter;
    private ArrayList<Member> listMember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bill);

        initViews();

        Cursor cursor = memberAdapter.getMember(tripId);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            listMember.add(new Member(cursor.getString(cursor.getColumnIndexOrThrow(Helper.COLUMN_MEMBER_NAME)), Integer.parseInt(tripId)));
            cursor.moveToNext();
        }

        Cursor result = memberAdapter.getMember(tripId);
        String[] columns = new String[]{Helper.COLUMN_MEMBER_NAME};
        int[] to = new int[]{R.id.tvItemAddBillName};
        SimpleCursorAdapter dataAdapter = new SimpleCursorAdapter(this, R.layout.list_item_add_bill, result, columns, to, 0);
        lvList.setAdapter(dataAdapter);
    }

    private void initViews() {
        tripId = getIntent().getStringExtra("tripId");
        this.memberAdapter = new MemberAdapter(this);
        this.listMember = new ArrayList<>();
        this.etName = (EditText) findViewById(R.id.etAddBillName);
        this.etAmount = (EditText) findViewById(R.id.etAddBillAmount);
        Button bAdd = (Button) findViewById(R.id.bAddBillAdd);
        Button bSelect = (Button) findViewById(R.id.bAddBillSelect);
        this.lvList = (ListView) findViewById(R.id.lvAddBillMember);
        this.lvList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        bAdd.setOnClickListener(this);
        bSelect.setOnClickListener(this);
        this.lvList.setOnItemClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bAddBillSelect:
                addBill();
                break;
            case R.id.bAddBillAdd:
                if (addedBill)
                    showBill();
                else
                    Toast.makeText(this, "add member bill", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void insertMember() {
        noPerson = listMember.size();
        double total = 0;
        int noPay = array.size();
        double[] amount = new double[noPerson];
        double share;
        for (int j = 0; j < noPerson; j++)
            amount[j] = -1;
        for (int j = 0; j < noPerson; j++)
            amount[j] = listMember.get(j).getAmount();
        for (int j = 0; j < noPerson; j++)
            if (amount[j] != -1)
                total += amount[j];
        share = total / noPay;
        for (int j = 0; j < noPerson; j++)
            if (amount[j] != -1) {
                if (amount[j] - share > 0)
                    listMember.get(j).setCredit(listMember.get(j).getCredit() + amount[j]
                            - share);
                else
                    listMember.get(j).setDebit(listMember.get(j).getDebit() + share
                            - amount[j]);
            }
        String members = "";
        for (int i = 0; i < noPerson; i++) {
            double temp = memberAdapter.getBalance(tripId, listMember.get(i).getName());
            amount[i] = temp + listMember.get(i).getCredit() - listMember.get(i).getDebit();
            listMember.get(i).setBalance(amount[i]);
            memberAdapter.updateBalance(tripId, listMember.get(i).getName(), listMember.get(i).getBalance());
            members += listMember.get(i).getName();
            if (i < noPerson - 1)
                members += ", ";
        }
        insertBill(share, members);
    }

    private void insertBill(double share, String members) {
        BillAdapter billAdapter = new BillAdapter(this);
        billAdapter.createBill(etName.getText().toString(), etAmount.getText().toString(), share, members, tripId);
        Toast.makeText(AddBillActivity.this, etName.getText().toString() + " bill added!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), BillActivity.class);
        intent.putExtra("tripId", tripId);
        startActivity(intent);
    }

    private void showBill() {
        if (etName.getText().toString().matches("") || etAmount.getText().toString().matches("")) {
            Toast.makeText(this, "one or more fields are empty", Toast.LENGTH_SHORT).show();
            return;
        }
        int billAmount = 0;
        for (int i = 0; i < listMember.size(); i++)
            if (listMember.get(i).getAmount() != -1)
                billAmount += listMember.get(i).getAmount();
        if (!etAmount.getText().toString().matches(billAmount + "")) {
            Toast.makeText(this, "member bill don't add up to total bill", Toast.LENGTH_SHORT).show();
            addedBill = false;
        } else
            insertMember();
    }

    private void addBill() {
        array = lvList.getCheckedItemPositions();
        for (int i = 0; i < array.size(); i++) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(AddBillActivity.this);
            alertDialog.setTitle("Payment");
            alertDialog.setMessage(listMember.get(array.keyAt(i)).getName() + " amount paid");
            listMember.get(array.keyAt(i)).setAmount(0);
            final EditText input = new EditText(AddBillActivity.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            input.setLayoutParams(lp);
            input.setText("0");
            alertDialog.setView(input);
            final int finalI = i;
            alertDialog.setPositiveButton("Next",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (input.getText().toString().trim().matches(""))
                                dialog.dismiss();
                            else
                                listMember.get(array.keyAt(finalI)).setAmount(Double.parseDouble(input.getText().toString()));
                        }
                    });
            alertDialog.show();
            addedBill = true;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TextView tvStatus = (TextView) view.findViewById(R.id.tvItemAddBillStatus);
        if (lvList.isItemChecked(position)) {
            lvList.setItemChecked(position, true);
            tvStatus.setText("Yes");
        } else {
            lvList.setItemChecked(position, false);
            tvStatus.setText("No");
        }
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