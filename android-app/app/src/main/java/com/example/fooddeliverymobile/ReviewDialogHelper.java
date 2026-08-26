package com.example.fooddeliverymobile;

import android.app.Activity;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;

import androidx.appcompat.app.AlertDialog;

public final class ReviewDialogHelper {
    private ReviewDialogHelper() {
    }

    public static void show(Activity activity, int titleResource, SubmitListener listener) {
        View content = activity.getLayoutInflater().inflate(R.layout.dialog_review, null);
        RatingBar rating = content.findViewById(R.id.reviewRating);
        EditText comment = content.findViewById(R.id.reviewComment);
        new AlertDialog.Builder(activity)
                .setTitle(titleResource)
                .setView(content)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.submit_review, (dialog, which) -> {
                    int value = Math.round(rating.getRating());
                    if (value < 1) {
                        android.widget.Toast.makeText(activity,
                                R.string.error_review_rating,
                                android.widget.Toast.LENGTH_LONG).show();
                        return;
                    }
                    listener.submit(value, comment.getText().toString().trim());
                })
                .show();
    }

    public interface SubmitListener {
        void submit(int rating, String comment);
    }
}
