package com.example.foodfit;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";

    private TextView tvUsername, tvEmail, tvPhone;
    private Button btnSave, btnDelete, btnChangePassword;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        uid = currentUser.getUid();

        // UI references
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);

        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        // Load user data
        loadUserData();

        // Save changes
        btnSave.setOnClickListener(v -> saveChanges());

        // Delete profile
        btnDelete.setOnClickListener(v -> confirmDelete());

        // Setup edit buttons
        setupEditButtons();

        // Change password
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
    }

    private void setupEditButtons() {
        findViewById(R.id.btnEditUsername).setOnClickListener(v -> showEditDialog("Username", tvUsername));
        findViewById(R.id.btnEditEmail).setOnClickListener(v -> showEditDialog("Email", tvEmail));
        findViewById(R.id.btnEditPhone).setOnClickListener(v -> showEditDialog("Phone", tvPhone));
    }

    private void showEditDialog(String fieldName, TextView targetView) {
        EditText input = new EditText(this);
        input.setText(targetView.getText().toString());

        new AlertDialog.Builder(this)
                .setTitle("Edit " + fieldName)
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> targetView.setText(input.getText().toString().trim()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadUserData() {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        fillData(snapshot);
                    } else {
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show());
    }

    private void fillData(DocumentSnapshot doc) {
        tvUsername.setText(doc.getString("username"));
        tvEmail.setText(doc.getString("email"));
        tvPhone.setText(doc.getString("phone"));
    }

    private void saveChanges() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("username", tvUsername.getText().toString().trim());
        updates.put("email", tvEmail.getText().toString().trim());
        updates.put("phone", tvPhone.getText().toString().trim());

        db.collection("users").document(uid).update(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Profile")
                .setMessage("Are you sure you want to delete your profile and all related data? This action cannot be undone.")
                .setPositiveButton("Yes, Delete", (dialog, which) -> askPasswordAndDelete())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void askPasswordAndDelete() {
        EditText input = new EditText(this);
        input.setHint("Enter your password");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        new AlertDialog.Builder(this)
                .setTitle("Confirm Password")
                .setView(input)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String password = input.getText().toString().trim();
                    if (password.isEmpty()) {
                        Toast.makeText(this, "Password required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    reauthenticateAndDelete(password);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void reauthenticateAndDelete(String password) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
        user.reauthenticate(credential)
                .addOnSuccessListener(unused -> {
                    // ✅ Delete userSessions → meals → history → user doc → account
                    deleteCollection("userSessions", "uid", uid, () -> {
                        deleteCollection("meals", "uid", uid, () -> {
                            deleteCollection("history", "uid", uid, () -> {
                                deleteUserDocAndAccount(user);
                            });
                        });
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Reauthentication failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void deleteCollection(String collectionName, String field, String value, Runnable onComplete) {
        db.collection(collectionName)
                .whereEqualTo(field, value)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot) {
                        doc.getReference().delete();
                    }
                    onComplete.run();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete " + collectionName + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void deleteUserDocAndAccount(FirebaseUser user) {
        db.collection("users").document(uid).delete()
                .addOnSuccessListener(aVoid -> {
                    user.delete()
                            .addOnSuccessListener(unused2 -> {
                                Toast.makeText(this, "Profile & all data deleted", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(EditProfileActivity.this, PlanSelectionActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete account: " + e.getMessage(), Toast.LENGTH_LONG).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete profile: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // ================= Change Password Dialog =================
    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        builder.setView(view);
        builder.setCancelable(true);

        EditText etCurrent = view.findViewById(R.id.etCurrentPassword);
        EditText etNew = view.findViewById(R.id.etNewPassword);
        EditText etConfirm = view.findViewById(R.id.etConfirmPassword);
        TextView tvError = view.findViewById(R.id.tvError);
        Button btnVerify = view.findViewById(R.id.btnVerify);
        Button btnUpdate = view.findViewById(R.id.btnUpdatePassword);
        ImageView ivToggleNew = view.findViewById(R.id.ivToggleNew);
        ImageView ivToggleConfirm = view.findViewById(R.id.ivToggleConfirm);
        ImageView ivToggleCurrent = view.findViewById(R.id.ivToggleNew2);

        etNew.setEnabled(false);
        etConfirm.setEnabled(false);
        btnUpdate.setEnabled(false);

        FirebaseUser user = mAuth.getCurrentUser();

        // Verify current password
        btnVerify.setOnClickListener(v -> {
            String currentPass = etCurrent.getText().toString().trim();
            if (currentPass.isEmpty()) {
                tvError.setText("Enter current password");
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            tvError.setVisibility(View.GONE);
            btnVerify.setEnabled(false);

            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPass);
            user.reauthenticate(credential)
                    .addOnSuccessListener(unused -> {
                        etNew.setEnabled(true);
                        etConfirm.setEnabled(true);
                        btnUpdate.setEnabled(true);
                        Toast.makeText(this, "Password verified", Toast.LENGTH_SHORT).show();
                        btnVerify.setEnabled(true);
                    })
                    .addOnFailureListener(e -> {
                        tvError.setText("Invalid password");
                        tvError.setVisibility(View.VISIBLE);
                        btnVerify.setEnabled(true);
                    });
        });

        // Toggle password visibility
        ivToggleCurrent.setOnClickListener(v -> togglePasswordVisibility(etCurrent));
        ivToggleNew.setOnClickListener(v -> togglePasswordVisibility(etNew));
        ivToggleConfirm.setOnClickListener(v -> togglePasswordVisibility(etConfirm));

        // Update password
        btnUpdate.setOnClickListener(v -> {
            String newPass = etNew.getText().toString().trim();
            String confirm = etConfirm.getText().toString().trim();

            if (newPass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(confirm)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            user.updatePassword(newPass)
                    .addOnSuccessListener(unused -> Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
        });

        builder.show();
    }

    private void togglePasswordVisibility(EditText editText) {
        if ((editText.getInputType() & InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        }
        editText.setSelection(editText.getText().length());
    }
}
