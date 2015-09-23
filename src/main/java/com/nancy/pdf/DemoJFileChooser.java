package com.nancy.pdf;

/**
 * Created by nan.zhang on 9/22/15.
 */

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.Properties;


public class DemoJFileChooser extends JPanel implements ActionListener {
    private JButton button;
    private JFileChooser fileChooser;
    private JTextArea titleArea;
    private JTextArea textArea;
    private String fileChooserTitle = "Open file";
    private String previousFilePath;
    private static String propFileName = "options.prop";

    public DemoJFileChooser(JButton button, JTextArea titleArea, JTextArea textArea) {
        this.button = button;
        this.button.addActionListener(this);
        this.titleArea = titleArea;
        this.textArea = textArea;
    }

    public void actionPerformed(ActionEvent e) {
        fileChooser = new JFileChooser();
        if (null != previousFilePath) {
            fileChooser.setCurrentDirectory(new File(previousFilePath));
        } else {
            fileChooser.setCurrentDirectory(new java.io.File("."));
        }

        fileChooser.setDialogTitle(fileChooserTitle);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setFileFilter(new FileNameExtensionFilter("PDF", "pdf"));
        //
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            System.out.println("getCurrentDirectory(): "
                    + fileChooser.getCurrentDirectory());
            System.out.println("getSelectedFile() : "
                    + fileChooser.getSelectedFile());
            // store file path
            previousFilePath = fileChooser.getSelectedFile().getPath();
            // call pdf extractor
            try {
                File selectedFile = fileChooser.getSelectedFile();
                ExtractPageContent.parsePdf(selectedFile.getAbsolutePath(), "/tmp/med.txt");
                String resultFile = "/tmp/" + selectedFile.getName().replace("pdf", "txt");
                String formatText = ExtractPageContent.formatText("/tmp/med.txt", resultFile);
//                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(sb.toString()), null);
                titleArea.setText(selectedFile.getName().substring(0, selectedFile.getName().length() - 4));
                textArea.setText(formatText);
                textArea.setVisible(true);

            } catch (IOException exception) {
                exception.printStackTrace();
            }
        } else {
            System.out.println("No Selection ");
        }
    }

    public String getPreviousFilePath() {
        return previousFilePath;
    }

    public void setPreviousFilePath(String previousFilePath) {
        this.previousFilePath = previousFilePath;
    }

    public Dimension getPreferredSize() {
        return new Dimension(200, 200);
    }

    /**
     * Store location & size of UI
     */
    public static void storeOptions(Frame f, DemoJFileChooser fileChooser) throws Exception {
        File file = new File(propFileName);
        Properties p = new Properties();
        // restore the frame from 'full screen' first!
        f.setExtendedState(Frame.NORMAL);
        Rectangle r = f.getBounds();
        int x = (int) r.getX();
        int y = (int) r.getY();
        int w = (int) r.getWidth();
        int h = (int) r.getHeight();

        p.setProperty("x", "" + x);
        p.setProperty("y", "" + y);
        p.setProperty("w", "" + w);
        p.setProperty("h", "" + h);
        p.setProperty("path", fileChooser.getPreviousFilePath());

        BufferedWriter br = new BufferedWriter(new FileWriter(file));
        p.store(br, "Properties of the user frame");
    }

    /**
     * Restore location & size of UI
     */
    public static void restoreOptions(Frame f, DemoJFileChooser fileChooser) throws IOException {
        File file = new File(propFileName);
        Properties p = new Properties();
        BufferedReader br = new BufferedReader(new FileReader(file));
        p.load(br);

        int x = Integer.parseInt(p.getProperty("x"));
        int y = Integer.parseInt(p.getProperty("y"));
        int w = Integer.parseInt(p.getProperty("w"));
        int h = Integer.parseInt(p.getProperty("h"));

        Rectangle r = new Rectangle(x, y, w, h);

        f.setBounds(r);
        fileChooser.setPreviousFilePath(p.getProperty("path"));
    }

    public static void main(String args[]) {
        createAndShowGUI();
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI() {

        //Create and set up the window.
        JFrame frame = new JFrame("PDF Convertor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Set up the content pane.
        addComponentToPane(frame);
        //Use the content pane's default BorderLayout. No need for
        //setLayout(new BorderLayout());
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    private static void addComponentToPane(final JFrame frame) {
        Container pane = frame.getContentPane();
        final JPanel textPanel = new JPanel(new BorderLayout());
        final JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        final JTextArea textArea = new JTextArea();
        JButton button = new JButton("choose file");

        textArea.setPreferredSize(new Dimension(500, 600));
        textArea.setLineWrap(true);

        final JTextArea titleArea = new JTextArea();
        titleArea.setPreferredSize(new Dimension(500, 30));

        final DemoJFileChooser panel = new DemoJFileChooser(button, titleArea, textArea);
//        frame.getContentPane().add(panel, "North");
//        pane.add(panel, BorderLayout.PAGE_START);
        pane.add(button, BorderLayout.PAGE_START);

        JPanel titlePane = new JPanel();
        titlePane.add(new JScrollPane(titleArea));
//        pane.add(titlePane, BorderLayout.EAST);
        pane.add(titleArea, BorderLayout.PAGE_END);
        textPanel.add(new JScrollPane(textArea));
        centerPanel.add(textArea);
        textPanel.add(new JScrollPane(centerPanel));
//        textPanel.add(centerPanel.add(new JScrollPane(textArea)));

//        frame.getContentPane().add(textArea, "Center");

//        pane.add(textPanel, BorderLayout.CENTER);
        pane.add(textPanel, BorderLayout.CENTER);
//        centerPanel.add(textPanel);
//        pane.add(centerPanel, BorderLayout.CENTER);

//        frame.setSize(panel.getPreferredSize());

        frame.addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        try {
                            storeOptions(frame, panel);
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                        System.exit(0);
                    }
                }
        );

        File optionsFile = new File(propFileName);
        if (optionsFile.exists()) {
            try {
                restoreOptions(frame, panel);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        } else {
            frame.setLocationByPlatform(true);
        }
    }
}