# FOOD FIT – Personalized Food Tracking App

## Introduction
Food Fit is a modern digital platform that helps users track their nutrition, manage daily meals, and achieve fitness goals by combining personalized calorie estimation with real-time food recognition.  
Through an intuitive interface, users can calculate daily calorie intake using the **BMR (Basal Metabolic Rate) formula** based on age, gender, height, weight, goal weight, and fitness objective (lose, gain, or maintain).  

Food Fit integrates:  
- **USDA API** for searching meals with verified nutritional values.  
- **Gemini API** for scanning food items to get additional nutritional information.  

By bridging accurate calorie calculation with smart meal logging, Food Fit empowers users to stay on track with their health goals.

---

## Features

### User Profile & Authentication
- New users sign up with **username, email, password, and phone number**.  
- User data is securely stored in **Firebase Firestore** for future login and profile recovery.

### Calorie Calculation
- Uses **BMR formula** with activity factor to calculate daily energy expenditure (TDEE).  
- Adjusts calorie goals dynamically based on fitness objectives:  
  - **Lose Weight:** 500 kcal deficit  
  - **Gain Weight:** 500 kcal surplus  
  - **Maintain Weight:** baseline TDEE  

### Meal Management
- Users can create meals for **Breakfast, Lunch, and Dinner**.  
- Add food items via:  
  - **USDA API** → Search and add foods with nutritional breakdown.  
  - **Gemini API** → Scan food items for real-time recognition and extra nutritional values.  
- Meals are stored in Firebase for persistence and future tracking.

### Progress Tracking
- Daily calorie targets vs. consumed calories.  
- Nutritional data logging for improved health awareness.

---

## Important Setup Instructions
Before running the project, you need to:

1. **Generate your Gemini API key and URL**:  
   - Go to your Gemini API account (or whichever AI API you are using).  
   - Create a new API key.  
   - Copy the API URL and Key.

2. **Insert the key and URL into the project**:  
   - Open `PreviewActivity.java`.  
   - Paste your Gemini API Key and URL in the designated variables.

3. **Run the Project**:  
   - Make sure your Android device or emulator is ready.  
   - Build and run the app. The AI features will now work correctly.

> ⚠️ Note: Without the Gemini API key and URL, the AI functionality in `PreviewActivity` will not work.

---

## Additional Info
- **Requires:** Android Studio  
- **Firebase:** Authentication & secure data storage  
- **SDK Versions:**  
  - Minimum SDK: 21  
  - Target SDK: 34

---

## Inclusivity and Accessibility
- Works efficiently on both **low-end and modern smartphones**.  
- Secure real-time data management using **Firebase Firestore**.  
- Combination of **manual food entry (USDA API)** and **AI-powered food recognition (Gemini API)** ensures usability for beginners and advanced users alike.

---

## Technology Stack
- **Java & XML:** UI design and application logic  
- **Firebase:** Authentication, secure data storage, real-time sync  
- **USDA API:** Reliable food database with calories and macro/micronutrients  
- **Gemini API:** AI-powered image recognition for instant nutritional analysis  

---

## Development Team
- **Shravan Jadhav** – Solo Developer: app design, navigation flow, Firebase integration, core functionality  
- **Support Tools:** ChatGPT & Documentation – Assisted in refining logic, debugging, and documentation  

---

## Connect with Me
- 📫 Email: shravanjadhav041@gmail.com  
- 🌐 GitHub: [shravanjadhav041](https://github.com/shravanjadhav041)  
- 📸 Instagram: [@mr.shravan098](https://instagram.com/mr.shravan098)
