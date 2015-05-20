package tenerife.rutas.jfernandez.rutasdetenerife;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import com.google.android.gms.maps.model.LatLng;

public class GpxHandler extends DefaultHandler{
	
	private ArrayList<LatLng> coord;
	private ArrayList<Integer> altitud;
	private StringBuffer buffer;
	private boolean eleTag;	//etiqueta elevacion
	private boolean flagTipo;	//true extrae solo coordenadas, false solo altitud
	//private boolean timeTag;
	
	public ArrayList<LatLng> getPath(){
		return coord;
	}
	
	public GpxHandler(boolean tipo){
		flagTipo = tipo;
		if (tipo)	//true para extraer coordenadas
			coord = new ArrayList<LatLng>();
		else{		//false para extraer altura
			buffer = new StringBuffer();
			altitud = new ArrayList<Integer>();
		}
		eleTag = false;
	}
	
	public ArrayList<Integer> getAltitud(){		
		return altitud;
	}
	
	@Override
	public void startDocument (){
	}
	
	@Override
	public void endDocument(){
		//De momento nada
	}
	
	@Override
	public void startElement (String uri, String localName, String qName, Attributes attributes){
		if ((localName.equals("trkpt")) && (flagTipo)) {
            double lat = Double.parseDouble(attributes.getValue("lat"));
            double lon = Double.parseDouble(attributes.getValue("lon"));
            LatLng geopunto = new LatLng(lat, lon);
			coord.add(geopunto);
        }
		if ((localName.contentEquals("ele")) && (!flagTipo)){
			buffer.setLength(0);
			eleTag = true;
		}
		/*if (localName.contentEquals("time"))
			timeTag = true;*/
	}
	
	@Override
	public void endElement (String uri, String localName, String qName){
		if ((localName.contentEquals("ele")) && (!flagTipo)){
			eleTag = false;
			int elevacion = (int)Double.parseDouble(buffer.toString());
			altitud.add(elevacion);
		}
		/*if (localName.contentEquals("time")){
			timeTag = false;}*/
	}
	
	@Override
	public void characters (char[] ch, int start, int length){
		if ((eleTag)&& (!flagTipo)){
			buffer.append(ch, start, length);
			//elevacion = (int)Double.parseDouble(buffer.toString());
			//http://stackoverflow.com/questions/672454/how-to-parse-gpx-files-with-saxreader
		}
	}
}
