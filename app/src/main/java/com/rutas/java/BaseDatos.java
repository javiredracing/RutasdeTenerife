package com.rutas.java;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class BaseDatos extends SQLiteOpenHelper {
	
	private final static String TABLA = "Senderos";
	private final static String BASEDATOS = "BDRutas";
	//private final static String DB_PATH = "/data/data/com.rutas.java/databases/";
	private final static int DB_VERSION = 5;	//cambiar cuando haya una nueva version de la bd
	//private final static String DIRECCION = "ftp://colega:alrasaIN3@ftp.rutasdetenerife.com:20/"+ BASEDATOS;
	
    /*String CREATE_TABLE = 
    "CREATE TABLE IF NOT EXISTS Senderos (" +
    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
    "nombre TEXT NOT NULL, "+
    "inicX REAL NOT NULL, "+
    "inicY REAL NOT NULL, "+
    "finX REAL, "+
    "finY REAL, "+
    "duracion REAL NOT NULL, "+
    "longitud REAL NOT NULL, "+
    "dificultad TEXT NOT NULL, "+
    "descripcion TEXT NOT NULL,
    "kml TEXT,
    "homologado INTEGER",
    "region INTEGER"
    );";*/

	private SQLiteDatabase db;
	private Context contexto;
	
	//Constructor que crea la BD
	public BaseDatos (Context miContext){
		super(miContext, BASEDATOS, null, DB_VERSION);
		contexto = miContext;
		//db = this.getWritableDatabase();
		/*try{
		//Abrimos/creamos la BD		
			db = miContext.openOrCreateDatabase(BASEDATOS, 0, null);
			db.execSQL(CREATE_TABLE);
		}catch (Exception e){
			Log.d("BaseDatos", e.toString());
		}*/
	}



	/**
	 * Copy database file from assets folder inside the apk to the system database path.
	 * @return True if the database have copied successfully or if the database already exists without overwrite, false otherwise.
	 */
	public boolean crearBaseDatos()  {
		File outputFile = contexto.getDatabasePath(BASEDATOS);

		if (outputFile.exists()) {
			//Log.v("Crear DB","exists");
			if (checkVersion(outputFile)){
				//Log.v("Crear DB","version Updated");
				return true;
			}/*else
				Log.v("Crear DB","Override DB!");*/
		}

		outputFile = contexto.getDatabasePath(BASEDATOS);
		outputFile.getParentFile().mkdirs();

		try {
			InputStream inputStream = contexto.getAssets().open(BASEDATOS);
			OutputStream outputStream = new FileOutputStream(outputFile);

			// transfer bytes from the input stream into the output stream
			byte[] buffer = new byte[1024];
			int length;
			while ((length = inputStream.read(buffer)) > 0) {
				outputStream.write(buffer, 0, length);
			}

			// Close the streams
			outputStream.flush();
			outputStream.close();
			inputStream.close();

			outputFile.renameTo(contexto.getDatabasePath(BASEDATOS));

		} catch (IOException e) {
			if (outputFile.exists()) {
				outputFile.delete();
			}
			return false;
		}
		return true;
	}

	private boolean checkVersion(File file){
		SQLiteDatabase checkDB = null;
		boolean isUpdated = true;
		try{
			checkDB = SQLiteDatabase.openDatabase(file.getPath(), null, SQLiteDatabase.OPEN_READONLY);
		}catch(SQLiteException e){
			//database does't exist yet.
			isUpdated = false;
			Log.d("Check database", "database does't exist yet. ");
		}
		if (checkDB != null) {
			if (checkDB.getVersion() < DB_VERSION) {
				//Log.v("Borrar db", "" + db.getVersion());
				isUpdated = false;
			}
			checkDB.close();
		}else
			isUpdated = false;

		return isUpdated;
	}

	/*public void crearBaseDatos() throws IOException{
		if (!comprobarBD()){
			this.getReadableDatabase();
			try{
				importarBD();
			}catch (IOException e){ 
				throw new Error("Error copying database");
			}
			}
		}*/

	public void abrirBD()throws SQLException{
		//String myPath = DB_PATH + BASEDATOS;
		//Log.v("PATH",myPath);
		File f = contexto.getDatabasePath(BASEDATOS);
    	db = SQLiteDatabase.openDatabase(f.getPath(), null, SQLiteDatabase.OPEN_READONLY);
	}
	
	/*public void writeBD() throws SQLException{
		String myPath = DB_PATH + BASEDATOS;
		db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
	}*/
	
	/*private boolean comprobarBD(){
		SQLiteDatabase checkDB = null;
		String myPath = DB_PATH + BASEDATOS;
		boolean dbExist = false;
    	try{
    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    	}catch(SQLiteException e){
    		//database does't exist yet.
			Log.d("Check database", "database does't exist yet. ");
        }
    	if (checkDB != null){
    		if (checkDB.getVersion() < DB_VERSION){
    			//Log.v("Borrar db", "" + db.getVersion());
	    		checkDB.close();
	    		contexto.deleteDatabase(myPath);
    		}else{
    			checkDB.close();
    			dbExist = true;
    		}    			
    	}    	
    	return dbExist;
	}*/
	//Importa la BD de la direccion dada y se copia en la ruta determinada 
	/*private void importarBD() throws IOException{
		
		InputStream myInput;
		
		/*try{
			URL direccion = new URL(DIRECCION);
			URLConnection conexion = direccion.openConnection();
			myInput = new BufferedInputStream (conexion.getInputStream());
		}catch (IOException ioe) {
			//
	 		throw new Error("Unable to create database");	
		}*/

		/*myInput = contexto.getAssets().open(BASEDATOS);
		OutputStream myOutput = new FileOutputStream(DB_PATH + BASEDATOS);
		
		//transfer bytes from the inputfile to the outputfile
    	byte[] buffer = new byte[1024];
    	int length;
    	while ((length = myInput.read(buffer)) > 0){
    		myOutput.write(buffer, 0, length);
    	}
 
    	//Close the streams
    	myOutput.flush();
    	myOutput.close();
    	myInput.close();

	}*/
	//A�ade un nuevo sendero
	/*public long addSendero(String nombre, double inicX, double inicY, double finX, double finY, float duracion,
			float longitud, String dificultad, String descripcion, String kml){
		
		ContentValues cv = new ContentValues();
		cv.put("nombre", nombre);
		cv.put("inicX", inicX);
		cv.put("inicY", inicY);
		cv.put("finX", finX);
		cv.put("finY", finY);
		cv.put("duracion", duracion);
		cv.put("longitud", longitud);
		cv.put("dificultad", dificultad);
		cv.put("descripcion", descripcion);
		cv.put("kml", kml);
		return db.insert("Senderos", null, cv);
	}*/
	
	//Extrae los datos segun el campo
	/*public String[] getCampo(String valor){
		
		String consulta = "SELECT " + valor + " FROM " + TABLA;
		Cursor c = null;
		try{
			c = db.rawQuery(consulta, null);
		}catch(Exception e){
			Log.d("BaseDatos", e.toString());
		}
		String[] listaDatos = new String[c.getCount()];
		int i = 0;
		while(c.moveToNext()){
			listaDatos[i] = c.getString(0);
			i++;
		}
		c.close();
		return listaDatos;
	}*/
	
	/*public Cursor miConsulta (String where){
		Cursor c = null;
		String consulta = "SELECT nombre, inicX, inicY, finX, finY, duracion, longitud, dificultad, kml FROM " + TABLA;
		if (where != null)
			consulta += where;
		try{
			c = db.rawQuery(consulta, null);
		}catch(Exception e){
			Log.d("BaseDatos", e.toString());
		}
		return c;
	}*/
	
	public Cursor getInfoMap(Boolean distinct, String[] columns, String selection, String[] selectionArgs, String groupBy,
				String having, String orderBy){
		Cursor c = null;
		try{
				//c = db.query(TABLA, columns, selection, selectionArgs, groupBy, having, orderBy);
				c= db.query(distinct, TABLA, columns, selection, selectionArgs, groupBy, having, orderBy, null);
			}catch (Exception e){
				Log.d("BaseDatos/getDatos", e.toString());
			}
			return c;
		}
				
	//Obtiene la fila con la informacion necesaria para ser mostrada
	/*public String[] getInformacion(String nombreSendero){
		Cursor c = null;

		String where = "nombre = '" + nombreSendero + "'";
		String consulta = "SELECT nombre, descripcion, duracion, longitud, dificultad, kml, inicX, inicY FROM Senderos WHERE "+ where;
		try{
			c = db.rawQuery(consulta, null);
		}catch(Exception e){
			Log.d("BaseDatos.getInfo", e.toString());
		}
		String[] listaDatos = new String[c.getColumnCount()];
		//Escogemos solo los valores de la primera fila, los demas seran repetidos
		if (c.moveToFirst()){
			int valor = c.getColumnCount();
			for (int i = 0; i < valor; i++)
				listaDatos[i] = c.getString(i);
		}
		c.close();
		return listaDatos;
	}*/

	public String getDescriptionById(int id, String languaje){
		Cursor c = null;
		//TODO determine languaje
		String where = "id = '" + id + "'";
		String consulta = "SELECT es FROM description WHERE "+ where;
		try{
			c = db.rawQuery(consulta, null);
		}catch(Exception e){
			Log.d("BaseDatos.getInfo", e.toString());
		}
		String[] listaDatos = new String[c.getColumnCount()];
		//Escogemos solo los valores de la primera fila, los demas seran repetidos
		if (c.moveToFirst()){
			//int valor = c.getColumnCount();
			listaDatos[0] = c.getString(0);
		}
		c.close();
		return listaDatos[0];
	}
	public String getTrackNameById(int id){
		Cursor c = null;
		//TODO determine languaje
		String where = "id = '" + id + "'";
		String consulta = "SELECT kml FROM Senderos WHERE "+ where;
		try{
			c = db.rawQuery(consulta, null);
		}catch(Exception e){
			Log.d("BaseDatos.getInfo", e.toString());
		}
		String[] listaDatos = new String[c.getColumnCount()];
		//Escogemos solo los valores de la primera fila, los demas seran repetidos
		if (c.moveToFirst()){
			//int valor = c.getColumnCount();
			listaDatos[0] = c.getString(0);
		}
		c.close();
		return listaDatos[0];
	}

	
	/*public String[] getDatos(Boolean distinct, String[] columns, String selection, String[] selectionArgs, String groupBy,
			String having, String orderBy){
		Cursor c = null;
		try{
			//c = db.query(TABLA, columns, selection, selectionArgs, groupBy, having, orderBy);
			c= db.query(distinct, TABLA, columns, selection, selectionArgs, groupBy, having, orderBy, null);
		}catch (Exception e){
			Log.d("BaseDatos/getDatos", e.toString());
		}
		String[] listaDatos = new String[c.getCount()];
		int i = 0;
		while(c.moveToNext()){
			listaDatos[i] = c.getString(0);
			i++;
		}
		c.close();
		return listaDatos;
	}*/
	
	 @Override
     public synchronized void close(){
		if (db != null)
			db.close();
		super.close();
	}
	
	/*public String returnPath(){
		return db.getPath();
	}*/
	
	public int getVersion(){
		return db.getVersion();
		
	}
	
	public void setVersion(int version){
		db.setVersion(version);
	}
	
	/*public Date getDate(){
		File archivo = new File(DB_PATH + BASEDATOS);
		Date lastModDate = null;
		if (archivo.exists())
			lastModDate = new Date(archivo.lastModified());
		return lastModDate;
	}*/
	/*public boolean borraBaseDatos(){
		return contexto.deleteDatabase(BASEDATOS);
	}*/

	@Override
	public void onCreate(SQLiteDatabase arg0) {
	/*	try{
		importarBD();
		}catch (IOException ioe) {
			throw new Error("Unable to create database");
		}*/
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
}