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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by julian_dev on 2/18/2018.
 */

public class PrintActivity extends AppCompatActivity {
    TSCActivity TscDll = new TSCActivity();
    private TextView nameEditText;
    private TextView initialTextView;
    public TextView fmId;
    public TextView fmName;
    public TextView fmCedula;
    public LinearLayout productList;
    private Intent intent;
    static final int REQUEST_ENABLE_BT = 1;
    private String mac;
    BluetoothAdapter bluetoothAdapter;
    public File root;
    public File root2;
    public File docto;
    public Date c = Calendar.getInstance().getTime();
    public String idRecibo;
    public TreeMap<String, Double> quantitys = new TreeMap<String, Double>();
    public TreeMap<String, String> measure = new TreeMap<String, String>();
    public TreeMap<String, Double> cantidades = new TreeMap<String, Double>();
    public TreeMap<String, Integer> canastillas = new TreeMap<String,Integer>();
    public TreeMap<String,Integer> canastillasProducto = new TreeMap<String,Integer>();
    public Map<String,String> productsByMeasure = new LinkedHashMap<String,String>();
    public String canastillasPrestamo;
    public String canastillasVacias;
    public String insumo = null;
    public String entregaInsumo;
    public String motivoNoEntrega;
    public String NAcopiador;

    @SuppressLint({"WrongViewCast", "ResourceAsColor"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);
        productList = (LinearLayout) findViewById(R.id.listProducts);

        fmId = (TextView) findViewById(R.id.producer);
        fmName = (TextView) findViewById(R.id.producerName);
        fmCedula = (TextView) findViewById(R.id.producer2);

        nameEditText = (TextView) findViewById(R.id.name);
        initialTextView = (TextView) findViewById(R.id.initial);
        Button update_button = (Button) findViewById(R.id.update_button);

        intent = getIntent();
        String nameExtra = intent.getStringExtra(MainActivity.EXTRA_NAME);
        String initialExtra = intent.getStringExtra(MainActivity.EXTRA_INITIAL);
        int colorExtra = intent.getIntExtra(MainActivity.EXTRA_COLOR, 0);

        //nameEditText.setText(nameExtra);
        //nameEditText.setSelection(nameEditText.getText().length());

        initialTextView.setBackgroundColor(colorExtra);

        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    // update initialTextView
                    initialTextView.setText("");
                } else if (s.length() >= 1) {
                    // initialTextView set to first letter of nameEditText and update name stringExtra
                    initialTextView.setText(String.valueOf(s.charAt(0)));
                    intent.putExtra(MainActivity.EXTRA_UPDATE, true);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        //setting up the bluethooth adapter and check the status
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        CheckBlueToothState();

        String folder = nameExtra.replace(".xml","");

        //getting SDcard root path
        root = new File(Environment.getExternalStorageDirectory().toString()+"/odk/instances/"+folder);
        root2 = new File(Environment.getDataDirectory().toString()+"/odk/instances/"+folder);
        if(root.exists()){
            docto = new File(root+"/"+nameExtra);
        }else{
            docto = new File(root2+"/"+nameExtra);
        }
        canastillas.put("canastillas",0);
        //parsing xml to get products
        try {
            FileInputStream is = new FileInputStream(docto);

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);

            Element element=doc.getDocumentElement();
            element.normalize();

            //get the farmer id and name

            String farmer = doc.getElementsByTagName("cInternopr").item(0).getTextContent();
            String farmerCedula = doc.getElementsByTagName("idProductor").item(0).getTextContent();
            String farmerName = doc.getElementsByTagName("nProductor").item(0).getTextContent();
            NAcopiador = doc.getElementsByTagName("nAcopiador").item(0).getTextContent();

            if(doc.getElementsByTagName("Insumos").getLength()>0){
                insumo = doc.getElementsByTagName("Insumos").item(0).getTextContent();
            }
            if(doc.getElementsByTagName("EntregaInsumos").getLength()>0){
                entregaInsumo = doc.getElementsByTagName("EntregaInsumos").item(0).getTextContent();
            }
            if(doc.getElementsByTagName("motivoNoEntregaInsumos").getLength()>0){
                motivoNoEntrega = doc.getElementsByTagName("motivoNoEntregaInsumos").item(0).getTextContent();
            }

            canastillasPrestamo = doc.getElementsByTagName("cantidadCanastillasPrestamo").item(0).getTextContent();
            canastillasVacias = doc.getElementsByTagName("cantidadCanastillasVacias").item(0).getTextContent();
            idRecibo = doc.getElementsByTagName("instanceID").item(0).getTextContent();
            fmId.setText(farmer.toString());
            fmName.setText(farmerName.toString());
            fmCedula.setText(farmerCedula.toString());

            initialTextView.setText(farmerName);

            //get the products information
            NodeList nList = doc.getElementsByTagName("repeat_productos");

            for (int i=0; i<nList.getLength(); i++) {
                int canastilla = 0;

                Node node = nList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element2 = (Element) node;
                    String producto = getValue ("nProducto", element2);
                    String unidad = getValue ("unidad", element2);
                    Double quantity = Double.valueOf(getValue("peso_total_producto_destare", element2));
                    Log.d("check","Exist: "+element2.getElementsByTagName("cantidadCanastillaProducto").getLength());
                    if( element2.getElementsByTagName("cantidadCanastillaProducto").getLength() > 0){
                        canastilla = Integer.valueOf(getValue("cantidadCanastillaProducto", element2));
                        Log.d("check","canastillas"+canastilla);
                        canastillas.put("canastillas",canastillas.get("canastillas")+canastilla);

                        if (canastillasProducto.containsKey(producto)){
                            canastillasProducto.put(producto, canastillasProducto.get(producto)+canastilla);
                        }else{
                            canastillasProducto.put(producto,canastilla);
                        }
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
                }
            }

            //dinamically add textViews for each product existing in the map
            Set set0 = measure.entrySet();
            Iterator iterator0 = set0.iterator();
            while(iterator0.hasNext()){
                Map.Entry me0 = (Map.Entry)iterator0.next();
                Log.d("check measure","measure "+me0.getValue());
                if (me0.getValue().equals("Kilos")){
                    productsByMeasure.put((String)me0.getKey(),(String)me0.getValue());
                }
            }

            Set set01 = measure.entrySet();
            Iterator iterator01 = set01.iterator();
            while(iterator01.hasNext()){
                Map.Entry me0 = (Map.Entry)iterator01.next();
                if (me0.getValue().equals("Racimos")){
                    productsByMeasure.put((String)me0.getKey(),(String)me0.getValue());
                }
            }

            Set set02 = measure.entrySet();
            Iterator iterator02 = set02.iterator();
            while(iterator02.hasNext()){
                Map.Entry me02 = (Map.Entry)iterator02.next();
                if (me02.getValue().equals("Unidads")){
                    productsByMeasure.put((String)me02.getKey(),(String)me02.getValue());
                }
            }

            Log.d("check productos","producto "+productsByMeasure.size());

            Set set = productsByMeasure.entrySet();
            Iterator iterator = set.iterator();
            while(iterator.hasNext()) {
                Map.Entry me = (Map.Entry)iterator.next();
                TextView tv = new TextView(this);
                TextView t2 = new TextView(this);
                tv.setText((CharSequence) me.getKey());
                tv.setTextColor(Color.parseColor("#000000"));
                tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
                if (canastillasProducto.get(me.getKey())!=null){
                    t2.setText(measure.get(me.getKey())+": "+String.format( "%.2f", quantitys.get(me.getKey()) )+" / Canastillas: "+ canastillasProducto.get(me.getKey()));
                }else{
                    t2.setText(measure.get(me.getKey())+": "+String.format( "%.2f", quantitys.get(me.getKey())));
                }
                t2.setTextColor(Color.parseColor("#000000"));
                t2.setPadding(0,0,0,3);
                productList.addView(tv);
                productList.addView(t2);
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
            TextView canas = new TextView(this);
            canas.setText("Total Canastillas Con producto: "+canastillas.get("canastillas"));
            canas.setTextColor(Color.parseColor("#000000"));
            productList.addView(canas);
            TextView canas2 = new TextView(this);
            canas2.setText("Canastillas en prestamo: "+canastillasPrestamo);
            canas2.setTextColor(Color.parseColor("#000000"));
            productList.addView(canas2);
            TextView canas3 = new TextView(this);
            canas3.setText("Canastillas vacias: "+canastillasVacias);
            canas3.setTextColor(Color.parseColor("#000000"));
            productList.addView(canas3);

        }
        catch (Exception e) {e.printStackTrace();
        }
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
            if (insumo!= null && insumo.length()>1){
                TscDll.setup(70, 100, 4, 4, 0, 0, 0);
            }else{
                TscDll.setup(70, 95, 4, 4, 0, 0, 0);
            }
        }else if (quantitys.size() == 2){
            if (insumo!= null && insumo.length()>1) {
                TscDll.setup(70, 117, 4, 4, 0, 0, 0);
            }else{
                TscDll.setup(70, 95, 4, 4, 0, 0, 0);
            }
        }else if (quantitys.size() == 3){
            if (insumo!= null && insumo.length()>1) {
                TscDll.setup(70, 122, 4, 4, 0, 0, 0);
            }else{
                TscDll.setup(70, 100, 4, 4, 0, 0, 0);
            }
        }else if (quantitys.size() == 4){
            if (insumo!= null && insumo.length()>1) {
                TscDll.setup(70, 117, 4, 4, 0, 0, 0);
            }else{
                TscDll.setup(70, 102, 4, 4, 0, 0, 0);
            }
        }else if (quantitys.size() == 5){
            if (insumo!= null && insumo.length()>1) {
                TscDll.setup(70, 122, 4, 4, 0, 0, 0);
            }else{
                TscDll.setup(70, 102, 4, 4, 0, 0, 0);
            }
        }else if (quantitys.size() == 6){
            if (insumo!= null && insumo.length()>1) {
                TscDll.setup(70, 127, 4, 4, 0, 0, 0);
            }else{
                TscDll.setup(70, 107, 4, 4, 0, 0, 0);
            }
        }else if (quantitys.size() == 7){
            if (insumo!= null && insumo.length()>1) {
                TscDll.setup(70, 132, 4, 4, 0, 0, 0);
            }else{
                TscDll.setup(70, 112, 4, 4, 0, 0, 0);
            }
        }else if (quantitys.size() == 8){
            if (insumo!= null && insumo.length()>1) {
                TscDll.setup(70, 137, 4, 4, 0, 0, 0);
            }else{
                TscDll.setup(70, 117, 4, 4, 0, 0, 0);
            }
        }else if (quantitys.size() == 9){
            if (insumo!= null && insumo.length()>1) {
                TscDll.setup(70, 142, 4, 4, 0, 0, 0);
            }else{
                TscDll.setup(70, 122, 4, 4, 0, 0, 0);
            }
        }else if (quantitys.size() == 10){
            if (insumo!= null && insumo.length()>1) {
                TscDll.setup(70, 147, 4, 4, 0, 0, 0);
            }else{
                TscDll.setup(70, 127, 4, 4, 0, 0, 0);
            }
        }else if (quantitys.size() == 11){
            if (insumo!= null && insumo.length()>1) {
                TscDll.setup(70, 152, 4, 4, 0, 0, 0);
            }else{
                TscDll.setup(70, 132, 4, 4, 0, 0, 0);
            }
        }else if (quantitys.size() == 12){
            if (insumo!= null && insumo.length()>1) {
                TscDll.setup(70, 157, 4, 4, 0, 0, 0);
            }else{
                TscDll.setup(70, 137, 4, 4, 0, 0, 0);
            }
        }else if (quantitys.size() == 13){
            if (insumo!= null && insumo.length()>1) {
                TscDll.setup(70, 162, 4, 4, 0, 0, 0);
            }else{
                TscDll.setup(70, 142, 4, 4, 0, 0, 0);
            }
        }else if (quantitys.size() == 14){
            if (insumo!= null && insumo.length()>1) {
                TscDll.setup(70, 167, 4, 4, 0, 0, 0);
            }else{
                TscDll.setup(70, 147, 4, 4, 0, 0, 0);
            }
        }else if (quantitys.size() == 15){
            if (insumo!= null && insumo.length()>1) {
                TscDll.setup(70, 172, 4, 4, 0, 0, 0);
            }else{
                TscDll.setup(70, 152, 4, 4, 0, 0, 0);
            }
        }else if (quantitys.size() == 16){
            if (insumo!= null && insumo.length()>1) {
                TscDll.setup(70, 177, 4, 4, 0, 0, 0);
            }else{
                TscDll.setup(70, 157, 4, 4, 0, 0, 0);
            }
        }else if (quantitys.size() == 17){
            if (insumo!= null && insumo.length()>1) {
                TscDll.setup(70, 182, 4, 4, 0, 0, 0);
            }else{
                TscDll.setup(70, 162, 4, 4, 0, 0, 0);
            }
        }else if (quantitys.size() == 18){
            if (insumo!= null && insumo.length()>1) {
                TscDll.setup(70, 187, 4, 4, 0, 0, 0);
            }else{
                TscDll.setup(70, 167, 4, 4, 0, 0, 0);
            }
        }else if (quantitys.size() == 19){
            if (insumo!= null && insumo.length()>1) {
                TscDll.setup(70, 192, 4, 4, 0, 0, 0);
            }else{
                TscDll.setup(70, 172, 4, 4, 0, 0, 0);
            }
        }else if (quantitys.size() == 20){
            if (insumo!= null && insumo.length()>1) {
                TscDll.setup(70, 197, 4, 4, 0, 0, 0);
            }else{
                TscDll.setup(70, 177, 4, 4, 0, 0, 0);
            }
        }else if (quantitys.size() > 20){
            if (insumo!= null && insumo.length()>1) {
                TscDll.setup(70, 202, 4, 4, 0, 0, 0);
            }else{
                TscDll.setup(70, 177, 4, 4, 0, 0, 0);
            }
        }else{
            TscDll.setup(70, 55, 4, 4, 0, 0, 0);
        }

        TscDll.clearbuffer();
        TscDll.sendcommand("SET TEAR ON\n");
        TscDll.sendcommand("CODEPAGE UTF-8\n");
        TscDll.sendcommand("SET COUNTER @1 1\n");
        TscDll.sendcommand("@1 = \"0001\"\n");
        TscDll.printerfont(210, 40, "3", 0, 1, 1, "Comsab");
        TscDll.printerfont(83, 70, "1", 0, 1, 1, "Cooperativa Agromultiactiva San Bartolo");
        TscDll.printerfont(135, 95, "3", 0, 1, 1, "NIT 800.229.735-1");
        TscDll.printerfont(80, 130, "2", 0, 1, 1, "Recibo de mercancia: "+idRecibo.substring(idRecibo.length()-12));
        TscDll.printerfont(120, 160, "3", 0, 1, 1, "Fecha: "+formattedDate);
        TscDll.printerfont(0, 200, "3", 0, 1, 1, "Nombre productor:");
        TscDll.printerfont(0, 230, "3", 0, 1, 1, fmName.getText().toString());
        TscDll.printerfont(0, 260, "3", 0, 1, 1, "Código del productor:"+" "+fmId.getText().toString());
        TscDll.printerfont(0, 290, "3", 0, 1, 1, "Cédula del productor:"+" "+fmCedula.getText().toString());
        TscDll.printerfont(0, 320, "3", 0, 1, 1, "Acopiador:"+" "+NAcopiador);

        TscDll.printerfont(0, 350, "3", 0, 1, 1, "PRODUCTO");
        TscDll.printerfont(310, 350, "3", 0, 1, 1, "CANTIDAD");

        int counter = 1;
        int h1 = 350;
        Set set = productsByMeasure.entrySet();
        Iterator iterator = set.iterator();
        while(iterator.hasNext()&& counter <= quantitys.size()) {
            counter = counter + 1;
            h1 = h1+30;
            Map.Entry me = (Map.Entry)iterator.next();
            String pdto = (String) me.getKey();
            String pdto2;
            String unit = measure.get(me.getKey());
            //check the length of the product name and restrict to 25 characters
            if(pdto.length()>23){
                pdto2 = pdto.substring(0,22);
            }else{
                pdto2 = pdto;
            }
            
            //check the unit of measure name and put the simbol
            if(measure.get(me.getKey()).equals("Kilos")){
                unit = "Kg";
            }else if(measure.get(me.getKey()).equals("Racimos")){
                unit = "Ra";
            }
            
            TscDll.printerfont(0, h1, "2", 0, 1,1, pdto2);
            TscDll.printerfont(300, h1, "2", 0, 1,1, " "+unit+":"+String.format( "%.2f", quantitys.get(me.getKey()) ));
        }

        Set set2 = cantidades.entrySet();
        Iterator iterator2 = set2.iterator();
        while(iterator2.hasNext()){
            h1 = h1+30;
            Map.Entry cant = (Map.Entry)iterator2.next();
            TscDll.printerfont(20, h1, "2", 0, 1, 1,  "Total "+(String) cant.getKey()+": "+String.format( "%.2f",cant.getValue()));
        }
        TscDll.printerfont(20, h1+40, "2", 0, 1, 1, "Total Canastillas con producto: "+canastillas.get("canastillas"));
        TscDll.printerfont(20, h1+70, "2", 0, 1, 1, "Canastillas en prestamo: "+canastillasPrestamo);
        TscDll.printerfont(20, h1+100, "2", 0, 1, 1, "Canastillas vacias: "+canastillasVacias);
        //print the inputs part if exit
        if (insumo!= null && insumo.length()>1){
            TscDll.printerfont(210, h1+130, "2", 0, 1, 1, "INSUMOS");
            TscDll.printerfont(0, h1+165, "2", 0, 1, 1, insumo);
            if(entregaInsumo.equals("1")){
                TscDll.printerfont(0, h1+195, "2", 0, 1, 1, "Insumo entregado: Sí");
                TscDll.printerfont(130, h1+220, "2", 0, 1, 1, "PEDIDO DE CANASTILLAS");
                TscDll.printerfont(0, h1+250, "2", 0, 1, 1, "Teléfonos: 8415713 opción 0 - 3108358832");
                TscDll.printerfont(53, h1+280, "2", 0, 1, 1, "Teléfono y WhatsApp: 3113101143");
                TscDll.printerfont(45, h1+310, "3", 0, 1, 1, "SOLICITARLAS EL DÍA ANTERIOR");
                TscDll.printerfont(130, h1+340, "3", 0, 1, 1, "A LA RECOLECCIÓN");
            }else{
                TscDll.printerfont(0, h1+195, "2", 0, 1, 1, "Insumo entregado: No");
                if(motivoNoEntrega.equals("1")){
                    TscDll.printerfont(0, h1+225, "2", 0, 1, 1, "MOTIVO:");
                    TscDll.printerfont(0, h1+255, "2", 0, 1, 1, "El productor no se encontraba presente");
                    TscDll.printerfont(130, h1+290, "2", 0, 1, 1, "PEDIDO DE CANASTILLAS");
                    TscDll.printerfont(0, h1+320, "2", 0, 1, 1, "Teléfonos: 8415713 opción 0 - 3108358832");
                    TscDll.printerfont(53, h1+350, "2", 0, 1, 1, "Teléfono y WhatsApp: 3113101143");
                    TscDll.printerfont(45, h1+380, "3", 0, 1, 1, "SOLICITARLAS EL DÍA ANTERIOR");
                    TscDll.printerfont(130, h1+410, "3", 0, 1, 1, "A LA RECOLECCIÓN");
                }
                if(motivoNoEntrega.equals("2")){
                    TscDll.printerfont(0, h1+225, "2", 0, 1, 1, "MOTIVO:");
                    TscDll.printerfont(0, h1+255, "2", 0, 1, 1, "No se cuenta con los insumos");
                    TscDll.printerfont(130, h1+290, "2", 0, 1, 1, "PEDIDO DE CANASTILLAS");
                    TscDll.printerfont(0, h1+320, "2", 0, 1, 1, "Teléfonos: 8415713 opción 0 - 3108358832");
                    TscDll.printerfont(53, h1+350, "2", 0, 1, 1, "Teléfono y WhatsApp: 3113101143");
                    TscDll.printerfont(45, h1+380, "3", 0, 1, 1, "SOLICITARLAS EL DÍA ANTERIOR");
                    TscDll.printerfont(130, h1+410, "3", 0, 1, 1, "A LA RECOLECCIÓN");
                }
                if(motivoNoEntrega.equals("3")){
                    TscDll.printerfont(0, h1+225, "2", 0, 1, 1, "MOTIVO:");
                    TscDll.printerfont(0, h1+255, "2", 0, 1, 1, "Error en el despacho");
                    TscDll.printerfont(130, h1+290, "2", 0, 1, 1, "PEDIDO DE CANASTILLAS");
                    TscDll.printerfont(0, h1+320, "2", 0, 1, 1, "Teléfonos: 8415713 opción 0 - 3108358832");
                    TscDll.printerfont(53, h1+350, "2", 0, 1, 1, "Teléfono y WhatsApp: 3113101143");
                    TscDll.printerfont(45, h1+380, "3", 0, 1, 1, "SOLICITARLAS EL DÍA ANTERIOR");
                    TscDll.printerfont(130, h1+410, "3", 0, 1, 1, "A LA RECOLECCIÓN");
                }
            }
        }else{
            TscDll.printerfont(130, h1+135, "2", 0, 1, 1, "PEDIDO DE CANASTILLAS");
            TscDll.printerfont(0, h1+165, "2", 0, 1, 1, "Teléfonos: 8415713 opción 0 - 3108358832");
            TscDll.printerfont(53, h1+195, "2", 0, 1, 1, "Teléfono y WhatsApp: 3113101143");
            TscDll.printerfont(45, h1+225, "3", 0, 1, 1, "SOLICITARLAS EL DÍA ANTERIOR");
            TscDll.printerfont(130, h1+255, "3", 0, 1, 1, "A LA RECOLECCIÓN");
        }
        //TscDll.sendcommand("BLOCK 80,875,400,100, \"0\",0,8,8,\"We stand behind our products with one of the most comprehensive support programs in the Auto-ID industry.\"\n");
        TscDll.printlabel(1, 1);
        //TscDll.sendcommand("PRINT 1\n");
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
