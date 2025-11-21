package com.example.myrajouney.common.messaging;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.myrajouney.api.ApiService;
import com.example.myrajouney.api.models.ApiResponse;
import com.example.myrajouney.api.models.Medication;
import com.example.myrajouney.api.models.Appointment;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ChatBot {
    private Context context;
    private Random random;
    private ApiService apiService;
    
    public ChatBot(Context context) {
        this.context = context;
        this.random = new Random();
        this.apiService = com.example.myrajouney.api.ApiClient.getApiService(context);
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
        
        // Medication-related responses with real data
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
        
        // Appointment-related responses with real data
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
            // Try to get real medication data
            retrofit2.Call<ApiResponse<List<Medication>>> call = apiService.getPatientMedications();
            final String[] result = {null};
            final CountDownLatch latch = new CountDownLatch(1);
            
            call.enqueue(new retrofit2.Callback<ApiResponse<List<Medication>>>() {
                @Override
                public void onResponse(retrofit2.Call<ApiResponse<List<Medication>>> call, 
                                      retrofit2.Response<ApiResponse<List<Medication>>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        List<Medication> meds = response.body().getData();
                        if (meds != null && !meds.isEmpty()) {
                            StringBuilder sb = new StringBuilder("You have " + meds.size() + " medication(s): ");
                            int activeCount = 0;
                            for (Medication m : meds) {
                                if (m.isActive()) {
                                    activeCount++;
                                    if (activeCount <= 3) { // Show first 3
                                        sb.append("\n• ").append(m.getMedicationName() != null ? m.getMedicationName() : "Medication");
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
                public void onFailure(retrofit2.Call<ApiResponse<List<Medication>>> call, Throwable t) {
                    result[0] = "Make sure to take your medications as prescribed. Check the Medications section for your schedule.";
                    latch.countDown();
                }
            });
            
            // Wait for response with timeout
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
            // Try to get real appointment data
            retrofit2.Call<ApiResponse<List<Appointment>>> call = apiService.getAppointments();
            final String[] result = {null};
            final CountDownLatch latch = new CountDownLatch(1);
            
            call.enqueue(new retrofit2.Callback<ApiResponse<List<Appointment>>>() {
                @Override
                public void onResponse(retrofit2.Call<ApiResponse<List<Appointment>>> call, 
                                      retrofit2.Response<ApiResponse<List<Appointment>>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        List<Appointment> appts = response.body().getData();
                        if (appts != null && !appts.isEmpty()) {
                            // Find next upcoming appointment
                            Appointment nextAppt = null;
                            long now = System.currentTimeMillis();
                            for (Appointment a : appts) {
                                if (a.getStartTime() != null) {
                                    try {
                                        long apptTime = java.time.Instant.parse(a.getStartTime()).toEpochMilli();
                                        if (apptTime > now) {
                                            if (nextAppt == null || apptTime < java.time.Instant.parse(nextAppt.getStartTime()).toEpochMilli()) {
                                                nextAppt = a;
                                            }
                                        }
                                    } catch (Exception e) {
                                        // Ignore parsing errors
                                    }
                                }
                            }
                            
                            if (nextAppt != null) {
                                StringBuilder sb = new StringBuilder("Your next appointment: ");
                                sb.append(nextAppt.getTitle() != null ? nextAppt.getTitle() : "Appointment");
                                if (nextAppt.getStartTime() != null) {
                                    try {
                                        java.time.Instant instant = java.time.Instant.parse(nextAppt.getStartTime());
                                        java.time.format.DateTimeFormatter formatter = 
                                            java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a")
                                                .withZone(java.time.ZoneId.systemDefault());
                                        sb.append("\nDate: ").append(formatter.format(instant));
                                    } catch (Exception e) {
                                        sb.append("\nDate: ").append(nextAppt.getStartTime());
                                    }
                                }
                                sb.append("\n\nPrepare your questions and bring your symptom log!");
                                result[0] = sb.toString();
                            } else {
                                result[0] = "You have " + appts.size() + " appointment(s) in your records. Keep your appointments regular for better health monitoring.";
                            }
                        } else {
                            result[0] = "You don't have any upcoming appointments. Contact your doctor to schedule one if needed.";
                        }
                    } else {
                        result[0] = "Keep your appointments regular. They're important for monitoring your condition. Check the Appointments section for details.";
                    }
                    latch.countDown();
                }
                
                @Override
                public void onFailure(retrofit2.Call<ApiResponse<List<Appointment>>> call, Throwable t) {
                    result[0] = "Keep your appointments regular. They're important for monitoring your condition. Check the Appointments section for details.";
                    latch.countDown();
                }
            });
            
            // Wait for response with timeout
            if (latch.await(2, TimeUnit.SECONDS)) {
                return result[0] != null ? result[0] : "Keep your appointments regular. They're important for monitoring your condition.";
            } else {
                return "Keep your appointments regular. Check the Appointments section for your schedule.";
            }
        } catch (Exception e) {
            return "Keep your appointments regular. They're important for monitoring your condition.";
        }
    }
}
