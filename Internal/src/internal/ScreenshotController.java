package internal;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.media.MediaLocator;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 * @author: Adrian Marin Alcala Desc: Controlador de la imagen y reproduccion de
 * video. El mainframe se limpiara de codigo y se pondra en esta clase
 */
public class ScreenshotController implements ActionListener  {

    JFrame Master;
    JLabel imageContainer;
    JButton recordBtn;

    private Robot robot;
    static int recordingSpeed = 60;

    String directory, date;
    int bufferPosition = 0;
    String screenshotTimeStamp[] = new String[1];
    SimpleDateFormat formatter;
    BufferedImage screenShot[] = new BufferedImage[1];
    Thread saveThread = null;
    Thread showThread = null;
    File folder;
    boolean isRecording = false;
    Image replay[];
    ImageIcon replayImage;
    static JpegImagesToMovie imageToMovie = new JpegImagesToMovie();

    public ScreenshotController(JFrame root, JLabel srcLbl, JButton actionBtn) {
        this.Master = root;
        this.imageContainer = srcLbl;
        this.recordBtn = actionBtn;
        initRobot();

        formatter = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss-SSS");
        directory = System.getProperty("user.dir")
                + "\\src\\internal\\Screenshots\\";
        folder = new File(directory);

        initThreads();
    }

    private void initRobot() {
        try {
            robot = new Robot();
        } catch (AWTException ex) {
            JOptionPane.showMessageDialog(null,
                    "Ocurrio un error:" + ex.getMessage(),
                    "Error al iniciar Robot", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void initThreads(){
        if (saveThread == null) {
            saveThread = new Thread(() -> {
                while(isRecording){
                    try {
                        takeScreenshot();
                    } catch (IOException ex) {
                        Logger.getLogger(ScreenshotController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                createVideo();
            });
        }
        if(showThread == null){
            showThread = new Thread(() -> {
                while(isRecording){
                    showVideo();
                }
            });            
        }
    }
    
    public void takeScreenshot() throws IOException {
        date = formatter.format(Calendar.getInstance().getTime());
        
        screenshotTimeStamp[bufferPosition] = directory + date + ".jpg";
        screenShot[bufferPosition] = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        screenShot = Arrays.copyOf(screenShot, screenShot.length + 1);
        screenshotTimeStamp = Arrays.copyOf(screenshotTimeStamp, screenshotTimeStamp.length + 1);
        bufferPosition++;
        
        System.out.println("Imagen tomada correctamente \"" + date + ".jpg\"");     
    }

    private void createVideo() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = (int) screenSize.getWidth();
        int screenHeight = (int) screenSize.getHeight();
        
        formatter = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
        String outputFile = formatter.format(Calendar.getInstance().getTime()) + ".mp4";
        
        Vector<String> imgLst = new Vector<String>();  

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
        imageToMovie.doIt(screenWidth, screenHeight, 10, imgLst, oml);
    }

    private void retriveVideo() {
        //[./]Funciona Bien
        Vector<String> imgLst = new Vector<String>();
        File[] listOfFiles = folder.listFiles();
        for (File listOfFile : listOfFiles) {
            imgLst.add(listOfFile.getAbsolutePath());
        }
        Image url;
        replay = new Image[imgLst.size()];
        for (int i = 0; i < imgLst.size(); i++) {
            System.out.println("Procesando: " + imgLst.get(i));
            url = Toolkit.getDefaultToolkit().createImage(imgLst.get(i));
            if (url != null) {
                replay[i] = url.getScaledInstance(imageContainer.getWidth(),
                        imageContainer.getHeight(), Image.SCALE_SMOOTH);
            }
        }
    }

    private void showVideo() {
        retriveVideo();
        if (replay != null && replay.length != 0) {
            for (int i = 0; i < replay.length; i++) {
                if (replay[i] != null && isRecording) {
                    replayImage = new ImageIcon(replay[i]);
                    imageContainer.setIcon(replayImage);
                } else {
                    i -= 1;
                }
                try {
                    Thread.sleep(1000 / 60);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            isRecording = false;
            recordBtn.setText("Iniciar Grabación");
        }
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        recordBtn.setText((isRecording ? "Iniciar Grabación" : "Detener Grabación"));
        isRecording = !isRecording;
        //showVideo();
        //createVideo();
        System.out.println("Inicia");
        Instant startTime = Instant.now();
        while(Duration.between(startTime, Instant.now()).getSeconds() < 1){
            try {
                takeScreenshot();
            } catch (IOException ex) {
                Logger.getLogger(ScreenshotController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("\n\nSe guardaran " + bufferPosition + " imagenes nuevas");
        bufferPosition = 0;

        for(int i = 0; i < screenShot.length - 1; i++){
            try {
                if(screenShot[i] == null){
                    System.err.println("Error en la imagen " + i);
                }
                if(screenshotTimeStamp[i] == null){
                    System.err.println("Error en el nombre " + i);
                }                
                ImageIO.write(screenShot[i], "JPG", new File(screenshotTimeStamp[i]));
                bufferPosition++;
                System.out.println("Guardado correcto: " + screenshotTimeStamp[i]);
            } catch (IOException ex) {
                Logger.getLogger(ScreenshotController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        screenshotTimeStamp = new String[1];
        screenShot = new BufferedImage[1];
        bufferPosition = 0;
        System.err.println("Terminado en " + Duration.between(startTime, Instant.now()).getSeconds() + " segundos");
    }

}
