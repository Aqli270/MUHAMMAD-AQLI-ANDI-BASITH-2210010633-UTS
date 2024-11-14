package Note;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;
import com.toedter.calendar.JDateChooser;  // Import JDateChooser from JCalendar
import java.io.FileWriter;
import java.io.IOException;

public class AgendaApp extends JFrame {
    private JTextField titleField, timeField;
    private JTextArea descriptionArea;
    private JTable agendaTable;
    private DefaultTableModel tableModel;
    private JButton addButton, updateButton, deleteButton, exportButton;
    private JDateChooser dateChooser; // JDateChooser for date input
    private static final String DB_URL = "jdbc:mysql://localhost:3306/agenda_db"; // Ganti dengan nama database Anda
    private static final String DB_USER = "root"; // User default MySQL
    private static final String DB_PASSWORD = ""; // Password default kosong di XAMPP

    public AgendaApp() {
        setTitle("Agenda Pribadi");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        // Create input fields
        titleField = new JTextField(20);
        timeField = new JTextField(10);
        descriptionArea = new JTextArea(5, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        // Create JDateChooser for date input
        dateChooser = new JDateChooser(); // JDateChooser will allow users to pick a date

        // Create buttons
        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        exportButton = new JButton("Export CSV"); // New button for export CSV

        // Create table model and table
        tableModel = new DefaultTableModel(new Object[]{"ID", "Title", "Date", "Time", "Description"}, 0);
        agendaTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(agendaTable);

        // Add components to the frame
        add(new JLabel("Title"));
        add(titleField);
        add(new JLabel("Date"));
        add(dateChooser);  // Add JDateChooser to the frame
        add(new JLabel("Time (HH:MM)"));
        add(timeField);
        add(new JLabel("Description"));
        add(new JScrollPane(descriptionArea));
        add(addButton);
        add(updateButton);
        add(deleteButton);
        add(exportButton); // Add the export button
        add(scrollPane);

        // Button actions
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addAgenda();
            }
        });

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateAgenda();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteAgenda();
            }
        });

        exportButton.addActionListener(new ActionListener() { // Action listener for Export button
            @Override
            public void actionPerformed(ActionEvent e) {
                exportCSV();
            }
        });

        loadAgendaData();
        setVisible(true);
    }

    // Method to add agenda to the database
    private void addAgenda() {
        String title = titleField.getText();
        java.util.Date date = dateChooser.getDate();  // Get date from JDateChooser
        String time = timeField.getText();
        String description = descriptionArea.getText();

        // Check if date is selected
        if (date == null) {
            JOptionPane.showMessageDialog(this, "Please select a date!");
            return;
        }

        // Format the date to SQL format
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = sdf.format(date);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "INSERT INTO agenda (title, date, time, description) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, title);
            stmt.setString(2, formattedDate);
            stmt.setString(3, time);
            stmt.setString(4, description);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Agenda added successfully!");
            loadAgendaData(); // Reload the table data
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to update agenda in the database
    private void updateAgenda() {
        int selectedRow = agendaTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an agenda to update.");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String title = titleField.getText();
        java.util.Date date = dateChooser.getDate();  // Get date from JDateChooser
        String time = timeField.getText();
        String description = descriptionArea.getText();

        // Check if date is selected
        if (date == null) {
            JOptionPane.showMessageDialog(this, "Please select a date!");
            return;
        }

        // Format the date to SQL format
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = sdf.format(date);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "UPDATE agenda SET title = ?, date = ?, time = ?, description = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, title);
            stmt.setString(2, formattedDate);
            stmt.setString(3, time);
            stmt.setString(4, description);
            stmt.setInt(5, id);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Agenda updated successfully!");
            loadAgendaData(); // Reload the table data
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to delete agenda from the database
    private void deleteAgenda() {
        int selectedRow = agendaTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an agenda to delete.");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "DELETE FROM agenda WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, id);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Agenda deleted successfully!");
            loadAgendaData(); // Reload the table data
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to load all agenda data from the database into the table
    private void loadAgendaData() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT * FROM agenda";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // Clear existing table rows
            tableModel.setRowCount(0);

            // Populate table with data from database
            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String date = rs.getString("date");
                String time = rs.getString("time");
                String description = rs.getString("description");
                tableModel.addRow(new Object[]{id, title, date, time, description});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to export agenda data to a CSV file
    private void exportCSV() {
        String filePath = System.getProperty("user.home") + "/Downloads/agenda_data.csv";  // Path to save CSV in Downloads folder
        try (FileWriter writer = new FileWriter(filePath)) {
            // Write column headers to the CSV file
            writer.append("ID,Title,Date,Time,Description\n");

            // Write table data to the CSV file
            for (int row = 0; row < tableModel.getRowCount(); row++) {
                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    writer.append(tableModel.getValueAt(row, col).toString());
                    if (col < tableModel.getColumnCount() - 1) {
                        writer.append(",");  // Separate columns with commas
                    }
                }
                writer.append("\n");
            }
            JOptionPane.showMessageDialog(this, "Agenda exported to CSV successfully!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error exporting to CSV: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new AgendaApp();
    }
}
