package crawler;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;


public class WebCrawler extends JFrame implements Runnable {
    Thread timerThread;
    ThreadWorker worker;
    private int numberOfWorkers = 5;
    private int numberOfInactiveWorkers = 0;
    private ArrayList<ThreadWorker> workers = new ArrayList<>();
    private JTextArea workersTextField;
    private JToggleButton runButton;
    private JLabel parsedPages;
    private JLabel elapsedTime;
    private JTable table;

    // Stores the current minute and second.
    private int currentMinute = 0;
    private int currentSecond = 0;
    private int timeLimitSeconds = 120;
    private int queueEmptySeconds = 0;

    public static int maxDepth = 50;

    // Variable to store the running state of thread.
    public static boolean isRunning = false;

    public WebCrawler() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 600);

        setLayout(null);
        setTitle("Web Crawler");

        Border border = new LineBorder(Color.GRAY);

        JLabel urlLabel = new JLabel("Start URL:");
        urlLabel.setBounds(10, 10, 90, 20);
        add(urlLabel);

        JLabel workersLabel = new JLabel("Workers:");
        workersLabel.setBounds(10, 40, 100, 20);
        add(workersLabel);

        JLabel depthLabel = new JLabel("Maximum depth:");
        depthLabel.setBounds(10, 70, 100, 20);
        add(depthLabel);

        JLabel timeLimitLabel = new JLabel("Time limit:");
        timeLimitLabel.setBounds(10, 100, 100, 20);
        add(timeLimitLabel);

        JLabel secondsLabel = new JLabel("seconds");
        secondsLabel.setBounds(430, 100, 80, 20);
        add(secondsLabel);

        JLabel elapsedTimeLabel = new JLabel("Elapsed time:");
        elapsedTimeLabel.setBounds(10, 130, 100, 20);
        add(elapsedTimeLabel);

        elapsedTime = new JLabel("0:00");
        elapsedTime.setBounds(105, 130, 100, 20);
        add(elapsedTime);

        JLabel parsedPagesLabel = new JLabel("Parsed pages:");
        parsedPagesLabel.setBounds(10, 160, 100, 20);
        parsedPagesLabel.setName("ParsedLabel");
        add(parsedPagesLabel);

        parsedPages = new JLabel("0");
        parsedPages.setBounds(105, 160, 100, 20);
        parsedPages.setName("ParsedPages");
        add(parsedPages);

        JLabel titleLabel = new JLabel("Title: ");
        titleLabel.setBounds(10, 190, 40, 20);
        add(titleLabel);

        JLabel title = new JLabel();
        title.setName("TitleLabel");
        title.setBounds(50, 60, 430, 20);
        add(title);


        JTextArea textArea = new JTextArea();
        textArea.setName("HtmlTextArea");
        textArea.disable();
        textArea.setBounds(10, 60, 570, 520);
        textArea.setVisible(true);
        //add(textArea);

        JTextArea urlTextField = new JTextArea("");
        urlTextField.setName("UrlTextField");
        urlTextField.setBounds(105, 10, 380, 20);
        urlTextField.setBorder(border);
        urlTextField.setVisible(true);
        add(urlTextField);

        workersTextField = new JTextArea("5");
        workersTextField.setName("WorkersTextField");
        workersTextField.setBounds(105, 40, 475, 20);
        workersTextField.setBorder(border);
        workersTextField.setVisible(true);
        add(workersTextField);

        JTextArea depth = new JTextArea("50");
        depth.setName("DepthTextField");
        depth.setBounds(105, 70, 380, 20);
        depth.setBorder(border);
        depth.setVisible(true);
        add(depth);

        JTextArea timeLimit = new JTextArea("120");
        timeLimit.setName("TimeLimitTextField");
        timeLimit.setBounds(105, 100, 320, 20);
        timeLimit.setBorder(border);
        timeLimit.setVisible(true);
        add(timeLimit);

        JCheckBox depthCheckBox = new JCheckBox("Enabled");
        depthCheckBox.setName("DepthCheckBox");
        depthCheckBox.setBounds(510, 70, 80, 20);
        depthCheckBox.setSelected(true);
        add(depthCheckBox);

        JCheckBox timeLimitCheckBox = new JCheckBox("Enabled");
        timeLimitCheckBox.setName("timeLimitCheckBox");
        timeLimitCheckBox.setBounds(510, 100, 80, 20);
        timeLimitCheckBox.setSelected(true);
        add(timeLimitCheckBox);

        table = new JTable(toTableModel(HtmlLinksParserAlgorithm.htmlLinks));
        table.setName("TitlesTable");
        table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        table.setFillsViewportHeight(true);
        table.setEnabled(false);
        table.setVisible(true);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(10, 220, 570, 290);
        add(scrollPane);


        runButton = new JToggleButton("Run");
        runButton.setName("RunButton");
        runButton.setBounds(490, 5, 90, 30);
        runButton.setVisible(true);
        runButton.addActionListener(e -> {

            final String url = urlTextField.getText();
            String siteText = "";

            if (runButton.isSelected()) {
                runButton.setText("Stop");

                isRunning = true;
                timeLimitSeconds = 0;
                currentMinute = 0;
                currentSecond = 0;
                elapsedTime.setText(currentMinute + ":" + currentSecond);
                queueEmptySeconds = 0;

                if (workers.size() == 0) {
                    HtmlLinksParserAlgorithm.queue.offer(url);


                    //rework this try block
                    try {
                        int works = Integer.parseInt(workersTextField.getText());

                        if (works > 0 && works < 11) {
                            numberOfWorkers = works;
                        }
                    } catch (NumberFormatException ex) {
                        System.out.println(Arrays.toString(ex.getStackTrace()));
                    }

                    if (timeLimitCheckBox.isSelected()) {
                        try {
                            int time = Integer.parseInt(timeLimit.getText());

                            if (time > 0) {
                                timeLimitSeconds = time;
                            }
                        } catch (NumberFormatException ex) {
                            System.out.println(ex.getStackTrace());
                        }
                    }

                    if (depthCheckBox.isSelected()) {
                        try {
                            int maximumDepth = Integer.parseInt(depth.getText());

                            if (maximumDepth > 0) {
                                maxDepth = maximumDepth;
                            }
                        } catch (NumberFormatException ex) {
                            System.out.println(ex.getStackTrace());
                        }
                    }

                    for (int i = 0; i < numberOfWorkers; i++) {
                        worker = new ThreadWorker();
                        workers.add(worker);
                        worker.setName("Thread-" + i);
                        worker.start();
                    }
                }

                timerThread = new Thread(this);
                timerThread.start();


//                try {
//                    HtmlLinksParserAlgorithm.queue.offer(url);
//                    HtmlLinksParserAlgorithm.parseAllHtmlLinksInURL(url);
//                    siteText = HtmlLinksParserAlgorithm.getSiteText(url);
//                    textArea.setText(siteText);
//                } catch (IOException ex) {
//                    System.out.println(Arrays.toString(ex.getStackTrace()));
//                }

//                try {
//                    for (ThreadWorker worker: workers) {
//                        worker.join();
//                    }
//                } catch (InterruptedException ie) {
//                    System.out.println(ie.getStackTrace());
//                }

//                title.setText(HtmlLinksParserAlgorithm.getSiteTitle(siteText));


            } else {
                runButton.setText("Run");
                this.isRunning = false;
            }

        });
        add(runButton);


        JLabel exportLabelText = new JLabel("Export: ");
        exportLabelText.setBounds(10, 530, 50, 20);
        add(exportLabelText);


        JTextArea exportField = new JTextArea("");
        exportField.setName("ExportUrlTextField");
        exportField.setBounds(60, 530, 420, 20);
        exportField.setVisible(true);
        add(exportField);


        JButton saveButton = new JButton("Save");
        saveButton.setName("ExportButton");
        saveButton.setBounds(490, 525, 90, 30);
        add(saveButton);
        saveButton.addActionListener(e -> saveToFile(HtmlLinksParserAlgorithm.htmlLinks, exportField.getText()));

        setVisible(true);
    }

    public static TableModel toTableModel(Map<?, ?> map) {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"URL", "Title"}, 0
        );
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            model.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }
        return model;
    }

    public static void saveToFile(Map<String, String> map, String finePath) {
        String filePath = finePath.replaceAll("\\\\", "/");
        File file = new File(filePath);

        try (FileWriter writer = new FileWriter(file)) {
            for (var entry : map.entrySet()) {
                writer.write(entry.getKey() + "\n");
                writer.write(entry.getValue() + "\n");
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                Thread.sleep(1000);
                System.out.println("Inside Timer Thread.");

                currentSecond++;
                if (currentSecond == 59) {
                    currentSecond = 0;
                    currentMinute++;
                }
                elapsedTime.setText(currentMinute + ":" + currentSecond);
                timeLimitSeconds--;

                if (HtmlLinksParserAlgorithm.queue.size() == 0) {
                    queueEmptySeconds++;
                } else {
                    queueEmptySeconds = 0;
                }

                parsedPages.setText("" + HtmlLinksParserAlgorithm.sitesParsed);

                TableModel model = toTableModel(HtmlLinksParserAlgorithm.htmlLinks);
                table.setModel(model);

                for (ThreadWorker w : workers) {
                    if (!w.isAlive()) {
                        numberOfInactiveWorkers++;
                    }
                }

                if (numberOfInactiveWorkers == numberOfWorkers || timeLimitSeconds < 1 || queueEmptySeconds > 15) {
                    runButton.setText("Run");
                    runButton.setSelected(false);
                    isRunning = false;
                    workers.clear();
                }

                numberOfInactiveWorkers = 0;

            } catch (InterruptedException ex) {
                ex.getMessage();
            }
        }
    }

}