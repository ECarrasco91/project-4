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
import com.ezequielc.successplanner.models.Goal;

import java.util.List;

/**
 * Created by Ezequiel on 12/20/16.
 */

public class GoalRecyclerViewAdapter extends RecyclerView.Adapter<GoalRecyclerViewAdapter.GoalViewHolder> {
    private List<Goal> mGoalList;

    public GoalRecyclerViewAdapter(List<Goal> mGoalList) {
        this.mGoalList = mGoalList;
    }

    @Override
    public GoalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new GoalViewHolder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.goal_list_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(final GoalViewHolder holder, int position) {
        holder.mGoalTextView.setText(mGoalList.get(position).getGoal());

        holder.mGoalItemLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View view) {
                CharSequence[] options = {"Edit", "Delete"};
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext())
                        .setTitle("Goal options")
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i) {
                                    case 0: // Edit Option
                                        AlertDialog.Builder editOptionBuilder = new AlertDialog.Builder(view.getContext());
                                        LayoutInflater inflater = LayoutInflater.from(view.getContext());
                                        View dialogView = inflater.inflate(R.layout.dialog_add_goals, null);
                                        editOptionBuilder.setView(dialogView);

                                        // Grabs the text in the adapter position and set it to the edit text
                                        final EditText editText = dialogView.findViewById(R.id.goal_edit_text);
                                        editText.setText(mGoalList.get(holder.getAdapterPosition()).getGoal());

                                        editOptionBuilder.setPositiveButton("EDIT", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                if (editText.getText().toString().trim().length() == 0) {
                                                    Toast.makeText(view.getContext(), "Please fill field!", Toast.LENGTH_SHORT).show();
                                                    return;
                                                }

                                                String input = editText.getText().toString();
                                                DatabaseHelper databaseHelper = DatabaseHelper.getInstance(view.getContext());

                                                mGoalList.get(holder.getAdapterPosition()).setGoal(input);
                                                databaseHelper.updateGoal(mGoalList.get(holder.getAdapterPosition()));
                                                notifyItemChanged(holder.getAdapterPosition());
                                            }
                                        })
                                                .setNegativeButton("Cancel", null);
                                        editOptionBuilder.create().show();
                                        break;

                                    case 1: // Delete option
                                        // AlertDialog asking users if they want to delete goal
                                        AlertDialog.Builder deleteOptionBuilder = new AlertDialog.Builder(view.getContext());
                                        deleteOptionBuilder.setMessage("Delete goal?")
                                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(view.getContext());
                                                        databaseHelper.deleteGoal(mGoalList.get(holder.getAdapterPosition()));
                                                        mGoalList.remove(holder.getAdapterPosition());
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
        return mGoalList.size();
    }

    class GoalViewHolder extends RecyclerView.ViewHolder {
        TextView mGoalTextView;
        LinearLayout mGoalItemLayout;

        public GoalViewHolder(View itemView) {
            super(itemView);

            mGoalTextView = itemView.findViewById(R.id.goal_item);
            mGoalItemLayout = itemView.findViewById(R.id.goal_item_linear_layout);
        }
    }
}
