package com.example.myrajourney.common.messaging;

import android.content.Context;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.Medication;
import com.example.myrajourney.data.model.Appointment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatBot {
    private Context context;
    private Random random;
    private ApiService apiService;

    public ChatBot(Context context) {
        this.context = context;
        this.random = new Random();
        this.apiService = com.example.myrajourney.core.network.ApiClient.getApiService(context);
    }

    public String getResponse(String userMessage) {
        String message = userMessage.toLowerCase().trim();

        // Pain-related responses
        if (message.contains("pain") || message.contains("hurt") || message.contains("ache")) {
            String[] responses = {
                    "I understand you're experiencing pain. Have you taken your prescribed medication?",
                    "Pain management is important. Try gentle stretching or apply heat/cold as recommended.",
                    "If pain persists, please contact your doctor. Keep track of pain levels in your symptom log.",
                    "Remember to follow your pain management plan. Rest when needed."
            };
            return responses[random.nextInt(responses.length)];
        }

        // Medication-related responses
        if (message.contains("medication") || message.contains("medicine") || message.contains("pill") ||
                message.contains("meds") || message.contains("prescription")) {
            return getMedicationResponse();
        }

        // Exercise-related responses
        if (message.contains("exercise") || message.contains("workout") || message.contains("rehab")) {
            String[] responses = {
                    "Regular exercise helps with RA management. Follow your prescribed rehab exercises.",
                    "Start slowly and listen to your body. Don't overexert yourself.",
                    "Consistency is key. Even light exercise can help maintain joint flexibility.",
                    "If exercises cause pain, modify them or consult your physical therapist."
            };
            return responses[random.nextInt(responses.length)];
        }

        // Appointment-related responses
        if (message.contains("appointment") || message.contains("doctor") || message.contains("visit") ||
                message.contains("schedule") || message.contains("next appointment")) {
            return getAppointmentResponse();
        }

        // General health responses
        if (message.contains("tired") || message.contains("fatigue") || message.contains("energy")) {
            String[] responses = {
                    "Fatigue is common with RA. Ensure you're getting enough rest and sleep.",
                    "Pace yourself throughout the day. Take breaks when needed.",
                    "Good nutrition and gentle exercise can help with energy levels.",
                    "If fatigue is severe, discuss it with your healthcare team."
            };
            return responses[random.nextInt(responses.length)];
        }

        // Greeting responses
        if (message.contains("hello") || message.contains("hi") || message.contains("hey")) {
            String[] responses = {
                    "Hello! I'm here to help with your RA journey. How are you feeling today?",
                    "Hi there! How can I assist you with your rheumatoid arthritis management?",
                    "Hello! I'm your RA assistant. What would you like to know?",
                    "Hi! I'm here to support you. How are your symptoms today?"
            };
            return responses[random.nextInt(responses.length)];
        }

        // Help responses
        if (message.contains("help") || message.contains("support")) {
            String[] responses = {
                    "I can help with medication reminders, exercise guidance, pain management tips, and general RA information.",
                    "I'm here to support you with your RA management. Ask me about medications, exercises, or symptoms.",
                    "I can assist with tracking your symptoms, medication adherence, and providing general health tips.",
                    "Feel free to ask me about your treatment plan, exercises, or any concerns about your condition."
            };
            return responses[random.nextInt(responses.length)];
        }

        // Default responses
        String[] defaultResponses = {
                "I understand. How can I help you manage your RA better today?",
                "That's important to note. Would you like to log this in your symptom tracker?",
                "I'm here to support you. Feel free to ask about medications, exercises, or symptoms.",
                "Thank you for sharing. Is there anything specific about your RA management I can help with?",
                "I'm listening. How can I assist you with your rheumatoid arthritis care?",
                "That's helpful information. Would you like guidance on managing this aspect of your condition?"
        };

        return defaultResponses[random.nextInt(defaultResponses.length)];
    }

    private String getMedicationResponse() {
        try {
            Call<ApiResponse<List<Medication>>> call = apiService.getPatientMedications();
            final String[] result = {null};
            final CountDownLatch latch = new CountDownLatch(1);

            call.enqueue(new Callback<ApiResponse<List<Medication>>>() {
                @Override
                public void onResponse(Call<ApiResponse<List<Medication>>> call,
                                       Response<ApiResponse<List<Medication>>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        List<Medication> meds = response.body().getData();
                        if (meds != null && !meds.isEmpty()) {
                            StringBuilder sb = new StringBuilder("You have " + meds.size() + " medication(s): ");
                            int activeCount = 0;
                            for (Medication m : meds) {
                                // Safe check for status
                                boolean isMedActive = m.getStatus() != null &&
                                        (m.getStatus().equalsIgnoreCase("Ongoing") ||
                                                m.getStatus().equalsIgnoreCase("Active"));

                                if (isMedActive) {
                                    activeCount++;
                                    if (activeCount <= 3) {
                                        sb.append("\n• ").append(m.getName() != null ? m.getName() : "Medication");
                                        if (m.getDosage() != null) sb.append(" (").append(m.getDosage()).append(")");
                                    }
                                }
                            }
                            if (activeCount > 3) {
                                sb.append("\n... and ").append(activeCount - 3).append(" more");
                            }
                            sb.append("\n\nRemember to take them as prescribed!");
                            result[0] = sb.toString();
                        } else {
                            result[0] = "You don't have any active medications currently. Contact your doctor if you need prescriptions.";
                        }
                    } else {
                        result[0] = "Make sure to take your medications as prescribed. Check the Medications section for your schedule.";
                    }
                    latch.countDown();
                }

                @Override
                public void onFailure(Call<ApiResponse<List<Medication>>> call, Throwable t) {
                    result[0] = "Make sure to take your medications as prescribed. Check the Medications section for your schedule.";
                    latch.countDown();
                }
            });

            if (latch.await(2, TimeUnit.SECONDS)) {
                return result[0] != null ? result[0] : "Make sure to take your medications as prescribed. Set reminders if needed.";
            } else {
                return "Make sure to take your medications as prescribed. Check the Medications section for your schedule.";
            }
        } catch (Exception e) {
            return "Make sure to take your medications as prescribed. Set reminders if needed.";
        }
    }

    private String getAppointmentResponse() {
        try {
            Call<ApiResponse<List<Appointment>>> call = apiService.getAppointments();
            final String[] result = {null};
            final CountDownLatch latch = new CountDownLatch(1);

            call.enqueue(new Callback<ApiResponse<List<Appointment>>>() {
                @Override
                public void onResponse(Call<ApiResponse<List<Appointment>>> call,
                                       Response<ApiResponse<List<Appointment>>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        List<Appointment> appts = response.body().getData();
                        if (appts != null && !appts.isEmpty()) {
                            Appointment nextAppt = null;
                            long now = System.currentTimeMillis();

                            // Use SimpleDateFormat for compatibility with all Android versions
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

                            for (Appointment a : appts) {
                                if (a.getStartTime() != null) {
                                    try {
                                        Date date = sdf.parse(a.getStartTime());
                                        if (date != null && date.getTime() > now) {
                                            if (nextAppt == null) {
                                                nextAppt = a;
                                            } else {
                                                Date nextDate = sdf.parse(nextAppt.getStartTime());
                                                if (nextDate != null && date.getTime() < nextDate.getTime()) {
                                                    nextAppt = a;
                                                }
                                            }
                                        }
                                    } catch (ParseException e) {
                                        // Ignore parsing errors
                                    }
                                }
                            }

                            if (nextAppt != null) {
                                StringBuilder sb = new StringBuilder("Your next appointment: ");
                                sb.append(nextAppt.getTitle() != null ? nextAppt.getTitle() : "Appointment");
                                if (nextAppt.getStartTime() != null) {
                                    try {
                                        Date date = sdf.parse(nextAppt.getStartTime());
                                        SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
                                        if (date != null) {
                                            sb.append("\nDate: ").append(displayFormat.format(date));
                                        }
                                    } catch (ParseException e) {
                                        sb.append("\nDate: ").append(nextAppt.getStartTime());
                                    }
                                }
                                sb.append("\n\nPrepare your questions and bring your symptom log!");
                                result[0] = sb.toString();
                            } else {
                                result[0] = "You have " + appts.size() + " appointment(s) in your records. Keep your appointments regular.";
                            }
                        } else {
                            result[0] = "You don't have any upcoming appointments. Contact your doctor to schedule one if needed.";
                        }
                    } else {
                        result[0] = "Keep your appointments regular. Check the Appointments section for details.";
                    }
                    latch.countDown();
                }

                @Override
                public void onFailure(Call<ApiResponse<List<Appointment>>> call, Throwable t) {
                    result[0] = "Keep your appointments regular. Check the Appointments section for details.";
                    latch.countDown();
                }
            });

            if (latch.await(2, TimeUnit.SECONDS)) {
                return result[0] != null ? result[0] : "Keep your appointments regular. Check your schedule.";
            } else {
                return "Keep your appointments regular. Check the Appointments section for your schedule.";
            }
        } catch (Exception e) {
            return "Keep your appointments regular. They're important for monitoring your condition.";
        }
    }
}