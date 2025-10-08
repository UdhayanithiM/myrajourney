package com.example.myrajouney;

import android.content.Context;
import java.util.Random;

public class ChatBot {
    private Context context;
    private Random random;
    
    public ChatBot(Context context) {
        this.context = context;
        this.random = new Random();
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
        if (message.contains("medication") || message.contains("medicine") || message.contains("pill")) {
            String[] responses = {
                "Make sure to take your medications as prescribed. Set reminders if needed.",
                "If you're having side effects from medication, contact your doctor immediately.",
                "Don't skip doses. If you missed one, check with your doctor about what to do.",
                "Keep track of your medication schedule in the app for better adherence."
            };
            return responses[random.nextInt(responses.length)];
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
        if (message.contains("appointment") || message.contains("doctor") || message.contains("visit")) {
            String[] responses = {
                "Keep your appointments regular. They're important for monitoring your condition.",
                "Prepare questions for your doctor before each visit.",
                "Bring your symptom log and medication list to appointments.",
                "If you need to reschedule, contact the clinic as soon as possible."
            };
            return responses[random.nextInt(responses.length)];
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
}
