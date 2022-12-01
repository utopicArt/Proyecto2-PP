package internal;

import java.awt.AWTException;
import java.awt.Color;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;
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
 * @author: 
 * Desc: Controlador de la imagen y reproduccion devideo.
 */
public class ScreenshotController implements ActionListener {

    JFrame Master;
    JLabel imageContainer;
    JButton triggerBtn;
    JButton controllerBtn;

    private Robot robot;
    static int recordingSpeed = 60;

    String directory, date;
    SimpleDateFormat formatter;
    static LinkedList<BufferedImage> screenShot = new LinkedList<>();
    static Queue<BufferedImage> screenShot2Save = new LinkedList<>();
    static Queue<Image> replay = new LinkedList<>();
    static Queue<String> screenshotTimeStamp = new LinkedList<>();
    Thread executorThread = null;
    Thread showThread = null;
    Thread saveThread = null;
    File folder;
    Rectangle rec;
    static boolean isRecording = false;
    ImageIcon replayImage;
    static JpegImagesToMovie imageToMovie = new JpegImagesToMovie();

    public ScreenshotController(JFrame root, JLabel srcLbl, JButton actionBtn,
            JButton trigger) {
        this.Master = root;
        this.imageContainer = srcLbl;
        this.controllerBtn = actionBtn;
        this.triggerBtn = trigger;
        initRobot();

        formatter = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss-SSS");
        directory = System.getProperty("user.dir")
                + "\\src\\internal\\Screenshots\\";
        folder = new File(directory);
        if (!folder.isDirectory()) {
            new File(directory).mkdirs();
        }

        initThreads();
    }

    private void initRobot() {
        try {
            robot = new Robot();
        } catch (AWTException ex) {
            JOptionPane.showMessageDialog(null,
                    "Ocurrio un error:" + ex.getMessage(),
                    "Error al iniciar Robot", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initThreads() {
        if (executorThread == null) {
            executorThread = new Thread(() -> {
                while (true) {
                    try {
                        takeScreenshot();
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null,
                            "Ocurrio un error:" + ex.getMessage(),
                            "Error al tomar SS", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }, "screenCapture");
        }
        if (showThread == null) {
            showThread = new Thread(() -> {
                while (true) {
                    showVideo();
                }
            }, "realTimeCapture");
        }
        if(saveThread == null){
            saveThread = new Thread(() -> {
                saveScreenshots();
            });
        }
    }

    public void takeScreenshot() throws IOException {
        System.out.print("");
        while (isRecording) {
            date = formatter.format(Calendar.getInstance().getTime());
            screenshotTimeStamp.add(directory + date + ".jpg");
            screenShot.add(robot.createScreenCapture(
                    new Rectangle(Toolkit.getDefaultToolkit().getScreenSize())));
            screenShot2Save.add(screenShot.getLast());
            System.out.println("Imagen tomada correctamente \"" + date + ".jpg\"");
        }
    }

    private void saveScreenshots() {
        for (int i = 0; i < screenShot.size() - 1; i++) {
            try {
                if (screenShot2Save.peek() == null || screenshotTimeStamp.peek() == null) {
                    System.err.println("Ocurrió un error en la imagen " + i);
                    break;
                }
                ImageIO.write(screenShot.poll(), "JPG", 
                        new File(screenshotTimeStamp.poll()));
                System.out.println("Guardado correcto");
            } catch (IOException ex) {
                Logger.getLogger(ScreenshotController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void createVideo() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = (int) screenSize.getWidth();
        int screenHeight = (int) screenSize.getHeight();

        formatter = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
        String outputFile = formatter.format(Calendar.getInstance().getTime()) + ".mp4";

        ArrayList<String> imgLst = new ArrayList<>();

        System.out.println("Obteniendo imagenes de: " + this.directory);
        File[] listOfFiles = folder.listFiles();
        System.out.println("Imagenes encontradas: " + listOfFiles.length);

        for (File listOfFile : listOfFiles) {
            imgLst.add(listOfFile.getAbsolutePath());
        }
        imgLst.forEach((name) -> {
            System.out.println("Procesando: " + name);
        });

        MediaLocator oml;
        if ((oml = JpegImagesToMovie.createMediaLocator(outputFile)) == null) {
            System.err.println("No se puede construir media locator de: " + outputFile);
            System.exit(0);
        }

        imageToMovie.doIt(screenWidth, screenHeight, 10, imgLst, oml);
    }

    private void retriveVideo() {
        //[./]Funciona Bien
        ArrayList<String> imgLst = new ArrayList<>();
        File[] listOfFiles = folder.listFiles();
        for (File listOfFile : listOfFiles) {
            imgLst.add(listOfFile.getAbsolutePath());
        }
        Image url;
        for (int i = 0; i < imgLst.size(); i++) {
            System.out.println("Procesando: " + imgLst.get(i));
            url = Toolkit.getDefaultToolkit().createImage(imgLst.get(i));
            if (url != null) {
                replay.add(url.getScaledInstance(imageContainer.getWidth(),
                        imageContainer.getHeight(), Image.SCALE_SMOOTH));
            }
        }
    }

    public void showVideo() {
        //retriveVideo();
        System.out.print("");
        if (screenShot.peek() != null) {
            try {
                //Esta variable forza al sistema
                replayImage = new ImageIcon(screenShot.poll()
                        .getScaledInstance(imageContainer.getWidth(),
                        imageContainer.getHeight(), Image.SCALE_SMOOTH));
                imageContainer.setIcon(replayImage);
                System.err.println("Imagen mostrada");
                Thread.sleep((long) 16.6666667);
            } catch (InterruptedException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    boolean takingSS = false;

    boolean mainThreadState = false;
    boolean showThreadState = false;
    int statusInfo = 0;
    Instant startTime;
    @Override
    public void actionPerformed(ActionEvent evt) {
        controllerBtn.setText((isRecording ? "Iniciar Grabación" : "Detener Grabación"));
        isRecording = !isRecording;
        if(statusInfo == 0){
            startTime = Instant.now();
        }
        statusInfo++;

        triggerBtn.setEnabled(true);
        triggerBtn.setBackground((isRecording
                ? new Color(230, 20, 20) : new Color(189, 189, 189)));
        triggerBtn.setEnabled(false);
        
        if(statusInfo == 2){
            saveScreenshots();
            createVideo();
            statusInfo = 0;
        }
        
        if (!mainThreadState) {
            executorThread.start();
            mainThreadState = true;
        } else {
            System.out.println("\nEl hilo " + executorThread.getName()
                    + " ya se encuentra activo: " + executorThread.getState());
        }    
        if (!showThreadState) {
            showThread.start();
            showThreadState = true;
        }
    }
}
