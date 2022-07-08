package com.example.instagappsm.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuView;
import androidx.constraintlayout.motion.widget.MotionHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagappsm.R;
import com.example.instagappsm.activity.OtherUserProfile;
import com.example.instagappsm.model.Comments;
import com.example.instagappsm.model.Post;
import com.example.instagappsm.model.PostId;
import com.example.instagappsm.model.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private List<Users> usersList;
    private List<Comments> commentsList;
    private Activity context;
    private FirebaseAuth auth;
    private String currentId;
    private FirebaseFirestore firestore;
    private String idPost;
    private List<String> idListComment;


    public CommentAdapter(List<Comments> commentsList, Activity context, String idPost, List<String> idListComment) {
        this.commentsList = commentsList;
        this.context = context;
        this.idPost = idPost;
        this.idListComment = idListComment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment_post, parent, false);
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Comments comments = commentsList.get(position);

        String curentUserId = auth.getCurrentUser().getUid();
        String comment = comments.getComment();
        String userId = comments.getUser();

        holder.setmCommnent(comment);

        firestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    String name = task.getResult().getString("name");
                    String image = task.getResult().getString("image");

                    holder.setCircleImageView(image);
                    holder.circleImageViewComment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, OtherUserProfile.class);
                            intent.putExtra("name", name);
                            intent.putExtra("image", image);
                            context.startActivity(intent);
                        }
                    });
                    holder.setmUsername(name);
                    holder.mUsername.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, OtherUserProfile.class);
                            intent.putExtra("name", name);
                            intent.putExtra("image", image);
                            context.startActivity(intent);
                        }
                    });

                    if(curentUserId.equals(userId)) {
                        holder.mDeleteBtn.setVisibility(View.VISIBLE);
                        holder.mDeleteBtn.setClickable(true);
                        holder.mDeleteBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder alert = new AlertDialog.Builder(context,4);
                                alert.setTitle("Delete").setMessage("Are You Sure ?")
                                        .setNegativeButton("No", null)
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                firestore.collection("Posts/" + idPost + "/Comments").document(idListComment.get(position)).delete();
                                                commentsList.remove(position);
                                            }
                                        });
                                alert.show();
                                notifyDataSetChanged();
                            }
                        });
                    }
                    else {
                        holder.mDeleteBtn.setVisibility(View.INVISIBLE);
                        holder.mDeleteBtn.setClickable(false);
                    }
                }
                else {
                    Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView mUsername, mCommnent;
        ImageButton mDeleteBtn;
        CircleImageView circleImageViewComment;
        View mView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            mDeleteBtn = itemView.findViewById(R.id.imag_btn_delete_comment);

        }

        public void setmCommnent(String commnent) {
            mCommnent = mView.findViewById(R.id.comment_tv);
            mCommnent.setText(commnent);
        }

        public void setmUsername(String name) {
            mUsername = mView.findViewById(R.id.tv_user_comment);
            mUsername.setText(name);
        }

        public void setCircleImageView(String url) {
            circleImageViewComment = mView.findViewById(R.id.circleImageView_comment);
            Glide.with(context).load(url).into(circleImageViewComment);

        }
    }
}
