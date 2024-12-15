package com.example.week10;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements NoteFragment.OnNoteListInteractionListener {

    Note editingNote;
    ArrayList<Note> notes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        notes = retrieveNotes();


        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.container, NoteFragment.newInstance(notes));
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_new) {
            editingNote = createNote();
            notes.add(editingNote);

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.container, EditNoteFragment.newInstance(""), "edit_note");
            ft.addToBackStack(null);
            ft.commit();

            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private Note createNote() {
        Note note = new Note();
        SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
        int next = pref.getInt("next", 1);

        File dir = getFilesDir();
        String filePath = dir.getAbsolutePath() + "/note_" + next;
        Log.d("Create Note with path", filePath);

        note.setFilePath(filePath);

        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("next", next + 1);
        editor.apply();

        return note;
    }

    private ArrayList<Note> retrieveNotes() {
        ArrayList<Note> notes = new ArrayList<>();
        File dir = getFilesDir();
        File[] files = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                Log.d("Retrieving", "absolute path = " + file.getAbsolutePath());
                Log.d("Retrieving", "name = " + file.getName());

                Note note = new Note();
                note.setFilePath(file.getAbsolutePath());
                note.setDate(new Date(file.lastModified()));

                String header = getPreferences(Context.MODE_PRIVATE).getString(file.getName(), "No Header!");
                note.setHeader(header);

                notes.add(note);
            }
        }

        return notes;
    }

    @Override
    public void onBackPressed() {
        EditNoteFragment editFragment = (EditNoteFragment)
                getSupportFragmentManager().findFragmentByTag("edit_note");
        if (editFragment != null) {
            String content = editFragment.getContent();
            saveContent(editingNote, content);
        }
        super.onBackPressed();
    }

    private void saveContent(Note editingNote, String content) {
        editingNote.setDate(new Date());
        String header = content.length() < 30 ? content : content.substring(0, 30);
        editingNote.setHeader(header.replaceAll("\n", " "));

        FileWriter writer = null;
        File file = new File(editingNote.getFilePath());

        try {
            writer = new FileWriter(file);
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
        Log.d("Saving to Pref", "key = " + file.getName() + " value = " + editingNote.getHeader());
        editor.putString(file.getName(), editingNote.getHeader());
        editor.apply();
    }

    @Override
    public void onNoteSelected(Note note) {
        editingNote = note;

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, EditNoteFragment.newInstance(readContent(editingNote)), "edit_note");
        ft.addToBackStack(null);
        ft.commit();
    }

    private String readContent(Note editingNote) {
        StringBuilder content = new StringBuilder();
        File file = new File(editingNote.getFilePath());

        try (BufferedReader reader = new BufferedReader(new java.io.FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content.toString();
    }
}
