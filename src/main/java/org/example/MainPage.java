package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

public class MainPage {
    private JPanel panel1;
    private JTable sqlTable;
    private JComboBox<String> cmbTableSelect;
    private JButton addQuestionBtn;
    private JPanel btnPanel;
    private JButton addPaperBtn;
    private JScrollPane sqlScrollPane;
    private JButton Delete;
    private JTextField filterText;
    private DefaultTableModel sqlTableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private int filteringIndex = 5;
    public static int width = 640;

    public MainPage() {
        ToolTipManager.sharedInstance().setInitialDelay(100);
        createTable();

        addQuestionBtn.addActionListener(e -> launchQuestionDialog(false));
        addPaperBtn.addActionListener(e -> launchPaperDialog(false));
        filterText.setText(sqlTable.getColumnName(filteringIndex));
        sqlTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                if (me.getClickCount()==2) {
                    if (cmbTableSelect.getSelectedItem().toString().equals("Questions")) {
                        launchQuestionDialog(true);
                    }
                    else {
                        launchPaperDialog(true);
                    }
                }
            }
        });
        sqlTable.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                filteringIndex = sqlTable.getColumnCount() * e.getX() / width;
                filterText.setText(sqlTable.getColumnName(filteringIndex));
            }
        });

        cmbTableSelect.addActionListener(e -> createTable());
        Delete.addActionListener(e -> deleteRecord());
        filterText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                FilterTable();
            }
        });
        filterText.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                filterText.setText(sqlTable.getColumnName(filteringIndex));
            }
            @Override
            public void focusGained(FocusEvent e) {
                filterText.setText("");
            }
        });
        filterText.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                filterText.setText("");
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("MainPage");
        frame.setBounds(100, 100, width, 450);
        frame.setContentPane(new MainPage().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public void createTable() {
        Connection conn;
        try {
            String tableName = cmbTableSelect.getSelectedItem().toString();
            conn = DbManager.getConn();
            PreparedStatement ps;
            if (tableName.equals("Questions")) {
                String sql = "SELECT Link.Question_Id, Papers.Subject, Papers.Year, Questions.Question_Number, Questions.Marks, Questions.Text " +
                        "FROM Papers " +
                        "JOIN Link ON Papers.Paper_Id = Link.Paper_Id " +
                        "JOIN Questions ON Questions.Question_Id = Link.Question_Id";
                ps = conn.prepareStatement(sql);
            }
            else {
                ps = conn.prepareStatement("Select * from " + tableName);
            }
            ResultSet rs = ps.executeQuery();
            sqlTableModel = buildTableModel(rs);
            rowSorter = new TableRowSorter<>(sqlTableModel);
            sqlTable.setModel(sqlTableModel);
            sqlTable.setRowSorter(rowSorter);
            ps.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

    }

    public static DefaultTableModel buildTableModel(ResultSet rs)
            throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        // names of columns
        Vector<String> columnNames = new Vector<String>();
        int columnCount = metaData.getColumnCount();
        for (int column = 1; column <= columnCount; column++) {
            String cn = metaData.getColumnName(column);
            columnNames.add(cn);
        }
        // data of the table
        Vector<Vector<Object>> data = new Vector<Vector<Object>>();
        while (rs.next()) {
            Vector<Object> vector = new Vector<Object>();
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                vector.add(rs.getObject(columnIndex));
            }
            data.add(vector);
        }
        return new DefaultTableModel(data, columnNames) {
            // https://stackoverflow.com/a/3134006
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    // https://stackoverflow.com/a/1107941
    private void FilterTable() {
        RowFilter<DefaultTableModel, Object> rf;
        try {
            rf = RowFilter.regexFilter(filterText.getText(),filteringIndex);
            rowSorter.setRowFilter(rf);
        } catch (java.util.regex.PatternSyntaxException e) {
            System.out.println("regex error");
        }
    }

    public void launchQuestionDialog(boolean filled) {
        int row = sqlTable.getSelectedRow();
        EditQuestionDialog dialog;
        if (filled) {
            dialog = new EditQuestionDialog(
                    this,
                    sqlTable.getValueAt(row, 0).toString(),
                    sqlTable.getValueAt(row, 3).toString(),
                    sqlTable.getValueAt(row, 4).toString(),
                    sqlTable.getValueAt(row, 5).toString(),
                    false
            );
        }
        else { dialog = new EditQuestionDialog(this); }
        dialog.setBounds(100, 100, 400, 300);
        dialog.setVisible(true);
    }

    public void launchPaperDialog(boolean filled) {
        int row = sqlTable.getSelectedRow();
        EditPaperDialog dialog;
        if (filled) {
            dialog = new EditPaperDialog(
                    this,
                    sqlTable.getValueAt(row, 0).toString(),
                    sqlTable.getValueAt(row, 1).toString(),
                    sqlTable.getValueAt(row, 2).toString(),
                    sqlTable.getValueAt(row, 3).toString(),
                    sqlTable.getValueAt(row, 4).toString(),
                    sqlTable.getValueAt(row, 5).toString()
            );
        }
        else { dialog = new EditPaperDialog(this); }
        dialog.setBounds(100, 100, 400, 300);
        dialog.setVisible(true);
    }

    public void deleteRecord() {
        Connection conn;
        try {
            String id = sqlTable.getValueAt(sqlTable.getSelectedRow(), 0).toString();
            conn = DbManager.getConn();
            if (cmbTableSelect.getSelectedItem().toString().equals("Questions")) {
                PreparedStatement ps1 = conn.prepareStatement("DELETE FROM Link WHERE Question_Id = ?");
                ps1.setString(1, id);
                ps1.executeUpdate();
                PreparedStatement ps2 = conn.prepareStatement("DELETE FROM Questions WHERE Question_Id = ?");
                ps2.setString(1, id);
                ps2.executeUpdate();
                ps1.close(); ps2.close();
            }
            else {
                PreparedStatement ps0 = conn.prepareStatement("SELECT * FROM Link WHERE Paper_Id = ?");
                ps0.setString(1, id);
                ResultSet rs = ps0.executeQuery();
                while (rs.next()) {
                    String qId = rs.getString("Question_Id");
                    PreparedStatement ps = conn.prepareStatement("DELETE FROM Questions WHERE Question_Id = ?");
                    ps.setString(1, qId);
                    ps.executeUpdate();
                }

                PreparedStatement ps1 = conn.prepareStatement("DELETE FROM Link WHERE Paper_Id = ?");
                ps1.setString(1, id);
                ps1.executeUpdate();
                PreparedStatement ps2 = conn.prepareStatement("DELEte FROM Papers WHERE Paper_Id = ?");
                ps2.setString(1, id);
                ps2.executeUpdate();
                ps1.close(); ps2.close();
            }

            createTable();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

}
