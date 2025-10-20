package com.example.studentintelligentsystem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class ResultAdapter extends ArrayAdapter<String> {

    public ResultAdapter(@NonNull Context context, @NonNull List<String> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_result, parent, false);
        }

        String item = getItem(position);
        if (item != null) {
            String[] parts = item.split(" - ");
            if (parts.length == 2) {
                TextView text1 = convertView.findViewById(R.id.text1);
                TextView text2 = convertView.findViewById(R.id.text2);

                text1.setText(parts[0]);
                text2.setText(parts[1]);
            }
        }

        return convertView;
    }
}
