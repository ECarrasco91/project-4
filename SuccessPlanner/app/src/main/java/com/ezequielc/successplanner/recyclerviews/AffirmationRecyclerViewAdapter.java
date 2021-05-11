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

import com.ezequielc.successplanner.utils.DatabaseHelper;
import com.ezequielc.successplanner.R;
import com.ezequielc.successplanner.models.Affirmation;

import java.util.List;

/**
 * Created by Ezequiel on 12/20/16.
 */

public class AffirmationRecyclerViewAdapter extends RecyclerView.Adapter<AffirmationRecyclerViewAdapter.AffirmationViewHolder> {
    private List<Affirmation> mAffirmationList;

    public AffirmationRecyclerViewAdapter(List<Affirmation> mAffirmationList) {
        this.mAffirmationList = mAffirmationList;
    }

    @Override
    public AffirmationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AffirmationViewHolder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.affirmation_list_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(final AffirmationViewHolder holder, int position) {
        holder.mAffirmationTextView.setText(mAffirmationList.get(position).getAffirmation());

        holder.mAffirmationItemLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View view) {
                CharSequence[] options = {"Edit", "Delete"};
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext())
                        .setTitle("Affirmation options")
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i) {
                                    case 0: // Edit option
                                        AlertDialog.Builder editOptionBuilder = new AlertDialog.Builder(view.getContext());
                                        LayoutInflater inflater = LayoutInflater.from(view.getContext());
                                        View dialogView = inflater.inflate(R.layout.dialog_add_affirmations, null);
                                        editOptionBuilder.setView(dialogView);

                                        // Grabs the text in the adapter position and set it to the edit text
                                        final EditText editText = dialogView.findViewById(R.id.affirmations_edit_text);
                                        editText.setText(mAffirmationList.get(holder.getAdapterPosition()).getAffirmation());

                                        editOptionBuilder.setPositiveButton("EDIT", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                if (editText.getText().toString().trim().length() == 0) {
                                                    Toast.makeText(view.getContext(), "Please fill field!", Toast.LENGTH_SHORT).show();
                                                    return;
                                                }

                                                String input = editText.getText().toString();
                                                DatabaseHelper databaseHelper = DatabaseHelper.getInstance(view.getContext());

                                                mAffirmationList.get(holder.getAdapterPosition()).setAffirmation(input);
                                                databaseHelper.updateAffirmation(mAffirmationList.get(holder.getAdapterPosition()));
                                                notifyItemChanged(holder.getAdapterPosition());
                                            }
                                        })
                                                .setNegativeButton("Cancel", null);
                                        editOptionBuilder.create().show();
                                        break;

                                    case 1: // Delete option
                                        // AlertDialog asking users if they want to delete affirmation
                                        AlertDialog.Builder deleteOptionBuilder = new AlertDialog.Builder(view.getContext());
                                        deleteOptionBuilder.setMessage("Delete Affirmation?")
                                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(view.getContext());
                                                        databaseHelper.deleteAffirmations(mAffirmationList.get(holder.getAdapterPosition()));
                                                        mAffirmationList.remove(holder.getAdapterPosition());
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
        return mAffirmationList.size();
    }

    class AffirmationViewHolder extends RecyclerView.ViewHolder {
        TextView mAffirmationTextView;
        LinearLayout mAffirmationItemLayout;

        public AffirmationViewHolder(View itemView) {
            super(itemView);

            mAffirmationTextView = itemView.findViewById(R.id.affirmation_item);
            mAffirmationItemLayout = itemView.findViewById(R.id.affirmation_list_linear_layout);
        }
    }
}
