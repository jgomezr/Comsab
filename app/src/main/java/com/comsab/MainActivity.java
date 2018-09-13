package com.comsab;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MainActivity extends AppCompatActivity {
    private static final String DEBUG_TAG = "AppCompatActivity";
    public static final String EXTRA_UPDATE = "update";
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_COLOR = "color";
    public static final String EXTRA_INITIAL = "initial";
    public static final String TRANSITION_INITIAL = "initial_transition";
    public static final String TRANSITION_NAME = "name_transition";
    private File root;
    private File root2;
    public File docto;
    private ArrayList<File> fileList = new ArrayList<File>();
    private ArrayList<Card> cardlist = new ArrayList<Card>();
    private int[] colors;
    private String[] names;
    private RelativeLayout view;
    private RecyclerView recyclerView;
    private MaterialAdapter adapter;
    static final int REQUEST_ENABLE_BT = 1;
    private String mac;
    public FloatingActionButton fab;
    BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = (RelativeLayout) findViewById(R.id.views);
        colors = getResources().getIntArray(R.array.initial_colors);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        //getting SDcard root path
        root = new File(Environment.getExternalStorageDirectory().toString()+"/odk/instances");
        root2 = new File(Environment.getDataDirectory().toString()+"/odk/instances");

        if(root.exists()){
            getfile(root);
        }else{
            getfile(root2);
        }

        if(adapter == null){
            adapter = new MaterialAdapter(this, cardlist);
        }

        recyclerView = (RecyclerView) findViewById(R.id.vista);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        initCards();

        //setting up the bluethooth adapter and check the status
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        CheckBlueToothState();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setType("vnd.android.cursor.dir/vnd.odk.form");
                Uri webpage = Uri.parse("https://play.google.com/store/apps/details?id=org.odk.collect.android&hl=es");
                Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
                if (intent != null) {
                    startActivity(intent);
                }else{
                    startActivity(webIntent);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.recoleccion:
                Intent total =new Intent(this,PrintActivityTotal.class);
                startActivity(total);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(DEBUG_TAG, "requestCode is " + requestCode);
        // if adapter.getItemCount() is request code, that means we are adding a new position
        // anything less than adapter.getItemCount() means we are editing a particular position
        if (requestCode == adapter.getItemCount()) {
            if (resultCode == RESULT_OK) {
                // Make sure the Add request was successful
                // if add name, insert name in list
                CheckBlueToothState();
                String name = data.getStringExtra(EXTRA_NAME);
                String location = data.getStringExtra(EXTRA_NAME);
                int color = data.getIntExtra(EXTRA_COLOR, 0);
                adapter.addCard(name, location, color);
            }
        } else {
            // Anything other than adapter.getItemCount() means editing a particular list item
            // the requestCode is the list item position
                CheckBlueToothState();
                // Make sure the Update request was successful
                RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(0);
        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        recreate();
    }

    //public void doSmoothScroll(int position) {
       // recyclerView.smoothScrollToPosition(position);
    //}

    //method to get the list of XML files
    public ArrayList<File> getfile(File dir) {
        File listFile[] = dir.listFiles();
        if (listFile != null && listFile.length > 0) {
            for (int i = 0; i < listFile.length; i++) {

                if (listFile[i].isDirectory()) {
                    getfile(listFile[i]);
                } else {
                    if (listFile[i].getName().endsWith(".xml"))
                    {
                        fileList.add(listFile[i]);
                    }
                }
            }
        }
        return fileList;
    }

    //method to check the status of the bluetooth
    private void CheckBlueToothState(){
        if (bluetoothAdapter == null){
            Toast.makeText(this, "Bluetooth no soportado", Toast.LENGTH_SHORT).show();
        }else{
            if (bluetoothAdapter.isEnabled()){
                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                if (pairedDevices.size() > 0) {
                    // There are paired devices. Get the name and address of each paired device.
                    for (BluetoothDevice device : pairedDevices) {
                        String deviceName = device.getName();
                        String deviceHardwareAddress = device.getAddress(); // MAC address
                        mac=device.getAddress();
                    }
                }else{
                    Toast.makeText(this, "no hay dispositivos", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(this, "Bluetooth no está habilitado!", Toast.LENGTH_SHORT).show();
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    private void initCards(){
        Log.d(DEBUG_TAG,"number of cards"+ fileList.size());
        for (int i = fileList.size()-1 ; i >= 0; i--) {

            String folder = fileList.get(i).getName().replace(".xml","");
            root = new File(Environment.getExternalStorageDirectory().toString()+"/odk/instances/"+folder);
            root2 = new File(Environment.getDataDirectory().toString()+"/odk/instances/"+folder);
            if(root.exists()){
                docto = new File(root+"/"+fileList.get(i).getName());
            }else{
                docto = new File(root2+"/"+fileList.get(i).getName());
            }
            try {
                FileInputStream is = new FileInputStream(docto);

                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(is);

                Element element=doc.getDocumentElement();
                element.normalize();
                String formName=doc.getElementsByTagName("instanceName").item(0).getTextContent();
                String formInitials=formName.substring(0,4);
                //get the form name
                if(!formInitials.startsWith("can_") && formName.length()>0){
                    Card card = new Card();
                    card.setId((long) i);
                    card.setName(doc.getElementsByTagName("instanceName").item(0).getTextContent());
                    card.setLocation(fileList.get(i).getName());
                    card.setColorResource(colors[i]);
                    Log.d(DEBUG_TAG,"Card created with id"+ card.getId()+ ", name " + card.getName() + ", color " + card.getColorResource());
                    cardlist.add(card);
                }
            }
            catch (Exception e) {e.printStackTrace();
            }
        }
    }
}
