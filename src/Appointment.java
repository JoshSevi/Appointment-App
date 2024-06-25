import java.text.SimpleDateFormat;
import java.util.Date;

public class Appointment {
    private String patientName;
    private String patientContact;  // Add a field for patient contact number
    private String provider;
    private int duration;
    private Date time;

    public Appointment(String patientName, String patientContact, String provider, int duration, Date time) {
        this.patientName = patientName;
        this.patientContact = patientContact;
        this.provider = provider;
        this.duration = duration;
        this.time = time;
    }

    public Date getTime() {
        return time;
    }

    public String getProvider() {
        return provider;
    }

    public int getDuration() {
        return duration;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getPatientContact() {
        return patientContact;
    }

    @Override
    public String toString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return "Provider: " + provider + " | Patient: " + patientName + " | Contact: " + patientContact + " | Duration: " + duration + " minutes | Time: " + dateFormat.format(time);
    }
}


