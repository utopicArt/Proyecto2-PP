package internal;
import java.io.File;
import java.util.Vector;
import javax.media.MediaLocator;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class videoTransform {
    static JpegImagesToMovie imageToMovie = new JpegImagesToMovie();
    SimpleDateFormat formatter;
    String outputFile,
           directory;
    int screenWidth,
        screenHeight;    
    
    public videoTransform(String path){
        this.directory = path;
        
        formatter = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
        outputFile = formatter.format(Calendar.getInstance().getTime()) + ".mp4";
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.screenWidth = (int) screenSize.getWidth();
        this.screenHeight =(int) screenSize.getHeight();
    }

    public void createVideo(int fps) {

        Vector<String> imgLst = new Vector<String>();  
        //se leen todas las imagenes de la direccion dada y se colocan en el Vector imgLst
        File folder = new File( directory );
        System.out.println("Obteniendo imagenes de: " + this.directory);
        File[] listOfFiles = folder.listFiles();
        System.out.println( "Imagenes encontradas: " + listOfFiles.length );
        
        for (File listOfFile : listOfFiles) {
            imgLst.add(listOfFile.getAbsolutePath());
        }
        imgLst.forEach((name) -> {
            System.out.println("Procesando: " + name);
        });
        
        MediaLocator oml;
        if ((oml = imageToMovie.createMediaLocator( outputFile )) == null)
        {
            System.err.println("No se puede construir media locator de: " + outputFile);
            System.exit(0);
        }

        //Ancho, alto, FPS, lista de imagenes, MediaLocator
        imageToMovie.doIt(screenWidth, screenHeight, fps, imgLst, oml);
    }

}
