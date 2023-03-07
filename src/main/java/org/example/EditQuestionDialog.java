package org.example;

import javax.swing.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class EditQuestionDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPanel lowerBtnPanel;
    private JPanel EditorPanel;
    private JLabel PIDLabel;
    private JLabel QNumLabel;
    private JLabel QMarksLabel;
    private JLabel QTextLabel;
    private JComboBox<String> PaperComboBox;
    private JSpinner QNumSpinner;
    private JSpinner QMarksSpinner;
    private JScrollPane TextScrollPane;
    private JTextArea QTextArea;
    private JButton newPaperBtn;
    private HashMap<String, String> papers = new HashMap<>();
    private boolean isNew = true;
    private String id;
    private MainPage parent;

    public EditQuestionDialog(MainPage parent) {
        this.parent = parent;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        setPaperComboBox();
        SpinnerNumberModel numModel = new SpinnerNumberModel(1, 1, 500, 1);
        SpinnerNumberModel marksModel = new SpinnerNumberModel(1, 1, 500, 1);
        QNumSpinner.setModel(numModel);
        QMarksSpinner.setModel(marksModel);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        PaperComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PaperComboBox.setToolTipText(papers.get(PaperComboBox.getSelectedItem().toString()));
            }
        });
        newPaperBtn.addActionListener(e -> {
            onCancel();
            parent.launchPaperDialog(false);
        });
    }

    public EditQuestionDialog(MainPage parent, String id, String num, String marks, String text, boolean isNew) {
        this(parent);
        Connection conn;
        try {
            conn = DbManager.getConn();
            PreparedStatement ps = conn.prepareStatement("SELECT Paper_Id FROM Link WHERE Question_Id = ?");
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            rs.next();
            PaperComboBox.setSelectedItem(rs.getString("Paper_Id"));
        }
        catch(Exception e) {
            PaperComboBox.setSelectedItem("1");
            e.printStackTrace();
        }
        QNumSpinner.setValue(Integer.parseInt(num));
        QMarksSpinner.setValue(Integer.parseInt(marks));
        QTextArea.setText(text);
        this.isNew = isNew;
        this.id = id;
    }

    private void onOK() {
        Connection conn;
        try {
            String qNum = QNumSpinner.getValue().toString();
            String qMarks = QMarksSpinner.getValue().toString();
            String qText = QTextArea.getText();
            String Paper_Id = PaperComboBox.getSelectedItem().toString();
            conn = DbManager.getConn();

            if (isNew) {
                PreparedStatement insertSt = conn.prepareStatement("INSERT INTO Questions (Question_Number, Marks, Text) VALUES (?, ?, ?)");
                insertSt.setString(1, qNum);
                insertSt.setString(2, qMarks);
                insertSt.setString(3, qText);
                insertSt.executeUpdate();
                insertSt.close();
                PreparedStatement linkSt = conn.prepareStatement("INSERT INTO Link (Question_Id, Paper_Id) VALUES (last_insert_rowid(), ?)");
                linkSt.setString(1, Paper_Id);
                linkSt.executeUpdate();
                linkSt.close();
            }
            else {
                PreparedStatement insertSt = conn.prepareStatement("UPDATE Questions SET Question_Number = ?, Marks = ?, Text = ? WHERE Question_Id = ?");
                insertSt.setString(1, qNum);
                insertSt.setString(2, qMarks);
                insertSt.setString(3, qText);
                insertSt.setString(4, id);
                insertSt.executeUpdate();
                insertSt.close();
                PreparedStatement linkSt = conn.prepareStatement("UPDATE Link SET Paper_Id = ? WHERE Question_Id = ?");
                linkSt.setString(1, Paper_Id);
                linkSt.setString(2, id);
                linkSt.executeUpdate();
                linkSt.close();
            }
        }
        catch(Exception e) { e.printStackTrace(); }
        parent.createTable();
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public void setPaperComboBox() {
        Connection conn;
        try {
            conn = DbManager.getConn();
            PreparedStatement ps = conn.prepareStatement("Select * from Papers");
            ResultSet rs = ps.executeQuery();
            ArrayList<String> paper_ids = new ArrayList<>();
            papers = new HashMap<>();
            while (rs.next()) {
                String paper_id = rs.getString("Paper_Id");
                paper_ids.add(paper_id);
                String info = rs.getString("Subject") + ", " + rs.getString("Year");
                papers.put(paper_id, info);
            }
            ps.close();

            DefaultComboBoxModel<String> paperModel = new DefaultComboBoxModel<>(paper_ids.toArray(new String[0]));
            PaperComboBox.setModel(paperModel);
            PaperComboBox.setToolTipText(papers.get(paper_ids.get(0)));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
