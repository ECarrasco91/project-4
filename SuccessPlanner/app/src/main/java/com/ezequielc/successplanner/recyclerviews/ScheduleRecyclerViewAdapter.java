package com.ezequielc.successplanner.recyclerviews;

import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ezequielc.successplanner.DatabaseHelper;
import com.ezequielc.successplanner.R;
import com.ezequielc.successplanner.models.Schedule;

import java.util.List;

/**
 * Created by Ezequiel on 12/20/16.
 */

public class ScheduleRecyclerViewAdapter extends RecyclerView.Adapter<ScheduleRecyclerViewAdapter.ScheduleViewHolder> {
    private List<Schedule> mScheduleList;

    public ScheduleRecyclerViewAdapter(List<Schedule> mScheduleList) {
        this.mScheduleList = mScheduleList;
    }

    @Override
    public ScheduleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ScheduleViewHolder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.schedule_list_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(final ScheduleViewHolder holder, int position) {
        holder.mScheduleTextView.setText(mScheduleList.get(position).getSchedule());

        holder.mScheduleItemLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View view) {
                CharSequence[] options = {"Edit", "Delete"};
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext())
                        .setTitle("Schedule options")
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i) {
                                    case 0: // Edit option
                                        AlertDialog.Builder editOptionBuilder = new AlertDialog.Builder(view.getContext());
                                        LayoutInflater inflater = LayoutInflater.from(view.getContext());
                                        View dialogView = inflater.inflate(R.layout.dialog_add_schedule, null);
                                        editOptionBuilder.setView(dialogView);

                                        // Grabs the text in the adapter position and set it to the edit text
                                        final EditText editText = dialogView.findViewById(R.id.schedule_edit_text);
                                        editText.setText(mScheduleList.get(holder.getAdapterPosition()).getSchedule());

                                        editOptionBuilder.setPositiveButton("EDIT", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                if (editText.getText().toString().trim().length() == 0) {
                                                    Toast.makeText(view.getContext(), "Please fill field!", Toast.LENGTH_SHORT).show();
                                                    return;
                                                }

                                                String input = editText.getText().toString();
                                                DatabaseHelper databaseHelper = DatabaseHelper.getInstance(view.getContext());

                                                mScheduleList.get(holder.getAdapterPosition()).setSchedule(input);
                                                databaseHelper.updateSchedule(mScheduleList.get(holder.getAdapterPosition()));
                                                notifyItemChanged(holder.getAdapterPosition());
                                            }
                                        })
                                                .setNegativeButton("Cancel", null);
                                        editOptionBuilder.create().show();
                                        break;

                                    case 1: // Delete option
                                        // AlertDialog asking users if they want to delete schedule
                                        AlertDialog.Builder deleteOptionBuilder = new AlertDialog.Builder(view.getContext());
                                        deleteOptionBuilder.setMessage("Delete Schedule?")
                                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(view.getContext());
                                                        databaseHelper.deleteSchedule(mScheduleList.get(holder.getAdapterPosition()));
                                                        mScheduleList.remove(holder.getAdapterPosition());
                                                        notifyItemRemoved(holder.getAdapterPosition());
                                                    }
                                                })
                                                .setNegativeButton("No", null);
                                        deleteOptionBuilder.create().show();
                                        break;
                                }
                            }
                        });
                builder.create().show();
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mScheduleList.size();
    }

    class ScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView mScheduleTextView;
        LinearLayout mScheduleItemLayout;

        public ScheduleViewHolder(View itemView) {
            super(itemView);

            mScheduleTextView = itemView.findViewById(R.id.schedule_item);
            mScheduleItemLayout = itemView.findViewById(R.id.schedule_item_linear_layout);
        }
    }
}
