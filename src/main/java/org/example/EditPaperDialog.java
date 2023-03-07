package org.example;

import java.sql.*;

import javax.swing.*;
import java.awt.event.*;
import java.sql.PreparedStatement;

public class EditPaperDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPanel okPanel;
    private JLabel SubjectLabel;
    private JLabel LevelLabel;
    private JSpinner MonthSpinner;
    private JSpinner DaySpinner;
    private JSpinner YearSpinner;
    private JTextField SubjectField;
    private JRadioButton SLBtn;
    private JRadioButton HLBtn;
    private JLabel DateLabel;
    private MainPage parent;
    private boolean isNew = true;
    private String id;

    public EditPaperDialog(MainPage parent) {
        this.parent = parent;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        MonthSpinner.setModel(new SpinnerNumberModel(1, 1, 12, 1));
        DaySpinner.setModel(new SpinnerNumberModel(1, 1, 31, 1));
        YearSpinner.setModel(new SpinnerNumberModel(2022, 1900, 2100, 1));
//        JSpinner.NumberEditor editor = ;
        YearSpinner.setEditor(new JSpinner.NumberEditor(YearSpinner, "#"));

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

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
    }

    public EditPaperDialog(MainPage parent, String id, String day, String month, String year, String subject, String isSL) {
        this(parent);
        this.id = id;
        this.isNew = false;
        MonthSpinner.setValue(Integer.parseInt(month));
        DaySpinner.setValue(Integer.parseInt(day));
        YearSpinner.setValue(Integer.parseInt(year));
        SubjectField.setText(subject);
        if (isSL.equals("1")) {
            HLBtn.setSelected(false);
            SLBtn.setSelected(true);
        }
        else {
            SLBtn.setSelected(false);
            HLBtn.setSelected(true);
        }
    }

    private void onOK() {
        // add your code here
        Connection conn;
        try {
            String pDay = DaySpinner.getValue().toString();
            String pMonth = MonthSpinner.getValue().toString();
            String pYear = YearSpinner.getValue().toString();
            String pSubject = SubjectField.getText();
            conn = DbManager.getConn();
            String isSL = SLBtn.isSelected() ? "1" : "0";

            PreparedStatement insertSt;
            if (isNew) {
                insertSt = conn.prepareStatement("INSERT INTO Papers (Day, Month, Year, Subject, isSL) VALUES (?, ?, ?, ?, ?)");
                insertSt.setString(1, pDay);
                insertSt.setString(2, pMonth);
                insertSt.setString(3, pYear);
                insertSt.setString(4, pSubject);
                insertSt.setString(5, isSL);
            }
            else {
                insertSt = conn.prepareStatement("UPDATE Papers SET Day = ?, Month = ?, Year = ?, Subject = ?, isSL = ? WHERE Paper_Id = ?");
                insertSt.setString(1, pDay);
                insertSt.setString(2, pMonth);
                insertSt.setString(3, pYear);
                insertSt.setString(4, pSubject);
                insertSt.setString(5, isSL);
                insertSt.setString(6, id);
            }
            insertSt.executeUpdate();
            insertSt.close();
        }
        catch(Exception e) { e.printStackTrace(); }

        parent.createTable();
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

}
