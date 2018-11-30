package com.comsab;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tscdll.TSCActivity;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by julian_dev on 2/18/2018.
 */

public class PrintActivityTotal extends AppCompatActivity {
    TSCActivity TscDll = new TSCActivity();
    public LinearLayout productList;
    static final int REQUEST_ENABLE_BT = 1;
    private String mac;
    BluetoothAdapter bluetoothAdapter;
    private File root;
    private File root2;
    public File docto;
    private ArrayList<File> fileList = new ArrayList<File>();
    private ArrayList<Card> cardlist = new ArrayList<Card>();
    public Date c = Calendar.getInstance().getTime();
    public String idRecibo;
    public TreeMap<String, Double> quantitys = new TreeMap<String, Double>();
    public TreeMap<String, String> measure = new TreeMap<String, String>();
    public TreeMap<String, Double> cantidades = new TreeMap<String, Double>();
    public TreeMap<String, Integer> canastillas = new TreeMap<String, Integer>();
    public String NAcopiador;
    public Integer totalCanastillas;

    @SuppressLint({"WrongViewCast", "ResourceAsColor"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.total_print);
        productList = (LinearLayout) findViewById(R.id.listProducts);

        Button update_button = (Button) findViewById(R.id.update_button);

        //setting up the bluethooth adapter and check the status
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        CheckBlueToothState();

        totalCanastillas = 0;

        //getting SDcard root path
        root = new File(Environment.getExternalStorageDirectory().toString()+"/odk/instances");
        root2 = new File(Environment.getDataDirectory().toString()+"/odk/instances");

        if(root.exists()){
            getfile(root);
        }else{
            getfile(root2);
        }
        for (int i = 0; i < fileList.size(); i++) {
            String folder = fileList.get(i).getName().replace(".xml","");
            root = new File(Environment.getExternalStorageDirectory().toString()+"/odk/instances/"+folder);
            root2 = new File(Environment.getDataDirectory().toString()+"/odk/instances/"+folder);
            if(root.exists()){
                docto = new File(root+"/"+fileList.get(i).getName());
            }else{
                docto = new File(root2+"/"+fileList.get(i).getName());
            }
            //parsing xml to get products
            try {
                FileInputStream is = new FileInputStream(docto);

                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(is);


                Element element=doc.getDocumentElement();
                element.normalize();


                //get the products information
                NodeList nList = doc.getElementsByTagName("repeat_productos");

                for (int j=0; j<nList.getLength(); j++) {
                    int canastilla = 0;

                    Node node = nList.item(j);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element2 = (Element) node;
                        String producto = getValue ("nProducto", element2);
                        String unidad = getValue ("unidad", element2);
                        Double quantity = Double.valueOf(getValue("peso_total_producto_destare", element2));
                        if( element2.getElementsByTagName("cantidadCanastillaProducto").getLength() > 0){
                            canastilla = Integer.valueOf(getValue("cantidadCanastillaProducto", element2));
                        }else{
                            canastilla = 0;
                        }

                        if (quantitys.containsKey(producto)){
                            quantitys.put(producto, quantitys.get(producto)+quantity);
                        }else{
                            quantitys.put(producto,quantity);
                        }

                        if (measure.containsKey(producto)){

                        }else{
                            measure.put(producto,unidad);
                        }
                        if (cantidades.containsKey(unidad)){
                            cantidades.put(unidad,cantidades.get(unidad)+quantity);
                        }else{
                            cantidades.put(unidad,quantity);
                        }

                        if (canastillas.containsKey(producto)){
                            canastillas.put(producto, canastillas.get(producto)+canastilla);
                        }else{
                            canastillas.put(producto,canastilla);
                        }
                    }
                }
            }
            catch (Exception e) {e.printStackTrace();
            }
        }

        //dinamically add textViews for each product existing in the map

        Set set = quantitys.entrySet();
        Iterator iterator = set.iterator();
        while(iterator.hasNext()) {
            Map.Entry me = (Map.Entry)iterator.next();
            TextView tv = new TextView(this);
            TextView t2 = new TextView(this);
            TextView t3 = new TextView(this);
            tv.setText((CharSequence) me.getKey());
            tv.setTextColor(Color.parseColor("#000000"));
            tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
            t2.setText(measure.get(me.getKey())+": "+String.format( "%.2f", me.getValue() ));
            t2.setTextColor(Color.parseColor("#000000"));
            t2.setPadding(0,0,0,3);
            productList.addView(tv);
            productList.addView(t2);
            if(measure.get(me.getKey()).equals("Kilos") ){
                t3.setText("Canastillas: "+canastillas.get(me.getKey()).toString() );
                t3.setTextColor(Color.parseColor("#000000"));
                t3.setPadding(0,0,0,3);
                productList.addView(t3);
            }


        }

        Set set2 = cantidades.entrySet();
        Iterator iterator2 = set2.iterator();
        while(iterator2.hasNext()){
            Map.Entry cant = (Map.Entry)iterator2.next();
            TextView c = new TextView(this);
            c.setText("Total "+(CharSequence) cant.getKey()+": "+String.format( "%.2f",cant.getValue()));
            c.setTextColor(Color.parseColor("#000000"));
            productList.addView(c);
        }

        Set set3 = canastillas.entrySet();
        Iterator iterator3 = set3.iterator();
        while(iterator3.hasNext()){
            Map.Entry cant2 = (Map.Entry)iterator3.next();
            totalCanastillas = totalCanastillas +(Integer) cant2.getValue();
        }

        TextView d = new TextView(this);
        d.setText("Total Canastillas: "+totalCanastillas);
        d.setTextColor(Color.parseColor("#000000"));
        productList.addView(d);

    }
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            super.onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    // print method
    public void print(View view){
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(c);
        Toast.makeText(this, "imprimiendo", Toast.LENGTH_LONG).show();
        TscDll.openport(mac);
        if (quantitys.size() == 1){
            TscDll.setup(70, 78, 4, 4, 0, 0, 0);
        }else if (quantitys.size() == 2){
            TscDll.setup(70, 80, 4, 4, 0, 0, 0);
        }else if (quantitys.size() == 3){
            TscDll.setup(70, 86, 4, 4, 0, 0, 0);
        }else if (quantitys.size() == 4){
            TscDll.setup(70, 94, 4, 4, 0, 0, 0);
        }else if (quantitys.size() == 5){
            TscDll.setup(70, 102, 4, 4, 0, 0, 0);
        }else if (quantitys.size() == 6){
            TscDll.setup(70, 110, 4, 4, 0, 0, 0);
        }else if (quantitys.size() == 7){
            TscDll.setup(70, 118, 4, 4, 0, 0, 0);
        }else if (quantitys.size() == 8){
            TscDll.setup(70, 126, 4, 4, 0, 0, 0);
        }else if (quantitys.size() == 9){
            TscDll.setup(70, 134, 4, 4, 0, 0, 0);
        }else if (quantitys.size() == 10){
            TscDll.setup(70, 142, 4, 4, 0, 0, 0);
        }else if (quantitys.size() == 11){
            TscDll.setup(70, 150, 4, 4, 0, 0, 0);
        }else if (quantitys.size() == 12){
            TscDll.setup(70, 158, 4, 4, 0, 0, 0);
        }else if (quantitys.size() == 13){
            TscDll.setup(70, 164, 4, 4, 0, 0, 0);
        }else if (quantitys.size() == 14){
            TscDll.setup(70, 172, 4, 4, 0, 0, 0);
        }else if (quantitys.size() == 15){
            TscDll.setup(70, 180, 4, 4, 0, 0, 0);
        }else if (quantitys.size() == 16){
            TscDll.setup(70, 188, 4, 4, 0, 0, 0);
        }else if (quantitys.size() == 17){
            TscDll.setup(70, 196, 4, 4, 0, 0, 0);
        }else if (quantitys.size() == 18){
            TscDll.setup(70, 204, 4, 4, 0, 0, 0);
        }else if (quantitys.size() == 19){
            TscDll.setup(70, 212, 4, 4, 0, 0, 0);
        }else if (quantitys.size() == 20){
            TscDll.setup(70, 220, 4, 4, 0, 0, 0);
        }else if (quantitys.size() > 20){
            TscDll.setup(70, 300, 4, 4, 0, 0, 0);
        }else{
            TscDll.setup(70, 50, 4, 4, 0, 0, 0);
        }

        TscDll.clearbuffer();
        TscDll.sendcommand("SET TEAR ON\n");
        TscDll.sendcommand("CODEPAGE UTF-8\n");
        TscDll.sendcommand("SET COUNTER @1 1\n");
        TscDll.sendcommand("@1 = \"0001\"\n");
        TscDll.printerfont(210, 40, "3", 0, 1, 1, "Comsab");
        TscDll.printerfont(83, 70, "1", 0, 1, 1, "Cooperativa Agromultiactiva San Bartolo");
        TscDll.printerfont(135, 95, "3", 0, 1, 1, "NIT 800.229.735-1");
        TscDll.printerfont(80, 130, "2", 0, 1, 1, "Total productos recolectados");
        TscDll.printerfont(120, 160, "3", 0, 1, 1, "Fecha: "+formattedDate);

        int counter = 1;
        int h1 = 160;
        int h2 = 185;
        Set set = quantitys.entrySet();
        Iterator iterator = set.iterator();
        while(iterator.hasNext()&& counter <= quantitys.size()) {
            counter = counter + 1;
            h1 = h1+55;
            h2 = h2+55;
            Map.Entry me = (Map.Entry)iterator.next();
            TscDll.printerfont(70, h1, "2", 0, 1, 1, (String) me.getKey());
            TscDll.printerfont(70, h2, "2", 0, 1, 1, measure.get(me.getKey())+": "+String.format( "%.2f", me.getValue() ));
            TscDll.printerfont(300, h2, "2", 0, 1, 1, "Can: "+canastillas.get(me.getKey()));
        }

        Set set2 = cantidades.entrySet();
        Iterator iterator2 = set2.iterator();
        while(iterator2.hasNext()){
            h2 = h2+30;
            Map.Entry cant = (Map.Entry)iterator2.next();
            TscDll.printerfont(80, h2, "2", 0, 1, 1,  "Total "+(String) cant.getKey()+": "+String.format( "%.2f",cant.getValue()));
        }
        TscDll.printerfont(80, h2+25, "2", 0, 1, 1, "Total Canastillas: "+totalCanastillas);
        TscDll.printlabel(1, 1);
        TscDll.closeport(700);
    }

    private static String getValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = nodeList.item(0);
        return node.getNodeValue();
    }

    private String obtenerTexto(Node dato)
    {
        StringBuilder texto = new StringBuilder();
        NodeList fragmentos = dato.getChildNodes();

        for (int k=0;k<fragmentos.getLength();k++)
        {
            texto.append(fragmentos.item(k).getNodeValue());
        }

        return texto.toString();
    }

}
