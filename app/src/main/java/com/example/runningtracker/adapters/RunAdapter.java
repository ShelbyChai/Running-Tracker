package com.example.runningtracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runningtracker.Helper.RunHelper;
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
        TextView dateTimeView;

        RunViewHolder(View itemView) {
            super(itemView);

            nameView = itemView.findViewById(R.id.nameView);
            durationView = itemView.findViewById(R.id.durationView);
            distanceView = itemView.findViewById(R.id.distanceView);
            dateTimeView = itemView.findViewById(R.id.dateTimeView);
        }

        void bind(final Run run, final OnItemClickListener listener) {
            if (run != null) {
                nameView.setText(run.getName());
                distanceView.setText(RunHelper.formatDistance(run.getDistance()) + " km");
                durationView.setText(RunHelper.formatTime(run.getDuration()));
                dateTimeView.setText(run.getDateTimeFormatted());
            }

            itemView.setOnClickListener(v -> listener.onItemClick(run));
        }
    }
}
