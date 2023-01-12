package com.example.runningtracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runningtracker.R;
import com.example.runningtracker.model.entity.Run;

import java.util.ArrayList;
import java.util.List;

public class RunAdapter extends RecyclerView.Adapter<RunAdapter.RunViewHolder>{
    public interface OnItemClickListener {
        void onItemClick(Run run);
    }
    private List<Run> data;
    private Context context;
    private final LayoutInflater layoutInflater;
    private final OnItemClickListener listener;

    public RunAdapter(Context context, OnItemClickListener listener) {
        this.data = new ArrayList<>();
        this.context = context;
        this.listener = listener;
        this.layoutInflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public RunViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.db_layout_view, parent, false);
        return new RunViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RunViewHolder holder, int position) {
        holder.bind(data.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<Run> newData) {
        if (data != null) {
            data.clear();
            data.addAll(newData);
            notifyDataSetChanged();
        } else {
            data = newData;
        }
    }

    // Each individual element in the list is defined by a view holder object.
    static class RunViewHolder extends RecyclerView.ViewHolder {
        TextView nameView;
        TextView durationView;
        TextView distanceView;
        TextView paceView;
        TextView caloriesView;

        RunViewHolder(View itemView) {
            super(itemView);

            nameView = itemView.findViewById(R.id.nameView);
            durationView = itemView.findViewById(R.id.durationView);
            distanceView = itemView.findViewById(R.id.distanceView);
            paceView = itemView.findViewById(R.id.paceView);
            caloriesView = itemView.findViewById(R.id.caloriesView);
        }

        void bind(final Run run, final OnItemClickListener listener) {
            if (run != null) {
                nameView.setText(run.getName());
                durationView.setText(String.valueOf(run.getDuration()));
                distanceView.setText(String.valueOf(run.getDistance()));
                paceView.setText(String.valueOf(run.getPace()));
                caloriesView.setText(String.valueOf(run.getCalories()));
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onItemClick(run);
                }
            });
        }
    }
}
