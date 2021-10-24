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
    
    String directory,
            date;
    SimpleDateFormat formatter;
    BufferedImage screenShot = null;
    File folder;
    boolean isRecording = false;
    Image replay[];
    private static Thread th = null;
    ImageIcon replayImage;

    public ScreenshotController(JFrame root, JLabel destinationLabel,
            JButton actionBtn) {
        this.Master = root;
        this.imageContainer = destinationLabel;
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
        System.out.println("Imagen tomada correctamente \"" + date + ".jpg\"");
        //System.out.format("Se encontraron %d imagenes\n", folder.listFiles().length);
    }

    private void createVideo() {
        System.out.format("Se encontraron %d imagenes\n", folder.listFiles().length);
        videoTransform vT = new videoTransform(directory);
        System.out.println("Cuadros por segundo configurados en "
                + recordingSpeed);
        vT.createVideo(recordingSpeed);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        Master.setTitle("Nuevo titulo");
        System.err.println("Funcion 2 ejecutada correctamente");

        recordBtn.setText((isRecording ? "Iniciar Grabación" : "Detener Grabación"));
        isRecording = !isRecording;
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

    @Override
    public void run() {
        //[./]Funciona bien
        while (true) {
            System.out.print("");
            System.out.println("RUN");//Por alguna razon es necesario
            if (isRecording == true) {
                if (replay.length != 0) {
                    for (int i = 0; i < replay.length; i++) {
                        System.out.format("\nMostrando la imagen %d de la cola", i);
                        if (replay[i] != null && isRecording) {
                            replayImage = new ImageIcon(replay[i]);
                            imageContainer.setIcon(replayImage);
                        }
                        try {
                            Thread.sleep(1000 / 60);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                //break;
            }
        }
    }
}
