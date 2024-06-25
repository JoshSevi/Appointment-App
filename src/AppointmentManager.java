import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.text.MaskFormatter;

public class AppointmentManager {
    private static DefaultTableModel tableModel;
    private static JTable appointmentTable;

    public void run() {
        // Initialize the database connection
        AppointmentData.initialize();
        AppointmentData dataStorage = new AppointmentData();

        // Create the main frame
        JFrame frame = new JFrame("Healthcare Appointment Scheduling");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Calculate the new size based on the screen resolution
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int newWidth = (int) (screenSize.getWidth() * 0.4);
        int newHeight = (int) (screenSize.getHeight() * 0.6);

        // Set the size of the frame
        frame.setSize(newWidth, newHeight);

        // Calculate the center position
        int x = (screenSize.width - newWidth) / 2;
        int y = (screenSize.height - newHeight) / 2;

        // Set the location of the frame to center it on the screen
        frame.setLocation(x, y);

        frame.setLayout(new BorderLayout());

        // Create input components
        JTextField patientNameField = new JTextField(20);
        JFormattedTextField patientContactField = createFormattedContactField();
        JComboBox<String> providerComboBox = new JComboBox<>(new String[]{"Dr. A", "Dr. B"});
        JSpinner durationSpinner = new JSpinner(new SpinnerNumberModel(30, 15, 120, 15)); // Duration in minutes
        JSpinner timeSpinner = new JSpinner(new SpinnerDateModel());

        JButton addAppointmentButton = new JButton("Add Appointment");
        JButton removeAppointmentButton = new JButton("Remove Appointment");

        // Create input panel
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(7, 2));
        inputPanel.add(new JLabel("Provider:"));
        inputPanel.add(providerComboBox);
        inputPanel.add(new JLabel("Patient Name:"));
        inputPanel.add(patientNameField);
        inputPanel.add(new JLabel("Patient Contact Number:"));
        inputPanel.add(patientContactField);
        inputPanel.add(new JLabel("Duration (minutes):"));
        inputPanel.add(durationSpinner);
        inputPanel.add(new JLabel("Time:"));
        inputPanel.add(timeSpinner);
        inputPanel.add(addAppointmentButton);
        inputPanel.add(removeAppointmentButton);

        // Create a panel for the appointment table
        JPanel tablePanel = new JPanel(new BorderLayout());

        // Create column titles for the table
        Object[] columnTitles = {"Provider", "Patient", "Contact", "Duration", "Date & Time"};
        tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers(columnTitles);
        appointmentTable = new JTable(tableModel);
        appointmentTable.setDefaultRenderer(Object.class, new HighlightedAppointmentRenderer());

        JScrollPane scrollPane = new JScrollPane(appointmentTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(tablePanel, BorderLayout.CENTER);

        // Add input components and appointment list to the main frame
        frame.setVisible(true);

        // Add action listener for the "Add Appointment" button
        addAppointmentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String patientName = patientNameField.getText().trim();
                String patientContact = patientContactField.getText().trim();
                String selectedProvider = providerComboBox.getSelectedItem().toString();
                int duration = (int) durationSpinner.getValue();
                Date selectedTime = (Date) timeSpinner.getValue();

                if (patientName.isEmpty() || !patientName.matches("^[a-zA-Z ]+$")) {
                    JOptionPane.showMessageDialog(frame, "Patient's name cannot be empty and should only contain letters and spaces.");
                    return;
                }

                if (patientContact.isEmpty() || !patientContact.matches("^[0-9-]+$")) {
                    JOptionPane.showMessageDialog(frame, "Patient's contact number can only contain numbers and hyphens.");
                    return;
                }

                // Check for conflicts with existing appointments
                if (dataStorage.hasConflicts(selectedProvider, selectedTime, duration)) {
                    JOptionPane.showMessageDialog(frame, "Appointment time conflicts with an existing appointment for the same provider.");
                    return;
                }

                // Add appointment using the data storage
                dataStorage.addAppointment(new Appointment(patientName, patientContact, selectedProvider, duration, selectedTime));

                // Update the appointment table
                updateAppointmentTable(dataStorage.getAppointments());

                // Clear the input fields
                patientNameField.setText("");
                patientContactField.setValue(null);
                durationSpinner.setValue(30);
                timeSpinner.setValue(new Date());
            }
        });

        // Add action listener for the "Remove Appointment" button
        removeAppointmentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = appointmentTable.getSelectedRow();
                if (selectedIndex != -1) {
                    // Remove appointment using the data storage
                    dataStorage.removeAppointment(selectedIndex);

                    // Update the appointment table
                    updateAppointmentTable(dataStorage.getAppointments());
                }
            }
        });

        // Initialize and update the appointment table
        updateAppointmentTable(dataStorage.getAppointments());
    }

    private void updateAppointmentTable(List<Appointment> appointments) {
        // Clear the table
        tableModel.setRowCount(0);

        // Create a date format for formatting the time
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        for (Appointment appointment : appointments) {
            Object[] rowData = {
                    appointment.getProvider(),
                    appointment.getPatientName(),
                    appointment.getPatientContact(),
                    appointment.getDuration() + " minutes",
                    dateFormat.format(appointment.getTime())
            };

            tableModel.addRow(rowData);
        }
    }

    private class HighlightedAppointmentRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date currentDate = new Date();

            String appointmentString = tableModel.getValueAt(row, 4).toString();
            Date appointmentTime = null;

            try {
                appointmentTime = dateFormat.parse(appointmentString);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (appointmentTime != null) {
                long timeDiff = appointmentTime.getTime() - currentDate.getTime();

                Color backgroundColor;
                if (timeDiff > 24 * 60 * 60 * 1000) { // More than 24 hours in the future
                    backgroundColor = new Color(173, 216, 230); // Pastel Blue
                } else if (timeDiff > 0) { // Within the next 24 hours
                    backgroundColor = new Color(152, 251, 152); // Pastel Green
                } else if (timeDiff > -currentDate.getMinutes() * 60 * 1000) { // Within the current day
                    backgroundColor = new Color(255, 236, 139); // Pastel Yellow
                } else { // In the past
                    backgroundColor = new Color(255, 182, 193); // Pastel Red
                }

                c.setBackground(backgroundColor);
            }

            return c;
        }
    }

    private JFormattedTextField createFormattedContactField() {
        MaskFormatter maskFormatter = null;
        try {
            maskFormatter = new MaskFormatter("###-###-####");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        JFormattedTextField formattedTextField = new JFormattedTextField(maskFormatter);
        formattedTextField.setColumns(20);

        return formattedTextField;
    }
}
