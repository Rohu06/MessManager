package com.example.messmanager.util;

import android.content.Context;
import android.net.Uri;

import com.example.messmanager.data.local.entity.MealEntry;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

public class CsvExporter {

    public static void exportToCsv(Context context, Uri destinationUri, List<MealEntry> entries) throws Exception {
        try (OutputStream outputStream = context.getContentResolver().openOutputStream(destinationUri);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
             
            // Write CSV Header
            writer.write("Date,Lunch,Dinner,Skipped,Notes");
            writer.newLine();

            // Write entries
            for (MealEntry entry : entries) {
                StringBuilder sb = new StringBuilder();
                sb.append(entry.getDate()).append(",");
                sb.append(entry.isLunch() ? "Yes" : "No").append(",");
                sb.append(entry.isDinner() ? "Yes" : "No").append(",");
                sb.append(entry.isSkipped() ? "Yes" : "No").append(",");
                
                String notes = entry.getNotes() != null ? entry.getNotes() : "";
                // Escape quotes and wrap in quotes if there is a comma, newline or quote in notes
                if (notes.contains(",") || notes.contains("\"") || notes.contains("\n")) {
                    notes = "\"" + notes.replace("\"", "\"\"") + "\"";
                }
                sb.append(notes);
                
                writer.write(sb.toString());
                writer.newLine();
            }
            writer.flush();
        }
    }
}
