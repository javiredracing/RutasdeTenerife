package tenerife.rutas.jfernandez.rutasdetenerife;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by jfernandez on 09/06/2015.
 */
public class FragmentChart extends Fragment {
    private XYPlot plot;
    private Handler handlerGraf;
    private String xmlPath;
    private float dist = 0;
    private View v;
    private boolean isLoaded;
    private TextView tvDesnivelAcum, tvDesnivelDown;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getArguments();
        dist = b.getFloat(getString(R.string.VALUE_DIST), 0);
        xmlPath = b.getString(getString(R.string.VALUE_XML_ROUTE), "");
        isLoaded = false;
        handlerGraf = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                //super.handleMessage(msg);
                if (msg.obj != null){
                    final ArrayList<Integer> serieY = (ArrayList<Integer>) msg.obj;
                    //double valor = 0.0;
                    dist = dist / (float)serieY.size();
                    ArrayList<Double> serieX = new ArrayList<Double>();
                    double sum = 0;
                    int tam = serieY.size();
                    double max = 0;
                    double min = 3900;

                    int netAlt = 0;
                    int netDown = 0;
                    for (int i = 0; i < tam;i++){
                        double altitude = serieY.get(i);
                        if (altitude > max){
                            max = altitude;
                        }
                        if (altitude < min){
                            min = altitude;
                        }

                        /*Calculate cumulate elevation*/
                        if (i > 0){
                            int oldAlt = serieY.get(i-1);
                            int newAlt = serieY.get(i);
                            int diff = newAlt - oldAlt;
                            netAlt = netAlt + Math.max(0,diff);
                            netDown = netDown - Math.min(0,diff);
                        }
                        /**/
                        serieX.add(sum);
                        sum += dist;
                    }
                    /**/
                    tvDesnivelAcum.setText(""+netAlt +" m");
                    tvDesnivelDown.setText(""+Math.abs(netDown) +" m");
                    double upperRangeBoundary = 1500;
                    double lowRangeBoundary = 500;
                    if (max > upperRangeBoundary){
                        upperRangeBoundary = max + 400;
                    }
                    if (min < lowRangeBoundary){
                        lowRangeBoundary = 0;
                    }
                    XYSeries serie = new SimpleXYSeries(serieX, serieY,"Desnivel");
                    LineAndPointFormatter series1Format = new LineAndPointFormatter(
                            Color.rgb(0, 200, 0),                   // line color
                            null, // point color
                            Color.argb(150, 150, 190, 150),     //fill color
                            null);  //point text color
                    series1Format.getLinePaint().setStrokeWidth(2);
                    series1Format.getLinePaint().setStrokeJoin(Paint.Join.ROUND);

                    plot.addSeries(serie, series1Format);
                    plot.setRangeBoundaries(lowRangeBoundary, upperRangeBoundary, BoundaryMode.FIXED);
                    plot.setTicksPerRangeLabel(1);
                    plot.setTicksPerDomainLabel(3);
                    plot.setRangeLabel("altitud");
                    plot.setDomainLabel("distancia");
                    plot.getGraphWidget().setDomainLabelOrientation(-45);

                    plot.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);
                    plot.getGraphWidget().getBackgroundPaint().setColor(Color.TRANSPARENT);
                    plot.getGraphWidget().getDomainOriginLinePaint().setColor(Color.BLACK);
                    plot.getGraphWidget().getRangeOriginLinePaint().setColor(Color.BLACK);
                    plot.getGraphWidget().getDomainOriginLabelPaint().setColor(Color.WHITE);
                    plot.getBackgroundPaint().setColor(Color.TRANSPARENT);
                    plot.getBorderPaint().setColor(getResources().getColor(R.color.lightGreen));
                    plot.getBorderPaint().setAlpha(255);
                    plot.getBorderPaint().setStrokeWidth(4);
                    plot.getTitleWidget().getLabelPaint().setColor(Color.WHITE);
                    plot.getLegendWidget().getTextPaint().setColor(Color.WHITE);
                    plot.getRangeLabelWidget().getLabelPaint().setColor(Color.WHITE);
                    isLoaded = true;
                }
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (v == null){
            //Log.v("OnCreateView fragment", xmlPath);
            v = inflater.inflate(R.layout.info_chart, container, false);

            View field = v.findViewById(R.id.fieldAcum);
            tvDesnivelAcum = (TextView)field.findViewById(R.id.view_value);
            TextView tvTimeTitle = (TextView)field.findViewById(R.id.view_title);
            tvTimeTitle.setText("Acumulado");
            ImageView tvTimeIcon = (ImageView)field.findViewById(R.id.view_image);
            tvTimeIcon.setImageResource(R.drawable.statistics);

            field = v.findViewById(R.id.fieldMax);
            TextView tvDownTitle = (TextView)field.findViewById(R.id.view_title);
            tvDownTitle.setText("Acumulado");
            tvDesnivelDown = (TextView) field.findViewById(R.id.view_value);
            ImageView tvDownIcon = (ImageView)field.findViewById(R.id.view_image);
            tvDownIcon.setImageResource(R.drawable.down);
            plot = (XYPlot) v.findViewById(R.id.mySimpleXYPlot);
        }
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!xmlPath.contentEquals("") && (!isLoaded)){
            Thread myThread = new Thread(new Runnable(){
                public void run(){

                    SAXParserFactory spf = SAXParserFactory.newInstance();
                    try {
                        SAXParser sp = spf.newSAXParser();
                        XMLReader xr = sp.getXMLReader();
                        Message mensaje = new Message();

                        InputStream myInput;
                        /*if (!isInAssets(miXML)){
                            //File f = new File(Environment.getExternalStorageDirectory()+"/rutas/"+ xmlPath);
                            File f = new File("/data/data/com.rutas.java/files/", miXML);
                            myInput = new FileInputStream(f);
                        }
                        else*/
                        myInput = getActivity().getApplicationContext().getAssets().open(xmlPath);

                        InputSource archivo = new InputSource(myInput);
                        archivo.setEncoding("UTF-8");

                        String filenameArray[] = xmlPath.split("\\.");
                        String extension = filenameArray[filenameArray.length-1];

                        if (extension.toLowerCase().contentEquals("kml")){
                            KmlHandler kml = new KmlHandler();
                            xr.setContentHandler(kml);
                            xr.parse(archivo);
                            mensaje.obj = kml.getAltitud();
                        }else{
                            GpxHandler gpx = new GpxHandler(false);
                            xr.setContentHandler(gpx);
                            xr.parse(archivo);
                            mensaje.obj = gpx.getAltitud();
                        }
                        handlerGraf.sendMessage(mensaje);
                        myInput.close();
                    } catch (ParserConfigurationException e) {
                        Log.d("Parser ", e.toString());
                        e.printStackTrace();
                    } catch (SAXException e) {
                        Log.d("SAX ", e.toString());
                        e.printStackTrace();
                    } catch (IOException e) {
                        Log.d("InputStream ", e.toString());
                        e.printStackTrace();
                    }
                }});
            myThread.start();
        }
    }
}
