package com.example.studentintelligentsystem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ResultViewHolder> {

    private final List<Result> resultsList;

    public ResultsAdapter(List<Result> resultsList) {
        this.resultsList = resultsList;
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_result, parent, false);
        return new ResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        Result result = resultsList.get(position);
        holder.tvSubject.setText(result.getSubject());
        holder.tvScore.setText(result.getScore() + "%");
    }

    @Override
    public int getItemCount() {
        return resultsList.size();
    }

    static class ResultViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubject, tvScore;

        public ResultViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvScore = itemView.findViewById(R.id.tvScore);
        }
    }
}
