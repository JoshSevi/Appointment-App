import java.sql.*;
import java.util.*;
import java.util.LinkedList;
import java.util.Comparator;
import java.util.Date;

class AppointmentData {
    private static Connection connection;

    public static void initialize() {
        try {
            // Load the SQLite JDBC driver (this step may not be required in some cases)
            Class.forName("org.sqlite.JDBC");

            // Establish a database connection
            connection = DriverManager.getConnection("jdbc:sqlite:/Users/sevi/School/DSA/AppointmentApp/appointments.db");
            // Create the 'appointments' table if it doesn't exist, including the "patientContact" column
            String createTableSQL = "CREATE TABLE IF NOT EXISTS appointments (patientName TEXT, patientContact TEXT, provider TEXT, duration INT, time DATETIME)";
            connection.createStatement().executeUpdate(createTableSQL);

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void addAppointment(Appointment appointment) {
        try {
            // Insert the appointment into the database
            String insertSQL = "INSERT INTO appointments (patientName, patientContact, provider, duration, time) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);
            preparedStatement.setString(1, appointment.getPatientName());
            preparedStatement.setString(2, appointment.getPatientContact());
            preparedStatement.setString(3, appointment.getProvider());
            preparedStatement.setInt(4, appointment.getDuration());
            preparedStatement.setTimestamp(5, new java.sql.Timestamp(appointment.getTime().getTime()));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Appointment> getAppointments() {
        List<Appointment> appointments = new ArrayList<>();
        try {
            // Retrieve appointments from the database
            String selectSQL = "SELECT patientName, patientContact, provider, duration, time FROM appointments"; // Include patientContact
            ResultSet resultSet = connection.createStatement().executeQuery(selectSQL);

            while (resultSet.next()) {
                String patientName = resultSet.getString("patientName");
                String patientContact = resultSet.getString("patientContact"); // Retrieve patient contact
                String provider = resultSet.getString("provider");
                int duration = resultSet.getInt("duration");
                java.util.Date time = resultSet.getTimestamp("time");

                Appointment appointment = new Appointment(patientName, patientContact, provider, duration, time); // Update the constructor
                appointments.add(appointment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Sort appointments by time and duration
        sortAppointments(appointments);

        return appointments;
    }

    public boolean hasConflicts(String provider, Date newTime, int newDuration) {
        List<Appointment> appointmentsList = getAppointments();
        for (Appointment appointment : appointmentsList) {
            if (appointment.getProvider().equals(provider)) {
                long newStartTime = newTime.getTime();
                long newEndTime = newStartTime + newDuration * 60000;

                long existingStartTime = appointment.getTime().getTime();
                long existingEndTime = existingStartTime + appointment.getDuration() * 60000;

                if (newStartTime < existingEndTime && newEndTime > existingStartTime) {
                    return true;
                }
            }
        }
        return false;
    }

    public void removeAppointment(int index) {
        List<Appointment> appointmentsList = getAppointments();
        if (index >= 0 && index < appointmentsList.size()) {
            Appointment appointment = appointmentsList.get(index);
            try {
                // Remove the appointment from the database
                String deleteSQL = "DELETE FROM appointments WHERE patientName = ? AND patientContact = ? AND provider = ? AND duration = ? AND time = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(deleteSQL);
                preparedStatement.setString(1, appointment.getPatientName());
                preparedStatement.setString(2, appointment.getPatientContact());
                preparedStatement.setString(3, appointment.getProvider());
                preparedStatement.setInt(4, appointment.getDuration());
                preparedStatement.setTimestamp(5, new java.sql.Timestamp(appointment.getTime().getTime()));
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Sorting appointments by time and duration using a LinkedList
    private void sortAppointments(List<Appointment> appointments) {
        LinkedList<Appointment> sortedList = new LinkedList<>(appointments);

        sortedList.sort(new Comparator<Appointment>() {
            @Override
            public int compare(Appointment a1, Appointment a2) {
                int timeCompare = a1.getTime().compareTo(a2.getTime());
                if (timeCompare != 0) {
                    return timeCompare;
                }
                int durationCompare = Integer.compare(a1.getDuration(), a2.getDuration());
                return durationCompare;
            }
        });

        appointments.clear();
        appointments.addAll(sortedList);
    }
}
