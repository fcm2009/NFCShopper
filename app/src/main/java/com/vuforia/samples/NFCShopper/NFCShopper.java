package com.vuforia.samples.NFCShopper;

import android.content.Intent;
import android.graphics.Color;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Parcelable;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import com.vuforia.samples.VuforiaSamples.R;

import java.nio.charset.Charset;
import java.util.ArrayList;

public class NFCShopper extends Activity implements NfcAdapter.CreateNdefMessageCallback{

    ArrayList<Item> itemSelectedList;
    Item[] list;
    ArrayAdapter selectedAdapter;
    NfcAdapter mNfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfcshopper);

        list = new Item[4];
        list [0] = new Item(1, "Nuts");
        list [1] = new Item(2, "Vegetables");
        list [2] = new Item(3, "Pancakes");
        list [3] = new Item(4, "Fruits");;

        ListView itemList = (ListView) findViewById(R.id.itemList);
        ArrayAdapter adapter = new ArrayAdapter<Item>(this, R.layout.checkedtextlayout, R.id.checkedTextView,  list);
        itemList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        itemList.setAdapter(adapter);

        itemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckedTextView text = (CheckedTextView) view;
                if(text.isChecked())
                    text.setTextColor(Color.parseColor("#E71D36"));
                else
                    text.setTextColor(Color.parseColor("#2EC4B6"));
            }
        });

        ListView mainList = (ListView) findViewById(R.id.mainList);
        itemSelectedList = new ArrayList<Item>();
        selectedAdapter = new ArrayAdapter<Item>(this, R.layout.textlayout, R.id.textView, itemSelectedList);
        mainList.setAdapter(selectedAdapter);

        mainList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Item item = itemSelectedList.remove(position);
                selectedAdapter.notifyDataSetChanged();

                ListView itemList = (ListView)findViewById(R.id.itemList);
                int i;
                for(i = 0; i < list.length && !item.equal(list[i]); i++);
                CheckedTextView text = (CheckedTextView)itemList.getChildAt(i);
                if(text.isChecked()) {
                    text.toggle();
                    itemList.performItemClick(itemList.getChildAt(i), i, itemList.getChildAt(i).getId());
                }
            }
        });

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        // Register callback
        mNfcAdapter.setNdefPushMessageCallback(this, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }

    public void processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        // record 0 contains the MIME type, record 1 is the AAR, if present

        NdefRecord [] records = msg.getRecords();
        for(int i = 0; i < records.length; i++){
            for (Item item:itemSelectedList) {
                if(Integer.parseInt(new String(records[i].getId())) == item.id) {
                    item.image = new String(records[i].getPayload());
                    break;
                }
            }
        }
        setIntent(new Intent());
        Toast.makeText(this, "Transfer Completed Successfully", Toast.LENGTH_LONG).show();
    }


    public void addItemsToList(View view) {
        ListView listView = (ListView) findViewById(R.id.itemList);
        int len = listView.getCount();
        SparseBooleanArray checked = listView.getCheckedItemPositions();
        for (int i = 0; i < len; i++)
            if (checked.get(i))
                if(!find(list[i]))
                    itemSelectedList.add(list[i]);

        selectedAdapter.notifyDataSetChanged();

    }

    public boolean find(Item item){
        for(int i = 0; i < itemSelectedList.size(); i++)
            if(itemSelectedList.get(i).equal(item))
                return true;
        return false;
    }

    public void startCamera(View view){
        if(!itemSelectedList.isEmpty()) {

            boolean checkImagesOfList = true;
            for(int i = 0; i < itemSelectedList.size(); i++)
                checkImagesOfList = checkImagesOfList && (itemSelectedList.get(i).image != null);

            if(checkImagesOfList) {
                Intent intent = new Intent(this, com.vuforia.samples.VuforiaSamples.app.ImageTargets.ImageTargets.class);

                String[] str = new String[itemSelectedList.size()];
                for (int i = 0; i < str.length; i++)
                    str[i] = itemSelectedList.get(i).image;

                intent.putExtra("com.vuforia.samples.NFCShopper.imageNames", str);
                startActivity(intent);
            }

            else
                Toast.makeText(this, "Please put your phone on NFC reader", Toast.LENGTH_LONG).show();
        }

        else
            Toast.makeText(this, "Please add some items to the list", Toast.LENGTH_LONG).show();
    }

    public NdefMessage createNdefMessage(NfcEvent event) {
        if(itemSelectedList.isEmpty()) {
            Toast.makeText(this, "Please add some items to the list", Toast.LENGTH_LONG).show();
            return null;
        }
        else {
            NdefRecord[] records = new NdefRecord[itemSelectedList.size()];

            for (int i = 0; i < records.length; i++) {
                records[i] = NdefRecord.createMime("text/plain", String.valueOf(itemSelectedList.get(i).id).getBytes(Charset.forName("US-ASCII")));
            }
            NdefMessage msg = new NdefMessage(records);
            return msg;
        }
    }
}
