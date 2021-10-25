package internal;

import java.awt.AWTException;
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
import java.util.Calendar;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 * @author: Adrian Marin Alcala Desc: Controlador de la imagen y reproduccion de
 * video. El mainframe se limpiara de codigo y se pondra en esta clase
 */
public class ScreenshotController implements ActionListener, Runnable {

    JFrame Master;
    JLabel imageContainer;
    JButton recordBtn;

    private Robot robot;
    static int recordingSpeed = 10; //Estatico para modificarlo dinamicamente

    String directory, date;
    SimpleDateFormat formatter;
    BufferedImage screenShot = null;
    File folder;
    boolean isRecording = false;
    Image replay[];
    private static Thread th = null;
    ImageIcon replayImage;

    public ScreenshotController(JFrame root, JLabel destinyLabel,
            JButton actionBtn) {
        this.Master = root;
        this.imageContainer = destinyLabel;
        this.recordBtn = actionBtn;
        initRobot();

        formatter = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss-SSS");
        directory = System.getProperty("user.dir")
                + "\\src\\internal\\Screenshots\\";
        folder = new File(directory);

        if (th == null) {
            th = new Thread((Runnable) this);
            th.start();
        }
        System.out.println("Constructor");
    }

    static {
        System.out.println("Instancia");
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

    public void takeScreenshot() throws IOException {
        date = formatter.format(Calendar.getInstance().getTime());

        screenShot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        //ImageIO.write(screenShot, "JPG", new File(directory + date + ".jpg"));

        //[Aqui]
        ImageWriter writer = null;
        ImageOutputStream ios = null;
        File file = new File(directory + date + ".jpg");
        java.util.Iterator iter = ImageIO.getImageWritersByFormatName("jpg");

        if( iter.hasNext() )
        {
            writer = (ImageWriter)iter.next();
        }

        try {
            System.out.println("Accediendo a " + directory + date);
            ios = ImageIO.createImageOutputStream(file);
            if(ios == null){
                System.err.println("\nIOS nulO");
            }
            writer.setOutput(ios);
            ImageWriteParam param = new JPEGImageWriteParam( java.util.Locale.getDefault() );
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT) ;
            param.setCompressionQuality(0.98f);

            if(screenShot != null){
               writer.write(null, new IIOImage(screenShot, null, null ), param); 
            }else{
                System.err.println("\n\nImagen nula: ");
            }
        }
        catch(IOException e){
            
        }
        finally {
            if (ios != null) {
                try {
                    ios.flush();
                }
                catch (IOException e) {
                    System.err.println("\n\nOcurrió un error al hacer flush: "
                            + e.getMessage());
                }
                try {
                    ios.close();
                }
                catch (IOException e) {
                    System.err.println("\n\nOcurrió un error al cerrar: "
                            + e.getMessage());
                }
            }
            if (writer != null) {
                writer.dispose();
            }
        }//[/Aqui]
        
        System.out.println("Imagen tomada correctamente \"" + date + ".jpg\"");
        System.out.format("Se encontraron %d imagenes\n", folder.listFiles().length);        
    }

    private void createVideo() {
        System.out.format("Se encontraron %d imagenes\n", folder.listFiles().length);
        videoTransform vT = new videoTransform(directory);
        System.out.println("Cuadros por segundo configurados en "
                + recordingSpeed);
        vT.createVideo(recordingSpeed);
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
                System.out.format("\nMostrando la imagen %d de la cola", i);
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
        
        //while(true){
            try {
                takeScreenshot();
            } catch (IOException ex) {
                System.err.println("\n\nOcurrió un error en la funcion: "
                        + ex.getMessage());
            }
        //}

    }

    @Override
    public void run() {
        //[./]Funciona bien
        while (true) {
            //System.out.print("");
            //System.out.println("RUN");//Por alguna razon es necesario
            if (isRecording) {
                System.out.println("Llamar a funcion");
                //showVideo();                               
            }
        }
    }
}
