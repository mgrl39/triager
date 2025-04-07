package net.elpuig.triager.service;

import net.elpuig.triager.model.Patient;
import net.elpuig.triager.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PatientService {
    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public Patient addPatient(String nombre, String apellido, int edad, String sintomas, Patient.UrgencyLevel urgencia) {
        String id = String.valueOf(System.currentTimeMillis());
        Patient patient = new Patient(id, nombre, apellido, edad, sintomas, urgencia);
        return patientRepository.save(patient);
    }

    public List<Patient> listPatients() {
        return patientRepository.findAll();
    }

    public Optional<Patient> getPatient(String id) {
        return patientRepository.findById(id);
    }

    public Optional<Patient> attendNextPatient() {
        return patientRepository.findNextPatient();
    }

    public void confirmAttendance(Patient patient) {
        patientRepository.moveToHistory(patient);
    }

    public Map<String, List<Patient>> getPatientsByUrgency() {
        List<Patient> patients = patientRepository.findAll();
        return patients.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                patient -> patient.getUrgencia().getValue()
            ));
    }
} 